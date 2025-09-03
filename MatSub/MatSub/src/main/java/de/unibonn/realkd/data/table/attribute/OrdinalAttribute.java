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

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.unibonn.realkd.common.IndexSet;

/**
 * Attribute with an associated order for its possible values.
 * 
 * @author Bjoern Jacobs
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 * 
 */
public interface OrdinalAttribute<T> extends Attribute<T> {

	public T median();

	public T max();

	public T min();

	/**
	 * 
	 * @param frac
	 *            double from the half-open unit interval (0,1]
	 * @return the smallest value q such that at least frac objects have
	 *         attribute value q or less
	 */
	public T quantile(double frac);

	/**
	 * Determines the number of objects that have attribute value less than or
	 * equal to a query-value. This corresponds to the max integer i such that
	 * element i-1 of the sorted non-missing row-indices has an attribute value
	 * of less or equal than value (or 0 if no object has attribute value at
	 * least the query value).
	 * 
	 * @param value
	 *            query value
	 * 
	 * @return number of objects having attribute value less or equal to query
	 *         value
	 */
	public int orderNumber(T value);

	public int inverseOrderNumber(T value);

	/**
	 * Like {@link #orderNumber}, but restricted to subset of objects.
	 * 
	 * @param value
	 *            query value
	 * @param rows
	 *            subset of rows on which to count objects
	 * @return number of objects among rows having attribute values less or
	 *         equal to query value
	 */
	public int orderNumberOnRows(T value, Set<Integer> rows);

	/**
	 * 
	 * @return indices ordered increasingly with respect to corresponding values
	 */
	public List<Integer> sortedNonMissingRowIndices();

	/**
	 * 
	 * @return list of values according to order
	 */
	public default List<T> nonMissingValuesInOrder() {
		return sortedNonMissingRowIndices().stream().map(r -> value(r)).collect(toList());
	}

	/**
	 * 
	 * @return list of distinct values according to order
	 */
	public default List<T> distinctNonMissingValuesInOrder() {
		return sortedNonMissingRowIndices().stream().map(r -> value(r)).distinct().collect(toList());
	}

	public T lowerQuartile();

	public T upperQuartile();

	public T medianOnRows(IndexSet rowSet);

	public Comparator<T> valueComparator();

	public default int compare(int i1, int i2) {
		return valueComparator().compare(value(i1), value(i2));
	}

	@SuppressWarnings("unchecked")
	public default boolean lessOrEqual(Object v1, Object v2) {
		Class<? extends T> type = type();
		if (!(type.isInstance(v1)) || !(type.isInstance(v2))) {
			return false;
		}
		return valueComparator().compare((T) v1, (T) v2) <= 0;
	}

}
