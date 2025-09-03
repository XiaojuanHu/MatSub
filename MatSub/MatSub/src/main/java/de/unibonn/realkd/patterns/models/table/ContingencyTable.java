/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.models.table;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.unibonn.realkd.patterns.models.ProbabilisticModel;
import de.unibonn.realkd.util.Combinatorics;

/**
 * 
 * @author Sandy Moens
 * 
 * @author Mario Boley
 * 
 * @since 0.0.1
 * 
 * @version 0.5.1
 * 
 */
public interface ContingencyTable extends ProbabilisticModel {

	public Cell cell(List<? extends Object> values);

	public List<Dimension> dimensions();

	public default Dimension dimension(int index) {
		return dimensions().get(index);
	}

	public double entropy();

	public Set<Cell> nonZeroCells();

	public int domainSize();

	public int count(Cell cell);

	public default int countOfValues(List<? extends Object> values) {
		return count(cell(values));
	}

	public int totalCount();

	public double probability(Cell key);

	public double probabilityOfValues(List<? extends Object> values);

	public double probabilityOfBins(List<Integer> bins);

	/**
	 * Computes the contingency table representing the marginal distribution of the
	 * given dimension indices.
	 * 
	 */
	public ContingencyTable marginal(int... dims);

	/**
	 * Number of pairs of atomic events with identical values for some given
	 * disambiguation dimensions that have a distinct value w.r.t. to the other
	 * dimensions.
	 * 
	 * @param disambiguationIndices
	 *            the dimension indices for disambiguation
	 * @return number of ambiguous event pairs
	 */
	public default int ambiguityCount(int[] disambiguationIndices) {
		Multimap<Cell, Integer> cellCounts = ArrayListMultimap.create();
		nonZeroCells().forEach(c -> cellCounts.put(c.projection(disambiguationIndices), count(c)));
		int uncoveredEdgeCount = cellCounts.keySet().stream()
				.mapToInt(k -> Combinatorics.kPartiteEdgeCount(cellCounts.get(k))).sum();
		return uncoveredEdgeCount;
	}

	public ContingencyTable conditional(int dim, int... bins);

	public ContingencyTable merge(int dim, int from, int to);

}
