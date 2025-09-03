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
package edu.uab.cftp.sampling.distribution.tool;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.tool.Combinator;

/**
 * @author Sandy Moens
 * @since 0.6.0
 * @version 0.6.0
 */
public class Biases {
	
	public static Bias bias(StarOperation starOperation, double defaultBias) {
		return bias(starOperation, defaultBias, ImmutableMap.of());
	}
	
	public static Bias bias(StarOperation starOperation, double defaultBias, Map<Integer, Double> itemBiases) {
		if(starOperation.equals(StarOperation.MULTIPLICATIVE)) {
			return new MultiplicativeBiasNoSingletonsBPI(defaultBias, itemBiases);
		}
		return new AdditiveBiasNoSingletonsBPI(defaultBias, itemBiases);
	}
	
	private static abstract class BiasBPI implements Bias {

		protected double defaultBias;
		protected Map<Integer, Double> itemBiases;

		public BiasBPI(double defaultBias, Map<Integer, Double> itemBiases) {
			super();
			this.defaultBias = defaultBias;
			this.itemBiases = newHashMap(itemBiases);
		}

		public void addBias(int id, double bias) {
			itemBiases.put(id, bias);
		}

		public double bias(int id) {
			Double bias = itemBiases.get(id);
			if (bias != null) {
				return bias;
			}
			return defaultBias;
		}
	}
	
	private static abstract class BiasNoSingletonsBPI extends BiasBPI {

		protected static TidList difference(TidList tidList, TidList[] tidLists) {
			TidList difference = new TidList(tidList);
			for (TidList t : tidLists) {
				if (t != null) {
					difference.andNot(t);
				}
			}
			return difference;
		}

		public BiasNoSingletonsBPI(double defaultBias,
				Map<Integer, Double> itemBiases) {
			super(defaultBias, itemBiases);
		}

		@Override
		public double getWeight(TidList tidList, TidList[] negativeTidList,
				TidList X, TidList Y, TidList difference) {
			double emptySetBias = (X.cardinality() > 0
					|| somePartNonNull(negativeTidList) ? 0 : emptySetBias());
			double weight = inducedSetsBias(tidList, negativeTidList, X, Y);
			double singletonsBias = 0;

			if (X.cardinality() > 1) {
				singletonsBias = 0;
			} else {
				if (X.cardinality() == 0) {
					singletonsBias = singletonsBias(difference);
				} else {
					if (somePartNonNull(negativeTidList)) {
						singletonsBias = 0;
					} else {
						singletonsBias = singletonsBias(X);
					}
				}
			}

			return weight - singletonsBias - emptySetBias;
		}

		private boolean somePartContains(TidList[] transactions, int index) {
			for (TidList transaction : transactions) {
				if (transaction != null && transaction.get(index)) {
					return true;
				}
			}
			return false;
		}

		private boolean somePartNonNull(TidList[] transactions) {
			for (TidList transaction : transactions) {
				if (transaction != null) {
					return true;
				}
			}
			return false;
		}

		protected TidList intersect(TidList[] transactions, int[] indices) {
			for (int index : indices) {
				if (transactions[index] == null) {
					return null;
				}
			}

			TidList tidList;
			if (indices.length == 0) {
				tidList = new TidList();
			} else if (indices.length == 1) {
				tidList = new TidList(transactions[indices[0]]);
			} else {
				tidList = TidList.intersect(transactions[indices[0]],
						transactions[indices[1]]);
				for (int i = 2; i < indices.length; i++) {
					tidList.and(transactions[indices[i]]);
				}
			}
			return tidList;
		}

		protected boolean computeDXYNegativeTransactions(
				TidList[] negativeTransactions, int index, TidList X, TidList Y,
				boolean add) {
			boolean setToNull = false;
			for (int i = 0; i < negativeTransactions.length; i++) {
				if (negativeTransactions[i] != null) {
					if (!negativeTransactions[i].get(index) && add) {
						negativeTransactions[i] = null;
						setToNull = true;
					} else {
						negativeTransactions[i].removeAll(X);
						negativeTransactions[i].removeAll(Y);
					}
				}
			}
			return setToNull;
		}

