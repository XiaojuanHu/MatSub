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
import de.unibonn.realkd.patterns.models.regression.LinearRegressionModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * @author Elvin Evendijevs
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 *
 */
public enum AngularDistanceOfSlopes implements ModelDeviationMeasure, Identifiable {

	ANGULAR_DISTANCE_OF_SLOPES;

	@Override
	public Identifier identifier() {
		return Identifier.identifier("cosine_distance");
	}

	@Override
	public String caption() {
		return "cosine distance";
	}

	@Override
	public String description() {
		return "Cosine of the angle between global and local regression models.";
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
		return (subgroup.referenceModel() instanceof LinearRegressionModel
				&& subgroup.localModel() instanceof LinearRegressionModel);

	}

	@Override
	public AngularDistanceOfSlopes getMeasure() {
		return this;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return measurement(this, Double.NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;

		double gSlope = ((LinearRegressionModel) subgroup.referenceModel()).slope();
		double lSlope = ((LinearRegressionModel) subgroup.localModel()).slope();
		// the vector is (1, gSlope * 1)
		double globalVectorNorm = Math.sqrt(1 + Math.pow(gSlope, 2));
		// the vector is (1, lSlope * 1)
		double localVectorNorm = Math.sqrt(1 + Math.pow(lSlope, 2));

		double cosine = (1 + gSlope * lSlope) / (globalVectorNorm * localVectorNorm);
		return measurement(this, Math.acos(cosine) / Math.PI);
	}

}
