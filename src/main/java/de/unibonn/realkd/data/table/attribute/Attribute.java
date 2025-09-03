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

package de.unibonn.realkd.data.table.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.base.Documented;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;

/**
 * Interface for data attributes that aggregate a list of values, each of which
 * is associated with an underlying (virtual) data object (corresponding to
 * integer indices). Values can be missing for any or all indices.
 * 
 * @author Mario Boley
 * 
 * @param <T>
 *            the value type of the attribute
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
public interface Attribute<T> extends JsonSerializable, Identifiable, Documented {

	/**
	 * @return identifier of attribute
	 */
	public abstract Identifier identifier();

	/**
	 * @return short screen name of attribute that can be displayed to users
	 */
	public abstract String caption();

	/**
	 * 
	 * @return extended screen description of attribute that can be displayed to
	 *         users in order to provide detailed information
	 */
	public abstract String description();

	/**
	 * Checks if value for an object is present or missing. Only indices of
	 * non-missing objects can be used as argument for getValue
	 * 
	 * @see Attribute#value
	 * @param objectIndex
	 * @return whether value for object is missing
	 */
	public abstract boolean valueMissing(int objectIndex);

	/**
	 * Returns the value corresponding to the object with the specified index if
	 * that value is non-missing or otherwise throws an Exception.
	 * 
	 * @see Attribute
	 * @param objectIndex
	 * @return the value for an object associated with objectIndex if
	 *         non-missing
	 * @throws IllegalArgumentException
	 *             if value is missing
	 */
	public abstract T value(int objectIndex);

	/**
	 * Provides save access to the value for an object as optional. The optional
	 * is empty if the value is missing for the requested object and contain the
	 * object value otherwise.
	 * 
	 * @since 0.2.2
	 * @param objectIndex
	 * @return optional of value for index that is empty if value is missing
	 */
	public default Optional<T> getValueOption(int objectIndex) {
		return (valueMissing(objectIndex) ? Optional.empty() : Optional.of(value(objectIndex)));
	}

	/**
	 * Provides complete list of optional values for all data objects. The
	 * optionals are empty if the value is missing for the corresponding object.
	 * 
	 * @since 0.2.2
	 * @return list of value options for all object
	 */
	@JsonIgnore
	public default List<Optional<T>> getValues() {
		List<Optional<T>> result = new ArrayList<>(maxIndex() + 1);
		for (int i = 0; i < maxIndex() + 1; i++) {
			result.add(getValueOption(i));
		}
		return result;
	}

	/**
	 * Returns the bag of all values of indices that are non-missing. May
	 * contain duplicate values.
	 * 
	 * @return bag of all non-missing values
	 */
	public abstract Collection<? extends T> nonMissingValues();

	/**
	 * Returns number of indices between 0 and maximal index for which values
	 * are non-missing
	 * 
	 * @return number of non-missing values
	 */
	public abstract int numberOfNonMissingValues();

	/**
	 * Returns the maximum integer index that can be accepted as an argument for
	 * isMissing (and getValue if not missing).
	 * 
	 * @see Attribute#valueMissing(int)
	 * @see Attribute#value(int)
	 * @return maximum object index
	 * 
	 */
	public abstract int maxIndex();

	/**
	 * @return The row ids of rows where this attribute has/had a missing value
	 */
	public abstract IndexSet missingPositions();

	public abstract Class<? extends T> type();

}