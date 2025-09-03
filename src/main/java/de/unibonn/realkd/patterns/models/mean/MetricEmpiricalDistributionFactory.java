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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.util.Arrays;

public enum MetricEmpiricalDistributionFactory implements ModelFactory<MetricEmpiricalDistribution> {

	INSTANCE;

	public static final String STRING_NAME = "Empirical distribution";

	private MetricEmpiricalDistributionFactory() {
		;
	}

	@Override
	public Class<? extends MetricEmpiricalDistribution> modelClass() {
		return MetricEmpiricalDistribution.class;
	}

	@Override
	public MetricEmpiricalDistribution getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		List<MetricAttribute> metricAttributes = attributes.stream().peek(a -> {
			if (!(a instanceof MetricAttribute))
				throw new IllegalArgumentException(
						"Can only create empirical metric distribution for metric attributes");
		}).map(a -> (MetricAttribute) a).collect(Collectors.toList());
		return metricEmpiricalDistribution(dataTable, metricAttributes);
	}

	@Override
	public MetricEmpiricalDistribution getModel(DataTable dataTable, List<? extends Attribute<?>> attributes,
			IndexSet rows) {
		List<MetricAttribute> metricAttributes = attributes.stream().peek(a -> {
			if (!(a instanceof MetricAttribute))
				throw new IllegalArgumentException(
						"Can only create empirical metric distribution for metric attributes");
		}).map(a -> (MetricAttribute) a).collect(Collectors.toList());
		return metricEmpiricalDistribution(dataTable, metricAttributes, rows);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		if (attributes.size() == 0) {
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

	public static MetricEmpiricalDistribution metricEmpiricalDistribution(DataTable dataTable,
			List<MetricAttribute> attributes) {
		//System.err.println("first constructor");
		if (attributes.size() == 1) {
			MetricAttribute attribute = attributes.get(0);
			return new UnivariateMetricEmpiricalDistribution(dataTable, attribute, dataTable.population().objectIds(),
					attribute.mean(), attribute.variance(), attribute.median(),
					attribute.averageAbsoluteMedianDeviation());
		}
		List<Double> means = attributes.stream().map(a -> a.mean()).collect(Collectors.toList());
		List<Double> medians = attributes.stream().map(a -> a.median()).collect(Collectors.toList());
		double[][] covMatrix = computeCovarianceMatrix(attributes, means, dataTable.population().objectIds());
		List<Double> aamds = attributes.stream().map(a -> a.averageAbsoluteMedianDeviation()).collect(toList());
		return new MetricEmpiricalDistribution(dataTable, attributes, dataTable.population().objectIds(), means,
				covMatrix, medians, aamds);
	}

	public static MetricEmpiricalDistribution metricEmpiricalDistribution(DataTable dataTable,
			List<MetricAttribute> attributes, IndexSet rows) {
		List<Double> means = attributes.stream().map(a -> a.meanOnRows(rows)).collect(toList());
		//System.err.println("second constructor");
		if (attributes.size() == 1) {
			MetricAttribute attribute = attributes.get(0);
			return new UnivariateMetricEmpiricalDistribution(dataTable, attribute, rows, attribute.meanOnRows(rows),
					computeCovarianceMatrix(attributes, means, rows)[0][0], attribute.medianOnRows(rows),
					attribute.averageAbsoluteMedianDeviationOnRows(rows));
		}
		List<Double> medians = attributes.stream().map(a -> a.medianOnRows(rows)).collect(toList());
		List<Double> aamds = attributes.stream().map(a -> a.averageAbsoluteMedianDeviationOnRows(rows))
				.collect(toList());
		return new MetricEmpiricalDistribution(dataTable, attributes, rows, means,
				computeCovarianceMatrix(attributes, means, rows), medians, aamds);
	}

	private static double[][] computeCovarianceMatrix(List<MetricAttribute> attributes, List<Double> means,
			IndexSet rows) {
		double[][] result = new double[attributes.size()][attributes.size()];
		StreamSupport.stream(rows.spliterator(), false).forEach(z -> {
			Function<Integer, Function<Integer, Function<Double, Double>>> addCovContributionOfRow = i -> j -> (v -> v
					+ covContributionOfEntry(z, i, j, attributes, means));
			Arrays.apply(result, addCovContributionOfRow);
		});
		Arrays.apply(result, i -> j -> v -> (v / (rows.size() - 1)));
		return result;
	}

	private static double covContributionOfEntry(int z, int i, int j, List<MetricAttribute> attributes,
			List<Double> means) {
		Optional<Double> iOpt = ((MetricAttribute) attributes.get(i)).getValueOption(z);
		Optional<Double> jOpt = ((MetricAttribute) attributes.get(j)).getValueOption(z);
		Optional<Optional<Double>> contrOpt = iOpt.map(x -> jOpt.map(y -> (x - means.get(i)) * (y - means.get(j))));
		return contrOpt.orElse(Optional.of(0.0)).orElse(0.0);
	}

	@Override
	public String symbol() {
		return "sample-dist";
	}

}
