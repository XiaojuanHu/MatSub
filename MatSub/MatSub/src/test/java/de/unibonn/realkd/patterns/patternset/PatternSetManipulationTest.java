/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.patterns.patternset;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.testing.JsonSerializationTesting.testJsonSerialization;
import static de.unibonn.realkd.common.workspace.Workspaces.workspace;
import static de.unibonn.realkd.data.Populations.population;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import de.unibonn.realkd.common.testing.AbstractBufferedInputTest;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.propositions.DefaultPropositionalContext;
import de.unibonn.realkd.data.propositions.RandomDefaultPropositionalLogicSupplier;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.RandomLogicalDescriptorBuilder;

/**
 * @author Mario Boley
 *
 */
public class PatternSetManipulationTest extends AbstractBufferedInputTest<PatternSetManipulationTestInput> {

	@Parameters(name = "{0}")
	public static Iterable<Object[]> getData() {
		return Arrays.asList(new Object[][] { { new RandomAssociationPatternSetInputSpecification(10, 20, 3, 3) } });
	}

	private static class RandomAssociationPatternSetInputSpecification
			implements Supplier<PatternSetManipulationTestInput> {

		private final int numberOfProps;

		private final int numberOfObjects;

		private final int numberOfDescrElements;

		private final int numberOfPatternSetElements;

		public RandomAssociationPatternSetInputSpecification(int numberOfProps, int numberOfObjects,
				int numberOfDescrElements, int numberOfPatternSetElements) {
			this.numberOfProps = numberOfProps;
			this.numberOfObjects = numberOfObjects;
			this.numberOfDescrElements = numberOfDescrElements;
			this.numberOfPatternSetElements = numberOfPatternSetElements;
		}

		public PatternSetManipulationTestInput get() {
			Workspace workspace = workspace();
			Population population = population(id("Population"), numberOfObjects);
			DefaultPropositionalContext propLogic = RandomDefaultPropositionalLogicSupplier
					.randomDefaultPropositionalLogicSupplier("Random", numberOfProps, population).get();
			workspace.add(population);
			workspace.add(propLogic);

			RandomLogicalDescriptorBuilder descriptorBuilder = new RandomLogicalDescriptorBuilder(id("Random"),
					numberOfDescrElements);

			Set<Pattern<?>> patterns = new LinkedHashSet<>();

			for (int i = 0; i < numberOfPatternSetElements; i++) {
				LogicalDescriptor descriptor = descriptorBuilder.build(workspace);
				Pattern<?> association = Associations.association(descriptor);
				patterns.add(association);
			}

			return new PatternSetManipulationTestInput(workspace,
					PatternSets.createPatternSet(propLogic.population(), patterns));
		}

		@Override
		public String toString() {
			return "RndAssociationSet(" + numberOfProps + "props," + numberOfObjects + "objs,"
					+ numberOfPatternSetElements + "ptns," + numberOfDescrElements + "els)";
		}
	}

	public PatternSetManipulationTest(Supplier<PatternSetManipulationTestInput> inputSuppler) {
		super(inputSuppler);
	}

	@Test
	public void testDescriptorBuilderJsonSerialization() throws IOException {
		PatternSetDescriptor patternSet = getCurrentInput().patternSet.descriptor();
		SerialForm<PatternSetDescriptor> builder = patternSet.serialForm();
		testJsonSerialization(builder, SerialForm.class);
	}

	@Test
	public void testDescriptorToBuilderConsistency() {
		Workspace workspace = getCurrentInput().workspace;
		PatternSetDescriptor setDescriptor = getCurrentInput().patternSet.descriptor();
		SerialForm<PatternSetDescriptor> builder = setDescriptor.serialForm();
		PatternSetDescriptor clone = builder.build(workspace);
		assertEquals(setDescriptor, clone);
	}

	@Test
	public void testPatternSetToBuilderConsistency() {
		Workspace workspace = getCurrentInput().workspace;
		SerialForm<PatternSet> builder = getCurrentInput().patternSet.serialForm();
		Pattern<PatternSetDescriptor> clone = builder.build(workspace);
		assertEquals(getCurrentInput().patternSet, clone);
	}

	@Test
	public void testBuilderJsonSerialization() throws IOException {
		testJsonSerialization(getCurrentInput().patternSet.serialForm(), SerialForm.class);
	}

}
