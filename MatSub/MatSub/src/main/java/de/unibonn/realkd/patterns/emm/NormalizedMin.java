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

import static de.unibonn.realkd.common.base.Identifier.id;

import de.unibonn.realkd.common.base.Identifier;
//import de.unibonn.realkd.common.parameter.Parameter;
//import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.algorithms.emm.ExceptionalSubgroupSampler;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.mean.MetricEmpiricalDistribution;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public enum NormalizedMin implements UnivariateMetricEmpiricalModelDeviationMeasure {

	NORMALIZED_MIN;
	
	@Override
	public double value(MetricEmpiricalDistribution refModel, MetricEmpiricalDistribution localModel,
			MetricAttribute attribute) {

	    double border = ExceptionalSubgroupSampler.qualityFunctionParameters().get(0).doubleValue();

	    if (localModel.maxs().get(0) > border) {return 0.0;}
	    double distance = refModel.maxs().get(0)-localModel.maxs().get(0);
	    double denom = Math.max(refModel.maxs().get(0)-refModel.mins().get(0),0.0000001);

	    return distance / denom;
		
	}
	
	public Identifier identifier() {
		return id("normalized_min");
	}

	@Override
	public String caption() {
		return "normalized min";
	}

	@Override
	public String description() {
		return "Minimization of target property.";
	}
	
	@Override
	public String toString() {
		return caption();
	}
	
}
