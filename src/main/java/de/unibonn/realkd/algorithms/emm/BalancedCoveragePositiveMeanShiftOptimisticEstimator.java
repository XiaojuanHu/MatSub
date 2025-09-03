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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.BranchAndBoundSearchNode;
import de.unibonn.realkd.algorithms.emm.DoubleConvexSequenceMinimiser.DoubleSequenceValue;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;

class SelectionData {
	/**
	 * Value data. Contains no missing elements and is sorted in descending
	 * order
	 */
	public final double[] target;

	/**
	 * Control data - the category of the same indexed value. Contains no
	 * missing elements
	 */
	public final int[] control;

	/**
	 * Number of categories in the control. The categories are 0-based indices.
	 */
	public final int numCat;

	/** Occurrences per category */
	final int[] cntCat;

	/** Number of items in the selection. */
	public final int numSel;

	/**
	 * Selects a sorted subset of the population data based on the membership
	 * predicate.
	 * 
	 * @param dataPop
	 *            The population data.
	 * @param containsIndex
	 *            A predicate that determines if an original index is selected.
	 * @param numSel
	 *            Total number of elements in the selection.
	 */
	public SelectionData(PopulationData dataPop, IntPredicate containsIndex, int numSel) {
		control = new int[numSel];
		target = new double[numSel];
		cntCat = new int[dataPop.numCat];
		{ // Iterate over (cleaned) population: filter elements and accumulate
			// category counts
			int idxSel = 0; // Index within the selection
			for (int idxPop = 0; idxPop < dataPop.numPop; ++idxPop) {
				int idxFull = dataPop.mapIdxP2F[idxPop]; // Index in the
															// original data
				if (containsIndex.test(idxFull)) {
					double t = dataPop.target[idxPop];
					int c = dataPop.control[idxPop];
					target[idxSel] = t;
					control[idxSel] = c;
					++cntCat[c];
					++idxSel;
				}
			}
			this.numSel = numSel;
			this.numCat = dataPop.numCat;
		}
	}

	/**
	 * Selects a sorted subset of the population data based on the membership
	 * predicate.
	 * 
	 * @see {@link SelectionData#SelectionData(PopulationData, IntPredicate, int)}.
	 * @param dataPop
	 *            The population data.
	 * @param containsIndex
	 *            A predicate that determines if an original index is selected.
	 */
	public SelectionData(PopulationData dataPop, IntPredicate containsIndex) {
		this(dataPop, containsIndex, (int) IntStream.of(dataPop.mapIdxP2F).filter(containsIndex).count());
	}
}

/**
 * Stateful optimizer. Optionally tracks evaluations and optimal values. Also
 * exposes the low level interface.
 * 
 * @author Janis Kalofolias
 */
class Optimizer {
	private double fOpt;
	private int evaluations;
	private boolean isTracked = false;

	enum Algorithm {
		LINEAR, TERNARY_CONVEX, BINARY_CONVEX
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
	 *            The function to minimize.
	 * @param algorithm
	 *            The algorithm to use.
	 * @return The smallest index among the function minima.
	 */
	public int minSearch(int a, int b, IntToDoubleFunction fn, Algorithm algorithm) {
		reset();
		double[] fOpt = new double[1];
		int idxOpt = -1;
		switch (algorithm) {
		case LINEAR:
			idxOpt = linearMinSearch(a, b, fn, fOpt);
			break;
		case TERNARY_CONVEX:
			idxOpt = ternaryConvexMinSearch(a, b, fn, fOpt);
			break;
		case BINARY_CONVEX:
			idxOpt = binaryConvexMinSearch(a, b, fn, fOpt);
			break;
		default:
			break;
		}
		this.fOpt = fOpt[0];
		return idxOpt;
	}

	/**
	 * Search for the minimum of a function tracking the number of evaluations.
	 * 
	 * @see #minSearch(int, int, IntToDoubleFunction, Algorithm)
	 */
	public int minSearchTrack(int a, int b, IntToDoubleFunction fn, Algorithm algorithm) {
		this.isTracked = true;
		IntToDoubleFunction fnTrack = x -> {
			++this.evaluations;
			final double value = fn.applyAsDouble(x);
			return value;
		};
		final int idxMin = minSearch(a, b, fnTrack, algorithm);
		this.isTracked = false;
		return idxMin;
	}

	/** Get the optimum value of the last search. */
	public double getOptimum() {
		return this.fOpt;
	}

	/** Get the number of function evaluations of the last (tracked) search. */
	public int getEvaluations() {
		return this.evaluations;
	}

	/**
	 * Reset the internal counters. This function is automatically invoked
	 * before each (instance) search.
	 */
	public void reset() {
		fOpt = Double.POSITIVE_INFINITY;
		evaluations = isTracked ? 0 : -1;
	}

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
	 *            invocation the first element will contain the minimum value or
	 *            +infinity on error.
	 * @return The smallest minimizer.
	 */
	public static int linearMinSearch(int a, int b, IntToDoubleFunction fn, double[] fMin) {
		int idxMin = -1;
		double fOpt = Double.POSITIVE_INFINITY;
		for (int ii = a; ii < b; ++ii) {
			double fVal = fn.applyAsDouble(ii);
			if (fVal < fOpt) {
				fOpt = fVal;
				idxMin = ii;
			}
		}
		fMin[0] = fOpt;
		return idxMin;
	}

	/**
	 * Implements the (vanilla) ternary search algorithm. The function provided
	 * must be convex over the requested domain. Every iteration requires 2
	 * function evaluations.
	 * 
	 * @return
	 */
	public static int ternaryConvexMinSearch(int a, int b, IntToDoubleFunction f, double[] fMin) {
		int idxMin;
		while (true) {
			int span = (b - a) / 3;
			if (span == 0) {
				idxMin = linearMinSearch(a, b, f, fMin);
				break;
			}
			int idx1 = a + span;
			int idx2 = b - span;
			double f1 = f.applyAsDouble(idx1);
			double f2 = f.applyAsDouble(idx2);
			if (f1 > f2) { // The points in the left part (idx1 inclusive)
							// cannot be minima
				a = idx1 + 1; // Discard left
			} else { // The points in idx2 have a point which is larger or equal
						// to idx1
				b = idx2; // Discard right
			}
		}
		return idxMin;
	}

