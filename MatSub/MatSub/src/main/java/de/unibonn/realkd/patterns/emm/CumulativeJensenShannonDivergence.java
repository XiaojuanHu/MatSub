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
package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.measures.Measures.measurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.mean.UnivariateMetricEmpiricalDistribution;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * 
 * @author Hoang Vu Nguyen
 *
 * @since 0.1.1
 * 
 * @version 0.1.2
 * 
 */
public enum CumulativeJensenShannonDivergence implements ModelDeviationMeasure, Identifiable {
	
	CJS;

	public static final String STRING_NAME = "cum. Jensen Shannon divergence";

	@Override
	public Identifier identifier() {
		return id("cumulative_jensen_shannon_divergence");
	}
	
	@Override
	public String caption() {
		return STRING_NAME;
	}

	@Override
	public String description() {
		return "Divergence between global and local distribution of target attributes (in terms of cumulative distribution functions).";
	}

	private static final double MAX_ERROR = 0.05;
	private static final double ALPHA = 0.5;
	private static final int CLUMPS = 2;
	private static final int MAXMAX = 5;
	private static boolean NORMALIZE = true;

	static class RankToIndex {
		public int rank;
		public int index;
		public int bid;
	}

	static class IndexToRank {
		public int index;
		public int rank;
		public int subrank;
	}

	static class SortedObject implements Comparable<SortedObject> {
		public int index;
		public double value;

		public SortedObject(int index, double value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(SortedObject b) {
			if (this.value > b.value) {
				return 1;
			}

			if (this.value == b.value) {
				return 0;
			}

			return -1;
		}
	}

	static class MacroBin {
		public double lowerBound;
		public double upperBound;
		public ArrayList<Integer> pointIDs;

		public MacroBin(double lowerBound, double upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			pointIDs = new ArrayList<Integer>();
		}
	}

	static class MicroBin {
		public double lowerBound;
		public double upperBound;
		public ArrayList<Integer> pointIDs;
		public ArrayList<Integer> dims;

		public MicroBin(double lowerBound, double upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			pointIDs = new ArrayList<Integer>();
			dims = new ArrayList<Integer>();
		}
	}

	private CumulativeJensenShannonDivergence() {
		;
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		Model globalModel = subgroup.referenceModel();
		Model localModel = subgroup.localModel();
		return (globalModel instanceof UnivariateMetricEmpiricalDistribution
				&& localModel instanceof UnivariateMetricEmpiricalDistribution);
	}

	@Override
	public ModelDeviationMeasure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return measurement(this, Double.NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		IndexSet extensionA = subgroup.getTargetTable().population().objectIds();
		IndexSet extensionB = subgroup.supportSet();
		List<? extends Attribute<?>> attributes = subgroup.targetAttributes();
		return measurement(getMeasure(), discretizeDataAndComputeDivergence(attributes, extensionA, extensionB));
	}

	// @Override
	// public double distance(Model globalModel, Model localModel)
	// throws IllegalArgumentException, ArithmeticException {
	// checkArgument(globalModel.attributes().size() ==
	// globalModel.attributes().size(),
	// "Number of attributes between global and local model must agree.");
	//
	// return discretizeDataAndComputeDivergence(globalModel, localModel);
	// }

	private static double getValue(MetricAttribute metricAttribute, int row) {
		return metricAttribute.valueMissing(row) ? metricAttribute.mean() : metricAttribute.value(row);
	}

	// @Override
	// public ModelDeviationMeasure getCorrespondingInterestingnessMeasure() {
	// return ModelDeviationMeasure.CJS;
	// }

	// @Override
	// public boolean isApplicable(Model globalModel, Model localModel) {
	// return
	// (EmpiricalCumulativeDistribution.class.isAssignableFrom(globalModel.getClass())
	// &&
	// EmpiricalCumulativeDistribution.class.isAssignableFrom(localModel.getClass())
	// ||
	// UnivariateCumulativeDensity.class.isAssignableFrom(globalModel.getClass())
	// &&
	// UnivariateCumulativeDensity.class.isAssignableFrom(localModel.getClass()));
	// }

	@Override
	public String toString() {
		return STRING_NAME;
	}

