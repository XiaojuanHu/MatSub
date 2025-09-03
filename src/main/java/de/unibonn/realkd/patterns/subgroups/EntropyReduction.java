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
package de.unibonn.realkd.patterns.subgroups;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.measures.Measures.measurement;
import static java.lang.Math.max;

import java.util.stream.DoubleStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum EntropyReduction
		implements ErrorReductionMeasure, MeasurementProcedure<EntropyReduction, Object> {

	ENTROPY_REDUCTION;

	@Override
	public String caption() {
		return "entropy reduction";
	}

	@Override
	public String description() {
		return "The relative difference between entropy of the reference model and the local model or zero if this difference is negative.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		return (subgroup.localModel() instanceof ContingencyTable)
				&& (subgroup.referenceModel() instanceof ContingencyTable);
	}

	@Override
	@JsonIgnore
	public EntropyReduction getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return measurement(this, Double.NaN);
		}
		Subgroup<?> pmmDescriptor = (Subgroup<?>) descriptor;
		ContingencyTable localModel = (ContingencyTable) pmmDescriptor.localModel();
		ContingencyTable refModel = (ContingencyTable) pmmDescriptor.referenceModel();
		double refEntropy = refModel.entropy();
		double localEntropy = localModel.entropy();
		return Measures.measurement(EntropyReduction.ENTROPY_REDUCTION, max(refEntropy - localEntropy, 0) / refEntropy,
				ImmutableList.of(measurement(QualityMeasureId.REF_ENTROPY, refEntropy),
						measurement(QualityMeasureId.LOCAL_ENTROPY, localEntropy),
						measurement(QualityMeasureId.LOCAL_MODE_PROBABILITY, maxProb(localModel))));
	}

	private double maxProb(ContingencyTable table) {
		DoubleStream probabilities = table.nonZeroCells().stream().mapToDouble(key -> table.probability(key));
		return probabilities.max().orElseGet(() -> Double.NaN);
	}

	@Override
	public String toString() {
		return "Entropy gain";
	}

	@Override
	public Identifier identifier() {
		return id("entropy_reduction");
	}

}
