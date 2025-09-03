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

package de.unibonn.realkd.algorithms.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.util.Comparison;

/**
 * Collection of post-processors that can be used to prune sampled candidate
 * patterns.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public enum SinglePatternPostProcessor {

	/**
	 * Fixes random order of descriptor elements and tries to remove each
	 * element in that order. Element removed if resulting pattern quality is
	 * not smaller (according to comparator argument) than of pattern with
	 * element.
	 * 
	 */
	OPPORTUNISTIC_LINEAR_PATTERNPRUNER {
		@Override
		public <T extends Pattern<?>> T prune(T origin, PatternOptimizationFunction optimizationFunction,
				BiFunction<LogicalDescriptor, ? super T, ? extends T> toPattern,
				Function<? super T, LogicalDescriptor> descriptorOf) {

			// new fct param: Pattern x Descriptor -> Pattern
			// default: (p,d)->toPattern(d)
			// disadvantage: have to re-check support (probably ok for now)
			final Comparator<Pattern<?>> relaxedPreferenceOrder = optimizationFunction.relaxedPreferenceOrder(0.001);
			final LogicalDescriptor originalDescriptor = descriptorOf.apply(origin);
			// final List<Integer> priorityList =
			// Sampling.getPermutation(originalDescriptor.size());
			final List<Proposition> priorityList = new ArrayList<>(originalDescriptor.elements());
			Collections.shuffle(priorityList);

			T current = origin;

			// for (Integer index : priorityList) {
			// Pattern<?> candidate = toPattern.apply(
			// descriptorOf.apply(current).getGeneralization(originalDescriptor.getElements().get(index)),
			// current);
			for (Proposition p : priorityList) {
				T candidate = toPattern.apply(descriptorOf.apply(current).generalization(p), current);

				if (relaxedPreferenceOrder.compare(candidate, current) <= 0) {
					current = candidate;
				}
			}
			return current;
		}

		@Override
		public String toString() {
			return "LinearRandomPruner";
		}
	},

	GREEDY_PATTERNPRUNER {
		@Override
		public <T extends Pattern<?>> T prune(T origin, PatternOptimizationFunction optimizationFunction,
				BiFunction<LogicalDescriptor, ? super T, ? extends T> toPattern,
				Function<? super T, LogicalDescriptor> toDescriptor) {

			final Comparator<Pattern<?>> relaxedPreferenceOrder = optimizationFunction.relaxedPreferenceOrder(0.001);
			T current = origin;
			boolean improvement = false;
			do {
				final LogicalDescriptor currentDescriptor = toDescriptor.apply(current);
				improvement = false;
				T bestCandidate = null;
				for (Proposition p : currentDescriptor) {
					T candidate = toPattern.apply(currentDescriptor.generalization(p), current);
					bestCandidate = bestCandidate == null ? candidate
							: Comparison.min(relaxedPreferenceOrder, bestCandidate, candidate);
				}
				if (relaxedPreferenceOrder.compare(bestCandidate, current) <= 0) {
					current = bestCandidate;
					improvement = true;
				}
			} while (improvement && toDescriptor.apply(current).size() > 1);
			return current;
		}

		@Override
		public String toString() {
			return "GreedyPruner";
		}
	},

	/**
	 * Trivial pruner that does not change the input pattern and returns the
	 * same object.
	 * 
	 */
	NO_POSTPROCESSOR {
		@Override
		public <T extends Pattern<?>> T prune(T origin, PatternOptimizationFunction optimizationFunction,
				BiFunction<LogicalDescriptor, ? super T, ? extends T> builder,
				Function<? super T, LogicalDescriptor> patternToLogicalDescriptor) {
			return origin;
		}

		@Override
		public String toString() {
			return "NoPostProcessor";
		}
	};

	/**
	 * 
	 * @param origin
	 *            the seed for the pruning procedure
	 * @param optimizationFunction
	 *            the function to maximize by the pruning procedure
	 * @return
	 */
	public abstract <T extends Pattern<?>> T prune(T origin, PatternOptimizationFunction optimizationFunction,
			BiFunction<LogicalDescriptor, ? super T, ? extends T> toPattern,
			Function<? super T, LogicalDescriptor> descriptorOf);

}