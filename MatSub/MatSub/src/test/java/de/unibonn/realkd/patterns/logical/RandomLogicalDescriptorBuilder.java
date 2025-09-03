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

import java.util.List;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.RuntimeBuilder;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.testing.TestRandomSupplier;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.util.Sampling;

/**
 * @author Mario Boley
 *
 */
public class RandomLogicalDescriptorBuilder implements
		RuntimeBuilder<LogicalDescriptor, Workspace> {

	private final Identifier propositionalLogicIdentifier;

	private final int numberOfProductPropositions;

	public RandomLogicalDescriptorBuilder(Identifier propositionalLogicIdentifier,
			int numberOfProductElements) {
		this.numberOfProductPropositions = numberOfProductElements;
		this.propositionalLogicIdentifier = propositionalLogicIdentifier;
	}

	public LogicalDescriptor build(Workspace workspace) {
		PropositionalContext propositionalLogic = (PropositionalContext) workspace
				.get(propositionalLogicIdentifier);
		List<Integer> indices = Sampling.getRandomIntegersWithoutReplacement(
				numberOfProductPropositions, propositionalLogic
						.propositions().size(), TestRandomSupplier.INSTANCE
						.get());
		return LogicalDescriptors.create(
				propositionalLogic.population(),
				indices.stream()
						.map(i -> propositionalLogic.propositions().get(i))
						.collect(Collectors.toList()));
	}

}
