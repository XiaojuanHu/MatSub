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

package de.unibonn.realkd.algorithms.emm;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.PlainTransactionDB;

public interface RowWeightComputer {

	public static class UniformRowWeightComputer implements RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
				List<Attribute<?>> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			for (PlainTransaction transaction : db.getTransactions()) {
				weights.put(transaction, 1.0);
			}
			return weights;
		}
	}

	public static class MeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
				List<Attribute<?>> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			Attribute<?> targetAttribute = targets.get(0);
			if (!(targetAttribute instanceof MetricAttribute)) {
				return weights;
			}

			double mean = ((MetricAttribute) targetAttribute).mean();

			Iterator<PlainTransaction> it = db.getTransactions().iterator();

			for (int i = 0; i <= targetAttribute.maxIndex(); i++) {
				// for (Double value : ((NumericAttribute) targetAttribute)
				// .getValues()) {
				if (targetAttribute.valueMissing(i)) {
					weights.put(it.next(), 0.0);
				} else {
					weights.put(
							it.next(),
							Math.abs(((MetricAttribute) targetAttribute)
									.value(i) - mean));
				}
			}

			return weights;
		}
	}

	public static class PositiveMeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
				List<Attribute<?>> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			Attribute<?> targetAttribute = targets.get(0);
			if (!(targetAttribute instanceof MetricAttribute)) {
				return weights;
			}

			double mean = ((MetricAttribute) targetAttribute).mean();

			Iterator<PlainTransaction> it = db.getTransactions().iterator();

			for (int i = 0; i < dataTable.population().size(); i++) {

				// for (Double value : ((NumericAttribute) targetAttribute)
				// .getValues()) {
				if (targetAttribute.valueMissing(i)) {
					weights.put(it.next(), 0.0);
				} else {
					weights.put(
							it.next(),
							max(((MetricAttribute) targetAttribute).value(i)
									- mean, 0));
				}
			}

			return weights;
		}
	}

	public static class MultiplicatingMeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
				List<Attribute<?>> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute<?> attribute : targets) {
				if (!(attribute instanceof MetricAttribute)) {
					return weights;
				}
			}

			Iterator<PlainTransaction> it = db.getTransactions().iterator();

			for (Integer row : dataTable.population().objectIds()) {
				weights.put(it.next(), computeWeight(dataTable, row, targets));
			}

			return weights;
		}

		private double computeWeight(DataTable dataTable, Integer row,
				List<Attribute<?>> targets) {
			double weight = 1.0;
			if (dataTable.atLeastOneAttributeValueMissingFor(row, targets)) {
				return weight;
			}
			for (Attribute<?> attribute : targets) {
				double value = ((MetricAttribute) attribute).value(row);
				double mean = ((MetricAttribute) attribute).mean();
				weight *= Math.abs(value - mean);
			}
			return weight;
		}
	}

	public static class SquaredSumMeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
				List<Attribute<?>> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute<?> attribute : targets) {
				if (!(attribute instanceof MetricAttribute)) {
					return weights;
				}
			}

			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			// for (List<String> row : dataTable.getTableDataRows()) {
			// weights.put(it.next(), computeWeight(row, targets));
			// }
			for (Integer row : dataTable.population().objectIds()) {
				weights.put(it.next(), computeWeight(dataTable, row, targets));
			}

			return weights;
		}

		private double computeWeight(DataTable dataTable, Integer row,
				List<Attribute<?>> targets) {
			double weight = 0;
			if (dataTable.atLeastOneAttributeValueMissingFor(row, targets)) {
				return weight;
			}
			for (Attribute<?> attribute : targets) {
				double value = ((MetricAttribute) attribute).value(row);
				double mean = ((MetricAttribute) attribute).mean();
				weight += Math.abs(value - mean);
			}
			return sqrt(weight);
		}
	}

	public Map<PlainTransaction, Double> getRowWeights(DataTable dataTable,
			List<Attribute<?>> targets, PlainTransactionDB db);

}
