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
package de.unibonn.realkd.common.measures;

import static de.unibonn.realkd.util.Comparison.equalsIncludingNaN;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

/**
 * Represents the result of measuring some quantity.
 * 
 * @author Sandy Moens
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.6.0
 * 
 * @see Measure
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class Measurement {

//	private static final Logger LOGGER = Logger.getLogger(Measurement.class.getName());

	private final Measure measure;

	private final double value;

	private final List<Measurement> auxiliaryMeasurements;

	private final String stringRepr;

	Measurement(Measure measure, double value) {
		this(measure, value, ImmutableList.of());
	}

	Measurement(@JsonProperty("measure") Measure measure, @JsonProperty("value") double value,
			@JsonProperty("auxiliaryMeasurements") List<Measurement> auxiliaryMeasurements) {
		this.measure = measure;
		this.value = value;
		this.auxiliaryMeasurements = auxiliaryMeasurements == null ? ImmutableList.of() : auxiliaryMeasurements;
		this.stringRepr = measure.caption() + ": " + value;
	}

	/**
	 * Constructor for Jackson which takes auxiliary measurements as array
	 * (because arrays allow more concise json in current object mapper setting)
	 */
	@JsonCreator
	private Measurement(@JsonProperty("measure") Measure measure, @JsonProperty("value") double value,
			@JsonProperty("auxiliaryMeasurements") Measurement[] auxiliaryMeasurements) {
		this(measure, value, (auxiliaryMeasurements == null) ? ImmutableList.of()
				: Arrays.stream(auxiliaryMeasurements).collect(Collectors.toList()));
	}

	/**
	 * Identifies the quality measure for which value was taken.
	 * 
	 * @return id of measure
	 * 
	 */
	@JsonProperty("measure")
	public Measure measure() {
		return measure;
	}

	/**
	 * Value for measure that was received in this measurement. Note that
	 * non-deterministic measurement procedures may produce different values on
	 * subsequent applications for the same pattern descriptor.
	 * 
	 * @return measure value
	 * 
	 */
	@JsonProperty("value")
	public double value() {
		return value;
	}

	/**
	 * Provides list of other measurements that have been taken as auxiliary
	 * operation in order to compute this measurement.
	 * 
	 * @return list of measurements
	 * 
	 */
	public List<Measurement> auxiliaryMeasurements() {
		return auxiliaryMeasurements;
	}

	@JsonProperty("auxiliaryMeasurements")
	public Measurement[] auxiliaryMeasurementsAsArray() {
		return auxiliaryMeasurements.stream().toArray(i -> new Measurement[i]);
	}

	public String toString() {
		return stringRepr;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(this instanceof Measurement)) {
			return false;
		}
		return this.measure.equals(((Measurement) other).measure())
				&& equalsIncludingNaN(this.value, (((Measurement) other).value()));
	}

	public int hashCode() {
		return Objects.hash(measure, value);
	}

}
