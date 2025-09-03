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

package de.unibonn.realkd.patterns.models.mean;

import static java.util.stream.Collectors.toList;
import static java.util.EnumSet.allOf;
import static java.util.Optional.empty;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.HasExportableForm;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelParameter;

public class MetricEmpiricalDistribution implements Model, HasExportableForm {

	private static enum MetricEmpiricalDistributionParameter implements ModelParameter {

		MEAN("mean vector", "Empirical mean vector.", m -> m.means), COV("cov. matrix", "Empirical covariance matrix.",
				m -> m.covarianceMatrix);

		private final String caption;

		private final String description;

		private final Function<MetricEmpiricalDistribution, Object> valueMap;

		private MetricEmpiricalDistributionParameter(String caption, String description,
				Function<MetricEmpiricalDistribution, Object> valueMap) {
			this.caption = caption;
			this.description = description;
			this.valueMap = valueMap;
		}

		public String caption() {
			return caption;
		}

		@Override
		public String description() {
			return description;
		}

		public Object value(MetricEmpiricalDistribution model) {
			return valueMap.apply(model);
		}

	}

//	private enum MeanDeviationDistance implements ModelDistanceFunction {
//
//		INSTANCE;
//
//		@Override
//		public double distance(Model globalModel, Model localModel) {
//			if (!(globalModel instanceof MetricEmpiricalDistribution
//					&& localModel instanceof MetricEmpiricalDistribution)) {
//				throw new IllegalArgumentException("models must be mean deviation models");
//			}
//
//			double distance = 0;
//			List<Double> globalMeans = ((MetricEmpiricalDistribution) globalModel).means();
//			List<Double> localMeans = ((MetricEmpiricalDistribution) localModel).means();
//			for (int i = 0; i < globalMeans.size(); i++) {
//				distance += Math.abs(globalMeans.get(i) - localMeans.get(i));
//			}
//			return distance;
//		}
//
//		@Override
//		public ModelDeviationMeasure getCorrespondingInterestingnessMeasure() {
//			return ConcreteModelDeviationMeasure.MANHATTAN_MEAN_DISTANCE;
//		}
//
//		/**
//		 * Test if both models represent mean vectors of same dimensionality.
//		 */
//		@Override
//		public boolean isApplicable(Model globalModel, Model localModel) {
//			return (globalModel instanceof MetricEmpiricalDistribution
//					&& localModel instanceof MetricEmpiricalDistribution && ((MetricEmpiricalDistribution) globalModel)
//							.means().size() == ((MetricEmpiricalDistribution) localModel).means().size());
//		}
//
//		public String toString() {
//			return "Manhattan mean distance";
//		}
//
//	}

//	public static final ModelDistanceFunction MANHATTEN_MEAN_DEVIATION = MeanDeviationDistance.INSTANCE;

	private final List<Double> means;

	private final List<Double> medians;

	private final double[][] covarianceMatrix;

	private final List<Double> avgAbsMedDevs;

    private final List<Double> mins;
    private final List<Double> maxs;

	MetricEmpiricalDistribution(DataTable dataTable, List<MetricAttribute> attributes, IndexSet rows,
				    List<Double> means, double[][] covMatrix, List<Double> medians, List<Double> avgAbsMedDevs) {
		this.means = means;
		this.covarianceMatrix = covMatrix;
		this.medians = medians;
		this.avgAbsMedDevs = avgAbsMedDevs;
		this.mins = attributes.stream().map(a -> a.minOnRows(rows)).collect(toList());
		this.maxs = attributes.stream().map(a -> a.maxOnRows(rows)).collect(toList());
	}

    MetricEmpiricalDistribution(DataTable dataTable, List<MetricAttribute> attributes, IndexSet rows,
				List<Double> means, double[][] covMatrix, List<Double> medians, List<Double> avgAbsMedDevs,
				List<Double> mins, List<Double> maxs) {
                this.means = means;
                this.covarianceMatrix = covMatrix;
                this.medians = medians;
                this.avgAbsMedDevs = avgAbsMedDevs;
		this.mins = mins;
		this.maxs = maxs;
        }

	public List<Double> means() {
		return means;
	}

	public List<Double> medians() {
		return medians;
	}

	public double[][] covarianceMatrix() {
		return covarianceMatrix;
	}

	public List<Double> averageAbsMedianDeviations() {
		return avgAbsMedDevs;
	}

    public List<Double> mins() {
                return mins;
        }
    public List<Double> maxs() {
                return maxs;
        }

	@Override
	public String toString() {
		return "MetricEmpiricalDistribution(means=" + means + ")";
	}

	@Override
	public Collection<? extends ModelParameter> parameters() {
		return allOf(MetricEmpiricalDistributionParameter.class);
	}

	@Override
	public Optional<? extends Object> value(ModelParameter parameter) {
		if (parameter instanceof MetricEmpiricalDistributionParameter) {
			return Optional.of(((MetricEmpiricalDistributionParameter) parameter).value(this));
		}
		return empty();
	}

	@KdonTypeName("metricEmpiricalDistribution")
	public static class MetricEmpiricalDistributionExportableForm implements JsonSerializable {

		@JsonProperty("means")
		private final Double[] means;

		@JsonProperty("medians")
		private final Double[] medians;

		@JsonProperty("covarianceMatrix")
		private final double[][] covarianceMatrix;

		@JsonProperty("medianDeviations")
		private final Double[] medianDeviations;

	    @JsonProperty("mins")
                private final Double[] mins;

	    @JsonProperty("maxs")
                private final Double[] maxs;

		@JsonCreator
		private MetricEmpiricalDistributionExportableForm(@JsonProperty("means") Double[] means,
				@JsonProperty("covarianceMatrix") double[][] covarianceMatrix,
				@JsonProperty("medians") Double[] medians,
								  @JsonProperty("medianDeviations") Double[] medianDeviations,
								  @JsonProperty("mins") Double[] mins,
								  @JsonProperty("maxs") Double[] maxs) {
			this.means = means;
			this.covarianceMatrix = covarianceMatrix;
			this.medians = medians;
			this.medianDeviations = medianDeviations;
			this.mins = mins;
			this.maxs = maxs;
			
		}

	}

	@Override
	public JsonSerializable exportableForm() {
		return new MetricEmpiricalDistributionExportableForm(means.stream().toArray(i -> new Double[i]),
				covarianceMatrix, medians.stream().toArray(i -> new Double[i]),
								     avgAbsMedDevs.stream().toArray(i -> new Double[i]),
								     mins.stream().toArray(i -> new Double[i]),
								     maxs.stream().toArray(i -> new Double[i]));
	}

}
