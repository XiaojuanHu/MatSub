/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.models.regression;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.optimization.StochasticCoordinateDescent;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.util.Lists;

/**
 * <p>
 * Fits a linear regression model between two metric attributes that minimizes
 * the squared error. Note that optimization is done only approximately by
 * stochastic coordinate descent. Hence, model parameters will vary between
 * different runs.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public enum LeastSquareRegressionModelFactory implements ModelFactory<LinearRegressionModel> {

	INSTANCE;
	
	private static final Logger LOGGER = Logger.getLogger(ModelFactory.class.getName());

	private static final DoubleBinaryOperator SQUARE_LOSS_DERIVATIVE = (s, l) -> s - l;

	@Override
	public Class<? extends LinearRegressionModel> modelClass() {
		return LinearRegressionModel.class;
	}

	@Override
	public LinearRegressionModel getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return this.getModel(dataTable, attributes, dataTable.population().objectIds());
	}

	private class TrainingData {

		public final List<List<Double>> examples;

		public final List<Double> labels;

		public TrainingData(List<List<Double>> examples, List<Double> labels) {
			this.examples = examples;
			this.labels = labels;
		}

	}

	private TrainingData trainingData(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		List<MetricAttribute> covariates = attributes.subList(0, attributes.size() - 1).stream()
				.map(a -> (MetricAttribute) a).collect(Collectors.toList());
		MetricAttribute regressand = (MetricAttribute) attributes.get(attributes.size() - 1);

		List<List<Double>> examples = new ArrayList<>();
		List<Double> labels = new ArrayList<>();
		StreamSupport.stream(rows.spliterator(), false).filter(r -> !dataTable.atLeastOneAttributeValueMissingFor(r, attributes)).forEach(r -> {
			List<Double> example = new ArrayList<>();
			example.add(1.0);
			covariates.forEach(a -> example.add(a.value(r) / normalizationFactor(a)));
			examples.add(example);
			labels.add(regressand.value(r) / normalizationFactor(regressand));
		});
		return new TrainingData(examples, labels);
	}

	public double normalizationFactor(MetricAttribute a) {
		return max(abs(a.min()), abs(a.max()));
	}

	@Override
	public LinearRegressionModel getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		TrainingData trainingData = trainingData(dataTable, attributes, rows);
		LOGGER.fine(() -> "created: " + trainingData.examples.size() + " examples and " + trainingData.labels.size()
				+ " labels");
		List<Double> weights = new ArrayList<>();
		// one weight per attribute - 1 for the regressand +1 for the augmented
		// input space
		for (int i = 0; i < attributes.size(); i++) {
			weights.add(0.0);
		}
		if (trainingData.examples.size() > 1) {
			final int inverseAccuracy = 100;
			StochasticCoordinateDescent.stochasticCoordinateDescent(SQUARE_LOSS_DERIVATIVE, weights,
					trainingData.examples, trainingData.labels, Lists.generatorBackedList(i -> 0.0, weights.size()), 1,
					0.0, weights.size() * trainingData.examples.size() * inverseAccuracy);
		}
		final Double slope = weights.get(1);
		final Double intercept = weights.get(0);
		LOGGER.fine(() -> "fitted slope/intercept: "
				+ slope * normalizationFactor((MetricAttribute) attributes.get(1))
						/ normalizationFactor((MetricAttribute) attributes.get(0))
				+ "/" + intercept * normalizationFactor((MetricAttribute) attributes.get(1)));
		return new LinearRegressionModel(slope * normalizationFactor((MetricAttribute) attributes.get(1))
				/ normalizationFactor((MetricAttribute) attributes.get(0)), intercept * normalizationFactor((MetricAttribute) attributes.get(1)));
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		return TheilSenLinearRegressionModelFactory.INSTANCE.isApplicable(attributes);
	}

	@Override
	public String toString() {
		return "Least squares linear regression model";
	}

	@Override
	public String symbol() {
		return "LSF";
	}

}
