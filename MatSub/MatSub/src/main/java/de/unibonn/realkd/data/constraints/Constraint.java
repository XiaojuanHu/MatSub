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

import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonTypeName;

/**
 * Logical predicate for values of a certain type.
 * 
 * @author Mario Boley
 *
 * @param <T>
 *            the type of values for which the constraint is defined
 *            
 * @since 0.1.0
 * 
 * @version 0.3.0
 * 
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
@KdonTypeName("constraint")
public interface Constraint<T> extends JsonSerializable {

	public abstract boolean holds(T value);

	/**
	 * @return a human-interpretable description that can be used as a concise
	 * explanation of when the constraint is true; should also contain (parts
	 * of) the name of the constraint, e.g., "high [5,8.9]".
	 * 
	 */
	public abstract String description();

	/**
	 * @return the name of the constraint that can be combined with an attribute
	 * name to result in a logical expression. For instance: "=high".
	 */
	public abstract String suffixNotationName();

	/**
	 * Test for logical implication that allows false negatives.
	 */
	public abstract boolean implies(Constraint<T> anotherConstraint);
	
}
