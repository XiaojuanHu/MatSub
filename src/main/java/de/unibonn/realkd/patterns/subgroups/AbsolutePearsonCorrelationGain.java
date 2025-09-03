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

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum AbsolutePearsonCorrelationGain
		implements ErrorReductionMeasure, MeasurementProcedure<AbsolutePearsonCorrelationGain, Subgroup<?>> {

	ABSOLUTE_PEARSON_GAIN;

	@Override
	public String caption() {
		return "Pearson correlation gain";
	}

	@Override
	public String description() {
		return "The difference between the absolute Pearson correlation coefficients in subgroup and in reference data.";
	}

	@Override
	public boolean isApplicable(Subgroup<?> subgroup) {
		return (subgroup.targetAttributes().size() == 2 && subgroup.localModel() instanceof MetricEmpiricalDistribution)
				&& (subgroup.referenceModel() instanceof MetricEmpiricalDistribution);
	}

	@Override
	@JsonIgnore
	public AbsolutePearsonCorrelationGain getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Subgroup<?> subgroup) {
		double localCov = ((MetricEmpiricalDistribution) subgroup.localModel()).covarianceMatrix()[0][1];
		double refCov = ((MetricEmpiricalDistribution) subgroup.referenceModel()).covarianceMatrix()[0][1];
		double refStd1 = sqrt(((MetricEmpiricalDistribution) subgroup.referenceModel()).covarianceMatrix()[0][0]);
		double refStd2 = sqrt(((MetricEmpiricalDistribution) subgroup.referenceModel()).covarianceMatrix()[1][1]);
		double localStd1 = sqrt(((MetricEmpiricalDistribution) subgroup.localModel()).covarianceMatrix()[0][0]);
		double localStd2 = sqrt(((MetricEmpiricalDistribution) subgroup.localModel()).covarianceMatrix()[1][1]);
		double localCorrelation = localCov / (localStd1 * localStd2);
		double refCorrelation = refCov / (refStd1 * refStd2);
		double result = max(Math.abs(localCorrelation) - Math.abs(refCorrelation), 0);
		return Measures.measurement(getMeasure(), result,
				ImmutableList.of(Measures.measurement(QualityMeasureId.REFERENCE_PEARSON, refCorrelation),
						Measures.measurement(QualityMeasureId.LOCAL_PEARSON, localCorrelation)));
	}

	@Override
	public String toString() {
		return "Pearson correlation gain";
	}

	@Override
	public Identifier identifier() {
		return Identifier.id("absolute_pearson_gain");
	}

}
