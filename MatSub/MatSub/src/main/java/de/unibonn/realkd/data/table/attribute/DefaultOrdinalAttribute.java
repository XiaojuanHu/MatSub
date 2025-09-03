/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.table.attribute;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
final class DefaultOrdinalAttribute<T> extends DefaultAttribute<T> implements OrdinalAttribute<T> {

	private final OrderedValueContainer<T> orderedValueContainer;

	@JsonCreator
	DefaultOrdinalAttribute(@JsonProperty("identifier") Identifier identifier, @JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("values") List<T> values, @JsonProperty("comparator") Comparator<T> comparator,
			@JsonProperty("type") Class<T> type) {
		super(identifier, name, description, values, type);
		this.orderedValueContainer = new OrderedValueContainer<>(values, comparator);
	}

	@Override
	public T median() {
		return orderedValueContainer.median();
	}

	@Override
	public T max() {
		return orderedValueContainer.max();
	}

	@Override
	public T min() {
		return orderedValueContainer.min();
	}

	@Override
	public T quantile(double frac) {
		return orderedValueContainer.quantile(frac);
	}

	@Override
	public int orderNumber(T value) {
		return orderedValueContainer.orderNumber(value);
	}

	@Override
	public int orderNumberOnRows(T value, Set<Integer> rows) {
		return orderedValueContainer.orderNumberOnRows(value, rows);
	}

	@Override
	public List<Integer> sortedNonMissingRowIndices() {
		return orderedValueContainer.sortedNonMissingRowIndices();
	}

	@Override
	public T lowerQuartile() {
		return orderedValueContainer.lowerQuartile();
	}

	@Override
	public T upperQuartile() {
		return orderedValueContainer.upperQuartile();
	}

	@Override
	public T medianOnRows(IndexSet rowSet) {
		// TODO change to optional of T
		return orderedValueContainer.getMedianOnRows(rowSet).orElse(null);
	}

	@Override
	@JsonProperty("comparator")
	public Comparator<T> valueComparator() {
		return orderedValueContainer.comparator();
	}

	@Override
	public int inverseOrderNumber(T value) {
		return orderedValueContainer.inverseOrderNumber(value);
	}

}
