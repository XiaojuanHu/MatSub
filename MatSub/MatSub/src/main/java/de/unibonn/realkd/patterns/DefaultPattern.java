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

package de.unibonn.realkd.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.data.Population;

public class DefaultPattern<T extends PatternDescriptor> implements Pattern<T> {

	private final List<Measure> measuresInOrder;

	private final Map<Measure, Measurement> measureToMeasurement;

	private final Population population;

	private final T descriptor;

	private final List<Measurement> measurements;

	public DefaultPattern(Population population, T descriptor, List<Measurement> measurements) {
		this.population = population;
		this.descriptor = descriptor;

		this.measurements = measurements;
		this.measuresInOrder = new ArrayList<>();
		this.measureToMeasurement = new HashMap<>();
		measurements.forEach(this::addMeasurement);
	}

	/**
	 * Recursively adds first all auxiliary measurements and then measurement
	 * itself to flattened list of measurements.
	 */
	private void addMeasurement(Measurement measurement) {
		measurement.auxiliaryMeasurements().forEach(this::addMeasurement);
		if (!measureToMeasurement.containsKey(measurement.measure())) {
			this.measuresInOrder.add(measurement.measure());
			this.measureToMeasurement.put(measurement.measure(), measurement);
		}
	}
	
	@Override
	public Pattern<T> add(Measurement measurement) {
		return this;
	}

	@Override
	public Optional<Measurement> measurement(Measure measure) {
		return Optional.ofNullable(measureToMeasurement.get(measure));
	}

	@Override
	public List<Measurement> measurements() {
		return measurements;
	}

	@Override
	public List<Measure> measures() {
		return measuresInOrder;
	}

	@Override
	public double value(Measure measure) {
		return measureToMeasurement.get(measure).value();
	}

	@Override
	public boolean hasMeasure(Measure measure) {
		return measureToMeasurement.containsKey(measure);
	}

	@Override
	public boolean hasMeasurement(Class<? extends Measure> measureType) {
		for (Measurement measurement : measurements) {
			if (measureType.isAssignableFrom(measurement.measure().getClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final Population population() {
		return this.population;
	}

	@Override
	public T descriptor() {
		return descriptor;
	}

	@Override
	public final String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(typeString() + "(");
		// resultBuilder.append("\n");
		resultBuilder.append(descriptor.toString());
		resultBuilder.append(", ");
		resultBuilder.append(getAdditionsForStringRepresentation());

		// if (descriptor instanceof TableSubspaceDescriptor) {
		// resultBuilder.append("attributes: [\n");
		// Iterator<Attribute<?>> attributeIterator = ((TableSubspaceDescriptor)
		// descriptor)
		// .getReferencedAttributes().iterator();
		// while (attributeIterator.hasNext()) {
		// Attribute<?> attribute = attributeIterator.next();
		// resultBuilder.append("\t" + attribute.name());
		// resultBuilder
		// .append(attributeIterator.hasNext() ? ",\n" : "\n");
		// }
		// resultBuilder.append("],\n");
		// }
		resultBuilder.append("[");
		Iterator<Measure> measureIterator = measures().iterator();
		while (measureIterator.hasNext()) {
			Measure measure = measureIterator.next();
			resultBuilder.append(measure.caption() + ": " + value(measure));
			resultBuilder.append(measureIterator.hasNext() ? "," : "");
		}
		resultBuilder.append("]");
		resultBuilder.append(")");
		return resultBuilder.toString();
	}

	/**
	 * @return pattern type information for string representation
	 */
	protected String typeString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Hook that can be overriden by sub-classes in order to add information to
	 * the string representation of pattern as computed by {@link #toString} .
	 */
	protected String getAdditionsForStringRepresentation() {
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof DefaultPattern)) {
			return false;
		}
		DefaultPattern<?> that = (DefaultPattern<?>) o;
		return this.measurements.equals(that.measurements) && this.descriptor.equals(that.descriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(measureToMeasurement, descriptor);
	}

}
