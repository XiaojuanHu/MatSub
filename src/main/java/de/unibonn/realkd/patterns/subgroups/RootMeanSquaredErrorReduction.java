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
import static java.lang.Double.NaN;
import static java.lang.Math.max;

import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.models.regression.LinearRegressionModel;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public enum RootMeanSquaredErrorReduction
		implements ErrorReductionMeasure, MeasurementProcedure<RootMeanSquaredErrorReduction, Object> {

	RMSE_REDUCTION;

	@Override
	public String caption() {
		return "rmse gain";
	}

	@Override
	public String description() {
		return "The difference between reference rmse and local rmse or zero if this difference is negative.";
	}

	@Override
	public boolean isApplicable(Object descriptor) {
		if (!(descriptor instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		return ((subgroup.localModel() instanceof LinearRegressionModel)
				&& (subgroup.referenceModel() instanceof LinearRegressionModel));
	}

	@Override
	public RootMeanSquaredErrorReduction getMeasure() {
		return RootMeanSquaredErrorReduction.RMSE_REDUCTION;
	}

	@Override
	public Measurement perform(Object descriptor) {
		if (!isApplicable(descriptor)) {
			return measurement(this, NaN);
		}
		Subgroup<?> subgroup = (Subgroup<?>) descriptor;
		MetricAttribute x = (MetricAttribute) subgroup.targetAttributes().get(0);
		MetricAttribute y = (MetricAttribute) subgroup.targetAttributes().get(1);
		double localRmse = rmse(x, y, subgroup.supportSet(), (LinearRegressionModel) subgroup.localModel());
		double refRmse = rmse(x, y, subgroup.getTargetTable().population().objectIds(),
				(LinearRegressionModel) subgroup.referenceModel());
		double gain = max(refRmse - localRmse, 0);
		return measurement(this, gain, ImmutableList.of(measurement(QualityMeasureId.REFERENCE_RMSE, refRmse),
				measurement(QualityMeasureId.LOCAL_RMSE, localRmse)));
	}

	private double rmse(MetricAttribute x, MetricAttribute y, IndexSet extension, LinearRegressionModel model) {
		ToDoubleFunction<Integer> errorTerm = i -> Math.pow(model.predict(x.value(i)) - y.value(i), 2);
		Stream<Integer> nonMissing = StreamSupport.stream(extension.spliterator(), false)
				.filter(i -> !x.valueMissing(i) && !y.valueMissing(i));
		double meanSqaredError = nonMissing.collect(Collectors.averagingDouble(errorTerm));
		double rmse = Math.sqrt(meanSqaredError);
		return rmse;
	}

	public String toString() {
		return getMeasure().caption();
	}

	@Override
	public Identifier identifier() {
		return id("root_mean_squared_error_reduction");
	}

}
