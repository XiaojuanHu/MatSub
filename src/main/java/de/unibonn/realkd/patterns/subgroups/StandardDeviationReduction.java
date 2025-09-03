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
import static java.lang.Math.sqrt;

import java.util.stream.DoubleStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;
import de.unibonn.realkd.util.Arrays;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum StandardDeviationReduction implements ErrorReductionMeasure, MeasurementProcedure<StandardDeviationReduction, Object>, Identifiable {

	STD_REDUCTION;
	
	@Override
	public String caption() {
		return "std. reduction";
	}

	@Override
	public String description() {
		return "The relative difference between the standard deviation of the reference model and the local model or zero if difference is negative.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		return (subgroup.localModel() instanceof MetricEmpiricalDistribution)
				&& (subgroup.referenceModel() instanceof MetricEmpiricalDistribution);
	}

	@Override
	@JsonIgnore
	public StandardDeviationReduction getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return measurement(this, Double.NaN);
		}
		Subgroup<?> pmmDescriptor = (Subgroup<?>) descriptor;
		if (!(pmmDescriptor.localModel() instanceof MetricEmpiricalDistribution && pmmDescriptor.referenceModel() instanceof MetricEmpiricalDistribution)) {
			return measurement(this, Double.NaN);
		}
		double[][] localCovMatrix = ((MetricEmpiricalDistribution) pmmDescriptor.localModel()).covarianceMatrix();
		double[][] refCovMatrix = ((MetricEmpiricalDistribution) pmmDescriptor.referenceModel()).covarianceMatrix();
		double localOneNorm = columnAbsSumStream(localCovMatrix).max().getAsDouble();
		double refOneNorm = columnAbsSumStream(refCovMatrix).max().getAsDouble();
		double refStd = sqrt(refOneNorm);
		double localStd = sqrt(localOneNorm);
		double result = max(refStd - localStd, 0) / refStd;
		return measurement(this, result,
				ImmutableList.of(measurement(QualityMeasureId.REFERENCE_STD, refStd),
						measurement(QualityMeasureId.LOCAL_STD, localStd)));
	}

	private DoubleStream columnAbsSumStream(double[][] matrix) {
		return Arrays.columnAggregateStream(matrix, 0.0, (x, y) -> x + Math.abs(y));
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public Identifier identifier() {
		return id("standard_deviation_reduction");
	}

}
