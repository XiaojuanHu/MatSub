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
package de.unibonn.realkd.patterns;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;

/**
 * <p>
 * Procedure for computing values of a quality measure for one or more pattern
 * descriptors. The measure for which a value is computed is identified by a
 * {@link QualityMeasureId}, and the value is provided in the form of a
 * {@link Measurement}, which can optionally contain the values of one or
 * more auxiliary measures (that have been produced during the computation).
 * </p>
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.WRAPPER_ARRAY)
public interface MeasurementProcedure<M extends Measure, T> {

	/**
	 * Tests whether {@link #perform(Object)} can be called for a
	 * specific descriptor or {@link #perform(List)} can be called if all list
	 * elements satisfy this check.
	 * 
	 * @param descriptor the descriptor for which applicability is tested
	 * 
	 * @return whether procedure is applicable to descriptors of class
	 * 
	 */
	public boolean isApplicable(T descriptor);

	/**
	 * Provides the id of the measure for which values are computed by
	 * {@link #perform(Object)} and {@link #perform(List)}.
	 * 
	 * @return measure that is computed by this procedure
	 * 
	 */
	public M getMeasure();

	/**
	 * Performs procedure to produce a quality measurement for a specified
	 * pattern descriptor.
	 * 
	 * @param descriptor
	 *            the descriptor for which quality measurement is performed
	 * 
	 * @return quality measurement for descriptor
	 * 
	 */
	public Measurement perform(T descriptor);

	/**
	 * Applies procedure to all descriptors of a given list. Per default this is
	 * reduced to applying procedure to all individual descriptors. However,
	 * more efficient implementations might be present in special cases
	 * 
	 * @param descriptors
	 *            list of descriptors to which to apply procedure
	 * 
	 * @return list of measurements in order corresponding to order of input
	 *         descriptors
	 * 
	 */
	public default List<Measurement> perform(
			List<T> descriptors) {
		return descriptors.stream().map(d -> perform(d))
				.collect(Collectors.toList());
	}

}
