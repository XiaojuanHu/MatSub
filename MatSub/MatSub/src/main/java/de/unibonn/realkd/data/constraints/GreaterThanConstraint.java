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
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@KdonTypeName("greaterThan")
public final class GreaterThanConstraint<T> extends ThresholdBasedConstraint<T> {

	// private static final int HASH_OFFSET = 217;

	@JsonCreator
	GreaterThanConstraint(@JsonProperty("value") T value, @JsonProperty("order") Comparator<T> comparator,
			@JsonProperty("label") String thresholdLabel) {
		super(value, comparator, thresholdLabel);
	}

	@Override
	public boolean holds(T value) {
		return order.compare(value, this.threshold) > 0;
	}

	@Override
	public String description() {
		return format("%s (%s,inf]", label, formattedThreshold);
	}

	// this is the "name"
	@Override
	public String suffixNotationName() {
		return format(">%s", formattedThreshold);
	}

	@Override
	public boolean implies(Constraint<T> anotherConstraint) {
		if (anotherConstraint instanceof GreaterThanConstraint) {
			GreaterThanConstraint<T> anotherLargerThanConstraint = (GreaterThanConstraint<T>) anotherConstraint;
			return (order.compare(anotherLargerThanConstraint.threshold(), this.threshold) <= 0);
		}
		if (anotherConstraint instanceof NotEqualsConstraint) {
			NotEqualsConstraint<T> notEqualsConstraint = (NotEqualsConstraint<T>) anotherConstraint;
			return order.compare(notEqualsConstraint.value(), this.threshold) <= 0;
		}
		if (anotherConstraint instanceof DerivedConstraint) {
			return ((DerivedConstraint<T>) anotherConstraint).impliedBy(this);
		}
		return false;
	}

}
