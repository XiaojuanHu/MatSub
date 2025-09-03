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
package de.unibonn.realkd.util;

import static de.unibonn.realkd.util.Search.dynamicProgramming;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * @author Mario Boley
 * 
 * @since 0.2.0
 * 
 * @version 0.3.0
 *
 */
public class SearchTest {

	@Test
	public void findSmallestTest() {
		assertEquals(Search.findSmallest(-3353, 241, i -> i > 3), 4);
		assertEquals(Search.findSmallest(1, 3, i -> i >= 2), 2);
	}

	@Test
	public void testMaxIndices() {
		assertEquals(ImmutableList.of(0, 2), Search.localMaxIndices(ImmutableList.of(3.0, 1.0, 1.0)));
		assertEquals(ImmutableList.of(2), Search.localMaxIndices(ImmutableList.of(0.3, 0.7, 9.0)));
		assertEquals(ImmutableList.of(1, 3), Search.localMaxIndices(ImmutableList.of(-3.0, 0.3, 0.1, 0.1)));
		assertEquals(ImmutableList.of(1, 3), Search.localMaxIndices(ImmutableList.of(0.0, 0.2, 0.1, 0.2)));
		assertEquals(ImmutableList.of(2, 3), Search.localMaxIndices(ImmutableList.of(1.0, 2.0, 3.0, 3.0)));
		assertEquals(ImmutableList.of(0, 1, 2, 3), Search.localMaxIndices(ImmutableList.of(3.0, 3.0, 3.0, 3.0)));
		assertEquals(ImmutableList.of(2, 4), Search.localMaxIndices(ImmutableList.of(1.0, 2.0, 3.0, 3.0, 4.0)));
	}

	public static class BinomialCoefficientProblem {

		public final int n;

		public final int k;

		private BinomialCoefficientProblem(int n, int k) {
			this.n = n;
			this.k = k;
		}

	}

	public static BinomialCoefficientProblem problem(int n, int k) {
		return new BinomialCoefficientProblem(n, k);
	}

	@Test
	public void testDynamicProgrammingForBinomiaCoefficients() {
		Function<BinomialCoefficientProblem, Integer> terminal = p -> 1;
		Function<BinomialCoefficientProblem, List<BinomialCoefficientProblem>> reducer = p -> (p.n == 0 || p.k == 0
				|| p.n == p.k) ? ImmutableList.of()
						: ImmutableList.of(problem(p.n - 1, p.k - 1), problem(p.n - 1, p.k));
		BiFunction<BinomialCoefficientProblem, List<Integer>, Integer> combiner = (i, l) -> l.get(0) + l.get(1);
		Integer expected = 252;
		assertEquals(expected, dynamicProgramming(terminal, reducer, combiner, problem(10, 5), p -> p.n * 6 + p.k));
//		assertEquals(252, dynamicProgramming(terminal, reducer, combiner, problem(50, 20), p -> p.n * 171 + p.k));
	}

	@Test
	public void dynamicProgrammingForFibonacciTest() {
		// 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987
		Function<Integer, Integer> terminal = i -> 1;
		Function<Integer, List<Integer>> reducer = i -> (i < 3) ? ImmutableList.of() : ImmutableList.of(i - 2, i - 1);
		BiFunction<Integer, List<Integer>, Integer> combiner = (i, l) -> l.get(0) + l.get(1);
		Integer expected1 = 55;
		assertEquals(expected1, dynamicProgramming(terminal, reducer, combiner, 10, i -> i));
		Integer expected2 = 610;
		assertEquals(expected2, dynamicProgramming(terminal, reducer, combiner, 15, i -> i));
	}

}
