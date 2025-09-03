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

package de.unibonn.realkd.patterns.models.table;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import de.unibonn.realkd.util.Arrays;

public class CategoricalDimension implements Dimension {

	private final Map<? extends Object, Integer> bins;

	private final SortedMap<Integer, String> captions;

	CategoricalDimension(Map<? extends Object, Integer> bins) {
		this.bins = bins;
		this.captions = new TreeMap<>();
		for (Entry<? extends Object, Integer> entry : bins.entrySet()) {
			captions.merge(entry.getValue(), entry.getKey().toString(), (c, a) -> c.concat("+" + a));
		}
	}

	@Override
	public int bin(Object value) {
		return bins.get(value);
	}

	@Override
	public List<String> binCaptions() {
		return captions.values().stream().collect(toList());
	}

	@Override
	public int numberOfBins() {
		return bins.values().size();
	}

	private static int binAfterMerge(int i, int from, int to) {
		if (i < from) {
			return i;
		} else if (i > to) {
			return i - (to - from);
		} else {
			return from;
		}
	}

	@Override
	public Dimension merge(int from, int to) {
		Map<? extends Object, Integer> newBins = bins.entrySet().stream()
				.collect(toMap(e -> e.getKey(), e -> binAfterMerge(e.getValue(), from, to)));
		return new CategoricalDimension(newBins);
	}

	@Override
	public Dimension condition(int[] binsToCondition) {
		Map<? extends Object, Integer> newBins = bins.entrySet().stream()
				.filter(i -> Arrays.containedIn((int) i.getKey() - 1, binsToCondition))
				.collect(toMap(e -> e.getKey(), e -> e.getValue()));

		return new CategoricalDimension(newBins);
	}

}
