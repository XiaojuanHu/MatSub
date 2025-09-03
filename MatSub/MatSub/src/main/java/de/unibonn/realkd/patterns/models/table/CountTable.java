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

import static com.google.common.collect.Maps.newHashMap;
import static de.unibonn.realkd.util.Arrays.containedIn;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CountTable {

	private int totalCount;
	public Map<Cell, AtomicInteger> table;

	public CountTable() {
		table = newHashMap();
		totalCount = 0;
	}

	private CountTable(Map<Cell, AtomicInteger> table) {
		this.table = table;
		this.totalCount = table.values().stream().mapToInt(c -> c.intValue()).sum();
	}

	public Set<Cell> nonZeroCells() {
		return table.keySet();
	}

	public void incrementCount(Cell cell, int amount) {
		AtomicInteger value = table.get(cell);
		if (value == null) {
			value = new AtomicInteger(amount);
			table.put(cell, value);
		} else {
			value.addAndGet(amount);
		}
		totalCount = totalCount + amount;
	}

	public void incrementCount(Cell cell) {
		incrementCount(cell, 1);
	}

	public int count(Cell key) {
		AtomicInteger value = table.get(key);
		if (value == null) {
			return 0;
		}

		return value.get();
	}

	public int total() {
		return totalCount;
	}

	/*
	 * public CountTable projection(int... dims) { CountTable projectTable = new
	 * CountTable(); for (Cell cell : nonZeroCells()) {
	 * projectTable.incrementCount(cell.projection(dims), table.get(cell).get()); }
	 * return projectTable; }
	 */

	/**
	 * 
	 * Returns a count table with cells being altered according to a function
	 * 
	 */
	public CountTable map(Function<Cell, Cell> cellMap) {
		CountTable result = new CountTable();
		for (Cell cell : nonZeroCells()) {
			result.incrementCount(cellMap.apply(cell), table.get(cell).get());
		}
		return result;
	}

	/**
	 * 
	 * Returns a count table where cells have been filtered according to the
	 * predicate
	 * 
	 */
	public CountTable filter(Predicate<Cell> condition) {
		Map<Cell, AtomicInteger> filteredTable = table.entrySet().stream().filter(e -> condition.test(e.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		return new CountTable(filteredTable);
	}

	/**
	 * 
	 * Returns a count table conditioned on bins of a dimension
	 */
	public CountTable conditional(int dim, int... bins) {
		Predicate<Cell> condition = c -> containedIn(c.key().get(dim), bins);
		return filter(condition);
	}

	/**
	 * Returns a new table with down-projected cells
	 * 
	 */
	public CountTable projection(int... dims) {
		Function<Cell, Cell> func = cell -> cell.projection(dims);
		return this.map(func);
	}

	/**
	 * 
	 * Returns a count table where bins of a dimension have been merged, and keys
	 * have been renamed
	 */
	public CountTable merge(int dim, int[] bins) {
		IntSummaryStatistics stat = Arrays.stream(bins).summaryStatistics();

		int k = stat.getMin();
		IntUnaryOperator newBinIndex = new IntUnaryOperator() {
			@Override
			public int applyAsInt(int i) {
				boolean isMerged = containedIn(i, bins);
				if (isMerged) {
					return k;
				} else if (i < k) {

					return i;
				} else {
					return (int) (i + 1 - IntStream.of(bins).filter(j -> j < i).count());
				}

			}

		};

		return this.map(cell -> cell.projection(dim, newBinIndex));

	}

}
