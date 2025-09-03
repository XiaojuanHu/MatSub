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
package de.unibonn.realkd.common.measures;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public interface MeasurementContainer {

	/**
	 * 
	 * @return all top-level measurements
	 */
	public abstract List<Measurement> measurements();

	/**
	 * Implementation can choose whether this includes non-top level
	 * measurements as well (currently it does for Pattern)
	 * 
	 * @param measure
	 *            query measure
	 * @return measurement of measure if present or empty otherwise
	 */
	public default Optional<Measurement> measurement(Measure measure) {
		return measurements().stream().filter(m -> m.measure().equals(measure)).findFirst();
	}

	/**
	 * Returns best (highest) measurement of a measure of given type if such a
	 * measurement is present or empty otherwise.
	 * 
	 * @param measureClass
	 *            the type of the measure
	 * @return highest measurement of type or empty if no such measure
	 * 
	 */
	public default Optional<Measurement> measurement(Class<? extends Measure> measureClass) {
		Optional<Measurement> result = Optional.empty();
		for (Measurement measurement : measurements()) {
			if (measureClass.isAssignableFrom(measurement.measure().getClass())
					&& measurement.value() >= result.map(r -> r.value()).orElse(Double.NEGATIVE_INFINITY)) {
				result = Optional.of(measurement);
			}
		}
		return result;
	}

	public default List<Measurement> measurements(Class<? extends Measure> measureClass) {
		return measurements().stream().filter(m -> measureClass.isAssignableFrom(m.measure().getClass()))
				.collect(toList());
	}

}
