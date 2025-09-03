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
import static de.unibonn.realkd.patterns.correlated.SumOfEntropies.SUM_OF_ENTROPIES;
import static de.unibonn.realkd.patterns.correlated.TotalCorrelationNormalizer.TOTAL_CORRELATION_NORMALIZER;
import static de.unibonn.realkd.patterns.correlated.SumOfMutualInformations.SUM_OF_MUTUAL_INFORMATIONS;

import static java.lang.Double.NaN;

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
public enum NormalizedTotalCorrelation
		implements CorrelationMeasure, MeasurementProcedure<NormalizedTotalCorrelation, Object> {

	NORMALIZED_TOTAL_CORRELATION;

	@Override
	public Identifier identifier() {
		return Identifier.id("normalized_total_correlation");
	}

	@Override
	public String caption() {
		return "normalized total correlation";
	}

	@Override
	public String description() {
		return "The normalized difference of a sum of univariate entropies and joint entropy given a set of random variables";
	}

	@Override
	public NormalizedTotalCorrelation getMeasure() {
		return NormalizedTotalCorrelation.NORMALIZED_TOTAL_CORRELATION;
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

		double entropyOfFirstAttribute;

		switch (numAttributes) {
		case 0:
			return Measures.measurement(getMeasure(), NaN, ImmutableList.of(measurement(SUM_OF_ENTROPIES, NaN),
					measurement(TOTAL_CORRELATION_NORMALIZER, NaN), measurement(SUM_OF_MUTUAL_INFORMATIONS, NaN)));
		case 1:
			entropyOfFirstAttribute = descriptor.nWayContingencyTable().entropy();
			return Measures.measurement(getMeasure(), 0,
					ImmutableList.of(measurement(SUM_OF_ENTROPIES, entropyOfFirstAttribute),
							measurement(TOTAL_CORRELATION_NORMALIZER, 0), measurement(SUM_OF_MUTUAL_INFORMATIONS, 0)));
		}

		entropyOfFirstAttribute = descriptor.nWayContingencyTable().marginal(0).entropy();
		double numerator = 0;
		double denominator = 0;
		for (int i = 1; i < descriptor.attributeSet().size(); i++) {

			ContingencyTable jointContTable = descriptor.nWayContingencyTable()
					.marginal(IntStream.rangeClosed(0, i).toArray());
			ContingencyTable marginalConditionalContTable = descriptor.nWayContingencyTable()
					.marginal(IntStream.range(0, i).toArray());
			ContingencyTable marginalTargetContTable = descriptor.nWayContingencyTable().marginal(i);
			numerator = numerator + ContingencyTables.mutualInformation(jointContTable, marginalConditionalContTable,
					marginalTargetContTable);
			denominator = denominator + marginalTargetContTable.entropy();
		}

		return Measures.measurement(getMeasure(), numerator / denominator,
				ImmutableList.of(measurement(SUM_OF_ENTROPIES, denominator + entropyOfFirstAttribute),
						measurement(TOTAL_CORRELATION_NORMALIZER, denominator),
						measurement(SUM_OF_MUTUAL_INFORMATIONS, numerator)));
	}

	// incremental calculation, given information from the child pattern
	public Measurement perform(Object object, double oldScore, double oldDenominator) {
		if (!isApplicable(object)) {
			return Measures.measurement(this, NaN);
		}

		AttributeSetRelation descriptor = (AttributeSetRelation) object;
		int numAttributes = descriptor.attributeSet().size();

		if (numAttributes < 3) {
			perform(object);
		}

		double firstAttributeEntropy = descriptor.nWayContingencyTable().marginal(0).entropy();

		ContingencyTable conditionalMarginal = descriptor.nWayContingencyTable()
				.marginal(IntStream.range(0, numAttributes - 1).toArray());
		ContingencyTable targetMarginal = descriptor.nWayContingencyTable().marginal(numAttributes - 1);

		double targetEntropy = targetMarginal.entropy();
		double jointEntropy = descriptor.nWayContingencyTable().entropy();
		double conditionalEntropy = conditionalMarginal.entropy();

		double newNumeratorTerm = InformationTheory.mutualInformation(targetEntropy, conditionalEntropy, jointEntropy);

		double newDenominator = oldDenominator + targetEntropy;
		double newScore = (oldScore * oldDenominator + newNumeratorTerm) / newDenominator;

		return Measures.measurement(getMeasure(), newScore,
				ImmutableList.of(measurement(SUM_OF_ENTROPIES, newDenominator + firstAttributeEntropy),
						measurement(TOTAL_CORRELATION_NORMALIZER, newDenominator),
						measurement(SUM_OF_MUTUAL_INFORMATIONS, (oldScore * oldDenominator + newNumeratorTerm))));
	}

}
