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

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.util.InformationTheory;

/**
 * Factory methods for constructing contingency tables and their components.
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.7.2
 *
 */
public class ContingencyTables {

	private static final Logger LOGGER = Logger.getLogger(ContingencyTables.class.getName());

	private ContingencyTables() {
		;
	}

	public static ContingencyTable contingencyTable(DataTable dataTable, Attribute<?> attribute, IndexSet rows) {
		return contingencyTable(dataTable, ImmutableList.of(attribute), rows);
	}

	public static ContingencyTable contingencyTable(DataTable dataTable, List<? extends Attribute<?>> attributes) {
		return contingencyTable(dataTable, attributes, dataTable.population().objectIds());
	}

	public static TwoDimensionalContingencyTable contingencyTable(DataTable dataTable, Attribute<?> attr1,
			Attribute<?> attr2) {
		Dimension dim1 = dimension(attr1);
		Dimension dim2 = dimension(attr2);
		CountTable countTable = countTable(dataTable, ImmutableList.of(attr1, attr2),
				dataTable.population().objectIds(), ImmutableList.of(dim1, dim2));
		return contingencyTable(dim1, dim2, countTable);
	}

	public static ContingencyTable contingencyTable(DataTable dataTable, List<? extends Attribute<?>> attributes,
			IndexSet rows) {
		List<Dimension> dimensions = dimensions(attributes);
		CountTable countTable = countTable(dataTable, attributes, rows, dimensions);
		return contingencyTable(dimensions, countTable);
	}

	public static ContingencyTable contingencyTable(List<Dimension> dimensions, CountTable countTable) {
		if (dimensions.size() == 2) {
			return contingencyTable(dimensions.get(0), dimensions.get(1), countTable);
		}
		List<ContingencyTableParameter> params = parameters(dimensions, countTable);
		return new ContingencyTableImplementation(dimensions, countTable, params);
	}

	public static TwoDimensionalContingencyTable contingencyTable(Dimension dim1, Dimension dim2,
			CountTable countTable) {
		List<ContingencyTableParameter> params = parameters(ImmutableList.of(dim1, dim2), countTable);
		return new TwoDimensionalContingencyTableImplementation(dim1, dim2, countTable, params);
	}

	private static List<ContingencyTableParameter> parameters(List<Dimension> dimensions, CountTable countTable) {
		return countTable.nonZeroCells().stream().map(k -> new ContingencyTableParameter(k, label(k, dimensions)))
				.collect(toList());
	}

	public static CountTable countTable(DataTable dataTable, List<? extends Attribute<?>> attributes, IndexSet rows,
			List<Dimension> dimensions) {
		LOGGER.finer("constructing count table on " + rows.size() + " rows for attributes: " + attributes);
		CountTable table = new CountTable();
		for (int row : rows) {
			/*
			 * TODO: actually, marginal probabilities of non missing values should still
			 * increase; is there a solution for this? -Mario
			 */
			if (dataTable.atLeastOneAttributeValueMissingFor(row, attributes)) {
				continue;
			}

			table.incrementCount(cell(attributes, row, dimensions));
		}

		return table;
	}

	/**
	 * 
	 * @param row with present values for all attributes
	 * @return cell in the contingency table that corresponds to combination of all
	 *         attribute values in row
	 */
	private static Cell cell(List<? extends Attribute<?>> attributes, int row, List<Dimension> dimensions) {
		List<Integer> key = newArrayListWithCapacity(attributes.size());
		Iterator<Dimension> itKC = dimensions.iterator();

		for (Attribute<?> attribute : attributes) {
			key.add(itKC.next().bin(attribute.value(row)));
		}

		return new Cell(key);
	}

	public static List<Dimension> dimensions(List<? extends Attribute<?>> attributes) {
		List<Dimension> result = newArrayListWithCapacity(attributes.size());
		for (Attribute<?> attribute : attributes) {
			Dimension dimension = dimension(attribute);
			if (dimension != null) {
				result.add(dimension);
			}
		}
		return result;
	}

