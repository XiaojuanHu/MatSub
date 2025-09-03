/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.algorithms.branchbound;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.function.Function;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.LcmSearchNode;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.RandomDefaultPropositionalLogicSupplier;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptors;

/**
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.4.0
 *
 */
public class BranchAndBoundSearchTest {

	private Population population = Populations.population(Identifier.id("Test_population"), 100);

	private PropositionalContext propLogic = RandomDefaultPropositionalLogicSupplier
			.randomDefaultPropositionalLogicSupplier("Statement on test population", 20, population).get();

	private Function<LcmSearchNode<LogicalDescriptor>, Collection<LcmSearchNode<LogicalDescriptor>>> succ = BranchAndBoundSearch
			.allDescriptorsExpander(propLogic, x -> true, x -> x);

	@Test
	public void allSingletonsFromEmptyDescriptor() {
		// LogicalDescriptor root = LogicalDescriptors.create(propLogic,
		// ImmutableList.of());
		Collection<LcmSearchNode<LogicalDescriptor>> successors = succ
				.apply(BranchAndBoundSearch.allDescriptorRootNode(propLogic, x -> x));
		assertEquals(propLogic.propositions().size(), successors.size());
	}

	@Test
	public void noAugmentationFromDescriptorWithLastProp() {
		LcmSearchNode<LogicalDescriptor> descriptorWithLast = BranchAndBoundSearch
				.allDescriptorNode(
						LogicalDescriptors.create(propLogic.population(),
								ImmutableList.of(propLogic.propositions().get(propLogic.propositions().size() - 1))),
						x -> x, propLogic.propositions().size());
		Collection<LcmSearchNode<LogicalDescriptor>> successors = succ
				.apply(descriptorWithLast);
		assertEquals(0, successors.size());
	}

}
