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

import java.util.List;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.MeasurementContainer;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.Population;

/**
 * <p>
 * A pattern <em>interprets</em> a syntactic {@link PatternDescriptor} through a
 * collection of quality measurements that assess by their semantics in what
 * sense the descriptor is noteworthy and by their quantity to what degree it is
 * so.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public interface Pattern<T extends PatternDescriptor> extends MeasurementContainer, HasSerialForm<Pattern<T>> {

	public Pattern<T> add(Measurement measurement);
	
	public abstract boolean equals(Object o);

	public abstract int hashCode();

	public abstract String toString();

	public abstract Population population();

	/**
	 * <p>
	 * Provides a 'flat' view on all measures for which values are present in
	 * this pattern. In addition to the measures of all top-level measurements
	 * returned by {@link #measurements()}, this list contains all auxiliary
	 * measurements (recursively).
	 * </p>
	 * <p>
	 * Every measure is guaranteed to only show up once in result list---even
	 * though some measures can appear as auxiliary measurement in more than one
	 * other measurement.
	 * </p>
	 * 
	 * @return 'flat' list of all quality measures present in pattern
	 * 
	 */
	public abstract List<Measure> measures();

	/**
	 * Provides the measurement value for any measure listed by
	 * {@link #measures()}.
	 * 
	 * @param measure
	 *            identifier of measure
	 * @return measurement value for measure
	 */
	public abstract double value(Measure measure);
	
	/**
	 * Convenience method with result identical to {@link #measures()}
	 * .contains(measure).
	 * 
	 */
	public abstract boolean hasMeasure(Measure measure);

	/**
	 * Check presence of a measurement on type level of measure.
	 * 
	 * @param measureClass
	 *            the type of the measure
	 * @return whether pattern contains any measurement of type
	 */
	public abstract boolean hasMeasurement(Class<? extends Measure> measure);

	public abstract T descriptor();

	/**
	 * Provides a flat representation of the pattern that can be serialized to
	 * JSON and that constructs an equivalent pattern when its build method is
	 * called with an equivalent data workspace.
	 * 
	 * @return serial form of pattern
	 * 
	 */
	public default SerialForm<? extends Pattern<T>> serialForm() {
		throw new UnsupportedOperationException();
	}

}