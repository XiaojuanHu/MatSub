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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class Cell {

	private List<Integer> key;

	public Cell(List<Integer> key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Cell)) {
			return false;
		}
		return this.hashCode() == other.hashCode() && this.key.equals(((Cell) other).key);
	}

	public List<Integer> key() {
		return key;
	}

	public Cell projection(int... dims) {
		List<Integer> projectedKey = stream(dims).mapToObj(d -> key.get(d)).collect(toList());
		return new Cell(projectedKey);
	}

	public Cell projection(int dim, IntUnaryOperator newBinIndex) {
		List<Integer> projectedKey = new ArrayList<>();
		for (int i = 0; i < key.size(); i++) {
			if (i == dim) {
				projectedKey.add(newBinIndex.applyAsInt(key.get(i)));
			} else {
				projectedKey.add(key.get(i));
			}
		}

		return new Cell(projectedKey);
	}

	@Override
	public String toString() {
		return key.toString();
	}

}
