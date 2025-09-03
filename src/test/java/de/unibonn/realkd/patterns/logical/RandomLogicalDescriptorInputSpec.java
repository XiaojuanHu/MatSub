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
package de.unibonn.realkd.patterns.logical;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.workspace.Workspaces.workspace;
import static de.unibonn.realkd.data.Populations.population;

import java.util.function.Supplier;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.RandomDefaultPropositionalLogicSupplier;

public class RandomLogicalDescriptorInputSpec implements Supplier<LogicalDescriptorTestInput> {

	public static final Identifier PROB_LOGIC_NAME = id("Random");

	public final int numOfProps;

	public final int numOfObjects;

	public final int numOfDescriptorElements;

	public RandomLogicalDescriptorInputSpec(int numOfProps, int numOfObjects, int numOfDescriptorElements) {
		this.numOfProps = numOfProps;
		this.numOfObjects = numOfObjects;
		this.numOfDescriptorElements = numOfDescriptorElements;
	}

	public LogicalDescriptorTestInput get() {
		Workspace dataWorkspace = workspace();
		Population population = population(id("Population"), numOfObjects);
		PropositionalContext propLogic = RandomDefaultPropositionalLogicSupplier
				.randomDefaultPropositionalLogicSupplier(PROB_LOGIC_NAME.toString(), numOfProps, population).get();
		dataWorkspace.add(population);
		dataWorkspace.add(propLogic);
		RandomLogicalDescriptorBuilder descriptionFactory = new RandomLogicalDescriptorBuilder(PROB_LOGIC_NAME,
				numOfDescriptorElements);
		LogicalDescriptor descriptor = descriptionFactory.build(dataWorkspace);
		return new LogicalDescriptorTestInput(dataWorkspace, descriptor);
	}

	public String toString() {
		return "RndLogAndDescr(" + numOfProps + "," + numOfObjects + "," + numOfDescriptorElements + ")";
	}

}