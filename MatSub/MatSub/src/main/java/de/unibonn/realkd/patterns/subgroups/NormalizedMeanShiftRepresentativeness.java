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

import static java.lang.Math.abs;
import static java.lang.Math.max;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum NormalizedMeanShiftRepresentativeness implements RepresentativenessMeasure<MetricEmpiricalDistribution> {

	NORMALIZED_MEAN_SHIFT_REPRESENTATIVENESS;

	@Override
	public String caption() {
		return "Normalized mean-shift representativeness";
	}

	@Override
	public String description() {
		return "max(1-|m(P)-m(Q)|/std(Q),0)";
	}

	@Override
	public Measurement measurement(ControlledSubgroup<?, MetricEmpiricalDistribution> subgroup) {
		double meanQ = ((MetricEmpiricalDistribution) subgroup.localControlModel()).means().get(0);
		double meanP = ((MetricEmpiricalDistribution) subgroup.referenceControlModel()).means().get(0);
		double stdP = ((MetricEmpiricalDistribution) subgroup.referenceControlModel()).covarianceMatrix()[0][0];
		return Measures.measurement(this, max(1 - abs(meanP - meanQ) / stdP, 0));
	}

	@Override
	public Identifier identifier() {
		return Identifier.identifier("normalized_mean_shift_representativeness");
	}

}