	public static <T> Dimension dimension(Attribute<T> attribute) {
		if (attribute instanceof CategoricAttribute) {
			return ContingencyTables.categoricalDimension(((CategoricAttribute<T>) attribute).categories());
		}

		if (attribute instanceof OrdinalAttribute) {
			return binaryOrdinalDimension((OrdinalAttribute<T>) attribute);
		}
		throw new UnsupportedOperationException(
				"no default dimension for attributes that are neither categoric nor ordinal");
	}

	public static <T> BinaryOrdinalDimension<T> binaryOrdinalDimension(OrdinalAttribute<T> attribute) {
		return new BinaryOrdinalDimension<T>(attribute.valueComparator(), attribute.median());
	}

	public static String label(Cell cell, List<Dimension> dimensions) {
		return range(0, cell.key().size()).mapToObj(i -> dimensions.get(i).binCaptions().get(cell.key().get(i)))
				.collect(joining(",", "(", ")"));
	}

	public static CategoricalDimension categoricalDimension(List<? extends Object> categories) {
		Map<? extends Object, Integer> bins = range(0, categories.size()).boxed()
				.collect(Collectors.toMap(i -> categories.get(i), i -> i));
		return new CategoricalDimension(bins);
	}

	public static double mutualInformation(ContingencyTable jointAB, ContingencyTable marginalA,
			ContingencyTable marginalB) {
		return InformationTheory.mutualInformation(marginalA.entropy(), marginalB.entropy(), jointAB.entropy());
	}

	public static double expectedMutualInformationUnderPermutationModel(ContingencyTable firstCTable,
			ContingencyTable secondCTable) {
		double sum = 0;
		int totalCount = firstCTable.totalCount();

		for (Cell firstDimCell : firstCTable.nonZeroCells()) {
			int numPointsCellFirstTable = firstCTable.count(firstDimCell);

			for (Cell secondDimCell : secondCTable.nonZeroCells()) {
				int numPointsCellSecondTable = secondCTable.count(secondDimCell);

				int min = Math.max(1, numPointsCellFirstTable + numPointsCellSecondTable - totalCount);
				int max = Math.min(numPointsCellFirstTable, numPointsCellSecondTable);

				// use the hypergeometric distrbiution for first probability
				double iterProb = new HypergeometricDistribution(totalCount, numPointsCellFirstTable,
						numPointsCellSecondTable).probability(min);

				double cellContribution = multiplyNumbers(
						cellMICalculation(min, totalCount, numPointsCellFirstTable, numPointsCellSecondTable),
						iterProb);

				// use recurrence relation of hypergeometric for next
				// probabilities
				for (int i = min + 1; i <= max; i++) {
					double temp1 = multiplyNumbers(iterProb,
							(numPointsCellFirstTable - (i - 1)) * (numPointsCellSecondTable - (i - 1)));
					double temp2 = ((i) * (totalCount - numPointsCellFirstTable - numPointsCellSecondTable + i));
					iterProb = temp1 / temp2;
					cellContribution += multiplyNumbers(
							cellMICalculation(i, totalCount, numPointsCellFirstTable, numPointsCellSecondTable),
							iterProb);
				}
				sum += cellContribution;
			}
		}
		return sum;
	}

//	public static double parallelExpectedMutualInformationUnderPermutationModel(ContingencyTable firstCTable,
//			ContingencyTable secondCTable) {
//		double sum = 0;
//		int totalCount = firstCTable.totalCount();
//		List<ExpectedUtil> listOfPairs = new ArrayList<>();
//
//		for (Cell firstDimCell : firstCTable.nonZeroCells()) {
//			int numPointsCellFirstTable = firstCTable.count(firstDimCell);
//			for (Cell secondDimCell : secondCTable.nonZeroCells()) {
//				int numPointsCellSecondTable = secondCTable.count(secondDimCell);
//				listOfPairs.add(new ExpectedUtil(numPointsCellFirstTable, numPointsCellSecondTable));
//			}
//		}
//		sum = listOfPairs.parallelStream().mapToDouble(n -> n.cellContribution(totalCount)).sum();
//		return sum;
//	}

	public static double parallelExpectedMutualInformationUnderPermutationModel(ContingencyTable firstCTable,
			ContingencyTable secondCTable) {
		int totalCount = firstCTable.totalCount();
		DoubleAdder adder = new DoubleAdder();
		firstCTable.nonZeroCells().parallelStream()
				.forEach(a -> secondCTable.nonZeroCells().parallelStream()
						.mapToDouble(b -> cellContribution(totalCount, firstCTable.count(a), secondCTable.count(b)))
						.forEach(adder::add));
		return adder.sum();
	}

