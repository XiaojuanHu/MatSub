/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.correlated;

import static de.unibonn.realkd.common.measures.Measures.measurement;
import static de.unibonn.realkd.patterns.correlated.NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION;
import static de.unibonn.realkd.patterns.correlated.ReliableNormalizedTotalCorrelationCorrectionTerm.RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM;
import static de.unibonn.realkd.patterns.correlated.SumOfEntropies.SUM_OF_ENTROPIES;
import static de.unibonn.realkd.patterns.correlated.SumOfMutualInformations.SUM_OF_MUTUAL_INFORMATIONS;
import static de.unibonn.realkd.patterns.correlated.JointEntropy.JOINT_ENTROPY;
import static de.unibonn.realkd.patterns.correlated.TotalCorrelationNormalizer.TOTAL_CORRELATION_NORMALIZER;
import static java.lang.Double.NaN;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;
import de.unibonn.realkd.util.InformationTheory;

/**
 * @author Panagiotis Mandros
 *
 */
public enum ReliableNormalizedTotalCorrelation
		implements CorrelationMeasure, MeasurementProcedure<ReliableNormalizedTotalCorrelation, Object> {
	RELIABLE_NORMALIZED_TOTAL_CORRELATION;

	@Override
	public Identifier identifier() {
		return Identifier.id("reliable_normalized_total_correlation");
	}

	@Override
	public String caption() {
		return "reliable normalized total correlation";
	}

	@Override
	public String description() {
		return "The normalized total correlation of a set of random variables minus its upper bounded expected value assuming independence";
	}

	@Override
	public boolean isApplicable(Object object) {
		return object instanceof AttributeSetRelation;
	}

	@Override
	public Measurement perform(Object object) {
		if (!isApplicable(object)) {
			return Measures.measurement(this, NaN);
		}

		AttributeSetRelation descriptor = (AttributeSetRelation) object;
		int numAttributes = descriptor.attributeSet().size();

		List<Double> uniEntropies = IntStream.range(0, numAttributes)
				.mapToObj(i -> descriptor.nWayContingencyTable().marginal(i).entropy()).collect(Collectors.toList());

		List<Integer> domainSizes = IntStream.range(0, numAttributes)
				.mapToObj(i -> descriptor.nWayContingencyTable().marginal(i).nonZeroCells().size())
				.collect(Collectors.toList());

		int[] sortedIndices = IntStream.range(0, numAttributes).boxed()
				.sorted((i, j) -> domainSizes.get(j).compareTo(domainSizes.get(i))).mapToInt(ele -> ele).toArray();

		List<Integer> sortedDomainSizes = returnSorted(domainSizes, sortedIndices);

		double sumOfEntropies = uniEntropies.stream().reduce(0.0, Double::sum);
		double maxEntropy = uniEntropies.stream().max(Comparator.comparing(Double::valueOf)).get();

		switch (numAttributes) {
		case 0:
			return Measures.measurement(getMeasure(), NaN,
					ImmutableList.of(measurement(NORMALIZED_TOTAL_CORRELATION, NaN), measurement(SUM_OF_ENTROPIES, NaN),
							measurement(SUM_OF_MUTUAL_INFORMATIONS, NaN),
							measurement(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM, NaN),
							measurement(JOINT_ENTROPY, NaN), measurement(TOTAL_CORRELATION_NORMALIZER, NaN)));
		case 1:
			return Measures.measurement(getMeasure(), 0, ImmutableList.of(measurement(NORMALIZED_TOTAL_CORRELATION, 0),
					measurement(SUM_OF_ENTROPIES, sumOfEntropies), measurement(SUM_OF_MUTUAL_INFORMATIONS, 0),
					measurement(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM, 0),
					measurement(JOINT_ENTROPY, sumOfEntropies), measurement(TOTAL_CORRELATION_NORMALIZER, NaN)));
		}

		int numSamples = descriptor.nWayContingencyTable().marginal(0).totalCount();
		double sumMutualInfos = 0;
		double denominator = sumOfEntropies - maxEntropy;
		double reliableNumerator = 0;
		double prod = sortedDomainSizes.get(0);

		ContingencyTable prevJointTable = descriptor.nWayContingencyTable().marginal(0);

		for (int i = 1; i < descriptor.attributeSet().size(); i++) {

			ContingencyTable jointContTable = descriptor.nWayContingencyTable()
					.marginal(IntStream.rangeClosed(0, i).toArray());

			ContingencyTable marginalTargetContTable = descriptor.nWayContingencyTable().marginal(i);
			sumMutualInfos = sumMutualInfos
					+ ContingencyTables.mutualInformation(jointContTable, prevJointTable, marginalTargetContTable);

			prod = prod * sortedDomainSizes.get(i);
			reliableNumerator = reliableNumerator
					+ Math.log((double) (numSamples + prod) / (numSamples - 1)) / Math.log(2);
			prevJointTable = jointContTable;
		}

		return Measures.measurement(getMeasure(), ((sumMutualInfos - reliableNumerator) / denominator),
				ImmutableList.of(measurement(NORMALIZED_TOTAL_CORRELATION, sumMutualInfos / denominator),
						measurement(SUM_OF_ENTROPIES, sumOfEntropies),
						measurement(SUM_OF_MUTUAL_INFORMATIONS, sumMutualInfos),
						measurement(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM,
								reliableNumerator / denominator),
						measurement(TOTAL_CORRELATION_NORMALIZER, denominator)));
	}

	public Measurement perform(Object object, double sumMutualInfos, double sumOfEntropies, List<Integer> domSizes,
			double newEntropy, double maxEntropy, double prevJointEntropy) {
		if (!isApplicable(object)) {
			return Measures.measurement(this, NaN);
		}

		AttributeSetRelation descriptor = (AttributeSetRelation) object;
		int numAttributes = descriptor.attributeSet().size();

		if (numAttributes < 2) {
			perform(object);
		}

		ContingencyTable jointTable = descriptor.nWayContingencyTable();

		double targetEntropy = newEntropy;
		double jointEntropy = jointTable.entropy();
		double conditionalEntropy = prevJointEntropy;
		int numSamples = jointTable.totalCount();

		double newNumeratorMutualInfoTerm = InformationTheory.mutualInformation(targetEntropy, conditionalEntropy,
				jointEntropy);

		double reliableNumerator = 0;
		double prod = domSizes.get(0);

		for (int i = 1; i < descriptor.attributeSet().size(); i++) {
			prod = prod * domSizes.get(i);
			reliableNumerator = reliableNumerator
					+ Math.log((double) (numSamples + prod) / (numSamples - 1)) / Math.log(2);
		}

		double newNumerator = (sumMutualInfos + newNumeratorMutualInfoTerm) - reliableNumerator;
		double newDenominator = sumOfEntropies+newEntropy - maxEntropy;
		double newScore = newNumerator / newDenominator;

		return Measures.measurement(getMeasure(), newScore,
				ImmutableList.of(
						measurement(NORMALIZED_TOTAL_CORRELATION,
								(sumMutualInfos + newNumeratorMutualInfoTerm) / newDenominator),
						measurement(SUM_OF_ENTROPIES, sumOfEntropies+newEntropy),
						measurement(SUM_OF_MUTUAL_INFORMATIONS, (sumMutualInfos + newNumeratorMutualInfoTerm)),
						measurement(RELIABLE_NORMALIZED_TOTAL_CORRELATION_CORRECTION_TERM,
								reliableNumerator / newDenominator),
						measurement(JOINT_ENTROPY, jointEntropy),
						measurement(TOTAL_CORRELATION_NORMALIZER, newDenominator)));
	}

	@Override
	public ReliableNormalizedTotalCorrelation getMeasure() {
		return ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION;
	}

	private static <T> List<T> returnSorted(List<T> list, int[] indices) {
		return IntStream.range(0, list.size()).mapToObj(i -> list.get(indices[i])).collect(Collectors.toList());
	}

}
