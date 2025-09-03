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

import static java.util.EnumSet.allOf;
import static java.util.Optional.empty;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelParameter;

/**
 * <p>
 * Regression model of one metric variable (regressand) given as a linear
 * function from one other metric variable (covariate). The function is defined
 * by two parameters: slope and intercept.
 * </p>
 * 
 * @author Elvin Evendijevs
 * 
 * @since 0.0.1
 * 
 * @version 0.5.0
 *
 */
@KdonTypeName("univariateLinearRegressionModel")
@KdonDoc("Simple linear regression model y=ax+b where a is referred to as slope and b as intercept.")
public class LinearRegressionModel
		implements Model, HasSerialForm<LinearRegressionModel>, SerialForm<LinearRegressionModel> {

	private static enum UnicovariateLinearRegressionParameter implements ModelParameter {

		INTERCEPT("intercept", "Intercept or offset of the linear function", m -> m.intercept), 
		SLOPE("slope", "Slope of the linear function", m -> m.slope);

		private final String caption;

		private final String description;

		private final Function<LinearRegressionModel, Double> valueMap;

		private UnicovariateLinearRegressionParameter(String caption, String description,
				Function<LinearRegressionModel, Double> valueMap) {
			this.caption = caption;
			this.description = description;
			this.valueMap = valueMap;
		}

		@Override
		public String caption() {
			return caption;
		}

		@Override
		public String description() {
			return description;
		}

		public Double value(LinearRegressionModel m) {
			return valueMap.apply(m);
		}

	}

	private final double slope;

	private final double intercept;

	@JsonCreator
	public LinearRegressionModel(@JsonProperty("slope") double slope, @JsonProperty("intercept") double intercept) {
		this.slope = slope;
		this.intercept = intercept;
	}

	@JsonProperty("slope")
	public double slope() {
		return this.slope;
	}

	@JsonProperty("intercept")
	public double intercept() {
		return this.intercept;
	}

	public Double predict(Double x) {
		return this.slope * x + intercept;
	}

	@Override
	public String toString() {
		return "LinearRegressionModel(" + slope() + "," + intercept() + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LinearRegressionModel)) {
			return false;
		}
		LinearRegressionModel otherModel = (LinearRegressionModel) other;
		return this.slope == otherModel.slope && this.intercept == otherModel.intercept;
	}
	
	@Override
	public Collection<UnicovariateLinearRegressionParameter> parameters() {
		return allOf(UnicovariateLinearRegressionParameter.class);
	}
	
	public Optional<Double> value(ModelParameter p) {
		if (!(p instanceof UnicovariateLinearRegressionParameter)) {
			return empty();
		}
		return Optional.of(((UnicovariateLinearRegressionParameter) p).value(this));
	}

	@Override
	public SerialForm<LinearRegressionModel> serialForm() {
		return this;
	}

	@Override
	public LinearRegressionModel build(Workspace workspace) {
		return this;
	}

}
