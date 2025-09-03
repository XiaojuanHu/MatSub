/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.realkd.common.IndexSet;

/**
 * Attribute with finite set of possible values, for which it makes sense to
 * provide collection of all distinct values occurring in data and exact
 * empirical value frequencies.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.3.0
 *
 */
public interface CategoricAttribute<T> extends Attribute<T> {

	public abstract List<T> categories();

	public abstract List<Double> categoryFrequencies();

	public default List<Double> getCategoryFrequenciesOnRows(IndexSet rowSet) {
		Map<T, Double> categoryToFrequency = new HashMap<>();
		int numberOfNonMissingValues = 0;
		for (Integer row : rowSet) {
			if (valueMissing(row)) {
				continue;
			}
			T value = value(row);
			if (categoryToFrequency.containsKey(value)) {
				double newCount = categoryToFrequency.get(value) + 1.0;
				categoryToFrequency.put(value, newCount);
			} else {
				categoryToFrequency.put(value, 1.0);
			}
			numberOfNonMissingValues++;
		}

		List<Double> result = new ArrayList<>(categories().size());
		for (int i = 0; i < categories().size(); i++) {
			if (categoryToFrequency.containsKey(categories().get(i))) {
				result.add(categoryToFrequency.get(categories().get(i)) / (double) numberOfNonMissingValues);
			} else {
				result.add(0.0);
			}
		}

		return result;
	}

}