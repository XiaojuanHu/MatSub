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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

final class OrConstraint<T> implements DerivedConstraint<T> {

	private static final int OR_HASH_OFFSET = -213;

	@JsonProperty("firstOperand")
	private final Constraint<T> firstEntailedConstraint;

	@JsonProperty("secondOperand")
	private final Constraint<T> secondEntailedConstraint;

	@JsonCreator
	OrConstraint(@JsonProperty("firstOperand") Constraint<T> firstEntailedConstraint,
			@JsonProperty("secondOperand") Constraint<T> secondEntailedConstraint) {
		this.firstEntailedConstraint = firstEntailedConstraint;
		this.secondEntailedConstraint = secondEntailedConstraint;
	}

	@Override
	public boolean holds(T value) {
		return (firstEntailedConstraint.holds(value) || secondEntailedConstraint.holds(value));
	}

	@Override
	public String description() {
		return "(" + firstEntailedConstraint.description() + " OR " + secondEntailedConstraint.description() + ")";
	}

	@Override
	public String suffixNotationName() {
		return "(" + firstEntailedConstraint.suffixNotationName() + " OR "
				+ secondEntailedConstraint.suffixNotationName() + ")";
	}

	@Override
	public boolean implies(Constraint<T> anotherConstraint) {
		// disjunction implies another constraints if it is implied by both
		// entailed constraints
		return (firstEntailedConstraint.implies(anotherConstraint)
				&& secondEntailedConstraint.implies(anotherConstraint));
	}

	@Override
	public boolean impliedBy(Constraint<T> anotherConstraint) {
		// disjunction is implied by another constraint if this constraint
		// implies either entailed constraint
		return (anotherConstraint.implies(firstEntailedConstraint)
				|| anotherConstraint.implies(secondEntailedConstraint));
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrConstraint)) {
			return false;
		}
		OrConstraint<?> that = (OrConstraint<?>) other;
		return (this.firstEntailedConstraint.equals(that.firstEntailedConstraint)
				&& this.secondEntailedConstraint.equals(that.secondEntailedConstraint));
	}

	public int hashCode() {
		return Objects.hash(firstEntailedConstraint, secondEntailedConstraint) + OR_HASH_OFFSET;
	}

}
