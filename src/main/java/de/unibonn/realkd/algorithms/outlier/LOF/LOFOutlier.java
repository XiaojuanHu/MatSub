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
package de.unibonn.realkd.algorithms.outlier.LOF;

import static de.unibonn.realkd.common.IndexSets.copyOf;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.subSetParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.outlier.Outlier;

/**
 * <p>
 * Class compute the Local Outlier factor for all examples in the datasets we
 * use this class for static mode of LOF.
 * </p>
 * 
 * @author amr Koura
 * 
 */
public class LOFOutlier extends AbstractMiningAlgorithm {

	protected Parameter<DataTable> dataTableParameter;
	// protected NumericTargetAttributesParameter targetAttrParam=null;
	protected SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> targetAttrParam = null;
	protected FractionOFLOFParameter KParameter;

	protected SortedSet<Integer> outlierIdxs = new TreeSet<>();

	public LOFOutlier() {
	}

	/**
	 * constuct the algorithm with DataWorkspace
	 * 
	 * @param workspace
	 */
	public LOFOutlier(Workspace workspace) {

		super();

		dataTableParameter = MiningParameters.dataTableParameter(workspace);
		// targetAttrParam = new NumericTargetAttributesParameter(
		// dataTableParameter);
		targetAttrParam = subSetParameter(id("targets"), "Subspace attributes",
				"The subspace of attributes for which outliers should be found", () -> dataTableParameter.current()
						.attributes().stream().filter(x -> x instanceof MetricAttribute).collect(Collectors.toSet()),
				dataTableParameter);

		KParameter = new FractionOFLOFParameter(this);

		registerParameter(dataTableParameter);

		registerParameter(KParameter);

		registerParameter(targetAttrParam);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getName()
	 */
	@Override
	public String caption() {
		return this.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getDescription()
	 */
	@Override
	public String description() {
		return "Local Outlier Factor Algorithm";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.realkd.algorithms.MiningAlgorithm#getCategory()
	 */
	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.OUTLIER_DETECTION;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unibonn.realkd.algorithms.AbstractMiningAlgorithm#concreteCall()
	 */
	protected int numExamples;
	protected double[][] trainingMatrix;
	protected int[][] sortedTrainingMatrix;
	protected double[] lof;

	public double[] getLof() {
		return lof;
	}

	@Override
	protected Collection<Pattern<?>> concreteCall() {
		// the real logic will start here

		DataTable dt = this.getDataTable();

		// Determine indices for target attrs and make sure they are numeric
		List<String> names = dt.attributeNames();
		List<Integer> idxs = new ArrayList<>();

		for (Attribute<?> attribute : this.targetAttrParam.current()) {
			int idx = names.indexOf(attribute.caption());
			// Check.isTrue(dt.isNumeric(idx));
			idxs.add(idx);
		}

		numExamples = dt.population().size();
		int numDims = idxs.size();

		computeLofValues(dt, idxs, KParameter.current());

		System.out.println("printing the values of LOF:");
		for (int i = 0; i < numExamples; i++) {
			if (lof[i] > 1.5)
				System.out.println((i + 1) + "  " + lof[i] + "   ==> Outlier");
			else
				System.out.println((i + 1) + "  " + lof[i]);

		}

		double sumDistances = 1.0;
		Set<Attribute<?>> attrs = new HashSet<>();
		attrs.addAll(targetAttrParam.current());

		double propOutliers = (numExamples - outlierIdxs.size()) * 1.0 / numExamples;

		Outlier oPattern = new Outlier(this.getDataTable(), copyOf(outlierIdxs), attrs, Math.abs(sumDistances),
				1 - propOutliers);

		List<Pattern<?>> results = new ArrayList<>();
		results.add(oPattern);

		return results;

	}

	void computeLofValues(DataTable dt, List<Integer> idxs, int KValue) {
		// begin the training
		TrainingModel model = new TrainingModel(dt, idxs);
		trainingMatrix = model.getMatirx();
		sortedTrainingMatrix = model.getMatrixSortedIndicies();

		// apply the model over the data to compute the the LOF for each example
		lof = new double[numExamples];
		// get the K values

		for (int i = 0; i < numExamples; i++) {
			// compute K-distance of this point
			int Kdistace = getK_Distance(KValue, i, sortedTrainingMatrix[i]);
			lof[i] = 0;
			// we don't want to compare with the same point
			for (int j = 1; j < Kdistace; j++) {
				lof[i] += (LRD(KValue, sortedTrainingMatrix[i][j]) / LRD(KValue, i));
			}
			lof[i] = lof[i] / Kdistace;

			// detect if outlier
			if (lof[i] > 1.5) {
				outlierIdxs.add(i);

			}
		}
	}

	/**
	 * compute the reachability distance from sample in position second to sample in
	 * position first
	 * 
	 * @param K
	 * @param firstIndex
	 * @param secondIndex
	 * @return
	 */
	private double reachabilityDistance(int K, int firstIndex, int secondIndex) {
		double result = 0.0;

		// get the K distance of B
		int K_distnaceOfSecond = getK_Distance(K, secondIndex, sortedTrainingMatrix[secondIndex]);

		double KDistanceB = trainingMatrix[secondIndex][sortedTrainingMatrix[secondIndex][K_distnaceOfSecond]];

		double distanceBetweenFirstAndSecond = trainingMatrix[firstIndex][secondIndex];
		return Math.max(KDistanceB, distanceBetweenFirstAndSecond);

	}

	/**
	 * compute Local reachability density for one sample
	 * 
	 * @param K
	 * @param sampleIndex
	 * @return
	 */
	private double LRD(int K, int sampleIndex) {
		double result = 0.0;

		int kDistance = getK_Distance(K, sampleIndex, sortedTrainingMatrix[sampleIndex]);
		// we don't want to compute the point with it self, so we start from 1
		for (int i = 1; i < kDistance; i++) {
			result += reachabilityDistance(K, sampleIndex, sortedTrainingMatrix[sampleIndex][i]);
		}
		result = kDistance / result;

		return result;
	}

	/**
	 * compute the lenght of set that are within distance equals K from the sample
	 * 
	 * @param K
	 * @param sampleIndex
	 * @param sortedIndecies
	 * @return
	 */
	private int getK_Distance(int K, int sampleIndex, int[] sortedIndecies) {
		int result = K + 1;
		for (int i = K; i < numExamples; i++)
			if (trainingMatrix[sampleIndex][sortedIndecies[i]] == trainingMatrix[sampleIndex][sortedIndecies[i + 1]])
				result++;
			else
				break;

		return result;

	}

	public DataTable getDataTable() {
		return dataTableParameter.current();
	}

}
