/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.functional;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.measures.Measures.measurement;
import static de.unibonn.realkd.patterns.functional.CoDomainEntropy.CODOMAIN_ENTROPY;
import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.functional.FractionOfInformation.FRACTION_OF_INFORMATION;
import static de.unibonn.realkd.patterns.models.table.ContingencyTables.mutualInformation;
import static de.unibonn.realkd.patterns.models.table.ContingencyTables.parallelExpectedMutualInformationUnderPermutationModel;
import static de.unibonn.realkd.patterns.models.table.MutualInformation.MUTUAL_INFORMATION;
import static java.util.stream.IntStream.range;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;

/**
 * @author Mario Boley
 * 
 * @author Panagiotis Mandros
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum ReliableFractionOfInformation
		implements FunctionalDependencyMeasure, MeasurementProcedure<ReliableFractionOfInformation, Object> {

	RELIABLE_FRACTION_OF_INFORMATION;

	@Override
	public String caption() {
		return "reliable fraction of information";
	}

	@Override
	public String symbol() {
		return "F'(X;Y)";
	}

	@Override
	public String description() {
		return "The fraction of information of the co-domain contained in the domain minus its expected value assuming independence.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		return descriptor instanceof BinaryAttributeSetRelation;
	}

	@Override
	public ReliableFractionOfInformation getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object object) {
		if (!(object instanceof BinaryAttributeSetRelation)) {
			return Measures.measurement(this, Double.NaN);
		}
		BinaryAttributeSetRelation descriptor = (BinaryAttributeSetRelation) object;
		int[] domainIndices = range(0, descriptor.domain().size()).toArray();
		ContingencyTable marginalY = descriptor.contingencyTable().marginal(domainIndices.length);
		double entropyY = marginalY.entropy();
		return perform(descriptor, entropyY, marginalY);
	}

	public Measurement perform(BinaryAttributeSetRelation descriptor, double entropyY, ContingencyTable marginalY) {
		if (descriptor.domain().isEmpty()) {
			return Measures.measurement(this, 0,
					ImmutableList.of(Measures.measurement(CODOMAIN_ENTROPY, entropyY),
							Measures.measurement(EXPECTED_MUTUAL_INFORMATION, 0),
							Measures.measurement(FRACTION_OF_INFORMATION, 0)));
		} else {
			int[] domainIndices = range(0, descriptor.domain().size()).toArray();
			ContingencyTable marginalX = descriptor.contingencyTable().marginal(domainIndices);
			double mutualInformation = mutualInformation(descriptor.contingencyTable(), marginalX, marginalY);
			double expectedMI = parallelExpectedMutualInformationUnderPermutationModel(marginalX, marginalY);
			return perform(mutualInformation, entropyY, expectedMI);
		}
	}

	/**
	 * @param mutualInformation
	 * @param entropyY
	 * @param expectedMI
	 * @return
	 */
	public Measurement perform(double mutualInformation, double entropyY, double expectedMI) {
		double result = (mutualInformation) / (entropyY) - expectedMI / entropyY;
		// TODO changed from < DOUBLE_PRECISION which was small constant;
		// should call some general arithmetic function for this;
		// @Panos: this holds for general arithmetic shenanigans we think we
		// have to perform (but if we do we should do so consistently)
		if (result < 0.0) {
			result = 0;
		}

		return measurement(this, result,
				ImmutableList.of(measurement(CODOMAIN_ENTROPY, entropyY),
						measurement(MUTUAL_INFORMATION, mutualInformation),
						measurement(EXPECTED_MUTUAL_INFORMATION, expectedMI),
						measurement(FRACTION_OF_INFORMATION, result + expectedMI / entropyY)));
	}

	@Override
	public Identifier identifier() {
		return id("reliable_fraction_of_information");
	}

}
