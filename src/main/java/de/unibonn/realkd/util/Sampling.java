/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Collection of utility methods for generating random objects.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 *
 */
public class Sampling {

	/**
	 * Exhaustive sampling based on a list of positive weights.
	 * 
	 * WARNING: implemented inefficiently without binary search
	 * 
	 * @param weights
	 *            list of non-negative potential weights, at least one of which
	 *            must be strictly larger than 0.0
	 * @return index of input list drawn with a probability according to its
	 *         weight
	 */
	public static int exhaustiveSamplingFromWeights(List<Double> weights) {
		List<Double> cumulativeWeights = new ArrayList<>(weights.size());
		double sum = 0;
		for (int i = 0; i < weights.size(); i++) {
			sum += weights.get(i);
			cumulativeWeights.add(sum);
		}
		Random random = new Random();
		Double value = random.nextDouble() * sum;
		for (int i = 0; i < cumulativeWeights.size(); i++) {
			if (value <= cumulativeWeights.get(i)) {
				return i;
			}
		}

		// this may never happen
		throw new AssertionError("Error in sampling index wrt weight: " + weights);
		// return -1;
	}

	/**
	 * returns random permutation of ints from 0 (inclusive) to n (exclusive)
	 */
	public static List<Integer> getPermutation(int n) {
		List<Integer> init = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			init.add(i);
		}
		Random random = new Random();
		List<Integer> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			int nextIndex = random.nextInt(init.size());
			result.add(init.get(nextIndex));
			init.remove(nextIndex);
		}
		return result;
	}

	/**
	 * Generates a list of integers of specified length taken without
	 * replacement from 0 (inclusive) to range (exclusive).
	 * 
	 * <p>
	 * WARNING: this explicitly generates the whole sampling range in memory.
	 * </p>
	 * 
	 */
	public static List<Integer> getRandomIntegersWithoutReplacement(int length, int range) {
		return getRandomIntegersWithoutReplacement(length, range, new Random());
	}

	/**
	 * Generates a list of integers of specified length taken without
	 * replacement from 0 (inclusive) to range (exclusive).
	 * 
	 * <p>
	 * WARNING: this explicitly generates the whole sampling range in memory.
	 * </p>
	 * 
	 */
	public static List<Integer> getRandomIntegersWithoutReplacement(int length, int range, Random random) {
		List<Integer> init = new ArrayList<>(range);
		for (int i = 0; i < range; i++) {
			init.add(i);
		}
		List<Integer> result = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			int nextIndex = random.nextInt(init.size());
			result.add(init.get(nextIndex));
			init.remove(nextIndex);
		}
		return result;
	}

}