	private static double discretizeDataAndComputeDivergence(List<? extends Attribute<?>> attributes,
			IndexSet extensionA, IndexSet extensionB) throws IllegalArgumentException, ArithmeticException {

		// checkArgument(a.length != 0,"a must have at least 1 row");
		// checkArgument(a[0].length != 0,"a must have at least 1 column");
		// checkArgument(b.length != 0,"b must have at least 1 row");
		// checkArgument(b[0].length != 0,"b must have at least 1 column");
		// checkArgument(a[0].length ==
		// b[0].length,"a and b must have the same number of columns");

		//
		// int rowsA = modelA.rows().size();
		// int rowsB = modelB.rows().size();
		// int rows = rowsA + rowsB;
		// int cols = modelA.attributes().size();
		int rowsA = extensionA.size();
		int rowsB = extensionB.size();
		int rows = rowsA + rowsB;
		int cols = attributes.size();
		Map<Integer, Integer[]> pidToIdxAndType = new HashMap<Integer, Integer[]>(rows);
		Map<Integer, Integer[]> IR = new HashMap<Integer, Integer[]>(rows);
		Map<Integer, Integer[]> RI = new HashMap<Integer, Integer[]>(rows);
		double[] CRES1 = new double[cols];
		double[] CRES2 = new double[cols];

		// intialize pointType
		int j = 0;
		for (int r : extensionA) {
			Integer[] tmp = new Integer[2];
			tmp[0] = r;
			tmp[1] = 1;
			pidToIdxAndType.put(j, tmp);
			j++;
		}
		for (int r : extensionB) {
			Integer[] tmp = new Integer[2];
			tmp[0] = r;
			tmp[1] = 2;
			pidToIdxAndType.put(j, tmp);
			j++;
		}

		SortedObject[] so = new SortedObject[rows];
		double sum = 0;
		double[] MAX_VAL = new double[cols];
		for (int i = 0; i < cols; i++) {
			ArrayList<SortedObject> soj = new ArrayList<SortedObject>();
			ArrayList<SortedObject> soa = new ArrayList<SortedObject>();
			ArrayList<SortedObject> sob = new ArrayList<SortedObject>();

			j = 0;
			MetricAttribute metricAttribute = (MetricAttribute) attributes.get(i);
			MAX_VAL[i] = metricAttribute.max();
			for (int r : extensionA) {
				so[j] = new SortedObject(j, getValue(metricAttribute, r));
				j++;
			}
			// metricAttribute = (MetricAttribute) attributes.get(i);
			MAX_VAL[i] = Math.max(MAX_VAL[i], metricAttribute.max());
			for (int r : extensionB) {
				so[j] = new SortedObject(j, getValue(metricAttribute, r));
				j++;
			}

			Arrays.sort(so);

			for (j = 0; j < rows; j++) {
				Integer[] tmp = IR.get(so[j].index);
				if (tmp == null) {
					tmp = new Integer[cols];
				}
				tmp[i] = j;
				IR.put(so[j].index, tmp);

				tmp = RI.get(j);
				if (tmp == null) {
					tmp = new Integer[cols + 1]; // the extra column is for bid
				}
				tmp[i] = so[j].index;
				RI.put(j, tmp);

				int type = pidToIdxAndType.get(so[j].index)[1];
				soj.add(new SortedObject(type, so[j].value));
				if (type == 1)
					soa.add(new SortedObject(so[j].index, so[j].value));
				else
					sob.add(new SortedObject(so[j].index, so[j].value));
			}

			CRES1[i] = MCJS(soa, sob, soj, 1, MAX_VAL[i]);
			CRES2[i] = MCJS(sob, soa, soj, 2, MAX_VAL[i]);

			// for normalization purpose
			double x = P(soa, 0, MAX_VAL[i]);
			double y = P(sob, 0, MAX_VAL[i]);
			sum += x + y;
		}

		double v1 = discretizeData(1, attributes, extensionA, extensionB, pidToIdxAndType, CRES1, CRES2, IR, RI,
				MAX_VAL, rowsA);
		double v2 = discretizeData(2, attributes, extensionA, extensionB, pidToIdxAndType, CRES2, CRES1, IR, RI,
				MAX_VAL, rowsB);
		double ret = v1 + v2;
		if (NORMALIZE) {
			ret = ret / sum;
			if (ret > 1 + 0.000001)
				throw new ArithmeticException("score must be <= 1!");
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private static double discretizeData(int type, List<? extends Attribute<?>> attributes, IndexSet extensionA,
			IndexSet extensionB, Map<Integer, Integer[]> pidToIdxAndType, double[] CRES1, double[] CRES2,
			Map<Integer, Integer[]> IR, Map<Integer, Integer[]> RI, double[] MAX_VAL, int length)
			throws IllegalArgumentException, ArithmeticException {
		// get all dimension
		int rowsA = extensionA.size();
		int rowsB = extensionB.size();
		int rows = rowsA + rowsB;
		int cols = attributes.size();
		// int rowsA = modelA.rows().size();
		// int rowsB = modelB.rows().size();
		// int rows = rowsA + rowsB;
		// int cols = modelA.attributes().size();
		SortedObject[] so = new SortedObject[cols];
		for (int i = 0; i < cols; i++)
			so[i] = new SortedObject(i, CRES1[i] + CRES2[i]);
		Arrays.sort(so);
		int dimBack = so[so.length - 1].index;

		// discretize each of the remaining dimensions
		ArrayList<MicroBin> initialMicroBins;
		ArrayList<MacroBin> candidateMacroBins;
		ArrayList<Integer>[] discretizedData = new ArrayList[rows];
		for (int i = 0; i < rows; i++) {
			discretizedData[i] = new ArrayList<Integer>();
			discretizedData[i].add(0);
		}
		int B = (int) Math.pow(rows, ALPHA);
		B = (int) Math.min(B, MAXMAX);
		int MAX_BINS;
		int dimFront;
		double[] val = new double[1];
		double ret = CRES1[dimBack];
		Map<ArrayList<Integer>, ArrayList<Integer>> cellPointIDs = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
		ArrayList<MacroBin> bins = null;
		for (int i = so.length - 2; i >= 0; i--) {
			updateCells(discretizedData, cellPointIDs);

			initialMicroBins = findEqualFrequencyBinning(type, dimBack, attributes, extensionA, extensionB,
					pidToIdxAndType, CLUMPS * B, IR, RI, length);
			candidateMacroBins = convertMicroToMacroBins(initialMicroBins);

			// discretize tmpDim
			dimFront = so[i].index;
			MAX_BINS = B;
			bins = CKLBinning(type, dimFront, MAX_BINS, attributes, extensionA, extensionB, pidToIdxAndType,
					candidateMacroBins, cellPointIDs, val, IR, RI, MAX_VAL);
			int bid = 0;
			for (MacroBin tmp : bins) {
				for (Integer pid : tmp.pointIDs) {
					discretizedData[pid].add(bid);
				}
				bid++;
			}
			ret += val[0];
			dimBack = dimFront;
		}

		return ret;
	}

	private static void updateCells(ArrayList<Integer>[] discretizedData,
			Map<ArrayList<Integer>, ArrayList<Integer>> map) {
		int rows = discretizedData.length;
		map.clear();
		ArrayList<Integer> pids = null;
		for (int i = 0; i < rows; i++) {
			pids = map.get(discretizedData[i]);
			if (pids == null) {
				pids = new ArrayList<Integer>();
			}
			pids.add(i);
			map.put(discretizedData[i], pids);
		}
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<MacroBin> CKLBinning(int type, int curDim, int MAX_BINS,
			List<? extends Attribute<?>> attributes, IndexSet extensionA, IndexSet extensionB,
			Map<Integer, Integer[]> pidToIdxAndType, ArrayList<MacroBin> candidateMacroBins,
			Map<ArrayList<Integer>, ArrayList<Integer>> cellPointIDs, double[] val, Map<Integer, Integer[]> IR,
			Map<Integer, Integer[]> RI, double[] MAX_VAL) throws IllegalArgumentException, ArithmeticException {
		ArrayList<MacroBin> ret = new ArrayList<MacroBin>();

		int rowsA = extensionA.size();
		int rowsB = extensionB.size();
		int rows = rowsA + rowsB;
		int cols = attributes.size();
		int[] pointToCell = new int[rows];
		int incr = 0;
		for (ArrayList<Integer> cell : cellPointIDs.values()) {
			for (Integer pid : cell)
				pointToCell[pid] = incr;

			incr++;
		}

		// create the table for dynamic programming to store already solved
		// sub-problems
		int numTotalMacroBins = candidateMacroBins.size();
		// System.out.println("numTotalMacroBins = " + numTotalMacroBins);
		if (numTotalMacroBins >= 2) {
			ArrayList<MacroBin>[][] dynamicBinnings = new ArrayList[numTotalMacroBins + 1][MAX_BINS + 1];
			double[][] dynamicBinningCosts = new double[numTotalMacroBins + 1][MAX_BINS + 1];
			int[][] dynamicNumPoints = new int[numTotalMacroBins + 1][MAX_BINS + 1];

			// find optimal binning of size 2
			MacroBin tmpMergeMacroBin = null;
			MacroBin tmpMergeMacroBinRight = null;
			double tmpCost;
			ArrayList<MacroBin> tmpMacroBins = new ArrayList<MacroBin>();
			for (int numFirstMacroBins = 2; numFirstMacroBins <= numTotalMacroBins; numFirstMacroBins++) {
				dynamicBinningCosts[numFirstMacroBins][2] = Double.NEGATIVE_INFINITY;

				// loop through each possible bin position
				// maxIndex contains the minimum bin index of the right macro
				// bin
				for (int maxIndex = 1; maxIndex < numFirstMacroBins; maxIndex++) {
					// clear the temporary binning
					tmpMacroBins.clear();

					// merge all macro bins from position 0 to (maxIndex - 1)
					tmpMergeMacroBin = cloneBin(candidateMacroBins.get(0));
					for (int i = 1; i < maxIndex; i++)
						tmpMergeMacroBin = mergeMacroBins(tmpMergeMacroBin, candidateMacroBins.get(i));

					// merge all macro bins from position maxIndex to
					// (numFirstMacroBins - 1)
					tmpMergeMacroBinRight = cloneBin(candidateMacroBins.get(maxIndex));
					for (int i = maxIndex + 1; i < numFirstMacroBins; i++)
						tmpMergeMacroBinRight = mergeMacroBins(tmpMergeMacroBinRight, candidateMacroBins.get(i));

					// add two new macro bins to the temporary binning
					tmpMacroBins.add(tmpMergeMacroBin);
					tmpMacroBins.add(tmpMergeMacroBinRight);

					// compute the coding cost of the temporary binning
					tmpCost = 0;
					int c = 0;
					int c1 = 0;
					int c2 = 0;
					for (int i = 0; i < rows; i++) {
						RI.get(i)[cols] = -1;
					}

					int incl = 0;
					for (Integer pid : tmpMergeMacroBin.pointIDs) {
						RI.get(IR.get(pid)[curDim])[cols] = pointToCell[pid];
						if (pidToIdxAndType.get(pid)[1] == type) {
							incl++;
						}
					}

					for (Integer pid : tmpMergeMacroBinRight.pointIDs) {
						RI.get(IR.get(pid)[curDim])[cols] = cellPointIDs.size() + pointToCell[pid];
						if (pidToIdxAndType.get(pid)[1] == type) {
							incl++;
						}
					}

					ArrayList<SortedObject>[] a = new ArrayList[2 * cellPointIDs.size()];
					ArrayList<SortedObject>[] b = new ArrayList[2 * cellPointIDs.size()];
					ArrayList<SortedObject>[] ab = new ArrayList[2 * cellPointIDs.size()];
					c1 = c2 = 0;
					int index;
					int bid;
					for (int i = 0; i < rows; i++) {
						MetricAttribute metricAttribute = (MetricAttribute) attributes.get(curDim);
						// if (i < rowsA) {
						// metricAttribute = (MetricAttribute)
						// attributes.get(curDim);
						// } else {
						// metricAttribute = (MetricAttribute)
						// modelB.attributes().get(curDim);
						// }

						if (RI.get(i)[cols] != -1) {
							index = RI.get(i)[curDim];
							bid = RI.get(i)[cols];
							if (ab[bid] == null) {
								ab[bid] = new ArrayList<SortedObject>();
							}
							int pointType = pidToIdxAndType.get(index)[1];
							double pointValue = getValue(metricAttribute, pidToIdxAndType.get(index)[0]);
							ab[bid].add(new SortedObject(pointType, pointValue));
							if (pointType == type) {
								if (a[bid] == null) {
									a[bid] = new ArrayList<SortedObject>();
								}
								a[bid].add(new SortedObject(c1, pointValue));
								c1++;
							} else {
								if (b[bid] == null) {
									b[bid] = new ArrayList<SortedObject>();
								}
								b[bid].add(new SortedObject(c2, pointValue));
								c2++;
							}
						}
					}

					c = 0;
					ArrayList<SortedObject> bt;
					for (int i = 0; i < cellPointIDs.size(); i++) {
						if (a[i] != null) {
							c += a[i].size();
							bt = b[i] != null ? b[i] : new ArrayList<SortedObject>();
							tmpCost += a[i].size() * MCJS(a[i], bt, ab[i], type, MAX_VAL[curDim]) / incl;
						}

						if (a[cellPointIDs.size() + i] != null) {
							c += a[cellPointIDs.size() + i].size();
							bt = b[cellPointIDs.size() + i] != null ? b[cellPointIDs.size() + i]
									: new ArrayList<SortedObject>();
							tmpCost += a[cellPointIDs.size() + i].size() * MCJS(a[cellPointIDs.size() + i], bt,
									ab[cellPointIDs.size() + i], type, MAX_VAL[curDim]) / incl;
						}
					}

					if (c != incl) {
						System.out.println(c + " --- " + incl);
						throw new RuntimeException("not match value");
					}

					// if the cost is larger than the current cost, then update
					// the cost, the binning, and the total number of points of
					// the binning
					if (tmpCost > dynamicBinningCosts[numFirstMacroBins][2]) {
						dynamicBinningCosts[numFirstMacroBins][2] = tmpCost;
						dynamicNumPoints[numFirstMacroBins][2] = incl;
						if (dynamicBinnings[numFirstMacroBins][2] == null)
							dynamicBinnings[numFirstMacroBins][2] = new ArrayList<MacroBin>();
						else
							dynamicBinnings[numFirstMacroBins][2].clear();
						dynamicBinnings[numFirstMacroBins][2].add(tmpMergeMacroBin);
						dynamicBinnings[numFirstMacroBins][2].add(tmpMergeMacroBinRight);
					}
				} // end for
			} // end for

			// build the rest of the table
			// for each (numMacroBins, numFirstMacroBins) pairs, find the
			// binning that yields the smallest cost
			int tmpTotalNumPoints;
			int optimalPosition;
			MacroBin optimalNewBin = null;
			for (int numMacroBins = 3; numMacroBins <= MAX_BINS; numMacroBins++) {
				for (int numFirstMacroBins = numMacroBins; numFirstMacroBins <= numTotalMacroBins; numFirstMacroBins++) {
					dynamicBinningCosts[numFirstMacroBins][numMacroBins] = Double.NEGATIVE_INFINITY;
					optimalPosition = -1;
					optimalNewBin = null;

					// loop through each possible bin position
					for (int maxIndex = numMacroBins - 1; maxIndex < numFirstMacroBins; maxIndex++) {
						// merge all the macro bins from position maxIndex to
						// (numFirstMacroBins - 1)
						tmpMergeMacroBinRight = cloneBin(candidateMacroBins.get(maxIndex));
						for (int i = maxIndex + 1; i < numFirstMacroBins; i++)
							tmpMergeMacroBinRight = mergeMacroBins(tmpMergeMacroBinRight, candidateMacroBins.get(i));

						// compute the total number of data points
						int c = 0;
						int c1 = 0;
						int c2 = 0;
						for (int i = 0; i < rows; i++) {
							RI.get(i)[cols] = -1;
						}

						int incl = 0;
						for (Integer pid : tmpMergeMacroBinRight.pointIDs) {
							RI.get(IR.get(pid)[curDim])[cols] = pointToCell[pid];
							if (pidToIdxAndType.get(pid)[1] == type) {
								incl++;
							}
						}

						tmpTotalNumPoints = dynamicNumPoints[maxIndex][numMacroBins - 1] + incl;

						// retrieve the stored results
						tmpCost = dynamicNumPoints[maxIndex][numMacroBins - 1]
								* dynamicBinningCosts[maxIndex][numMacroBins - 1] / tmpTotalNumPoints;
						if (Double.isNaN(tmpCost)) {
							tmpCost = 0;
						}

						ArrayList<SortedObject>[] a = new ArrayList[2 * cellPointIDs.size()];
						ArrayList<SortedObject>[] b = new ArrayList[2 * cellPointIDs.size()];
						ArrayList<SortedObject>[] ab = new ArrayList[2 * cellPointIDs.size()];
						c1 = c2 = 0;
						int index;
						int bid;
						for (int i = 0; i < rows; i++) {
							MetricAttribute metricAttribute = (MetricAttribute) attributes.get(curDim);
							// if (i < rowsA) {
							// metricAttribute = (MetricAttribute)
							// attributes.get(curDim);
							// } else {
							// metricAttribute = (MetricAttribute)
							// modelB.attributes().get(curDim);
							// }

							if (RI.get(i)[cols] != -1) {
								index = RI.get(i)[curDim];
								bid = RI.get(i)[cols];
								int pointType = pidToIdxAndType.get(index)[1];
								double pointValue = getValue(metricAttribute, pidToIdxAndType.get(index)[0]);
								if (pointType == type) {
									if (a[bid] == null) {
										a[bid] = new ArrayList<SortedObject>();
									}
									a[bid].add(new SortedObject(c1, pointValue));
									c1++;
								} else {
									if (b[bid] == null) {
										b[bid] = new ArrayList<SortedObject>();
									}
									b[bid].add(new SortedObject(c2, pointValue));
									c2++;
								}
								if (ab[bid] == null) {
									ab[bid] = new ArrayList<SortedObject>();
								}
								ab[bid].add(new SortedObject(pointType, pointValue));
							}
						}

						c = 0;
						ArrayList<SortedObject> bt;
						for (int i = 0; i < cellPointIDs.size(); i++)
							if (a[i] != null) {
								c += a[i].size();
								bt = b[i] != null ? b[i] : new ArrayList<SortedObject>();
								tmpCost += a[i].size() * MCJS(a[i], bt, ab[i], type, MAX_VAL[curDim])
										/ tmpTotalNumPoints;
							}

						if (c != incl) {
							System.out.println(c + " --- " + incl);
							throw new RuntimeException("not match value");
						}

						// if the new cost is larger than the current cost, then
						// update the binning
						if (Double.isNaN(tmpCost))
							throw new RuntimeException("not a number!");
						if (tmpCost > dynamicBinningCosts[numFirstMacroBins][numMacroBins]) {
							dynamicBinningCosts[numFirstMacroBins][numMacroBins] = tmpCost;
							dynamicNumPoints[numFirstMacroBins][numMacroBins] = tmpTotalNumPoints;
							optimalPosition = maxIndex;
							optimalNewBin = tmpMergeMacroBinRight;
						} // end if
					} // end for

					// update the table's entry with the binning yielding the
					// largest cost
					dynamicBinnings[numFirstMacroBins][numMacroBins] = new ArrayList<MacroBin>();
					for (int i = 0; i < numMacroBins - 1; i++) {
						tmpMergeMacroBin = cloneBin(dynamicBinnings[optimalPosition][numMacroBins - 1].get(i));
						dynamicBinnings[numFirstMacroBins][numMacroBins].add(tmpMergeMacroBin);
					}
					dynamicBinnings[numFirstMacroBins][numMacroBins].add(optimalNewBin);
				} // end for
			} // end for

			double minCost = Double.NEGATIVE_INFINITY;
			int minNumMacroBins = -1;
			for (int numMacroBins = 2; numMacroBins <= MAX_BINS && numMacroBins <= MAXMAX; numMacroBins++)
				if (dynamicBinningCosts[numTotalMacroBins][numMacroBins] > minCost) {
					minCost = dynamicBinningCosts[numTotalMacroBins][numMacroBins];
					minNumMacroBins = numMacroBins;
				}
			val[0] = minCost;
			for (int i = 0; i < minNumMacroBins; i++)
				ret.add(dynamicBinnings[numTotalMacroBins][minNumMacroBins].get(i));
		} else {
			int c = 0;
			int c1 = 0;
			int c2 = 0;
			for (int i = 0; i < rows; i++)
				RI.get(i)[cols] = -1;

			int incl = 0;
			for (Integer pid : candidateMacroBins.get(0).pointIDs) {
				RI.get(IR.get(pid)[curDim])[cols] = pointToCell[pid];
				if (pidToIdxAndType.get(pid)[1] == type)
					incl++;
			}

			ArrayList<SortedObject>[] a = new ArrayList[cellPointIDs.size()];
			ArrayList<SortedObject>[] b = new ArrayList[cellPointIDs.size()];
			ArrayList<SortedObject>[] ab = new ArrayList[cellPointIDs.size()];
			c1 = c2 = 0;
			double tmpCost = 0;
			int index;
			int bid;
			for (int i = 0; i < rows; i++) {
				MetricAttribute metricAttribute = (MetricAttribute) attributes.get(curDim);
				// if (i < rowsA) {
				// metricAttribute = (MetricAttribute)
				// modelA.attributes().get(curDim);
				// } else {
				// metricAttribute = (MetricAttribute)
				// modelB.attributes().get(curDim);
				// }

				if (RI.get(i)[cols] != -1) {
					index = RI.get(i)[curDim];
					bid = RI.get(i)[cols];
					int pointType = pidToIdxAndType.get(index)[1];
					double pointValue = getValue(metricAttribute, pidToIdxAndType.get(index)[0]);
					if (pointType == type) {
						if (a[bid] == null)
							a[bid] = new ArrayList<SortedObject>();
						a[bid].add(new SortedObject(c1, pointValue));
						c1++;
					} else {
						if (b[bid] == null)
							b[bid] = new ArrayList<SortedObject>();
						b[bid].add(new SortedObject(c2, pointValue));
						c2++;
					}
					if (ab[bid] == null)
						ab[bid] = new ArrayList<SortedObject>();
					ab[bid].add(new SortedObject(pointType, pointValue));
				}
			}

			c = 0;
			ArrayList<SortedObject> bt;
			for (int i = 0; i < cellPointIDs.size(); i++)
				if (a[i] != null) {
					c += a[i].size();
					bt = b[i] != null ? b[i] : new ArrayList<SortedObject>();
					tmpCost += a[i].size() * MCJS(a[i], bt, ab[i], type, MAX_VAL[curDim]) / incl;
				}

			if (c != incl) {
				System.out.println(c + " --- " + incl);
				throw new RuntimeException("not match value");
			}

			val[0] = tmpCost;
			ret.add(candidateMacroBins.get(0));
		}

		return ret;
	}

	// divide a dimension into equal-frequency bins
	private static ArrayList<MicroBin> findEqualFrequencyBinning(int type, int curDim,
			List<? extends Attribute<?>> attributes, IndexSet extensionA, IndexSet extensionB,
			Map<Integer, Integer[]> pidToIdxAndType, int numDesiredBins, Map<Integer, Integer[]> IR,
			Map<Integer, Integer[]> RI, int length) {
		ArrayList<MicroBin> ret = new ArrayList<MicroBin>();
		int rowsA = extensionA.size();
		int rowsB = extensionB.size();
		int rows = rowsA + rowsB;

		// number of distinct values per bin
		int totalNumBins = numDesiredBins;
		if (numDesiredBins > length)
			totalNumBins = length;
		int binCount = (int) Math.floor(length * 1.0 / totalNumBins);

		int c = 0;
		MicroBin tmpBin = new MicroBin(0, 0);
		for (int i = 0; i < rows; i++) {
			int index = RI.get(i)[curDim];
			tmpBin.pointIDs.add(index);
			if (pidToIdxAndType.get(index)[1] == type)
				c++;

			MetricAttribute metricAttribute = (MetricAttribute) attributes.get(curDim);
			// if (i < rowsA) {
			// metricAttribute = (MetricAttribute) attributes.get(curDim);
			// } else {
			// metricAttribute = (MetricAttribute) attributes.get(curDim);
			// }
			if (c == binCount || i == rows - 1) {
				tmpBin.lowerBound = getValue(metricAttribute, pidToIdxAndType.get(tmpBin.pointIDs.get(0))[0]);
				tmpBin.upperBound = getValue(metricAttribute,
						pidToIdxAndType.get(tmpBin.pointIDs.get(tmpBin.pointIDs.size() - 1))[0]);
				ret.add(tmpBin);
				tmpBin = new MicroBin(0, 0);
				c = 0;
			}
		} // end for

		return ret;
	}

	private static double MCJS(ArrayList<SortedObject> a, ArrayList<SortedObject> b, ArrayList<SortedObject> ab,
			int type, double MAX_VAL) throws IllegalArgumentException, ArithmeticException {
		double ret = 0;
		double cp1 = PlogP(a, 0, MAX_VAL);
		ret += cp1;

		double cp2 = PlogPQ(ab, a.size(), b.size(), type, MAX_VAL);
		ret -= cp2;

		double cp3 = 0;
		if (b.size() > 0)
			cp3 = P(b, 0, MAX_VAL) / (2 * Math.log(2));
		ret += cp3;

		double cp4 = P(a, 0, MAX_VAL) / (2 * Math.log(2));
		ret -= cp4;

		if (ret < 0 && Math.abs(ret) > MAX_ERROR) {
			System.out.println(cp1 + " --- " + cp2 + " --- " + cp3 + " --- " + cp4);
			System.out.println(ret);
			throw new RuntimeException("Invalid calculation of MCKL");
		}

		if (Double.isNaN(ret)) {
			System.out.println(cp1 + " --- " + cp2 + " --- " + cp3 + " --- " + cp4);
			System.out.println(ret);
			throw new RuntimeException("Something wrong!");
		}

		return ret;
	}

	private static double P(ArrayList<SortedObject> a, double epsilon, double MAX_VAL) throws IllegalArgumentException {
		if (a.size() == 0) {
			return 0;
		}

		double ret = 0;

		if (epsilon != 0)
			ret += a.get(0).value * epsilon;

		for (int i = 1; i < a.size(); i++) {
			if (a.get(i).value < a.get(i - 1).value) {
				System.out.println(a.get(i - 1) + " --- " + a.get(i));
				throw new IllegalArgumentException("wrong order");
			}

			if (a.get(i).value != a.get(i - 1).value)
				ret += (a.get(i).value - a.get(i - 1).value) * (epsilon + i / (1.0 * a.size()));
		}

		ret += (MAX_VAL - a.get(a.size() - 1).value) * (1 + epsilon);

		return ret;
	}

	private static double PlogP(ArrayList<SortedObject> a, double epsilon, double MAX_VAL)
			throws IllegalArgumentException {
		if (a.size() == 0) {
			return 0;
		}

		double ret = 0;
		double logBase = Math.log(2);

		if (epsilon != 0)
			ret += a.get(0).value * epsilon * Math.log(epsilon) / logBase;

		for (int i = 1; i < a.size(); i++) {
			if (a.get(i).value < a.get(i - 1).value) {
				System.out.println(a.size() + " --- " + a.get(i - 1).value + " --- " + a.get(i).value);
				throw new IllegalArgumentException("wrong order");
			}

			if (a.get(i).value != a.get(i - 1).value)
				ret += (a.get(i).value - a.get(i - 1).value) * (epsilon + i / (1.0 * a.size()))
						* Math.log(epsilon + i / (1.0 * a.size())) / logBase;
		}

		ret += (MAX_VAL - a.get(a.size() - 1).value) * (1 + epsilon) * Math.log(1 + epsilon) / logBase;

		return ret;
	}

	private static double PlogPQ(ArrayList<SortedObject> tmp, int numA, int numB, int type, double MAX_VAL)
			throws IllegalArgumentException, ArithmeticException {
		if (tmp.size() == 0) {
			return 0;
		}

		double ret = 0;

		double logBase = Math.log(2);

		// compute twisted result
		int curIndexA = 0;
		int curIndexB = 0;
		double part1;
		double part2;
		if (tmp.get(0).index == type)
			curIndexA++;
		else
			curIndexB++;

		for (int i = 1; i < tmp.size(); i++) {
			if (tmp.get(i).value < tmp.get(i - 1).value) {
				System.out.println(tmp.get(i - 1).value + " --- " + tmp.get(i).value);
				throw new IllegalArgumentException("wrong order");
			}

			if (numA != 0)
				part1 = curIndexA / (1.0 * numA);
			else {
				if (curIndexA != 0)
					throw new ArithmeticException("Division by zero");
				else
					part1 = 0;
			}

			if (numB != 0)
				part2 = curIndexB / (2.0 * numB) + part1 / 2;
			else {
				if (curIndexB != 0)
					throw new ArithmeticException("Division by zero");
				else
					part2 = part1 / 2;
			}

			ret += (tmp.get(i).value - tmp.get(i - 1).value) * part1 * Math.log(part2) / logBase;

			if (tmp.get(i).index == type)
				curIndexA++;
			else
				curIndexB++;
		}

		if (curIndexA != numA || curIndexB != numB) {
			System.out.println(curIndexA + " --- " + numA + " --- " + curIndexB + " --- " + numB + " --- " + type);
			throw new ArithmeticException("Something wrong!");
		}

		if (numA != 0 && numB == 0)
			ret -= (MAX_VAL - tmp.get(tmp.size() - 1).value);

		return ret;
	}

	private static ArrayList<MacroBin> convertMicroToMacroBins(ArrayList<MicroBin> initialMicroBins) {
		ArrayList<MacroBin> ret = new ArrayList<MacroBin>();

		// for each micro bin, create a macro bin containing it
		MacroBin tmpMacroBin = null;
		for (MicroBin tmpMicroBin : initialMicroBins) {
			tmpMacroBin = new MacroBin(tmpMicroBin.lowerBound, tmpMicroBin.upperBound);
			for (Integer pid : tmpMicroBin.pointIDs)
				tmpMacroBin.pointIDs.add(pid.intValue());
			ret.add(tmpMacroBin);
		}

		return ret;
	}

	// clone a macro bin
	private static MacroBin cloneBin(MacroBin a) {
		// init the new macro bin
		MacroBin ret = new MacroBin(a.lowerBound, a.upperBound);

		// get the total number of data points
		for (Integer pid : a.pointIDs)
			ret.pointIDs.add(new Integer(pid));

		return ret;
	}

	// merge two macro bins
	private static MacroBin mergeMacroBins(MacroBin a, MacroBin b) throws RuntimeException {
		// init the new macro bin
		if (a.lowerBound > b.upperBound)
			throw new RuntimeException("Invalid bin merge");

		MacroBin ret = new MacroBin(a.lowerBound, b.upperBound);

		// get the total number of data points
		ret.pointIDs = a.pointIDs;
		for (Integer pid : b.pointIDs)
			ret.pointIDs.add(pid.intValue());

		return ret;
	}

}
