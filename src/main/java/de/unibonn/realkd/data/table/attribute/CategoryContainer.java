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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for holding occurrence frequencies of values in a collection.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 * @param <T>
 *            the type of values
 */
class CategoryContainer<T> {

	private final List<T> categories;

	private final List<Double> categoryFrequencies;

	/**
	 * 
	 * @param values
	 *            collection of non-null (non-missing) values
	 */
	public CategoryContainer(Collection<T> values) {
		categories = new ArrayList<>();
		categoryFrequencies = new ArrayList<>();
		Map<T, Double> categoryToFrequency = new HashMap<>();
		for (T value : values) {
			if (categoryToFrequency.containsKey(value)) {
				double newCount = categoryToFrequency.get(value) + 1.0;
				categoryToFrequency.put(value, newCount);
			} else {
				categoryToFrequency.put(value, 1.0);
			}
		}
		for (T category : categoryToFrequency.keySet()) {
			getCategories().add(category);
			getCategoryFrequencies().add(categoryToFrequency.get(category) / values.size());
		}

	}

	public List<T> getCategories() {
		return categories;
	}

	public List<Double> getCategoryFrequencies() {
		return categoryFrequencies;
	}

}