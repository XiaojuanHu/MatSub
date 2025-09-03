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
package de.unibonn.realkd.algorithms.emm;

import java.util.function.IntToDoubleFunction;

abstract class SequenceValue<T extends SequenceValue<T>> {
	@Override
	public abstract T clone();

	public abstract void update(T value);

	public abstract int getIndex();

	public abstract boolean lt(T other);
}

interface SequenceEvaluator<T extends SequenceValue<T>> {
	public void apply(int index, T value);
}

public class ConvexMinimizer {
	private ConvexMinimizer() {
	}

	public static <T extends SequenceValue<T>, V extends SequenceValue<V>, E extends SequenceEvaluator<V>> V minimiseSequence(
			int a, int b, V infinity, E se, Algorithm algorithm) {
		V fOpt = algorithm.minimise(a, b, infinity, se);
		return fOpt;
	}

	public static <T extends SequenceValue<T>, V extends SequenceValue<V>, E extends SequenceEvaluator<V>> V minimiseSequence(
			int a, int b, V infinity, E se) {
		return minimiseSequence(a, b, infinity, se, Algorithm.TERNARY_CONVEX);
	}
}

enum Algorithm {
	LINEAR {
		/**
		 * Implements linear search over an integer interval [a,b).
		 * 
		 * @param a
		 *            The beginning of the search interval (inclusive)
		 * @param b
		 *            The end of the search interval (non-inclusive)
		 * @param fn
		 *            The function whose minimum is searched
		 * @param fMin
		 *            A pre-allocated double array f at least 1 element. After
		 *            invocation the first element will contain the minimum
		 *            value or +infinity on error.
		 * @return The smallest minimiser.
		 */
		@Override
		public <T extends SequenceValue<T>, E extends SequenceEvaluator<T>> T minimise(int a, int b, T infinity, E fn) {
			T fOpt = infinity.clone();
			T fVal = infinity.clone();
			for (int ii = a; ii < b; ++ii) {
				fn.apply(ii, fVal);
				if (fVal.lt(fOpt)) {
					fOpt.update(fVal);
				}
			}
			return fOpt;
		}
	},
	TERNARY_CONVEX {
		/**
		 * Implements the (vanilla) ternary search algorithm. The function
		 * provided must be convex over the requested domain. Every iteration
		 * requires 2 function evaluations.
		 * 
		 * @return
		 */
		@Override
		public <T extends SequenceValue<T>, E extends SequenceEvaluator<T>> T minimise(int a, int b, T infinity, E fn) {
			T fOpt = infinity.clone();
			T f1 = infinity.clone();
			T f2 = infinity.clone();
			while (true) {
				int span = (b - a) / 3;
				if (span == 0) {
					fOpt = LINEAR.minimise(a, b, infinity, fn);
					break;
				}
				int idx1 = a + span;
				int idx2 = b - span;
				fn.apply(idx1, f1);
				fn.apply(idx2, f2);
				if (f2.lt(f1)) { // The
									// points
									// in
									// the
									// left
									// part
									// (idx1
									// inclusive)
									// cannot
									// be
									// minima
					a = idx1 + 1; // Discard left
				} else { // The points in idx2 have a point which is larger
							// or equal
							// to idx1
					b = idx2; // Discard right
				}
			}
			return fOpt;
		}
	},
	BINARY_CONVEX {
		/**
		 * Implements a reuse modification to the ternary search algorithm. The
		 * function provided must be convex over the requested domain. Every
		 * iteration requires 2 function evaluations.
		 * 
		 * @return
		 */
		@Override
		public <T extends SequenceValue<T>, E extends SequenceEvaluator<T>> T minimise(int a, int b, T infinity, E fn) {
			T fOpt = infinity.clone();
			if (b - a <= 3) {
				fOpt = LINEAR.minimise(a, b, infinity, fn);
			} else {
				int spanL = (b - a) / 3;
				int spanR = spanL;
				int idxL = a + spanL;
				int idxR = b - spanR;
				int idxM;
				
				T fM = infinity.clone();
				T fL = infinity.clone();
				T fR = infinity.clone();
				fn.apply(idxL, fL);
				fn.apply(idxR, fR);
				while (true) {
					// Set midpoint
					if (fR.lt(fL)) { // The points in the left part (idx
									// inclusive)
						// cannot be minima
						a = idxL + 1; // Discard left
						idxM = idxR;
						fM = fR;
						spanL = idxM - a;
						if (spanL == 0) {
							if (spanR == 2) {
								idxR = a + 1; // Halving the spanR
								fn.apply(idxR, fR);
								if (!fR.lt(fM)) { // (fM <= fR) 
									fOpt.update(fM);
								} else {
									fOpt.update(fR);
								}
							} else {
								fOpt.update(fM);
							}
							break;
						}
					} else {
						b = idxR; // Discard right, inclusive
						idxM = idxL;
						fM = fL;
						spanR = b - idxL;
						if (spanR == 0) {
							fn.apply(a,fL);
							fOpt.update(fL);
							break;
						}
					}
					if (spanL > spanR) {
						spanL /= 2;
						idxL = a + spanL;
						fn.apply(idxL, fL);
						idxR = idxM;
						fR.update(fM);
					} else {
						spanR = (spanR + 1) / 2;
						idxR = b - spanR;
						fn.apply(idxR, fR);
						idxL = idxM;
						fL.update(fM);
					}
				}
				// NaN not supported here any more
				// if (Double.isNaN(fMin[0])) {
				// 	fOpt.update(infinity);
				// }
			}
			return fOpt;
		}
	};
	public abstract <T extends SequenceValue<T>, E extends SequenceEvaluator<T>> T minimise(int a, int b, T infinity,
			E fn);
}