	/**
	 * Implements a reuse modification to the ternary search algorithm. The
	 * function provided must be convex over the requested domain. Every
	 * iteration requires 2 function evaluations.
	 * 
	 * @return
	 */
	public static int binaryConvexMinSearch(int a, int b, IntToDoubleFunction fn, double[] fMin) {
		int idxMin;
		if (b - a <= 3) {
			idxMin = linearMinSearch(a, b, fn, fMin);
		} else {
			int spanL = (b - a) / 3;
			int spanR = spanL;
			int idxL = a + spanL;
			int idxR = b - spanR;
			int idxM;
			double fM;

			double fL = fn.applyAsDouble(idxL);
			double fR = fn.applyAsDouble(idxR);
			while (true) {
				// Set midpoint
				if (fL > fR) { // The points in the left part (idx1 inclusive)
					// cannot be minima
					a = idxL + 1; // Discard left
					idxM = idxR;
					fM = fR;
					spanL = idxM - a;
					if (spanL == 0) {
						if (spanR == 2) {
							idxR = a + 1; // Halving the spanR
							fR = fn.applyAsDouble(idxR);
							if (fM <= fR) {
								idxMin = idxM;
								fMin[0] = fM;
							} else {
								idxMin = idxR;
								fMin[0] = fR;
							}
						} else {
							idxMin = idxM;
							fMin[0] = fM;
						}
						break;
					}
				} else {
					b = idxR; // Discard right, inclusive
					idxM = idxL;
					fM = fL;
					spanR = b - idxL;
					if (spanR == 0) {
						fL = fn.applyAsDouble(a);
						idxMin = a;
						fMin[0] = fL;
						break;
					}
				}
				if (spanL > spanR) {
					spanL /= 2;
					idxL = a + spanL;
					fL = fn.applyAsDouble(idxL);
					idxR = idxM;
					fR = fM;
				} else {
					spanR = (spanR + 1) / 2;
					idxR = b - spanR;
					fR = fn.applyAsDouble(idxR);
					idxL = idxM;
					fL = fM;
				}
			}
			if (Double.isNaN(fMin[0])) {
				idxMin = -1;
				fMin[0] = Double.POSITIVE_INFINITY;
			}
		}
		return idxMin;
	}
}

/**
 * Provides the tight optimistic estimator for Representative Subgroups (with Binary control) maximising Coverage-(Positive)
 * Mean Shift.
 * 
 * @author Janis Kalofolias
 *
 * @since 0.5.1
 * 
 * @version 0.6.1
 * 
 */
public class BalancedCoveragePositiveMeanShiftOptimisticEstimator implements ToDoubleFunction<SelectionData> {

	// TODO: establish independence to the dataPop and remove
	// /** Holds population data, target and control values */
	// private final PopulationData dataPop; // Population Data
	/** Holds statistics on the population data */
	private Optimizer.Algorithm optimisationAlgorithm = Optimizer.Algorithm.TERNARY_CONVEX;
	private final PopulationStatistics statPop;
	private double defaultControlClass0Probability; // Default to use in instantiating a CCSMeasure
	private double expCovTend;
	private double expRepr;
	private double expScale;

	protected int debug = 0;

