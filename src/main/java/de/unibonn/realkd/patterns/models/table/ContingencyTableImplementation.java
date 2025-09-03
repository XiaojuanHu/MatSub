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

import static com.google.common.collect.Sets.union;
import static de.unibonn.realkd.common.base.Lazy.lazy;
import static de.unibonn.realkd.patterns.models.table.ContingencyTables.contingencyTable;
import static de.unibonn.realkd.patterns.models.table.ShannonEntropy.ENTROPY;
import static java.lang.Double.NaN;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.base.Lazy;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.patterns.models.ModelParameter;
import de.unibonn.realkd.patterns.models.ProbabilisticModel;
import de.unibonn.realkd.util.InformationTheory;

/**
 * 
 * @author Sandy Moens
 * 
 * @author Mario Boley
 * 
 * @since 0.0.1
 * 
 * @version 0.7.2
 *
 */
public class ContingencyTableImplementation implements ContingencyTable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ContingencyTableImplementation.class.getName());

	private final CountTable table;

	private final List<Dimension> dimensions;

	private final List<ContingencyTableParameter> parameters;

	private final Lazy<Double> entropy;

	private final Lazy<? extends List<Measurement>> measurements;

	ContingencyTableImplementation(List<Dimension> dimensions, CountTable countTable,
			List<ContingencyTableParameter> parameters) {
		this.dimensions = dimensions;
		this.table = countTable;
		this.parameters = parameters;
		this.entropy = lazy(() -> computeEntropy());
		this.measurements = lazy(() -> ImmutableList.of(Measures.measurement(ENTROPY, entropy())));
	}

	private double computeEntropy() {
		DoubleStream probabilities = table.nonZeroCells().stream().mapToDouble(key -> probability(key));
		return InformationTheory.entropy(probabilities);
	}

	@Override
	public ContingencyTable marginal(int... dims) {
		sort(dims);
		List<Dimension> projectedDimensions = stream(dims).mapToObj(d -> dimensions.get(d)).collect(toList());
		CountTable projectTable = table.projection(dims);
		return contingencyTable(projectedDimensions, projectTable);
	}

	public ContingencyTable merge(int dim, int from, int to) {
		if (from >= to) {
			return this;
		}
		CountTable mergedTable = table.merge(dim, IntStream.rangeClosed(from, to).toArray());
		List<Dimension> newDims = range(0, dimensions.size())
				.mapToObj(i -> (i == dim) ? dimension(i).merge(from, to) : dimension(i)).collect(toList());
		return contingencyTable(newDims, mergedTable);
	}

	/**
	 * Returns a contingency table representing the conditional distribution of the
	 * sub-population whose values of a given dimension fall into one of a number of
	 * given bins.
	 * 
	 */
	public ContingencyTable conditional(int dim, int... bins) {
		List<Dimension> newDims = range(0, dimensions.size())
				.mapToObj(i -> (i == dim) ? dimension(i).condition(bins) : dimension(i)).collect(toList());
		return contingencyTable(newDims, table.conditional(dim, bins));
	}

	@Override
	public double probabilityOfBins(List<Integer> bins) {
		Cell cell = new Cell(bins);
		return probability(cell);
	}

	@Override
	public double probabilityOfValues(List<? extends Object> values) {
		return probability(cell(values));
	}

	@Override
	public double probability(Cell key) {
		return table.count(key) / (double) table.total();
	}

	@Override
	public int count(Cell cell) {
		return table.count(cell);
	}

	@Override
	public Cell cell(List<? extends Object> values) {
		List<Integer> bins = IntStream.range(0, values.size()).mapToObj(i -> dimensions.get(i).bin(values.get(i)))
				.collect(Collectors.toList());
		return new Cell(bins);
	}

	@Override
	public double totalVariationDistance(ProbabilisticModel q) {
		if (q instanceof ContingencyTable) {
			return totalVariationDistance((ContingencyTable) q);
		}
		return NaN;
	}

	@Override
	public double hellingerDistance(ProbabilisticModel q) {
		if (q instanceof ContingencyTable) {
			return hellingerDistance((ContingencyTable) q);
		}
		return NaN;
	}

	public double totalVariationDistance(ContingencyTable q) {
		double absoluteDifference = 0.0;
		for (Cell key : union(this.nonZeroCells(), q.nonZeroCells())) {
			absoluteDifference += Math.abs(this.probability(key) - q.probability(key));
		}
		return absoluteDifference / 2.0;
	}

	public double hellingerDistance(ContingencyTable q) {
		Stream<Cell> keys = Sets.union(this.nonZeroCells(), q.nonZeroCells()).stream();
		Stream<Double> squaredDiffOfRoots = keys.map(k -> pow(sqrt(this.probability(k)) - sqrt(q.probability(k)), 2));
		Double sum = squaredDiffOfRoots.reduce(0.0, (x, y) -> x + y);
		return sqrt(sum) / sqrt(2);
	}

	@Override
	public Set<Cell> nonZeroCells() {
		return table.nonZeroCells();
	}

	@Override
	public String toString() {
		return "ContingencyTable(" + table + ")";
	}

	@Override
	public Optional<Double> value(ModelParameter parameter) {
		if (!(parameter instanceof ContingencyTableParameter)) {
			return Optional.empty();
		}
		return Optional.of(probability(((ContingencyTableParameter) parameter).key()));
	}

	@Override
	public Collection<ContingencyTableParameter> parameters() {
		return parameters;
	}

	@Override
	public List<Dimension> dimensions() {
		return dimensions;
	}

	@Override
	public List<Measurement> measurements() {
		return measurements.get();
	}

	@Override
	public double entropy() {
		return entropy.get();
	}

	@Override
	public int totalCount() {
		return table.total();
	}

	@Override
	public int domainSize() {
		return table.nonZeroCells().size();
	}

}