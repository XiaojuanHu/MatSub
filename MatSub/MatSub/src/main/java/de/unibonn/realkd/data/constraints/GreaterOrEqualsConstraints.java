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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;

/**
 * <p>
 * Constraint that holds if test value is greater or equal than some threshold
 * (with respect to some given order).
 * </p>
 * 
 * <p>
 * Implementation maps to disjunction of equals ({@link Constraints#equalTo})
 * and greater-than ({@link Constraints#greaterThan}) constraint in order to
 * resolve implication checks.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
@KdonTypeName("greaterOrEquals")
public class GreaterOrEqualsConstraints<T> extends ThresholdBasedConstraint<T> implements DerivedConstraint<T> {

	private final Constraint<T> greaterOrEquals;
	private final String description;

	@JsonCreator
	GreaterOrEqualsConstraints(@JsonProperty("value") T threshold, @JsonProperty("order") Comparator<T> order,
			@JsonProperty("label") String label) {
		super(threshold, order, label);
		Constraint<T> equalsConstraint = Constraints.equalTo(threshold);
		Constraint<T> greaterThanConstraint = Constraints.greaterThan(threshold, order);
		this.greaterOrEquals = Constraints.or(greaterThanConstraint, equalsConstraint);
		this.description = (threshold instanceof Double || threshold instanceof Float)
				? format("%s [%s,inf]", label, formattedThreshold) : format("at least %s", formattedThreshold);
	}

	@Override
	public boolean holds(T value) {
		return greaterOrEquals.holds(value);
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public String suffixNotationName() {
		return format(">=%s", formattedThreshold);
	}

	@Override
	public boolean implies(Constraint<T> anotherConstraint) {
		return greaterOrEquals.implies(anotherConstraint);
	}

    @Override
        public boolean impliedBy(Constraint<T> anotherConstraint) {
                return anotherConstraint.implies(greaterOrEquals);
        }

}