	public int getDebug() {
		return debug;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public static final Logger LOGGER = Logger.getLogger("BalancedCoveragePositiveMeanShiftOptimisticEstimator");

	private <T> BalancedCoveragePositiveMeanShiftOptimisticEstimator(PopulationData dataPop, PopulationStatistics statPop) {
		assert dataPop.numCat == 2 : "Class count is not 2.";

		this.statPop = statPop;
	}
	
	public <T> BalancedCoveragePositiveMeanShiftOptimisticEstimator(PopulationData dataPop) {
		assert dataPop.numCat == 2 : "Class count is not 2.";

		statPop = new PopulationStatistics(dataPop.target, dataPop.control, dataPop.cntCat);
		assert IntStream.of(dataPop.cntCat).sum() == dataPop.numPop : "leaking elements: count classes";
		setControlClass0Probability();
		setExponents(1, 1);
	}

	public <T> BalancedCoveragePositiveMeanShiftOptimisticEstimator(PopulationData dataPop, double controlClass0Probability) {
		this(dataPop, new PopulationStatistics(dataPop.target, dataPop.control, dataPop.cntCat));
		
		setControlClass0Probability(controlClass0Probability);
	}

	/**
	 * Set the exponents that scale the objective function. The objective is
	 * (fCov*fTend)^\alpha * (fRepr)^\beta, where \alpha and \beta are the
	 * exponents of the coverage-tendency and the representativeness term,
	 * respectively.
	 * 
	 * @param expCovTend
	 *            The exponent to raise the joint coverage-tendency term to.
	 * @param expRepr
	 *            The exponent of the representativeness term.
	 */
	public BalancedCoveragePositiveMeanShiftOptimisticEstimator setExponents(double expCovTend, double expRepr) {
		this.expScale = Math.max(expCovTend, expRepr);
		this.expCovTend = expCovTend / expScale;
		this.expRepr = expRepr / expScale;
		return this;
	}

	/**
	 * Set the exponent of the representativeness term in the objective
	 * function.
	 * 
	 * @see #setExponents(double, double)
	 * @param expRepr
	 *            the value of the exponent.
	 * @return old value of the representativeness exponent
	 */
	public BalancedCoveragePositiveMeanShiftOptimisticEstimator setExponentRepr(double expRepr) {
		final double oldExpCovTend = getExponentCovTend();
		setExponents(oldExpCovTend, expRepr);
		return this;
	}

	public double getExponentRepr() {
		return expRepr * expScale;
	}

	/**
	 * Set the exponent of the joint coverage-tendency term in the objective
	 * function.
	 * 
	 * @see #setExponents(double, double)
	 * @param expRepr
	 *            the value of the exponent.
	 * @return this object (useful to chain set operations)
	 */
	public BalancedCoveragePositiveMeanShiftOptimisticEstimator setExponentCovTend(double expCovTend) {
		final double oldExpRepr = getExponentRepr();
		setExponents(expCovTend, oldExpRepr);
		return this;
	}

	public double getExponentCovTend() {
		return expCovTend * expScale;
	}
	
	/**
	 * Set the desired control class ratio (count of class 1 over that of class 0)
	 * 
	 * If not specified, defaults to the one from the population.
	 * 
	 * @param probability
	 * @return this object (useful to chain set operations)
	 */
	public BalancedCoveragePositiveMeanShiftOptimisticEstimator setControlClass0Probability(double probability) {
		if (probability >= 0 && probability<=1) {
			defaultControlClass0Probability = probability;
		} else {
			throw new RuntimeException("The value of the probability parameter must lie in [0,1]");
		}
		return this;
	}

	public BalancedCoveragePositiveMeanShiftOptimisticEstimator setControlClass0Probability() {
		final double probability = (double) statPop.cntCat[0]/(statPop.cntCat[0]+statPop.cntCat[1]);
		return setControlClass0Probability(probability);
	}

	public double getControlClass0Probability() {
		return defaultControlClass0Probability;
	}
	/**
	 * Compute the value of the objective function on a specific point of the
	 * CCS.
	 * 
	 * @param l
	 *            the point in the CCS
	 * @param meas
	 *            An instance of the CCS measure class
	 * @return The value of the current point.
	 */
	public double objectiveValue(final int[] l, final ClassCountSpaceMeasures meas) {
		final double fnCov = meas.computeMeasure(l, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_COVERAGE);
		final double fnTend = meas.computeMeasure(l, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_MEAN);
		final double fnRepr = meas.computeMeasure(l,
				ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_TOTAL_VARIATION_SIMILARITY);
		final double fnCovTendWei = Math.pow(fnCov * fnTend, expCovTend);
		final double fnReprWei = Math.pow(fnRepr, expRepr);
		final double fnVal = fnCovTendWei * fnReprWei;
		return fnVal;
	}

	public class SelectionEstimation {
		final int[] optCounts;
		final double optValue;
		final int[] ctOptCounts;
		final double ctOptValue;
		final int ctOptIndex;

		class SSTOptimalValue extends SequenceValue<SSTOptimalValue> {
			protected int subIndex;
			protected DoubleSequenceValue dsv;

			public SSTOptimalValue(double value, int index, int subIndex) {
				dsv = new DoubleSequenceValue(value, index);
				this.subIndex = subIndex;
			}

			public SSTOptimalValue() {
				dsv = new DoubleSequenceValue();
				this.subIndex = -1;
			}

			@Override
			public SSTOptimalValue clone() {
				final double value = dsv.getValue();
				final int index = dsv.getIndex();
				SSTOptimalValue c = new SSTOptimalValue(value, index, subIndex);
				return c;
			}

			public void update(double value, int index, int subIndex) {
				dsv.update(value, index);
				this.subIndex = subIndex;
			}

			public void update(SSTOptimalValue value) {
				dsv.update(value.dsv);
				subIndex = value.subIndex;
			}

			@Override
			public int getIndex() {
				return dsv.getIndex();
			}

			public int getSubIndex() {
				return subIndex;
			}

			public double getValue() {
				return dsv.getValue();
			}

			@Override
			public boolean lt(SSTOptimalValue other) {
				return dsv.lt(other.dsv);
			}
		}

		class SSTOptimumEvaluator implements SequenceEvaluator<SSTOptimalValue> {
			protected final int dimSrc;
			protected ToDoubleFunction<int[]> fnVal;
			protected final int cntSrcMin;
			protected final int cntSrcMax;
			protected final Algorithm algorithm;

			private final int[] coord;
			private DoubleConvexSequenceMinimiser dsm;

			SSTOptimumEvaluator(int dimSrc, int[] ctOptCounts, final SelectionData dataSel, ClassCountSpaceMeasures m,
					Algorithm algorithm) {
				// Set up minimum/maximum indices
				final int ctOptCountMin = Math.min(ctOptCounts[0], ctOptCounts[1]);
				final int ctOptCountMax = Math.max(ctOptCounts[0], ctOptCounts[1]);
				final int cntCatSrc = dataSel.cntCat[dimSrc];
				this.cntSrcMin = ctOptCountMin;
				this.cntSrcMax = Math.min(ctOptCountMax, cntCatSrc); // Search

				// Setup function values and properties
				this.fnVal = l -> -objectiveValue(l, m);
				this.dimSrc = dimSrc;
				this.algorithm = algorithm;

				// Initialise privates
				coord = new int[2];
				dsm = new DoubleConvexSequenceMinimiser(algorithm);
			}

			protected double sweepValue(int index) {
				coord[dimSrc] = index;
				final double value = fnVal.applyAsDouble(coord);
				return value;
			}

			@Override
			public void apply(int index, SSTOptimalValue result) {
				// Set Constant Index
				coord[1 - dimSrc] = index;
				// Optimum search along search dimension
				final int cntSrcMaxCur = Math.min(index, cntSrcMax);
				final int idxOpt = dsm.minimise(cntSrcMin, cntSrcMaxCur + 1, this::sweepValue);
				final double fOpt = dsm.getOptimum();

				result.update(fOpt, index, idxOpt);
			}
		}

		public SelectionEstimation(final SelectionData dataSel) {
			this(dataSel, new SelectionStatistics(dataSel));
		}

		public SelectionEstimation(final SelectionData dataSel, final SelectionStatistics statSel) {
			this(dataSel, statSel, new ClassCountSpaceMeasures(statSel, statPop, getControlClass0Probability()));
		}

		public SelectionEstimation(final SelectionData dataSel, final SelectionStatistics statSel,
				final ClassCountSpaceMeasures m) {
			if (dataSel.numSel == 0) { // Initialise sensible values for an
										// empty selector
				optCounts = null;
				optValue = Double.NEGATIVE_INFINITY;
				ctOptCounts = null;
				ctOptValue = Double.NEGATIVE_INFINITY;
				ctOptIndex = -1;
			} else {
				
				final double controlClass0Probability = m.getControlProbabilities()[0];
				final double controlClassRatio1over0 = 
						(1-controlClass0Probability)/controlClass0Probability;
				{
					final double[] fOpt = new double[1];
					ctOptIndex = m.getOptimalCTIndex(fOpt);
					ctOptValue = fOpt[0];
				}
				ctOptCounts = statSel.getCTPathPoint(ctOptIndex);

				optCounts = new int[] { -1, -1 };

				// These points along with ctOptCounts define the 3 vertices of the sufficient search triangle.
				final double raySlope1over0 = controlClassRatio1over0;
				// The minimisation with the selection counts is needed in cases that round yields infinity.
				final int[] SSTVertexA = new int[] { ctOptCounts[0], (int) Math.min(statSel.cntCat[1],Math.round(ctOptCounts[0] * raySlope1over0)) };
				final int[] SSTVertexB = new int[] { (int) Math.min(statSel.cntCat[0], Math.round(ctOptCounts[1] / raySlope1over0)), ctOptCounts[1] };

				double fMax = Double.NEGATIVE_INFINITY;
				Optimizer opt = new Optimizer();

				ToDoubleFunction<int[]> fnVal = l -> objectiveValue(l, m);

				{ // Search within the SST Triangle
					
					// We search within the SST along the directions over which the TVD is concave, so that the overall 
					// objective is also concave. The direction along which this search is done, is designated by dimScan
					final int dimScan = ctOptCounts[0]*raySlope1over0 < ctOptCounts[1] ? 0 : 1;
					final int dimConst = 1 - dimScan; // The other dimension, kept constant within each iteration
					
					// Maximum index over the search direction
					final int cntCatScan = dataSel.cntCat[dimScan];
	
	
	
				    final int cntConstMin = Math.min(SSTVertexA[dimConst],SSTVertexB[dimConst]);
				    final int cntConstMax = Math.max(SSTVertexA[dimConst],SSTVertexB[dimConst]);

					// The ray slope: Scan dimension over Const dimension
					final double raySlopeSOC = dimScan == 1?controlClassRatio1over0:1/controlClassRatio1over0;

					final int cntScanBegin = Math.min(SSTVertexA[dimScan],SSTVertexB[dimScan]);
					for(int cntIter = cntConstMin; cntIter <= cntConstMax; ++cntIter) {
						// The maximisation is needed to ensure at least one point is searched in corner cases.
					    final double cntScanMax = Math.max(cntScanBegin, Math.round(cntIter*raySlopeSOC));
					    // 	Search must not exceed count
					    final int cntScanEnd = Math.min((int) cntScanMax, cntCatScan); 
					    
					    final int cntConst = cntIter;
						final IntToDoubleFunction fValScan = dimScan == 0
								? cnt0 -> -fnVal.applyAsDouble(new int[] { cnt0, cntConst })
								: cnt1 -> -fnVal.applyAsDouble(new int[] { cntConst, cnt1 });

						final int idxOpt = opt.minSearch(cntScanBegin, cntScanEnd + 1, fValScan, optimisationAlgorithm);

						final double fOpt = -opt.getOptimum();

						// Hopefully useful debugging printing:
						// System.err.format("  %c Scanning: %2dx%2d-%2d, %6f (fmax=%9f) at (%2d %2d)\n", fOpt>fMax?'+':' ',cntIter,cntScanBegin,cntScanEnd,fOpt,fMax,dimScan==0?idxOpt:cntIter,dimScan==1?idxOpt:cntIter); 
						if (fOpt > fMax) {
							fMax = fOpt;
							optCounts[dimScan] = idxOpt;
							optCounts[dimConst] = cntIter;
						}
					}
				}

				// Compute the Manhattan distance to the equi-representativeness ray from a point in the CCS
				double distSSTOptToRay = Math.abs(optCounts[0]*raySlope1over0 - optCounts[1]); // Distance along dim 1
				// The distance along dim 0 is the one along dim1 divided by the slope. Thus, the Manhattan distance is 
				distSSTOptToRay = Math.min(distSSTOptToRay, distSSTOptToRay/raySlope1over0); 
				
				if (distSSTOptToRay < 1) { // Must search along the ray
					// We now scan along the slowest rising dimension, to cover the entire line in one pass 
				    final int dimScan = controlClassRatio1over0>1?0:1; 
				    final int dimConst = 1 - dimScan;
				    

					class RayScanner{
						public double fMax = Double.NEGATIVE_INFINITY;
						public double distToRayMin = 1;
						public final int[] optCounts = {-1,-1};
						private final double raySlopeSOC;
						final ToDoubleFunction<int[]> fnVal;
						
					
						public RayScanner(int dimScan, ToDoubleFunction<int[]> fnVal) {
							raySlopeSOC = dimScan == 1?controlClassRatio1over0:1/controlClassRatio1over0;
							this.fnVal = fnVal;
						}
						
						/** make a count vector based on the counts along scan and const dimensions.*/
					    int[] makeCounts(int cntScan, int cntConst) {
					    	final int[] cnt = {-1,-1};
					    	cnt[dimScan] = cntScan;
					    	cnt[dimConst] = cntConst;
					    	return cnt;
					    }
					
						/** Compute the distance to the equi-representativeness ray along the scan dimension. */
						double distToRayScan(int[] cnt) {
				    		return Math.abs(cnt[dimScan]-cnt[dimConst]*raySlopeSOC);
				    	}
						/** Reset the minimum distance to the specified value. This value is typically the distance from the ray of the optimum point within the SST, reset before each sweet of the upper or bottom portion of the equi-class-count ray. */
				    	public void resetDistance(double distToRay) {
				    		distToRayMin = distToRay;
				    	}
				    	
						/** Test if a new index along the ray is better than the current optimum. */
				    	public void testIndex(int cntScan, int cntConst) {
							final int[] cnt = makeCounts(cntScan, cntConst);
							final double dist = distToRayScan(cnt);
							// Hopefully useful debugging printing:
							// System.err.format("%c Point: (%d,%d): dist: %6f dist2ray: %6f\n", dist<distToRayMin?'+':' ',cnt[0],cnt[1],dist,distToRayMin);
							if(dist < distToRayMin) {
						    	final double fVal = fnVal.applyAsDouble(cnt);
						    	if (fVal>fMax) {
						    		optCounts[0] = cnt[0];optCounts[1] = cnt[1];
						    		fMax = fVal;
						    	}
						    	distToRayMin = dist;
							}
						}
					}
					
				    final double raySlopeSOC = dimScan==1?controlClassRatio1over0:1/controlClassRatio1over0;
				    final int cntConstMin = Math.min(SSTVertexA[dimConst],SSTVertexB[dimConst]);
				    final int cntConstMax = Math.max(SSTVertexA[dimConst],SSTVertexB[dimConst]);
			    	// Search along the portion of the equi-representativeness ray lying BELOW the SST
			    	RayScanner scanner = new RayScanner(dimScan, fnVal);
			    	
			    	scanner.resetDistance(distSSTOptToRay);// We run the ray along the direction of diminishing ct-values. Therefore, if we encounter a point with the same distance as a previous, it can only have a worse objective value. The maximal such distance is the one achieved by the current optimum within the SST.  
				    for(int cntIter=cntConstMin-1;cntIter>0;--cntIter) {
				        scanner.testIndex((int) Math.ceil(cntIter*raySlopeSOC),cntIter);
				    }
			    	scanner.resetDistance(distSSTOptToRay);// We run the ray along the direction of diminishing ct-values. Therefore, if we encounter a point with the same distance as a previous, it can only have a worse objective value. The maximal such distance is the one achieved by the current optimum within the SST.  
				    for(int cntIter=cntConstMin-1;cntIter>0;--cntIter) {
				        scanner.testIndex((int) Math.floor(cntIter*raySlopeSOC),cntIter);
				    }

				    // Search along the portion of the equi-representativeness ray lying ABOVE the SST
				    { // Search points immediately above the ray
				    	final int cntIterEnd = Math.min(dataSel.cntCat[dimConst], (int) Math.floor(dataSel.cntCat[dimScan]/raySlopeSOC));
				    	scanner.resetDistance(distSSTOptToRay); // This is necessary. Not finding a good point below does not rule out the existence of a more distant better point on the above portion.
					    for(int cntIter=cntConstMax+1;cntIter<=cntIterEnd;++cntIter) {
					        scanner.testIndex((int) Math.ceil(cntIter*raySlopeSOC),cntIter);
					    }
				    }
				    { // Search points immediately below the ray
				    	final int cntIterEnd = Math.min(dataSel.cntCat[dimConst], (int) Math.ceil(dataSel.cntCat[dimScan]/raySlopeSOC));
				    	scanner.resetDistance(distSSTOptToRay); // This is necessary. Not finding a good point below does not rule out the existence of a more distant better point on the above portion.
					    for(int cntIter=cntConstMax+1;cntIter<=cntIterEnd;++cntIter) {
					        scanner.testIndex((int) Math.floor(cntIter*raySlopeSOC),cntIter);
					    }
				    }
				    
					if(scanner.fMax > fMax) {
						fMax = scanner.fMax;
						optCounts[0] = scanner.optCounts[0];
						optCounts[1] = scanner.optCounts[1];
					}
				}
				
				optValue = Math.pow(fMax, expScale);
			}
		}

	}

	@Override
	public double applyAsDouble(SelectionData dataSel) {
		SelectionEstimation selEst = new SelectionEstimation(dataSel);
		return selEst.optValue;
	}

	/**
	 * @return the optimisationAlgorithm
	 */
	public Optimizer.Algorithm getOptimisationAlgorithm() {
		return optimisationAlgorithm;
	}

	/**
	 * @param optimisationAlgorithm
	 *            the optimisationAlgorithm to set
	 */
	public Optimizer.Algorithm setOptimisationAlgorithm(Optimizer.Algorithm optimisationAlgorithm) {
		final Optimizer.Algorithm optimisationAlgorithmOld = this.optimisationAlgorithm;
		this.optimisationAlgorithm = optimisationAlgorithm;
		return optimisationAlgorithmOld;
	}

	/**
	 * Create the realKD-internals-agnostic PopulationData class from the taget and control variables.
	 * The PopulationData class tracks the non-missing target and control value pairs, along with their initial
	 * indices, to facilitate efficient selecting of a sorted subset of the population at the B&B Search Nodes.  
	 * @param target The target attribute. Must be continuous.
	 * @param control The control attribute. Must be discrete.
	 * @return A PopulationData structure
	 * @see PopulationData
	 */
	public static <T> PopulationData makePopulationData(MetricAttribute target, CategoricAttribute<T> control) {
		// Reference the data from the attributes
		List<Optional<Double>> lstTargFull = target.getValues();
		List<Optional<T>> lstCtrlFull = control.getValues();
	
		// Get sorted non-missing targets and controls, in ascending order
		List<Integer> idxAsc = target.sortedNonMissingRowIndices();
		int[] idxOrder = Lists.reverse(idxAsc).stream().filter(i -> lstCtrlFull.get(i).isPresent())
				.mapToInt(Integer::intValue).toArray();
	
		IntToDoubleFunction fTarget = i -> lstTargFull.get(i).get();
		IntStream sOrderPop = IntStream.of(idxOrder);
		IntUnaryOperator fControl;
		{ // Map control values to indices
			Map<Object, Integer> mapC2U;
			AtomicInteger idxAt = new AtomicInteger(0);
			mapC2U = control.categories().stream().collect(Collectors.toMap(k -> k, k -> idxAt.getAndIncrement()));
			fControl = i -> {
				Optional<T> ctrlVal = lstCtrlFull.get(i);
				return ctrlVal.isPresent() ? mapC2U.get(ctrlVal.get()) : 0;
			};
		}
		int numFull = target.getValues().size();
		int numCat = control.categories().size();
		return new PopulationData(fTarget, fControl, sOrderPop, numFull, numCat);
	}

}


/**
 * Provides a set of common measures over the CCS for the population and the
 * currently selected set. CCS stands for the Class Counting Space.
 * 
 * @see ClassCountSpaceMeasures#computeMeasure
 * @author Janis Kalofolias
 */
class ClassCountSpaceMeasures {
	public enum Type {
		MEASURE_NORMALIZED_COVERAGE, MEASURE_NORMALIZED_MEAN, MEASURE_MEAN, MEASURE_NORMALIZED_TOTAL_VARIATION_SIMILARITY, MEASURE_TOTAL_VARIATION_DISTANCE
	};

