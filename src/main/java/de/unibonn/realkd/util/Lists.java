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
package de.unibonn.realkd.util;

import static com.google.common.collect.Lists.newArrayList;
import static de.unibonn.realkd.util.Search.expectationMaximization;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.util.Search.InPlaceDoubleArrayModifier;

/**
 * Static utility methods for lists.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Lists {

	private static final Logger LOGGER = Logger.getLogger(Lists.class.getName());
	
	/**
	 * @return a given list or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, List<T> list) {
		return test ? list : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1) {
		return test ? ImmutableList.of(e1) : ImmutableList.of();
	}

	/**
	 * @return list of elements supplied by given suppliers or the immutable
	 *         empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, Supplier<T> e1) {
		return test ? ImmutableList.of(e1.get()) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2) {
		return test ? ImmutableList.of(e1, e2) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3) {
		return test ? ImmutableList.of(e1, e2, e3) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4) {
		return test ? ImmutableList.of(e1, e2, e3, e4) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6) {
		return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6, T e7) {
		return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6, e7) : ImmutableList.of();
	}

	/**
	 * @return list of given elements or the immutable empty list.
	 *
	 */
	public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8) {
		return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8) : ImmutableList.of();
	}

	/**
         * @return list of given elements or the immutable empty list.
         *
         */
        public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8, T e9) {
                return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9) : ImmutableList.of();
        }

        public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8, T e9, T e10) {
	    return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10) : ImmutableList.of();
        }

    public static <T> List<T> listOrEmpty(boolean test, T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8, T e9, T e10, T e11) {
	return test ? ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11) : ImmutableList.of();
        }

	/**
	 * Computes the subsequence of non-repetitive values of a given input
	 * sequence of objects (e.g., {@code [1,1,3,2,2,3]->[1,3,2,3]}).
	 * 
	 * @param sequence
	 *            the input sequence of objects
	 * @return subsequence with directly repetitive values removed
	 */
	public static <T> List<T> valueSequence(List<T> sequence) {
		List<T> result = newArrayList();
		for (int i = 0; i < sequence.size(); i++) {
			T value = sequence.get(i);
			if (i == 0 || !value.equals(sequence.get(i - 1))) {
				result.add(value);
			}
		}
		return result;
	}

	public static Double getPercentile(List<Double> orderedInput, double frac) {
		int n = (int) Math.ceil(frac * orderedInput.size() - 1);
		return orderedInput.get(n);
	}

	public static List<Double> kMeansCutPoints(List<Double> values, int k, int maxIterations) {
		double[] means = new double[k];
		// upper bound for each bin; last cut points is fixed to be a.max();
		double[] cutPoints = IntStream.range(0, k)
				.mapToDouble(
						i -> (i == k - 1) ? values.get(values.size() - 1) : getPercentile(values, (i + 1) * 1.0 / k))
				.toArray();

		// sets mean[i] to be equal to the mean of all points with value v in
		// (cutPoints[i-1], cutPoints[i]]
		// (for i=1, [a.min(),cutPoints[0])
		InPlaceDoubleArrayModifier meanOptimizer = m -> {
			boolean changed = false;
			int i = 0;
			double sum = 0;
			int count = 0;
			Iterator<Double> valueIterator = values.iterator();
			// for (Integer row : a.getSortedNonMissingRowIndices()) {
			while (valueIterator.hasNext()) {
				Double value = valueIterator.next();
				if (value <= cutPoints[i]) {
					sum += value;
					count++;
				}
				if (value > cutPoints[i] || !valueIterator.hasNext()) {
					double mean = (count > 0) ? sum / count : (cutPoints[i + 1] - cutPoints[i]) / 2.0;
					if (means[i] != mean) {
						changed = true;
						means[i] = mean;
					}
					sum = value;
					count = 1;
					i++;
				}
			}
			return changed;
		};

		InPlaceDoubleArrayModifier cutPointOptimizer = c -> {
			boolean changed = false;
			int i = 0;
			double previousValue = values.get(0);
			for (Double value : values) {
				// Double value = a.getValue(row);
				if (abs(value - means[i]) > abs(value - means[i + 1])) {
					double newCutPoint = previousValue + (value - previousValue) / 2.0;
					if (cutPoints[i] != newCutPoint) {
						cutPoints[i] = newCutPoint;
						changed = true;
					}
					i++;
				}
				if (i == means.length - 1) {
					return changed;
				}
				previousValue = value;
			}
			return changed;
		};

		meanOptimizer.apply(means);

		int actualIterations = expectationMaximization(means, cutPoints, meanOptimizer, cutPointOptimizer,
				maxIterations);

		if (actualIterations == maxIterations) {
			LOGGER.warning("k-means search might not have converged; maximum number of iterations reached");
		}

		return range(0, k - 1).mapToObj(i -> cutPoints[i]).collect(toList());
	}

	public static <T> List<T> generatorBackedList(IntFunction<T> generator, int size) {
		return new GeneratorBackedList<T>(generator, size);
	}

	private static class GeneratorBackedList<T> extends AbstractList<T> implements List<T> {

		private final int size;

		private final IntFunction<T> generator;

		public GeneratorBackedList(IntFunction<T> generator, int size) {
			this.generator = generator;
			this.size = size;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public T get(int index) {
			return generator.apply(index);
		}

	}

}
