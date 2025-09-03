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

import static java.util.stream.IntStream.range;

import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Provides static utility methods for array operations.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.7.2
 *
 */
public class Arrays {

	private Arrays() {
		;
	}

	public static boolean containedIn(int i, int... intArray) {
		return IntStream.of(intArray).filter(j -> i == j).findAny().isPresent();
	}
	
	public static int[] array(int first, int second, int... more) {
		int[] res=new int[more.length+2];
		res[0] = first;
		res[1] = second;
		for (int i=0; i<more.length; i++) {
			res[i+2] = more[i];
		}
		return res;
	}
	
	public static int min(int first, int... rest) {
		int res = first;
		for (int i=0; i<rest.length; i++) {
			if (res > rest[i]) {
				res = rest[i];
			}
		}
		return res;
	}

	public static int[] filteredRange(int startInclusive, int endExclusive, IntPredicate filter) {
		return range(startInclusive, endExclusive).filter(filter).toArray();
	}

	/**
	 * Applies an operation to all entries of an array where the operation takes as
	 * argument row index (first index), column index (second index), as well as the
	 * value at this position. The operation is provided as a curried function.
	 * 
	 * @param array     the input array
	 * @param operation the operation to be applied to all entries in curried form
	 * 
	 */
	public static void apply(double[][] array,
			Function<Integer, Function<Integer, Function<Double, Double>>> operation) {
		range(0, array.length).forEach(i -> {
			range(0, array[i].length).forEach(j -> {
				array[i][j] = operation.apply(i).apply(j).apply(array[i][j]);
			});
		});
	}

	public static DoubleStream columnStream(double[][] array, int j) {
		return range(0, array.length).mapToDouble(i -> array[i][j]);
	}

	public static DoubleStream columnAggregateStream(double[][] array, double initValue, DoubleBinaryOperator op) {
		return range(0, array[0].length).mapToDouble(j -> columnStream(array, j).reduce(initValue, op));
	}

}
