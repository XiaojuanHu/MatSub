/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.models.weibull;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.EnumSet.allOf;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.patterns.models.ModelParameter;
import de.unibonn.realkd.patterns.models.ProbabilisticModel;
import de.unibonn.realkd.patterns.models.UnivariateContinuousProbabilisticModel;

/**
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.5.0
 *
 */
public class WeibullDistribution implements UnivariateContinuousProbabilisticModel {

	private static final Logger LOGGER = Logger.getLogger(WeibullDistribution.class.getName());

	private static enum WeibullParameter implements ModelParameter {

		SHAPE("shape", "Strictly positive shape parameter; 1 equals exponential distribution", d -> d.shape),

		SCALE("scale", "Strictly positive scale parameter.", d -> d.scale);

		private final String caption;

		private final String description;

		private final Function<WeibullDistribution, Double> valueMap;

		private WeibullParameter(String caption, String description, Function<WeibullDistribution, Double> valueMap) {
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

		public Double value(WeibullDistribution d) {
			return valueMap.apply(d);
		}

	}

	private final double shape;

	private final double scale;

	private final Function<Double, Double> densityFunction;

	private final Function<Double, Double> cumulativeDistributionFunction;

	private static class WeibullDensityFunction implements Function<Double, Double> {

		private final double k;

		private final double lambda;

		public WeibullDensityFunction(double shape, double scale) {
			this.k = shape;
			this.lambda = scale;
		}

		@Override
		public Double apply(Double x) {
			if (x <= 0.0) {
				return 0.0;
			} else {
				return (k / lambda) * pow(x / lambda, k - 1.0) * exp(-1.0 * pow(x / lambda, k));
			}
		}

	}

	private static class WeibullCumulativeDistributionFunction implements Function<Double, Double> {

		private final double k;

		private final double lambda;

		public WeibullCumulativeDistributionFunction(double shape, double scale) {
			this.k = shape;
			this.lambda = scale;
		}

		@Override
		public Double apply(Double x) {
			if (x <= 0.0) {
				return 0.0;
			} else {
				return 1 - exp(-1.0 * pow(x / lambda, k));
			}
		}

	}

	public WeibullDistribution(DataTable dataTable, double shape, double scale) {
		checkArgument(shape > 0, "shape paramater was not positive (" + shape + ")");
		checkArgument(scale > 0, "scale paramater was not positive (" + scale + ")");
		this.shape = shape;
		this.scale = scale;
		this.densityFunction = new WeibullDensityFunction(shape, scale);
		this.cumulativeDistributionFunction = new WeibullCumulativeDistributionFunction(shape, scale);
	}

	public double shape() {
		return shape;
	}

	public double scale() {
		return scale;
	}

	@Override
	public double hellingerDistance(ProbabilisticModel q) {
		if (q instanceof WeibullDistribution) {
			return hellingerDistance((WeibullDistribution) q);
		}
		LOGGER.warning("Can only compute distance to another Weibull distributions with identical shape; result NaN.");
		return Double.NaN;
	}

	public double hellingerDistance(WeibullDistribution q) {
		WeibullDistribution p = this;
		if (p.shape() != q.shape()) {
			LOGGER.warning("Can only compute distance for Weibull distributions with identical shape; result NaN.");
			return Double.NaN;
		}
		double k = p.shape();
		double hellingerDistance = sqrt(
				1.0 - 2 * pow(p.scale() * q.scale(), k / 2.0) / (pow(p.scale(), k) + pow(q.scale(), k)));

		return hellingerDistance;
	}

	public Function<Double, Double> densityFunction() {
		return densityFunction;
	}

	@Override
	public Function<Double, Double> cumulativeDistributionFunction() {
		return cumulativeDistributionFunction;
	}

	@Override
	public String toString() {
		return "Weibull(" + scale + "," + shape + ")";
	}

	@Override
	public Collection<WeibullParameter> parameters() {
		return allOf(WeibullParameter.class);
	}

	@Override
	public Optional<Double> value(ModelParameter parameter) {
		if (!(parameter instanceof WeibullParameter)) {
			return Optional.empty();
		}
		return Optional.of(((WeibullParameter) parameter).value(this));
	}

}