	/** A collection of statistics for the currently selected set */
	private final SelectionStatistics selStats;
	/** A collection of statistics for the entire population */
	private final PopulationStatistics popStats;
	
	/** The target control class probabilities. */
	private final double[] controlProbabilities;
	/** Minimum control class probability. Needed for normalised TVD */
	private final double minControlProbability;
	
	ClassCountSpaceMeasures(SelectionStatistics selStats, PopulationStatistics popStats, double controlClass0Probability) {
		this.selStats = selStats;
		this.popStats = popStats;
		if (Double.isNaN(controlClass0Probability)) {
			controlClass0Probability = popStats.cntCat[0]/(popStats.cntCat[0]+popStats.cntCat[1]);
		}
		controlProbabilities = new double[] {controlClass0Probability, 1-controlClass0Probability};
		this.minControlProbability = DoubleStream.of(controlProbabilities).min().orElse(Double.POSITIVE_INFINITY);
	}

	ClassCountSpaceMeasures(SelectionStatistics selStats, PopulationStatistics popStats) {
		this(selStats, popStats, popStats.prbCat[0]);
	}

	
	/**
	 * 
	 * @param values
	 *            a <em>sorted</em> sequence of values
	 * @param categories
	 *            a <em>sorted</em> sequence of the categories of the values.
	 *            The categories must be indices from 0 to numCat
	 * @param cntCat
	 *            count of each category
	 * @param cntCatPop
	 *            count of each category on the population
	 */
	ClassCountSpaceMeasures(double[] values, int[] categories, int[] cntCat, PopulationStatistics popStats) {
		this(new SelectionStatistics(values, categories, cntCat), popStats);
	}

