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

package de.unibonn.realkd.data.table.attributegroups;

import java.util.List;

import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Interface for attribute groups that constitute a logical sequence of
 * measurements.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public interface OrderedAttributeSequence<T> extends AttributeGroup {

	/**
	 * 
	 * @return members of the attribute group in logical order of the sequence
	 *
	 */
	public abstract List<Attribute<T>> elements();

	/**
	 * Computes the change between two elements of the sequence. Result is given
	 * in the form of a new attribute, which can vary in form based on the
	 * specific implementation of the interface.
	 * 
	 * @param fromIndex
	 *            index of first sequence element
	 * @param toIndex
	 *            index of second sequence element
	 * @return Attribute that describes the change between sequence element
	 *         fromIndex and toIndex
	 * @throws IndexOutOfBoundsException
	 *             if either index invalid
	 */
	public abstract Attribute<T> getChangeAttribute(int fromIndex, int toIndex);

	public abstract String getNameInSequence(int index);

}