/**
 * Stateful optimizer. Optionally tracks evaluations and optimal values. Also
 * exposes the low level interface.
 * 
 * @author Janis Kalofolias
 */
class TrackingConvexMinimiser {
	protected int evaluations;

	class TrackingSequenceEvaluator<V extends SequenceValue<V>, E extends SequenceEvaluator<V>>
			implements SequenceEvaluator<V> {
		protected E sequenceEvaluator;

		public TrackingSequenceEvaluator(E sequenceEvaluator) {
			this.sequenceEvaluator = sequenceEvaluator;
		}

		@Override
		public void apply(int index, V value) {
			++evaluations;
			sequenceEvaluator.apply(index, value);
		}
	}

	public TrackingConvexMinimiser() {
		reset();
	}

	public void reset() {
		evaluations = 0;
	}

	public int getEvaluations() {
		return evaluations;
	}

	public <T extends SequenceValue<T>, V extends SequenceValue<V>, E extends SequenceEvaluator<V>> V minimiseSequence(
			int a, int b, V infinity, E se, Algorithm algorithm) {
		TrackingSequenceEvaluator<V, E> tse = new TrackingSequenceEvaluator<V, E>(se);
		V fOpt = algorithm.minimise(a, b, infinity, tse);
		return fOpt;
	}

	public <T extends SequenceValue<T>, V extends SequenceValue<V>, E extends SequenceEvaluator<V>> V minimiseSequence(
			int a, int b, V infinity, E se) {
		return minimiseSequence(a, b, infinity, se, Algorithm.TERNARY_CONVEX);
	}
}

class DoubleConvexSequenceMinimiser {
	private double[] value;
	protected int index;
	protected Algorithm algorithm;

	static class DoubleSequenceValue extends SequenceValue<DoubleSequenceValue> {
		protected double value;
		protected int index;
		public static final DoubleSequenceValue infinity = new DoubleSequenceValue();

		DoubleSequenceValue(double value, int index) {
			this.value = value;
			this.index = index;
		}

		DoubleSequenceValue() {
			this(Double.POSITIVE_INFINITY, -1);
		}

		public void update(double value, int index) {
			this.index = index;
			this.value = value;
		}

		public double getValue() {
			return value;
		}

		@Override
		public DoubleSequenceValue clone() {
			return new DoubleSequenceValue(this.value, this.index);
		}

