/**
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
 *
 */
package de.unibonn.realkd.algorithms.common;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.Comparator;
import java.util.function.Function;

import de.unibonn.realkd.patterns.Pattern;

/**
 * Interface for target functions for pattern optimization and search problems.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 * 
 */
public interface PatternOptimizationFunction extends Function<Pattern<?>, Double> {

	public enum Target {

		MAXIMIZATION {
			@Override
			public Comparator<Double> preference() {
				/*
				 * NaN is considered larger than all other Double values by
				 * built-in comparator of Double, but is least preferred.
				 * Therefore natural order on doubles has to be inverted
				 * whenever one operand is NaN
				 */
				return (x, y) -> (x.isNaN() || y.isNaN()) ? -1 * y.compareTo(x) : y.compareTo(x);
			}
		},
		MINIMIZATION {
			@Override
			public Comparator<Double> preference() {
				return (x, y) -> x.compareTo(y);
			}
		};

		public abstract Comparator<Double> preference();

	}

	/**
	 * @return the value of the function for the specified pattern
	 */
	public Double apply(Pattern<?> pattern);

	/**
	 * 
	 * @return the optimization target: maximization or minimization
	 */
	public default Target optimizationTarget() {
		return Target.MAXIMIZATION;
	}

	/**
	 * Preference order induced by the optimization function (for maximization,
	 * higher score pattern is "less than" lower score pattern in this order).
	 * 
	 * @return pattern comparator induced by function and optimization target
	 */
	public default Comparator<Pattern<?>> preferenceOrder() {
		return (p, q) -> {
			Double scoreP = apply(p);
			Double scoreQ = apply(q);
			int result = optimizationTarget().preference().compare(scoreP, scoreQ);
			return result;
		};
	}

	/**
	 * Preference order induced by the optimization function (for maximization,
	 * higher score pattern is "less than" lower score pattern in this
	 * order)---treating small relative differences as indistinguishable, i.e.,
	 * p=q if |score(p)-score(q)|/max(score(p),score(q)).
	 * 
	 * @param epsilon
	 *            the maximal relative function difference, for which two
	 *            patterns are treated as equal by this order
	 * 
	 * @return pattern comparator induced by function, optimization target, and
	 *         epsilon
	 * 
	 */
	public default Comparator<Pattern<?>> relaxedPreferenceOrder(double epsilon) {
		return (p, q) -> {
			Double scoreP = apply(p);
			Double scoreQ = apply(q);
			int result = abs((scoreP - scoreQ) / max(scoreP, scoreQ)) <= epsilon ? 0
					: optimizationTarget().preference().compare(scoreP, scoreQ);
			return result;
		};
	}

}
