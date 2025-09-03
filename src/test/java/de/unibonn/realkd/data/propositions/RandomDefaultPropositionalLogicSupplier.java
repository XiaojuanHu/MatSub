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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.common.IndexSets.copyOf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import de.unibonn.realkd.common.testing.TestRandomSupplier;
import de.unibonn.realkd.data.Population;

/**
 * <p>
 * Produces an instance of {@link DefaultPropositionalContext} with desired number
 * of propositions and data objects and random content.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2
 *
 */
public class RandomDefaultPropositionalLogicSupplier implements Supplier<DefaultPropositionalContext> {

	/**
	 * 
	 * @param propLogicName the name of collection of logical statements
	 * @param numberOfPropositions the number of logical statements to be generated
	 * @param population the population for which logical statements are defined
	 * @return
	 */
	public static RandomDefaultPropositionalLogicSupplier randomDefaultPropositionalLogicSupplier(String propLogicName,
			int numberOfPropositions, Population population) {
		return new RandomDefaultPropositionalLogicSupplier(propLogicName, "Collection of "+numberOfPropositions+" uniform random statements.", numberOfPropositions,
				population);
	}

	private final String productName;

	private final String productDescription;

	private final int numberOfProductPropositions;

	private final Population population;

	private RandomDefaultPropositionalLogicSupplier(String propLogicName, String propLogicDescription,
			int numberOfPropositions, Population population) {
		this.productName = propLogicName;
		this.productDescription = propLogicDescription;
		this.numberOfProductPropositions = numberOfPropositions;
		this.population = population;
	}

	public DefaultPropositionalContext get() {
		List<Proposition> propositions = new ArrayList<>();
		for (int i = 0; i < numberOfProductPropositions; i++) {
			HashSet<Integer> supportSet = new HashSet<>();
			for (int j = 0; j < population.size(); j++) {
				if (TestRandomSupplier.INSTANCE.get().nextBoolean()) {
					supportSet.add(j);
				}
			}
			propositions.add(Propositions.proposition(i, copyOf(supportSet)));
		}
		return new DefaultPropositionalContext(productName, productDescription, population, propositions);
	}

}
