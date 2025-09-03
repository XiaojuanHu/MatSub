/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class NotEqualsConstraint<T> implements Constraint<T> {

	private static final int NON_EQUALS_HASH_OFFSET = 329;

	@JsonProperty("value")
	private final T value;

	@JsonCreator
	NotEqualsConstraint(@JsonProperty("value") T value) {
		this.value = value;
	}
	
	public T value() {
		return value;
	}

	@Override
	public boolean holds(T v) {
		return !value.equals(v);
	}

	@Override
	public String description() {
		return "not " + value;
	}

	@Override
	public String suffixNotationName() {
		return "!=" + value;
	}

	@Override
	public boolean implies(Constraint<T> anotherConstraint) {
		return this.equals(anotherConstraint);
	}

	public int hashCode() {
		return value.hashCode() + NON_EQUALS_HASH_OFFSET;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof NotEqualsConstraint)) {
			return false;
		}
		return value.equals(((NotEqualsConstraint<?>) other).value);
	}

}
