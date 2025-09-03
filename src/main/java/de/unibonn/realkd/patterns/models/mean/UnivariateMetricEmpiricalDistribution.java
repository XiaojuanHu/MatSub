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
package de.unibonn.realkd.patterns.models.mean;

import static de.unibonn.realkd.patterns.models.MeanAbsoluteMedianDeviation.MEAN_ABSOLUTE_MEDIAN_DEVIATION;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelParameter;
import de.unibonn.realkd.patterns.models.UnivariateOrdinalProbabilisticModel;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class UnivariateMetricEmpiricalDistribution extends MetricEmpiricalDistribution
		implements UnivariateOrdinalProbabilisticModel<Double> {

	public enum UnivariateMetricEmpiricalDistributionParameter implements ModelParameter {

		MEAN("mean", "Empirical mean.") {
			@Override
			public Double value(UnivariateMetricEmpiricalDistribution model) {
				return model.mean;
			}
		},

		STD("std. dev.", "Empirical standard deviation.") {
			@Override
			public Double value(UnivariateMetricEmpiricalDistribution model) {
				return model.std;
			}
		},

		MEDIAN("median", "Empirical median") {
			@Override
			public Double value(UnivariateMetricEmpiricalDistribution model) {
				return model.median;
			}
		},

		AVG_ABS_DEV("avg. abs. dev.", "Empirical average absolute deviation from the median.") {
			@Override
			public Double value(UnivariateMetricEmpiricalDistribution model) {
				return model.avgAbsDev;
			}
		};

		private final String caption;

		private final String description;

		private UnivariateMetricEmpiricalDistributionParameter(String caption, String description) {
			this.caption = caption;
			this.description = description;
		}

		public String caption() {
			return caption;
		}

		@Override
		public String description() {
			return description;
		}

		public abstract Double value(UnivariateMetricEmpiricalDistribution model);

	}

	private final double mean;
	private final double std;
	private final double variance;
	private final double median;
	private final double avgAbsDev;
	private final ImmutableList<Measurement> accuracyMeasurements;
	private final Function<Double, Double> cumulativeDensityFunction;

	@JsonCreator
	UnivariateMetricEmpiricalDistribution(DataTable dataTable, MetricAttribute attribute, IndexSet rows, double mean,
			double variance, double median, double avgAbsMedDev) {
		super(dataTable, ImmutableList.of(attribute), rows, ImmutableList.of(mean), new double[][] { { variance } },
				ImmutableList.of(median), ImmutableList.of(avgAbsMedDev));
		this.mean = mean;
		this.variance = variance;
		this.std = Math.sqrt(variance);
		this.median = median;
		this.avgAbsDev = avgAbsMedDev;
		this.accuracyMeasurements = ImmutableList
				.of(Measures.measurement(MEAN_ABSOLUTE_MEDIAN_DEVIATION, avgAbsDev));
		if (rows.equals(dataTable.population().objectIds())) {
			this.cumulativeDensityFunction = x -> (double) (attribute).orderNumber(x)
					/ (double) attribute.sortedNonMissingRowIndices().size();
		} else {
			Set<Integer> nonMissingIndices = StreamSupport.stream(rows.spliterator(), false)
					.filter(i -> !attribute.valueMissing(i)).collect(Collectors.toSet());
			this.cumulativeDensityFunction = x -> (double) attribute.orderNumberOnRows(x, nonMissingIndices)
					/ (double) nonMissingIndices.size();
		}
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

	@JsonProperty("median")
	public double median() {
		return median;
	}

	@JsonProperty("avgAbsDev")
	public double avgAbsDev() {
		return avgAbsDev;
	}

	@Override
	public Collection<? extends ModelParameter> parameters() {
		return EnumSet.allOf(UnivariateMetricEmpiricalDistributionParameter.class);
	}

	@Override
	public Optional<Double> value(ModelParameter parameter) {
		if (parameter instanceof UnivariateMetricEmpiricalDistributionParameter) {
			return Optional.of(((UnivariateMetricEmpiricalDistributionParameter) parameter).value(this));
		}
		return Optional.empty();
	}

	@Override
	public List<Measurement> measurements() {
		return accuracyMeasurements;
	}

	@Override
	public Function<Double, Double> cumulativeDistributionFunction() {
		return cumulativeDensityFunction;
	}

	@Override
	public String toString() {
		return String.format("Sample(mean=%.5f, std=%.5f, med=%.5f, amd=%.5f)", mean, std, median, avgAbsDev);
	}

}
