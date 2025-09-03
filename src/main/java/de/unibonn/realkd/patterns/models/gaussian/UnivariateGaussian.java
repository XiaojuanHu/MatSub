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

package de.unibonn.realkd.patterns.models.gaussian;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.util.EnumSet.allOf;
import static java.util.Optional.empty;
import static org.apache.commons.math3.special.Erf.erf;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.commons.math3.special.Erf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.models.ModelParameter;
import de.unibonn.realkd.patterns.models.ProbabilisticModel;
import de.unibonn.realkd.patterns.models.UnivariateContinuousProbabilisticModel;

@KdonTypeName("univariateGaussian")
public class UnivariateGaussian implements UnivariateContinuousProbabilisticModel, HasSerialForm<UnivariateGaussian>, SerialForm<UnivariateGaussian> {

	private static final Logger LOGGER = Logger.getLogger(UnivariateGaussian.class.getName());

	private static enum UnivariateGaussianParameter implements ModelParameter {

		MEAN("mean", "Distribution mean value.", d -> d.mean), STD("std. variation", "Distribution standard variation",
				d -> d.std);

		private final String caption;

		private final String description;

		private final Function<UnivariateGaussian, Double> valueMap;

		private UnivariateGaussianParameter(String caption, String description,
				Function<UnivariateGaussian, Double> valueMap) {
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

		public Double value(UnivariateGaussian d) {
			return valueMap.apply(d);
		}

	}

	private final double mean;
	private final double variance;
	private final double std;
	private final Function<Double, Double> densityFunction;
	private final Function<Double, Double> cumulativeDistributionFunction;

	private static class GaussianDensityFunction implements Function<Double, Double> {

		private final double mean;

		private final double normalizationFactor;

		private final double variance;

		public GaussianDensityFunction(double mean, double std) {
			this.mean = mean;
			this.normalizationFactor = (std * 2 * Math.PI);
			this.variance = std * std;
		}

		@Override
		public Double apply(Double x) {
			double shift = x - mean;
			return Math.exp(-1.0 * shift * shift / (2 * variance)) / normalizationFactor;
		}

	}

	private static class GaussianCumulativeDistributionFunction implements Function<Double, Double> {

		private final double mean;

		private final double normalizationFactor;

		public GaussianCumulativeDistributionFunction(double mean, double std) {
			this.mean = mean;
			this.normalizationFactor = std * sqrt(2);
		}

		@Override
		public Double apply(Double x) {
			double shift = x - mean;
			return (1 + erf(shift / normalizationFactor)) / 2.0;
		}

	}

	@JsonCreator
	UnivariateGaussian(@JsonProperty("mean") double mean, @JsonProperty("variance") double variance) {
		this.mean = mean;
		this.variance = variance;
		this.std = Math.sqrt(variance);
		this.densityFunction = new GaussianDensityFunction(mean, std);
		this.cumulativeDistributionFunction = new GaussianCumulativeDistributionFunction(mean, std);
	}

	@JsonProperty("mean")
	public double mean() {
		return mean;
	}

	public double std() {
		return std;
	}

	@JsonProperty("variance")
	public double variance() {
		return variance;
	}

	@Override
	public double totalVariationDistance(ProbabilisticModel q) {
		if (q instanceof UnivariateGaussian) {
			return totalVariationDistance((UnivariateGaussian) q);
		}
		LOGGER.warning("Operand not univariate Gaussian; result NaN");
		return Double.NaN;
	}

	@Override
	public double hellingerDistance(ProbabilisticModel q) {
		if (q instanceof UnivariateGaussian) {
			return hellingerDistance((UnivariateGaussian) q);
		}
		LOGGER.warning("Operand not univariate Gaussian; result NaN");
		return Double.NaN;
	}

	@Override
	public double kullbackLeiblerDivergence(ProbabilisticModel q) {
		if (q instanceof UnivariateGaussian) {
			return kullbackLeiblerDivergence((UnivariateGaussian) q);
		}
		LOGGER.warning("Operand not univariate Gaussian; result NaN");
		return Double.NaN;
	}

	public double hellingerDistance(UnivariateGaussian q) {
		UnivariateGaussian p = this;
		double shift = q.mean() - p.mean();
		double squaredDistance = 1 - sqrt((2 * p.std() * q.std()) / (p.variance() + q.variance()))
				* exp(-1 * shift * shift / (4 * (p.variance() + q.variance())));
		return sqrt(squaredDistance);
	}

	/**
	 * KL divergence from this to some other univariate Gaussian.
	 * 
	 * @param q
	 *            the other Gaussian
	 * @return D(this|q)
	 */
	public double kullbackLeiblerDivergence(UnivariateGaussian q) {
		UnivariateGaussian p = this;
		if (q.variance() == 0.0) {
			return 0.0;
		}
		double logOfSigmaRatio = Math.log(Math.sqrt(q.variance()) / Math.sqrt(p.variance()));
		double meanDiff = p.mean() - q.mean();
		return logOfSigmaRatio + (p.variance() + meanDiff * meanDiff) / (2 * q.variance()) - 0.5;
	}

	public double totalVariationDistance(UnivariateGaussian q) {
		// TODO: check formula
		return Math.abs(Erf.erf((this.mean() - q.mean()) / (2 * Math.sqrt(2 * this.variance()))));
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
		return "Gaussian(" + mean + "," + std + ")";
	}

	@Override
	public Collection<UnivariateGaussianParameter> parameters() {
		return allOf(UnivariateGaussianParameter.class);
	}

	public Optional<Double> value(ModelParameter p) {
		if (!(p instanceof UnivariateGaussianParameter)) {
			return empty();
		}
		return Optional.of(((UnivariateGaussianParameter) p).value(this));
	}

	@Override
	public UnivariateGaussian build(Workspace workspace) {
		return this;
	}

	@Override
	public SerialForm<? extends UnivariateGaussian> serialForm() {
		return this;
	}

}
