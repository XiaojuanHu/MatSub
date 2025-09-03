/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.patterns.models.regression;

import java.util.ArrayList;
import java.util.List;

import com.google.common.math.Quantiles;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;

public enum TheilSenLinearRegressionModelFactory implements ModelFactory<LinearRegressionModel> {

	INSTANCE;

	private static final String STRING_NAME = "Theil Sen linear regression model";

	private TheilSenLinearRegressionModelFactory() {
		;
	}

	@Override
	public Class<? extends LinearRegressionModel> modelClass() {
		return LinearRegressionModel.class;
	}

	@Override
	public LinearRegressionModel getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return createModel(dataTable, attributes, dataTable.population().objectIds());
	}

	@Override
	public LinearRegressionModel getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		return createModel(dataTable, attributes, rows);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		if (attributes.size() != 2) {
			return false;
		}
		for (Attribute<?> attribute : attributes) {
			if (!(attribute instanceof MetricAttribute)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return STRING_NAME;
	}

	private LinearRegressionModel createModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		List<Double> covariateValues = new ArrayList<>();
		List<Double> regressandValues = new ArrayList<>();
		for (int row : rows) {
			if (attributes.get(0).valueMissing(row) || attributes.get(1).valueMissing(row)) {
				continue;
			}
			covariateValues.add(((MetricAttribute) attributes.get(0)).value(row));
			regressandValues.add(((MetricAttribute) attributes.get(1)).value(row));
		}

		double slope = estimateSlope(covariateValues, regressandValues);
		double intercept = estimateIntercept(slope, covariateValues, regressandValues);
		return new LinearRegressionModel(slope, intercept);
	}

	private Double estimateSlope(List<Double> covariateValues, List<Double> regressandValues) {
		List<Double> slopes = new ArrayList<>();
		for (int i = 0; i < covariateValues.size() - 1; i++) {
			for (int j = i + 1; j < covariateValues.size(); j++) {
				double covDiff = covariateValues.get(j) - covariateValues.get(i);
				if (covDiff != 0d) {
					slopes.add((regressandValues.get(j) - regressandValues.get(i)) / covDiff);
				}
			}
		}
		if (slopes.size() > 0) {
			return Quantiles.median().compute(slopes);
		}
		return 0.0;
	}

	private Double estimateIntercept(double slope, List<Double> covariateValues, List<Double> regressandValues) {
		List<Double> intercepts = new ArrayList<>();
		for (int i = 0; i < covariateValues.size(); i++) {
			intercepts.add(regressandValues.get(i) - slope * covariateValues.get(i));
		}
		if (intercepts.size() > 0) {
			return Quantiles.median().compute(intercepts);
		}
		return 0.0;
	}

	@Override
	public String symbol() {
		return "Theil-Sen";
	}

}
