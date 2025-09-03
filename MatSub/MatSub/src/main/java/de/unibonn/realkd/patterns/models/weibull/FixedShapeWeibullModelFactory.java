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

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.models.ModelFactory;

/**
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.4.0
 *
 */
public class FixedShapeWeibullModelFactory implements ModelFactory<WeibullDistribution> {

	private static final Logger LOGGER = Logger.getLogger(FixedShapeWeibullModelFactory.class.getName());

	public static final String STRING_NAME = "Weibull distribution";

	@JsonProperty("shape")
	private final double shape;

	private final String symbol;

	@JsonCreator
	public FixedShapeWeibullModelFactory(@JsonProperty("shape") double shape) {
		this.shape = shape;
		this.symbol = shape == 1.0 ? "Exp" : "Weibull[k=" + shape + "]";
	}

	@Override
	@JsonIgnore
	public Class<? extends WeibullDistribution> modelClass() {
		return WeibullDistribution.class;
	}

	@Override
	public WeibullDistribution getModel(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return getModel(dataTable, attributes, dataTable.population().objectIds());
	}

	private static class Accumulator {

		private double total = 0.0;
		private int count = 0;

		public double average() {
			return count > 0 ? total / count : 0;
		}

		public void accept(double i) {
			total += i;
			count++;
		}

		public void combine(Accumulator other) {
			total += other.total;
			count += other.count;
		}

	}

	@Override
	public WeibullDistribution getModel(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows) {
		double k = shape;
		MetricAttribute metricAttribute = (MetricAttribute) attributes.get(0);
		Stream<Integer> nonMissingValues = StreamSupport.stream(rows.spliterator(), false)
				.filter(i -> !metricAttribute.valueMissing(i));

		double averageOfPowers = nonMissingValues.mapToDouble(v -> Math.pow(v, k))
				.collect(Accumulator::new, Accumulator::accept, Accumulator::combine).average();
		double scale = Math.pow(averageOfPowers, 1.0 / k);

		if (scale <= 0.0) {
			LOGGER.severe("Non-positive scale (" + scale + ") for model fitted to rows " + rows);
		}

		return new WeibullDistribution(dataTable, k, scale);
	}

	@Override
	public boolean isApplicable(List<? extends Attribute<?>> attributes) {
		return (attributes.size() == 1 && attributes.get(0) instanceof MetricAttribute);
	}

	@Override
	public String toString() {
		return STRING_NAME;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FixedShapeWeibullModelFactory)) {
			return false;
		}
		FixedShapeWeibullModelFactory that = (FixedShapeWeibullModelFactory) other;
		return (this.shape == that.shape);
	}

	@Override
	public int hashCode() {
		return Objects.hash(shape);
	}

	@Override
	public String symbol() {
		return symbol;
	}

}