	ClassCountSpaceMeasures(double[] values, int[] categories, int[] cntCat) {
		this(new SelectionStatistics(values, categories, cntCat));
	}

	ClassCountSpaceMeasures(double[] values, int[] categories) {
		this(new SelectionStatistics(values, categories));
	}

	ClassCountSpaceMeasures(SelectionStatistics selStats) {
		this(selStats, new PopulationStatistics(selStats));
	}


	public PopulationStatistics getPopulationStats() {
		return popStats;
	}

	public SelectionStatistics getSelectionStats() {
		return selStats;
	}
	
	public double[] getControlProbabilities() {
		return controlProbabilities;
	}

	/**
	 * Computes the requested measure over the Class Counting Space (CCS)
	 * 
	 * @param cntRef
	 *            The point in the CCS corresponding to some (c-optimal)
	 *            refinement of the selection. Essentially an array of
	 *            occurrence counts per category in the refinement.
	 * @param measure
	 *            measure to apply
	 * @return value of the measure requested
	 */
	public double computeMeasure(int[] cntRef, Type measure) {
		int numCat = selStats.mapC2S.length;
		switch (measure) {
		case MEASURE_NORMALIZED_COVERAGE: {
			int numSel = IntStream.of(cntRef).sum();
			return (float) numSel / popStats.numItems;
		}
		case MEASURE_MEAN: {
			double valSum = 0;
			int numRef = 0; // Number of elements in the current refinement
			for (int cit = 0; cit < numCat; ++cit) {
				final int numRefCat = cntRef[cit];
				valSum += selStats.cumSum[cit][numRefCat];
				numRef += numRefCat;
			}
			return valSum / numRef;
		}
		case MEASURE_NORMALIZED_MEAN: {
			final double mean = computeMeasure(cntRef, Type.MEASURE_MEAN);
			final double nu = popStats.maxValue - popStats.meanValue;
			final double meanNrm = (mean - popStats.meanValue) / nu;
			return Math.max(meanNrm, 0);
		}
		case MEASURE_TOTAL_VARIATION_DISTANCE: {
			int numSel = IntStream.of(cntRef).sum();
			double tvd = 0;
			for (int cit = 0; cit < numCat; ++cit) {
				tvd += Math.abs((double) cntRef[cit] / numSel - controlProbabilities[cit]);
			}
			return tvd / 2;
		}
		case MEASURE_NORMALIZED_TOTAL_VARIATION_SIMILARITY: {
			double tvd = computeMeasure(cntRef, Type.MEASURE_TOTAL_VARIATION_DISTANCE);
			double nu = 1 - minControlProbability;
			return 1 - tvd / nu;
		}
		default:
			return Double.NaN;
		}
	}