		@Override
		public double inducedSetsBiasConditioned(TidList tidList,
				TidList[] negativeTidList, TidList X, TidList Y, int conditionIndex) {
			tidList.clear(conditionIndex);
			X.set(conditionIndex);

			int numberOfNegativeParts = negativeTidList.length;

			TidList[] negativeParts = new TidList[numberOfNegativeParts];
			for (int i = 0; i < numberOfNegativeParts; i++) {
				negativeParts[i] = negativeTidList[i] == null ? null : new TidList(
						negativeTidList[i]);
			}
			computeDXYNegativeTransactions(negativeParts, conditionIndex, X, Y,
					true);
			double weight = inducedSetsBias(tidList, negativeParts, X, Y);

			tidList.set(conditionIndex);
			X.clear(conditionIndex);
			return weight;
		}

		@Override
		public double getConditionedWeight(TidList tidList,
				TidList[] negativeTidList, TidList X, TidList Y, int conditionIndex) {
			double conditionalWeight = inducedSetsBiasConditioned(tidList,
					negativeTidList, X, Y, conditionIndex);
			double singletonBias = (X.cardinality() > 0
					|| somePartContains(negativeTidList, conditionIndex) ? 0
					: bias(conditionIndex));
			return conditionalWeight - singletonBias;
		}

		@Override
		public double getWeight(TidList posIntersection, TidList[] negativeParts) {
			double weight = powerSetBias(posIntersection);

			if (negativeParts.length == 0) {
				return weight;
			}

			return weight
					+ getWeightNegativeCounterParts(posIntersection, negativeParts);
		}

		private double getWeightNegativeCounterParts(TidList pos,
				TidList[] negativeParts) {

			List<TidList> negatives = new ArrayList<TidList>(negativeParts.length);
			boolean someNonNull = false;
			for (TidList tl : negativeParts) {
				if (tl != null) {
					someNonNull = true;
					TidList negative = TidList.intersect(pos, tl);
					if (!negative.isEmpty()) {
						negatives.add(negative);
					}
				}
			}
			if (negatives.isEmpty()) {
				if (someNonNull) {
					return -emptySetBias();
				}
				return 0;
			}
			double weight = 0;

			for (int i = 0; i < negatives.size(); i++) {
				Combinator comb = new Combinator(negatives.size(), i + 1);
				boolean allEmpty = true;
				while (comb.hasNext()) {
					TidList cp = intersect(negatives, comb.currentCombination());
					weight += ua.ac.be.mime.tool.Utils.pow(-1, i + 1) * powerSetBias(cp);
					comb.next();
					allEmpty = cp.isEmpty() && allEmpty;
				}
				TidList cp = intersect(negatives, comb.currentCombination());
				weight += ua.ac.be.mime.tool.Utils.pow(-1, i + 1) * powerSetBias(cp);
			}

			return weight;
		}

		protected TidList intersect(List<TidList> transactions, int[] indices) {
			for (int index : indices) {
				if (transactions.get(index) == null) {
					return null;
				}
			}

			TidList tidList;
			if (indices.length == 0) {
				tidList = new TidList();
			} else if (indices.length == 1) {
				tidList = new TidList(transactions.get(indices[0]));
			} else {
				tidList = TidList.intersect(transactions.get(indices[0]),
						transactions.get(indices[1]));
				for (int i = 2; i < indices.length; i++) {
					tidList.and(transactions.get(indices[i]));
				}
			}
			return tidList;
		}

	}
	
	private static class AdditiveBiasNoSingletonsBPI extends BiasNoSingletonsBPI {
		
		public AdditiveBiasNoSingletonsBPI(double defaultBias,
				Map<Integer, Double> itemBiases) {
			super(defaultBias, itemBiases);
		}
		
		@Override
		public double inducedSetsBias(TidList tidList, TidList[] negativeTidList,
				TidList X, TidList Y) {
			double number = computeNumberOfInducedPatterns(tidList, negativeTidList);
			double singletonsBias = singletonsBias(X);
			double weight = getWeight(tidList, negativeTidList);
			return number * singletonsBias + weight;
		}
		
