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
package de.unibonn.realkd.patterns.models.gaussian;

import static de.unibonn.realkd.common.IndexSets.difference;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * Provides static factory methods and constants for Gaussian modelling.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Gaussians {

	private Gaussians() {
		;
	}

	public static UnivariateGaussian gaussian(DataTable dataTable, Attribute<?> attribute, IndexSet rows) {
		MetricAttribute metricAttribute = (MetricAttribute) attribute;
		IndexSet nonMissingIndices = difference(rows, metricAttribute.missingPositions());
		if (nonMissingIndices.isEmpty()) {
			// TODO there is a good chance that this will in fact chrash for
			// some visualizations
			return new UnivariateGaussian(Double.NaN, Double.NaN);
		}

		double mean = metricAttribute.meanOnRows(rows);
		double sumOfsquaredMeanDists = 0.0;
		for (int rowIx : nonMissingIndices) {
			double value = metricAttribute.value(rowIx);
			sumOfsquaredMeanDists += (mean - value) * (mean - value);
		}
		double variance = sumOfsquaredMeanDists / nonMissingIndices.size();

		return new UnivariateGaussian(mean, variance);
	}

	public static UnivariateGaussian gaussian(DataTable dataTable, MetricAttribute attribute) {
		return new UnivariateGaussian(attribute.mean(), attribute.variance());
	}

}
