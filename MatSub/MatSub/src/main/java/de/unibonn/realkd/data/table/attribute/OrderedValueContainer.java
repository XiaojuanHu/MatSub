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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.util.Search;

/**
 * Internal data structure for a list of ordered (and possibly missing) values.
 * Not for use outside of the current attributes implementations.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
final class OrderedValueContainer<T> {

	private final Comparator<T> comparator;

	private final T min, max, median;

	private final List<Integer> sortedIndices;

	private final T lowerQuartile;

	private final T upperQuartile;

	private final List<T> values;

	public OrderedValueContainer(List<T> values, Comparator<T> comparator) {
		this.values = values;
		this.comparator = comparator;
		List<Integer> sortedNonMissingRowIndices = new ArrayList<>(values.size());
		List<T> nonMissingValues = new ArrayList<>(values.size());

		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) != null) {
				sortedNonMissingRowIndices.add(i);
				nonMissingValues.add(values.get(i));
			}
		}
		Collections.sort(sortedNonMissingRowIndices, (i, j) -> comparator.compare(values.get(i), values.get(j)));

		min = Collections.min(nonMissingValues, comparator);
		max = Collections.max(nonMissingValues, comparator);

		this.sortedIndices = sortedNonMissingRowIndices;

		{
			int lowerHalfPosition = sortedNonMissingRowIndices.size() / 2;
			int medianIdx = (sortedNonMissingRowIndices.size() % 2 == 0) ? lowerHalfPosition - 1 : lowerHalfPosition;
			median = values.get(sortedNonMissingRowIndices.get(medianIdx));
		}

		{
			int lowerQuarterPosition = sortedNonMissingRowIndices.size() / 4;

			int lowerQuarterIdx = (sortedNonMissingRowIndices.size() % 4 == 0) ? lowerQuarterPosition - 1
					: lowerQuarterPosition;
			lowerQuartile = values.get(sortedNonMissingRowIndices.get(lowerQuarterIdx));
		}

		{
			int upperQuarterPosition = 3 * sortedNonMissingRowIndices.size() / 4;
			int upperQuarterIdx = (3 * sortedNonMissingRowIndices.size() % 4 == 0) ? upperQuarterPosition - 1
					: upperQuarterPosition;
			upperQuartile = values.get(sortedNonMissingRowIndices.get(upperQuarterIdx));
		}
	}

	public T median() {
		return median;
	}

	public T max() {
		return max;
	}

	public T min() {
		return min;
	}

	public T quantile(double frac) {
		int n = (int) Math.ceil(frac * sortedIndices.size() - 1);
		return values.get(sortedIndices.get(n));
	}

	public List<Integer> sortedNonMissingRowIndices() {
		return sortedIndices;
	}

	public T lowerQuartile() {
		return lowerQuartile;
	}

	public T upperQuartile() {
		return upperQuartile;
	}

	public Optional<T> getMedianOnRows(IndexSet rowSet) {
		T median;
		List<T> nonMissingRowValues = new ArrayList<>();

		for (int rowIndex : rowSet) {
			if (values.get(rowIndex) == null) {
				continue;
			}
			nonMissingRowValues.add(values.get(rowIndex));
		}

		if (nonMissingRowValues.isEmpty()) {
			/*
			 * TODO previously here was Double.NaN, what to return with type
			 * String?
			 * 
			 * will create requirement that at least one value of every
			 * attribute has to be non-missing
			 * 
			 * perhaps better let this return an optional
			 * 
			 */
			return Optional.empty();
		}

		Collections.sort(nonMissingRowValues, comparator);
		if (nonMissingRowValues.size() % 2 == 0) {
			// TODO: do we need this case distinction here (no average can be
			// formed for general ordinal attribute)?
			int idx = nonMissingRowValues.size() / 2 - 1;
			median = (nonMissingRowValues.get(idx));
		} else {
			int idx = nonMissingRowValues.size() / 2;
			median = nonMissingRowValues.get(idx);
		}
		return Optional.of(median);
	}

	public Comparator<T> comparator() {
		return comparator;
	}

	public int orderNumber(T value) {
		if (comparator.compare(value, min) < 0) {
			return 0;
		}
		if (comparator.compare(value, max) >= 0) {
			return sortedIndices.size();
		}
		IntPredicate property = i -> comparator.compare(values.get(sortedIndices.get(i)), value) > 0;
		return Search.findSmallest(0, sortedIndices.size() - 1, property);
	}

	public int inverseOrderNumber(T value) {
		if (comparator.compare(value, min) < 0) {
			return sortedIndices.size();
		}
		if (comparator.compare(value, max) >= 0) {
			return 0;
		}
		// ordered value for index larger or equal than value
		IntPredicate property = i -> comparator.compare(values.get(sortedIndices.get(i)), value) >= 0;
		return sortedIndices.size() - Search.findSmallest(0, sortedIndices.size() - 1, property);
	}

	public int orderNumberOnRows(T value, Set<Integer> rows) {
		if (comparator.compare(value, min) < 0) {
			return 0;
		}
		if (comparator.compare(value, max) >= 0) {
			return rows.size();
		}
		int globalOrderNumber = orderNumber(value);

		/*
		 * check what way is faster to count row elements with value less then
		 * threshold: iterating over global ordered collection up to order
		 * number or filter row elements.
		 */
		if (globalOrderNumber < rows.size()) {
			IntStream lowerOrderInRows = IntStream.range(0, globalOrderNumber)
					.filter(i -> rows.contains(sortedIndices.get(i)));
			return (int) lowerOrderInRows.count();
		} else {
			Stream<Integer> rowsWithValueAtMostQueryValue = rows.stream()
					.filter(i -> (values.get(i) != null && comparator.compare(values.get(i), value) <= 0));
			return (int) rowsWithValueAtMostQueryValue.count();
		}
	}

}
