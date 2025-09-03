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

import java.util.List;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
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
public enum ManhattenMeanDistance implements ModelDeviationMeasure, Identifiable {

	MANHATTAN_MEAN_DISTANCE;

	@Override
	public Identifier identifier() {
		return Identifier.identifier("manhatten_mean_distance");
	}

	@Override
	public String caption() {
		return "Manhattan mean distance";
	}

	@Override
	public String description() {
		return "Manhatten distance between mean vector of global population and mean vector of subgroup.";
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		return (subgroup.referenceModel() instanceof MetricEmpiricalDistribution
				&& subgroup.localModel() instanceof MetricEmpiricalDistribution);
	}

	@Override
	public ManhattenMeanDistance getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return Measures.measurement(this, Double.NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;

		double distance = 0;
		List<Double> globalMeans = ((MetricEmpiricalDistribution) subgroup.referenceModel()).means();
		List<Double> localMeans = ((MetricEmpiricalDistribution) subgroup.localModel()).means();
		for (int i = 0; i < globalMeans.size(); i++) {
			distance += Math.abs(globalMeans.get(i) - localMeans.get(i));
		}
		return Measures.measurement(this, distance);
	}

}
