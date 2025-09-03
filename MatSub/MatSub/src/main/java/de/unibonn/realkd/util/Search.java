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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;

/**
 * Contains various generic search algorithms.
 * 
 * @author Mario Boley
 * 
 * @since 0.2.2
 * 
 * @version 0.3.0
 *
 */
public class Search {

	/**
	 * Performs binary search to find the smallest integer within a specified
	 * range that satisfies a monotone property, i.e., a property p with
	 * <code>p(x)=true</code> implies <code>p(y)=true</code> for
	 * <code>x &lt; y</code>.
	 * 
	 * @param min
	 *            lower boundary (inclusive) of set of potential solutions
	 * @param max
	 *            integer with {@code p(max)=true}
	 * @param property
	 *            {@code p} with {@code p(y) = true} if {@code x<y} and {@code p(x)=true}
	 * @return smallest integer {@code i} between {@code x} and {@code y} (including) such that
	 *         {@code p(i)=true}
	 */
	public static int findSmallest(int min, int max, IntPredicate property) {
		if (property.test(min)) {
			return min;
		}
		int lower = min; // invariant property does not hold for lower
		int upper = max; // invariant property holds for upper

		while (upper - lower > 1) {
			int pivot = lower + (upper - lower) / 2;
			if (property.test(pivot)) {
				upper = pivot;
			} else {
				lower = pivot;
			}
		}

		return upper;
	}

	/**
	 * Computes list of local maxima in a given list of comparables. This
	 * includes the first index (0) as well as the last index if the value at
	 * this index is larger then its successor or predecessor, respectively.
	 * 
	 * @param sequence
	 *            of comparables
	 * @return list of indices of local maxima in sequence
	 */
	public static <T extends Comparable<T>> List<Integer> localMaxIndices(List<T> sequence) {
		List<Integer> result = newArrayList();
		for (int i = 0; i < sequence.size(); i++) {
			/*
			 * check: last or larger than successor AND first or not smaller
			 * than predecessor
			 */
			if ((i >= sequence.size() - 1 || sequence.get(i).compareTo(sequence.get(i + 1)) >= 0)
					&& (i == 0 || sequence.get(i).compareTo(sequence.get(i - 1)) >= 0)) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * <p>
	 * Generic implementation for dynamic programming problems, whose (partial)
	 * solutions can be indexed by a single integer index.
	 * </p>
	 * <p>
	 * The implementation will buffer solutions in an array for which initially
	 * space is allocated equal to the target index.
	 * </p>
	 * 
	 * @param terminal
	 *            function computing the solution for irreducible subproblems
	 * @param reducer
	 *            function computing indices of subproblems to which the problem
	 *            with given index can be reduced
	 * @param combiner
	 *            function that combines solutions of subproblems
	 * @param maxIndex
	 *            target index of problem to be computed
	 * @return the solution of the target problem
	 * 
	 * @param <X>
	 *            type of problem descriptions
	 * 
	 * @param <Y>
	 *            type of problem solutions
	 * 
	 */
	public static <X, Y> Y dynamicProgramming(Function<X, Y> terminal, Function<X, List<X>> reducer,
			BiFunction<X, List<Y>, Y> combiner, X targetProblem, ToIntFunction<X> indexer) {
		return dynamicProgrammingTable(terminal, reducer, combiner, targetProblem, indexer)
				.get(indexer.applyAsInt(targetProblem));
	}

	@SuppressWarnings("unchecked")
	public static <X, Y> List<Y> dynamicProgrammingTable(Function<X, Y> terminal, Function<X, List<X>> reducer,
			BiFunction<X, List<Y>, Y> combiner, X targetProblem, ToIntFunction<X> indexer) {
		Object[] solutions = new Object[indexer.applyAsInt(targetProblem) + 1];
		Stack<X> toCompute = new Stack<>();
		toCompute.push(targetProblem);

		while (!toCompute.empty()) {
			X nextProblem = toCompute.peek();
			int problemIndex = indexer.applyAsInt(nextProblem);

			// has problem already been solved
			if (solutions[problemIndex] != null) {
				toCompute.pop();
				continue;
			}

			List<X> subProblems = reducer.apply(nextProblem);

			// problem can be solved by terminal operation
			if (subProblems.isEmpty()) {
				solutions[problemIndex] = terminal.apply(nextProblem);
				toCompute.pop();
				continue;
			}

			List<X> unsolvedSubProblems = subProblems.stream().filter(x -> solutions[indexer.applyAsInt(x)] == null)
					.collect(toList());

			// problem can be solved because we have solved all relevant
			// subproblems
			if (unsolvedSubProblems.isEmpty()) {
				List<Y> subProblemSolutions = subProblems.stream().map(x -> (Y) solutions[indexer.applyAsInt(x)])
						.collect(toList());
				solutions[problemIndex] = combiner.apply(nextProblem, subProblemSolutions);
				toCompute.pop();
				continue;
			}

			// problem cannot yet been solved
			unsolvedSubProblems.forEach(toCompute::push);
		}
		return (List<Y>) newArrayList(solutions);
		// return (Y) solutions[indexer.applyAsInt(targetProblem)];
	}

	public static interface InPlaceDoubleArrayModifier {

		public boolean apply(double[] params);

	}

	public static int expectationMaximization(double[] params, double[] vars,
			InPlaceDoubleArrayModifier parameterOptimizer, InPlaceDoubleArrayModifier variableOptimizer,
			int maxIterations) {
		for (int i = 0; i < maxIterations; i++) {
			if (!variableOptimizer.apply(vars)) {
				return i + 1;
			}
			if (!parameterOptimizer.apply(params)) {
				return i + 1;
			}
		}
		return maxIterations;
	}

}