	/**
	 * Get the index of the optimal point within the c-t Path
	 * 
	 * @param fCTOpt
	 *            An array of doubles: if it is not null, the optimal value is
	 *            stored in its first element.
	 * @return The coordinates within the CCS of the point with the greatest
	 *         value.
	 */
	public int getOptimalCTIndex(double[] fCTOpt) {
		int numItem = selStats.numSel;

		IntToDoubleFunction fnCTValue = idx -> {
			int[] ctPathPoint = selStats.getCTPathPoint(idx);
			double fVal = computeMeasure(ctPathPoint, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_COVERAGE)
					* computeMeasure(ctPathPoint, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_MEAN);
			return -fVal; // Make convex for the search algorithm
		};
		Optimizer opt = new Optimizer();
		int idx = opt.minSearch(0, numItem + 1, fnCTValue, Optimizer.Algorithm.TERNARY_CONVEX);
		if (fCTOpt != null) {
			fCTOpt[0] = -opt.getOptimum();
		}
		return idx;
	}

	public int getOptimalCTIndex() {
		int numItem = selStats.numSel;

		IntToDoubleFunction fnCTValue = idx -> {
			int[] ctPathPoint = selStats.getCTPathPoint(idx);
			double fVal = computeMeasure(ctPathPoint, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_COVERAGE)
					* computeMeasure(ctPathPoint, ClassCountSpaceMeasures.Type.MEASURE_NORMALIZED_MEAN);
			return -fVal; // Make convex for the search algorithm
		};
		Optimizer opt = new Optimizer();
		int idx = opt.minSearch(0, numItem + 1, fnCTValue, Optimizer.Algorithm.TERNARY_CONVEX);
		return idx;
	}

	/**
	 * Get the optimal point within the c-t Path
	 * 
	 * @return The coordinates within the CCS of the point with the greatest
	 *         value.
	 */
	public int[] getOptimalCTPoint() {
		int idx = getOptimalCTIndex();
		return selStats.getCTPathPoint(idx);
	}
}

// TODO: javadoc
class SelectionStatistics {
	/**
	 * Cumulative sum per category. The entry cumSum[k][s] holds the sum of the
	 * greatest s target values with category k.
	 */
	final double[][] cumSum;
	/**
	 * Cumulative category count per category (a.k.a. the c-t Path in the Class
	 * Counting Space). The entry cumCnt[k][s] holds the count of values with
	 * category k in the s greatest target values.
	 */
	final int[][] cumCnt;
	/**
	 * Selection index per category. The entry mapC2S[k][s] holds the index of
	 * the s-th largest target value with category k.
	 */
	final int[][] mapC2S;
	/** Occurrences per category */
	final int[] cntCat;
	/** Number of items in the selection */
	final int numSel;
	/** Number of classes */
	final int numCat;

	SelectionStatistics(double[][] cumSum, int[][] cumCnt, int[][] mapC2S, int[] cntCat, int numSel) {
		this.cumSum = cumSum.clone();
		this.mapC2S = mapC2S.clone();
		this.cumCnt = cumCnt.clone();
		this.cntCat = cntCat.clone();
		this.numSel = numSel;
		this.numCat = mapC2S.length;
	}

