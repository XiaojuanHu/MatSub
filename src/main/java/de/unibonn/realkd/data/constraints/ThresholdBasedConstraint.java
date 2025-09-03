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
package de.unibonn.realkd.data.constraints;

import static java.lang.String.format;

import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
@KdonTypeName("thresholdConstraint")
abstract class ThresholdBasedConstraint<T> implements Constraint<T> {

	@JsonProperty("value")
	protected final T threshold;

	protected final String formattedThreshold;

	protected final Comparator<T> order;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonProperty("label")
	protected final String label;

	@SuppressWarnings("unchecked")
	ThresholdBasedConstraint(T threshold, Comparator<T> order, String label) {
		this.threshold = threshold;
		this.formattedThreshold = (threshold instanceof Double || threshold instanceof Float) ? format("%g", threshold)
				: format("%s", threshold);
		this.order = order == null ? (Comparator<T>) Comparator.naturalOrder() : order;
		this.label = label;
	}

	/**
	 * Accessor for serialization that returns null for default natural order.
	 * 
	 * @return entailed order or null if this order is natural order
	 * 
	 */
	@JsonProperty("order")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected final Comparator<T> order() {
		return order.equals(Comparator.naturalOrder()) ? null : order;
	}

	/**
	 * @return the comparison threshold which defines this constraint
	 */
	public T threshold() {
		return this.threshold;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ThresholdBasedConstraint)) {
			return false;
		}
		ThresholdBasedConstraint<?> that = (ThresholdBasedConstraint<?>) other;
		return this.threshold.equals(that.threshold) && this.order.equals(that.order)
				&& this.getClass().equals(that.getClass());
	}

	@Override
	public int hashCode() {
		return Objects.hash(threshold, order, getClass());
	}

	@Override
	public String toString() {
		return suffixNotationName();
	}

}
