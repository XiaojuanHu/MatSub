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

import de.unibonn.realkd.common.KdonTypeName;

@KdonTypeName("equals")
public final class EqualsConstraint<T> implements Constraint<T> {

	@JsonProperty("value")
	private T value;

	@JsonCreator
	EqualsConstraint(@JsonProperty("value") T value) {
		this.value = value;
	}

	@Override
	public boolean holds(T queryValue) {
		return queryValue.equals(this.value);
	}

	@Override
	public String suffixNotationName() {
		return "=" + value;
	}

	@Override
	public String description() {
		return value.toString();
	}

	public T comparisonValue() {
		return value;
	}

	@Override
	public boolean implies(Constraint<T> anotherConstraint) {
		return anotherConstraint.holds(value);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof EqualsConstraint)) {
			return false;
		}
		return value.equals(((EqualsConstraint<?>) other).value);
	}
	
	private static final int EQUALS_HASH_OFFSET = 13;

	@Override
	public int hashCode() {
		return Objects.hashCode(value) + EQUALS_HASH_OFFSET;
	}
	
	@Override
	public String toString() {
		return suffixNotationName();
	}

}