	SelectionStatistics(double[][] cumSum, int[][] cumCnt, int[][] mapC2S, int[] cntCat) {
		this(cumSum, cumCnt, mapC2S, cntCat, IntStream.of(cntCat).sum());
	}

	SelectionStatistics(double[][] cumSum, int[][] cumCnt, int[][] mapC2S) {
		this(cumSum, cumCnt, mapC2S, Arrays.stream(mapC2S).mapToInt(item -> item.length).toArray());
	}

	SelectionStatistics(double[] values, int[] categories, int[] cntCat) {
		numCat = cntCat.length;
		numSel = values.length;
		assert values.length == categories.length : "Length of variables and categories must match";
		// Perform allocations
		cumCnt = new int[numCat][];
		cumSum = new double[numCat][];
		mapC2S = new int[numCat][];
		for (int cit = 0; cit < numCat; ++cit) {
			int numCatItems = cntCat[cit];
			cumCnt[cit] = new int[numSel + 1];
			cumSum[cit] = new double[numCatItems + 1];
			mapC2S[cit] = new int[numCatItems];
		}
		// Single pass and accumulate
		int[] catRunIdx = new int[numCat]; // Category running index: one index
											// per category
		if (numSel > 0) {
			{ // First iteration
				final int cat = categories[0];
				final double val = values[0];
				cumCnt[cat][1] = 1;
				// mapC2S[cat][catRunIdx[cat]] = 0; // Implicit
				++catRunIdx[cat];
				cumSum[cat][catRunIdx[cat]] = val;
			}
			for (int idxSel = 1; idxSel < numSel; ++idxSel) {
				final int cat = categories[idxSel];
				final double val = values[idxSel];
				for (int ci = 0; ci < numCat; ++ci) {
					cumCnt[ci][idxSel + 1] = cumCnt[ci][idxSel];
				}
				cumCnt[cat][idxSel + 1] += 1;
				mapC2S[cat][catRunIdx[cat]] = idxSel;
				cumSum[cat][catRunIdx[cat] + 1] = cumSum[cat][catRunIdx[cat]] + val;
				++catRunIdx[cat];
			}
		} else { // Selection is empty
			// No operation needed
		}
		this.cntCat = cntCat;
	}

	SelectionStatistics(double[] values, int[] categories) {
		this(values, categories, countCategories(categories));
	}

	SelectionStatistics(SelectionData dataSel) {
		this(dataSel.target, dataSel.control, dataSel.cntCat);
	}

	SelectionStatistics(SelectionStatistics s) {
		this(s.cumSum, s.cumCnt, s.mapC2S, s.cntCat, s.numSel);
	}

	public double getMean() {
		return Arrays.stream(cumSum).mapToDouble(item -> item[item.length - 1]).sum() / numSel;
	}

	public double getMax() {
		return Arrays.stream(mapC2S).mapToDouble(item -> item.length > 1 ? item[1] : Double.NEGATIVE_INFINITY).max()
				.orElse(Double.NEGATIVE_INFINITY);
	}

	/**
	 * Count the occurrence of each category in the stream.
	 * 
	 * @param categories
	 *            Stream of category indices (0-based).
	 * @param numCat
	 *            Maximum category index in categories K.
	 * @return a vector of K category occurrence counts
	 */
	public static int[] countCategories(IntStream categories, int numCat) {
		AtomicInteger[] cntCatAt = new AtomicInteger[numCat];
		for (int ai = 0; ai < numCat; ++ai) {
			cntCatAt[ai] = new AtomicInteger(0);
		}
		categories.forEach(cat -> {
			cntCatAt[cat].incrementAndGet();
		});
		int[] cntCat = IntStream.range(0, numCat).map(i -> cntCatAt[i].get()).toArray();
		return cntCat;
	}

	/**
	 * Count the occurrence of each category in the array.
	 * 
	 * @param categories
	 *            Stream of category indices (0-based).
	 * @return a vector of K category occurrence counts, where K is the maximum
	 *         number of categories.
	 */
	public static int[] countCategories(int[] categories) {
		int numCat = IntStream.of(categories).max().orElse(0) + 1;
		return countCategories(IntStream.of(categories), numCat);
	}

	/**
	 * Compute an element of the c-t path for the current selection
	 * 
	 * @param idx
	 *            Index of the element within the c-t path
	 * @return The coordinates of the point in the CCS space that corresponds to
	 *         the specified element of the c-t path.
	 */
	public int[] getCTPathPoint(int idx) {
		// Get the n-th element of the cumulative count entry from cumulative
		// category counts
		// TODO: Remove stream code.
		// int[] ctPathPoint = Stream.of(cumCnt).mapToInt(cumCnt ->
		// cumCnt[idx]).toArray();
		int[] ctPathPoint = Stream.of(cumCnt).mapToInt(cumCnt -> cumCnt[idx]).toArray();
		for (int cit = 0; cit < numCat; ++cit) {
			ctPathPoint[cit] = cumCnt[cit][idx];
		}
		return ctPathPoint;
	}
}

class PopulationStatistics {
	final double meanValue;
	final double maxValue;
	final int[] cntCat;
	final double[] prbCat;
	final int numItems;

	public PopulationStatistics(int[] cntCat, double meanValue, double maxValue) {
		this.cntCat = cntCat.clone();
		this.numItems = IntStream.of(cntCat).sum();
		this.prbCat = IntStream.of(cntCat).mapToDouble(item -> (double) item / this.numItems).toArray();
		this.meanValue = meanValue;
		this.maxValue = maxValue;
	}

	public PopulationStatistics(double[] values, int[] categories, int[] cntCat) {
		this.cntCat = cntCat;
		this.numItems = IntStream.of(cntCat).sum();
		this.prbCat = IntStream.of(cntCat).mapToDouble(item -> (double) item / this.numItems).toArray();
		this.meanValue = DoubleStream.of(values).sum() / numItems;
		this.maxValue = values[0];
	}

	public PopulationStatistics(double[] values, int[] categories) {
		this(values, categories, SelectionStatistics.countCategories(categories));
	}

	public PopulationStatistics(PopulationStatistics p) {
		this.cntCat = p.cntCat.clone();
		this.prbCat = p.prbCat.clone();
		this.numItems = p.numItems;
		this.meanValue = p.meanValue;
		this.maxValue = p.maxValue;
	}

