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

package de.unibonn.realkd.data.constraints;

import static java.lang.String.format;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;

/**
 * @author Bjoern Jacobs
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.6.0
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@KdonTypeName("lessThan")
public final class LessThanConstraint<T> extends ThresholdBasedConstraint<T> implements Constraint<T> {

	@JsonCreator
	LessThanConstraint(@JsonProperty("value") T value, @JsonProperty("order") Comparator<T> comparator,
			@JsonProperty("label") String thresholdLabel) {
		super(value, comparator, thresholdLabel);
	}

	@Override
	public boolean holds(T value) {
		return order.compare(value, this.threshold) < 0;
	}

	@Override
	public String description() {
		return format("%s [-inf,%s)", label, formattedThreshold);
	}

	@Override
	public String suffixNotationName() {
		return format("<%s", formattedThreshold);
	}

	@Override
	public boolean implies(Constraint<T> other) {
		if (other instanceof LessThanConstraint) {
			LessThanConstraint<T> otherLessThan = (LessThanConstraint<T>) other;
			return (order.compare(otherLessThan.threshold(), this.threshold) >= 0);
		}
		if (other instanceof NotEqualsConstraint) {
			NotEqualsConstraint<T> notEqualsConstraint = (NotEqualsConstraint<T>) other;
			return order.compare(notEqualsConstraint.value(), this.threshold) >= 0;
		}
		if (other instanceof DerivedConstraint) {
			return ((DerivedConstraint<T>) other).impliedBy(this);
		}
		return false;
	}

}
