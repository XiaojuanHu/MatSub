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
package de.unibonn.realkd.data.table.attribute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.unibonn.realkd.util.Sampling;

/**
 * 
 * @author Mario Boley
 * 
 * @since 0.2.0
 * 
 * @version 0.2.0
 *
 */
public class TestAttributeFactory {

	private final Random random;

	public TestAttributeFactory(Random random) {
		this.random = random;
	}

	/**
	 * Generates categorical attribute with specified name and random values
	 * such that:
	 * 
	 * <li>the marginal probability for the value of each individual attribute
	 * is uniformly from a given list of categories, and</li> <li>each specified
	 * category appears at least once.</li>
	 * 
	 * @param categories
	 *            must be not null list of unique categories (NOTE: can specify
	 *            one category as null in order to create missing values)
	 * 
	 * @param numberOfEntities
	 *            must be at least equal to the number of categories
	 * 
	 */
	public CategoricAttribute<String> getCategoricalAttributeWithUniformNonRedundantCategories(
			String name, List<String> categories, int numberOfEntities) {
		checkNotNull(categories);
		checkArgument(numberOfEntities >= categories.size(),
				"Must have at least as many entries as categories.");

		// Step 1: generate base value list independently uniformly at random
		List<String> values = new ArrayList<>(numberOfEntities);
		for (int i = 0; i < numberOfEntities; i++) {
			values.add(categories.get(random.nextInt(categories.size())));
		}

		// Step 2: insert each category at random (non-intersecting) positions
		List<Integer> insertPositions = Sampling
				.getRandomIntegersWithoutReplacement(categories.size(),
						numberOfEntities);
		for (int i = 0; i < categories.size(); i++) {
			values.set(insertPositions.get(i), categories.get(i));
		}

		String description = "Was generated uniformly at random from categories: "
				+ categories;

		return Attributes.categoricalAttribute(name, description, values);
	}

	/**
	 * Generates metric attributes with specified name and values drawn
	 * uniformly from a bounded range.
	 * 
	 */
	public MetricAttribute getMetricAttributeWithUniformValues(String name,
			double bound, int numberOfEntities) {
		List<Double> values = new ArrayList<>(numberOfEntities);
		for (int i = 0; i < numberOfEntities; i++) {
			values.add(random.nextDouble() * bound);
		}

		String description = "Was generated uniformly at random from interval [0,"
				+ bound + ")";

		return Attributes.metricDoubleAttribute(name, description, values);
	}

}
