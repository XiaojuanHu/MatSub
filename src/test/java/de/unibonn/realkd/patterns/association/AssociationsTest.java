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
package de.unibonn.realkd.patterns.association;

import static de.unibonn.realkd.patterns.Support.SUPPORT;
import static de.unibonn.realkd.patterns.logical.Area.AREA;
import static de.unibonn.realkd.patterns.logical.Lift.LIFT;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.testing.AbstractBufferedInputTest;
import de.unibonn.realkd.common.testing.JsonSerializationTesting;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptorTestInput;
import de.unibonn.realkd.patterns.logical.RandomLogicalDescriptorInputSpec;

/**
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @version 0.7.1
 *
 */
public class AssociationsTest extends AbstractBufferedInputTest<AssociationTestInput> {

	@Parameters(name = "{0}")
	public static Iterable<Object[]> getTestData() {
		return Arrays.asList(new Object[][] {
				{ new RandomAssociationTestInputSpec(new RandomLogicalDescriptorInputSpec(10, 20, 0),
						ImmutableList.of()) },
				{ new RandomAssociationTestInputSpec(new RandomLogicalDescriptorInputSpec(10, 20, 5),
						ImmutableList.of(LIFT)) },
				{ new RandomAssociationTestInputSpec(new RandomLogicalDescriptorInputSpec(10, 20, 10), ImmutableList
						.of(AREA, SUPPORT)) } });
	}

	private static class RandomAssociationTestInputSpec implements Supplier<AssociationTestInput> {

		private final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalMeasurementProcedures;
		private final RandomLogicalDescriptorInputSpec descriptorInputSpec;

		public RandomAssociationTestInputSpec(RandomLogicalDescriptorInputSpec descriptorInputSpec,
				List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalMeasurementProcedures) {
			this.descriptorInputSpec = descriptorInputSpec;
			this.additionalMeasurementProcedures = additionalMeasurementProcedures;
		}

		@Override
		public AssociationTestInput get() {
			LogicalDescriptorTestInput logicalDescriptorTestInput = descriptorInputSpec.get();
			Association association = Associations.association(logicalDescriptorTestInput.descriptor,
					additionalMeasurementProcedures);
			return new AssociationTestInput(logicalDescriptorTestInput.dataWorkspace, association);
		}

		@Override
		public String toString() {
			return "Ass(" + descriptorInputSpec.toString() + "," + additionalMeasurementProcedures + ")";
		}

	}

	public AssociationsTest(Supplier<AssociationTestInput> inputSuppler) {
		super(inputSuppler);
	}

	@Test
	public void testBuilderJsonSerialization() throws IOException {
		Association association = getCurrentInput().association;
		SerialForm<Association> builder = association.serialForm();
		JsonSerializationTesting.testJsonSerialization(builder, SerialForm.class);
	}

}