	public PopulationStatistics(SelectionStatistics s) {
		this(s.cntCat, s.getMean(), s.getMax());
	}

	/**
	 * Create and store some statistics over the population.
	 * 
	 * @param populationData
	 */
	public PopulationStatistics(PopulationData dataPop) {
		this(dataPop.target, dataPop.control, dataPop.cntCat);
	}

	public boolean validateMeasure(ClassCountSpaceMeasures m, String err) {
		if (cntCat.length != m.getSelectionStats().numCat) {
			err = "Class count mismatch";
			return false;
		}
		if (IntStream.of(cntCat).sum() < m.getSelectionStats().numSel) {
			err = "Population has less elements than selection.";
			return false;
		}
		for (int cit = 0; cit < m.getSelectionStats().numCat; ++cit) {
			if (cntCat[cit] >= m.getSelectionStats().cntCat[cit]) {
				err = "Populatin has lower category count than selection, for category " + cit + ".";
			}
		}
		return true;
	}
}

class PopulationData {
	/**
	 * Value data. Contains no missing elements and is sorted in descending
	 * order
	 */
	public final double[] target; // target data

	/**
	 * Control data - the category of the same indexed value. Contains no
	 * missing elements
	 */
	public final int[] control;

	/**
	 * Map indices from the kept, ordered population to the original index
	 * vectors.
	 */
	public final int[] mapIdxP2F;

	/**
	 * Number of categories in the control. The categories are 0-based indices.
	 */
	public final int numCat;

	/** Occurrences per category */
	final int[] cntCat;

	/** Number of items in the population. */
	public final int numPop;

	/** Number of items in the original vectors, including missing values. */
	public final int numFull;

	public PopulationData(IntToDoubleFunction fTarget, IntUnaryOperator fControl, IntStream sOrderPop, int numFull,
			int numCat) {
		this.numFull = numFull;
		this.numCat = numCat;
		this.cntCat = new int[numCat];

		mapIdxP2F = sOrderPop.toArray();
		numPop = mapIdxP2F.length;

		target = new double[numPop];
		control = new int[numPop];
		for (int idxPop = 0; idxPop < numPop; ++idxPop) {
			int idxFull = mapIdxP2F[idxPop];
			double t = fTarget.applyAsDouble(idxFull);
			int c = fControl.applyAsInt(idxFull);
			target[idxPop] = t;
			control[idxPop] = c;
			++cntCat[c];
		}
	}

	public PopulationData(DoubleStream sTarget, IntStream sControl, IntStream sValid) {
		int[] mapIdxP2F;
		{ // Extend isValid with an atomic increment, and filter out the invalid
			// indices
			AtomicInteger idxAt = new AtomicInteger(0);
			mapIdxP2F = sValid.map(x -> {
				// At each check, increase the counter.
				int index = idxAt.getAndIncrement();
				// For valid elements return the index, for invalid return -1
				return x == 0 ? -1 : index;
			}).filter(x -> x >= 0).toArray();
			numFull = idxAt.get();
		}
		numPop = mapIdxP2F.length;
		// Filter valid (unsorted) target/control values
		double[] target = new double[numPop];
		int[] control = new int[numPop];
		{
			OfDouble tit = sTarget.iterator(); // Stream iterators
			OfInt cit = sControl.iterator();
			List<Integer> cntCat = new ArrayList<Integer>();
			{ // Traverse the streams, keeping track of the invalid elements.
				int numCat = 0;
				int idxPop = 0; // Index within the population
				for (int idxFull = 0; idxFull < numFull; ++idxFull) {
					boolean v = mapIdxP2F[idxPop] == idxFull;
					int c = cit.nextInt();
					double t = tit.nextDouble();
					if (v) { // Element is valid element
						target[idxPop] = t;
						control[idxPop] = c;
						while (numCat <= c) {
							cntCat.add(0);
							++numCat;
						}
						cntCat.set(c, cntCat.get(c) + 1);
						++idxPop;
						if (idxPop == mapIdxP2F.length) { // No more valid items
															// expected
							break;
						}
					}
				}
				this.numCat = numCat;
				this.cntCat = cntCat.stream().mapToInt(Integer::intValue).toArray();
			}
		}
		// Sort values tracking the sorting index
		int[] idxSrt;
		{
			class Pair {
				final int k;
				final double v;

				Pair(int k, double v) {
					this.k = k;
					this.v = v;
				}
			}

			Pair[] pairs = (Pair[]) IntStream.range(0, numPop).mapToObj(i -> new Pair(i, target[i]))
					.sorted(Comparator.comparing(p -> -p.v)).toArray(Pair[]::new);
			idxSrt = Stream.of(pairs).mapToInt(p -> p.k).toArray();
			this.target = Stream.of(pairs).mapToDouble(p -> p.v).toArray();
		}
		// Sort and reshape
		this.mapIdxP2F = new int[numPop];
		this.control = new int[numPop];
		for (int idxPop = 0; idxPop < numPop; ++idxPop) {
			int idx = idxSrt[idxPop];
			int c = control[idx];
			this.control[idxPop] = c;
			this.mapIdxP2F[idxPop] = mapIdxP2F[idx];
		}
	}
}

/**
 * Optimistic estimator adaptor hiding the realKD-internals.
 * @author Janis Kalofolias
 * 
 */
class OptimisticEstimatorAdaptor 
	implements ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> {
	
	/** Population data instance for the entire population */ 
	private final PopulationData dataPop;
	/** Optimistic estimator that maps the SelectionData to a double. */
	private final ToDoubleFunction<SelectionData> fnOE;
	
	/**
	 * Create an optimistic estimator adaptor.
	 * @param dataPop a data population class corresponding to the population.
	 * @param fnOE  implementation that maps a 
	 */
	public OptimisticEstimatorAdaptor(PopulationData dataPop, ToDoubleFunction<SelectionData> fnOE) {
		this.dataPop = dataPop;
		this.fnOE = fnOE;
	}
	
	@Override
	public double applyAsDouble(BranchAndBoundSearchNode<ExceptionalModelPattern> node) {
		final IndexSet setQ = node.content.descriptor().supportSet();
		final int numSub = setQ.size();
		final SelectionData dataSel = new SelectionData(dataPop, setQ::contains, numSub);
		final double fVal = fnOE.applyAsDouble(dataSel);
		return fVal;
	}
}
