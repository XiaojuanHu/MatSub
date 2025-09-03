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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;

/**
 * 
 * Comparator based an ordered list of strings. The result of the comparison is
 * the difference between the indices of the compared object in the list. A
 * negative result means that the first element provided comes before the second
 * element in the list.
 * 
 * @author Bjoern Jacobs
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 * 
 */
@KdonTypeName("finiteOrder")
public class FiniteOrder implements Comparator<String> {

	@JsonProperty("order")
	private final String[] orderedValueList;

	private final HashMap<String, Integer> indexOfValue;

	public FiniteOrder(@JsonProperty("order") List<String> orderedValueList) {
		this(orderedValueList.stream().toArray(i -> new String[i]));
	}

	@JsonCreator
	public FiniteOrder(@JsonProperty("order") String[] orderedValueList) {
		this.orderedValueList = orderedValueList;

		indexOfValue = new HashMap<>(orderedValueList.length);
		for (int i = 0; i < orderedValueList.length; i++) {
			indexOfValue.put(orderedValueList[i], i);
		}
	}

	/**
	 * @return &lt; 0 if first comes before second; 0 if both elements are the
	 *         same; &gt; 0 if second comes before first.
	 */
	@Override
	public int compare(String o1, String o2) {
		return indexOfValue.get(o1).compareTo(indexOfValue.get(o2));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FiniteOrder)) {
			return false;
		}
		FiniteOrder that = (FiniteOrder) other;
		return Arrays.equals(this.orderedValueList, that.orderedValueList);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(orderedValueList);
	}

}
