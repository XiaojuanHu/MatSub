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
package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.common.measures.Measures.measurement;

import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public interface UnivariateMetricEmpiricalModelDeviationMeasure extends ModelDeviationMeasure {

	public double value(MetricEmpiricalDistribution refModel, MetricEmpiricalDistribution localModel,
			MetricAttribute attribute);

	@Override
	public default boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		Model referenceModel = subgroup.referenceModel();
		Model localModel = subgroup.localModel();
		return (referenceModel instanceof MetricEmpiricalDistribution
				&& localModel instanceof MetricEmpiricalDistribution
				&& ((MetricEmpiricalDistribution) referenceModel).means().size() == 1
				&& ((MetricEmpiricalDistribution) localModel).means().size() == 1);
	}

	public default UnivariateMetricEmpiricalModelDeviationMeasure getMeasure() {
		return this;
	}

	@Override
	public default Measurement perform(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return measurement(this, Double.NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		MetricAttribute attribute = (MetricAttribute) subgroup.targetAttributes().get(0);

		double value = value((MetricEmpiricalDistribution) subgroup.referenceModel(),
				(MetricEmpiricalDistribution) subgroup.localModel(), attribute);

		return measurement(this, value);
	}

}
