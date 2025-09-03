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

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.patterns.models.ProbabilisticModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum TotalVariationDistance implements ModelDeviationMeasure, Identifiable {

	TOTAL_VARIATION_DISTANCE;

	@Override
	public String caption() {
		return "total variation distance";
	}

	@Override
	public String description() {
		return "Total variation distance between global and local distribution of target attributes.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		return (((Subgroup<?>) descriptor).referenceModel() instanceof ProbabilisticModel)
				&& (((Subgroup<?>) descriptor).localModel() instanceof ProbabilisticModel);
	}

	@Override
	public ModelDeviationMeasure getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return measurement(this, Double.NaN);
		}
		double distance = ((ProbabilisticModel) ((Subgroup<?>) descriptor).referenceModel())
				.totalVariationDistance(((ProbabilisticModel) ((Subgroup<?>) descriptor).localModel()));
		return measurement(this, distance);
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public Identifier identifier() {
		return Identifier.id("total_var_dist");
	}

}