		private double computeNumberOfInducedPatterns(TidList tidList,
				TidList[] negativeTidList) {
			return computeNumberOfInducedPatterns(tidList)
					+ computeNumberOfInducedPatternsNegative(tidList,
							negativeTidList);
		}
		
		private double computeNumberOfInducedPatternsNegative(TidList tidList,
				TidList[] negativeTidList) {
			double numberOfInducedPatterns = 0;
		
			List<TidList> negatives = new ArrayList<TidList>(negativeTidList.length);
			for (TidList tl : negativeTidList) {
				if (tl != null) {
					negatives.add(TidList.intersect(tidList, tl));
				}
			}
		
			for (int i = 0; i < negatives.size(); i++) {
				Combinator comb = new Combinator(negatives.size(), i + 1);
				while (comb.hasNext()) {
					TidList cp = intersect(negatives, comb.currentCombination());
					numberOfInducedPatterns += ua.ac.be.mime.tool.Utils.pow(-1, i + 1)
							* computeNumberOfInducedPatterns(cp);
					comb.next();
				}
				TidList cp = intersect(negatives, comb.currentCombination());
				numberOfInducedPatterns += ua.ac.be.mime.tool.Utils.pow(-1, i + 1)
						* computeNumberOfInducedPatterns(cp);
			}
		
			return numberOfInducedPatterns;
		}
		
		private double computeNumberOfInducedPatterns(TidList tidList) {
			return ua.ac.be.mime.tool.Utils.pow(2, tidList.cardinality());
		}
		
		@Override
		public double powerSetBias(TidList tidList) {
			if (tidList == null) {
				return 0;
			}
			double singletonsBias = singletonsBias(tidList);
			double result = singletonsBias
					* ua.ac.be.mime.tool.Utils.pow(2, tidList.cardinality() - 1);
			return result;
		}
		
		@Override
		public double singletonsBias(TidList tidList) {
			if (tidList == null) {
				return 0;
			}
		
			double bias = 0;
			int ix = -1;
			while ((ix = tidList.nextSetBit(ix + 1)) != -1) {
				bias += bias(ix);
			}
		
			return bias;
		}
		
		@Override
		public double emptySetBias() {
			return 0;
		}
	}
	
	private static class MultiplicativeBiasNoSingletonsBPI extends BiasNoSingletonsBPI {
	
		public MultiplicativeBiasNoSingletonsBPI(double defaultBias,
				Map<Integer, Double> itemBiases) {
			super(defaultBias, itemBiases);
		}
		
		@Override
		public double inducedSetsBias(TidList tidList, TidList[] negativeTidList,
				TidList X, TidList Y) {
			double singletonsBias;
			if (X.cardinality() == 0) {
				singletonsBias = emptySetBias();
			} else {
				singletonsBias = 1;
				int ix = -1;
				while ((ix = X.nextSetBit(ix + 1)) != -1) {
					singletonsBias *= (bias(ix));
				}
			}
			return singletonsBias * getWeight(tidList, negativeTidList);
		}
		
		@Override
		public double powerSetBias(TidList tidList) {
			if (itemBiases.isEmpty()) {
				return Math.pow(2, tidList.cardinality());
			}
		
			double score = 1;
			int ix = -1;
			while ((ix = tidList.nextSetBit(ix + 1)) != -1) {
				score *= (1 + bias(ix));
			}
			return score;
		}
		
		@Override
		public double singletonsBias(TidList tidList) {
			if (tidList == null) {
				return 0;
			}
		
			double score = 0;
			int ix = -1;
			while ((ix = tidList.nextSetBit(ix + 1)) != -1) {
				score += (bias(ix));
			}
			return score;
		}
		
		@Override
		public double emptySetBias() {
			return 1;
		}
	
	}
	
	// Suppress default constructor for non-instantiability
	private Biases() {
		throw new AssertionError();
	}
	
}