		@Override
		public void update(DoubleSequenceValue sequenceValue) {
			index = sequenceValue.index;
			value = sequenceValue.value;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public boolean lt(DoubleSequenceValue other) {
			return value < other.value;
		}
	}

	static class DoubleFunctionSequenceEvaluator implements SequenceEvaluator<DoubleSequenceValue> {
		protected IntToDoubleFunction fn;

		public DoubleFunctionSequenceEvaluator(IntToDoubleFunction fn) {
			this.fn = fn;
		}

		@Override
		public void apply(int index, DoubleSequenceValue value) {
			double fVal = fn.applyAsDouble(index);
			value.update(fVal, index);
		}
	}

	public void reset() {
		value[0] = Double.POSITIVE_INFINITY;
		index = -1;
	}

	DoubleConvexSequenceMinimiser(Algorithm algorithm) {
		this.value = new double[1];
		this.algorithm = algorithm;
		reset();
	}

	DoubleConvexSequenceMinimiser() {
		this(Algorithm.TERNARY_CONVEX);
	}

	public DoubleConvexSequenceMinimiser setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public double getOptimum() {
		return value[0];
	}

	public int getOptimizer() {
		return index;
	}

	protected void setOptimum(double value) {
		this.value[0] = value;
	}

	/**
	 * Search for the minimum of a function. Depending on the algorithm, the
	 * function must satisfy certain assumptions.
	 * 
	 * @param a
	 *            The minimum integer to search (inclusive)
	 * @param b
	 *            The maximum integer to search (non-inclusive)
	 * @param fn
	 *            The function to minimise.
	 * @param algorithm
	 *            The algorithm to use to minimise.
	 * @return The smallest index among the function minima.
	 */
	public int minimise(int a, int b, IntToDoubleFunction fn, Algorithm algorithm) {
		reset();
		index = minimise(a, b, fn, algorithm, value);
		return index;
	}

	public int minimise(int a, int b, IntToDoubleFunction fn) {
		return minimise(a, b, fn, this.algorithm);
	}

	/**
	 * Search for the minimum of a function. Depending on the algorithm, the
	 * function must validate certain assumptions.
	 * 
	 * @param a
	 *            The minimum integer to search (inclusive)
	 * @param b
	 *            The maximum integer to search (non-inclusive)
	 * @param fn
	 *            The function to minimise.
	 * @param algorithm
	 *            The algorithm to use.
	 * @param fOpt
	 *            An array of at least one double, whose first element will be
	 *            set to the optimum value, if it is not null.
	 * @return The smallest index among the function minima.
	 */
	static public int minimise(int a, int b, IntToDoubleFunction fn, Algorithm algorithm, double[] fOpt) {
		DoubleFunctionSequenceEvaluator se = new DoubleFunctionSequenceEvaluator(fn);
		DoubleSequenceValue infinity = new DoubleSequenceValue();
		DoubleSequenceValue fVal = algorithm.minimise(a, b, infinity, se);
		if (fOpt != null) {
			fOpt[0] = fVal.getValue();
		}
		return fVal.getIndex();
	}
}

class TrackingDoubleConvexSequenceMinimiser extends DoubleConvexSequenceMinimiser {
	protected TrackingConvexMinimiser tm;

	public TrackingDoubleConvexSequenceMinimiser(Algorithm algorithm) {
		super(algorithm);
		tm = new TrackingConvexMinimiser();
		reset();
	}

	public TrackingDoubleConvexSequenceMinimiser() {
		this(Algorithm.TERNARY_CONVEX);
	}

	@Override
	public int minimise(int a, int b, IntToDoubleFunction fn, Algorithm algorithm) {
		DoubleFunctionSequenceEvaluator se = new DoubleFunctionSequenceEvaluator(fn);
		DoubleSequenceValue fVal = tm.minimiseSequence(a, b, DoubleSequenceValue.infinity, se, algorithm);
		this.index = fVal.getIndex();
		setOptimum(fVal.getValue());
		return this.index;
	}

	public int getEvaluations() {
		return tm.getEvaluations();
	}

	public void reset() {
		super.reset();
		if (tm != null) {
			tm.reset();
		}
	}
}