	public static double expectedMutualInformationUpperBoundUnderPermutationModel(ContingencyTable firstCTable,
			ContingencyTable secondCTable) {
		int totalCount = firstCTable.totalCount();
		int domainSizeFirst = firstCTable.nonZeroCells().size();
		int domainSizeSecond = secondCTable.nonZeroCells().size();

		return Math.log((double) (totalCount + domainSizeFirst * domainSizeSecond - domainSizeFirst - domainSizeSecond)
				/ (totalCount - 1)) / Math.log(2);
	}

	private static double cellContribution(int totalCount, int firstMarginal, int secondMarginal) {
		int min = Math.max(1, firstMarginal + secondMarginal - totalCount);
		int max = Math.min(firstMarginal, secondMarginal);

		// use the hypergeometric distrbiution for first probability
		double iterProb = new HypergeometricDistribution(totalCount, firstMarginal, secondMarginal).probability(min);

		double cellContribution = multiplyNumbers(cellMICalculation(min, totalCount, firstMarginal, secondMarginal),
				iterProb);

		// use recurrence relation of hypergeometric for next
		// probabilities
		for (int i = min + 1; i <= max; i++) {
			double temp1 = multiplyNumbers(iterProb, (firstMarginal - (i - 1)) * (secondMarginal - (i - 1)));
			double temp2 = ((i) * (totalCount - firstMarginal - secondMarginal + i));
			iterProb = temp1 / temp2;
			cellContribution += multiplyNumbers(cellMICalculation(i, totalCount, firstMarginal, secondMarginal),
					iterProb);
		}
		return cellContribution;
	}

//	public static class ExpectedUtil {
//
//		final int firstMarginal;
//		final int secondMarginal;
//
//		public ExpectedUtil(int firstMarginal, int secondMarginal) {
//			this.firstMarginal = firstMarginal;
//			this.secondMarginal = secondMarginal;
//		}
//
//		public double cellContribution(int totalCount) {
//			int min = Math.max(1, firstMarginal + secondMarginal - totalCount);
//			int max = Math.min(firstMarginal, secondMarginal);
//
//			// use the hypergeometric distrbiution for first probability
//			double iterProb = new HypergeometricDistribution(totalCount, firstMarginal, secondMarginal)
//					.probability(min);
//
//			double cellContribution = multiplyNumbers(cellMICalculation(min, totalCount, firstMarginal, secondMarginal),
//					iterProb);
//
//			// use recurrence relation of hypergeometric for next
//			// probabilities
//			for (int i = min + 1; i <= max; i++) {
//				double temp1 = multiplyNumbers(iterProb, (firstMarginal - (i - 1)) * (secondMarginal - (i - 1)));
//				double temp2 = ((i) * (totalCount - firstMarginal - secondMarginal + i));
//				iterProb = temp1 / temp2;
//				cellContribution += multiplyNumbers(cellMICalculation(i, totalCount, firstMarginal, secondMarginal),
//						iterProb);
//			}
//			return cellContribution;
//		}
//
//	}

	private static double cellMICalculation(int nij, int n, int a, int b) {

		if (nij == 0) {
			return 0;
		}
		double firstPart = 1.0 * nij / n;
		double secondPart = 1.0 * log2(1.0 * n * nij / (a * b));

		return firstPart * secondPart;
	}

	private static double logb(double a, double b) {
		if (a == 0) {
			return 0;
		} else {
			return Math.log(a) / Math.log(b);
		}
	}

	private static double log2(double a) {
		return logb(a, 2);
	}

	private static double multiplyNumbers(double a, double b) {
		if (a == 0 || b == 0) {
			return 0;
		} else {
			return a * b;
		}
	}

	// private static class ComplexDimension implements Dimension {
	//
	// @Override
	// public List<String> binCaptions() {
	// return null;
	// }
	//
	// @Override
	// public int bin(Object value) {
	// return 0;
	// }
	//
	// @Override
	// public int numberOfBins() {
	// return 0;
	// }
	//
	// }

}
