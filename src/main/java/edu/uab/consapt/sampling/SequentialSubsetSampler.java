package edu.uab.consapt.sampling;

import static edu.uab.cftp.sampling.distribution.tool.Biases.bias;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.uab.cftp.sampling.CouplingFromThePast;
import edu.uab.cftp.sampling.distribution.tool.Bias;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.cftp.sampling.distribution.tool.SubsetSampler;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;

public class SequentialSubsetSampler implements
		StoppableSamplerWithInput<List<PlainTransaction>, PlainItemSet> {

	private static class SSS implements SubsetSampler {

		private final Bias biasComputer;
		private boolean isStop;

		public SSS(Bias biasComputer) {
			this.biasComputer = biasComputer;
		}

		@Override
		public int[] drawSubset(TidList posIntersection,
				TidList[] negativeTransactions) {
			TidList difference = difference(posIntersection,
					negativeTransactions);

			int index;
			double prob = 0, conditionalWeight = 0, weight = 0;

			TidList X = new TidList(), Y = new TidList();

			Random random = new Random(CouplingFromThePast.nextRandomSeed());

			while (posIntersection.cardinality() > 0) {
				if (isStop) {
					return new int[] {};
				}
				if (X.cardinality() == 0 && posIntersection.cardinality() == 2) {
					int next = posIntersection.nextSetBit(0);
					X.set(next);
					X.set(posIntersection.nextSetBit(next + 1));
					break;
				}
				if (X.cardinality() == 1 && posIntersection.cardinality() == 1) {
					X.set(posIntersection.nextSetBit(0));
					break;
				}

				index = posIntersection.nextSetBit(0);

				conditionalWeight = this.biasComputer.getConditionedWeight(
						posIntersection, negativeTransactions, X, Y, index);
				weight = this.biasComputer.getWeight(posIntersection,
						negativeTransactions, X, Y, difference);

				prob = conditionalWeight / weight;

				boolean add = false;

				if (random.nextDouble() < prob) {
					add = true;
					X.set(index);
				} else {
					Y.set(index);
				}
				posIntersection.set(index, false);
				if (computeDXYNegativeTransactions(negativeTransactions, index,
						X, Y, add)) {
					difference = difference(posIntersection,
							negativeTransactions);
				} else {
					difference.clear(index);
				}
			}

			int[] result = new int[X.cardinality()];
			int ix = -1;
			int i = 0;
			while ((ix = X.nextSetBit(ix + 1)) != -1) {
				result[i++] = ix;
			}
			return result;
		}

		public void setStop(boolean isStop) {
			this.isStop = isStop;
		}

		private TidList difference(TidList tidList, TidList[] tidLists) {
			TidList difference = new TidList(tidList);
			for (TidList t : tidLists) {
				if (t != null) {
					difference.andNot(t);
				}
			}
			return difference;
		}

		private boolean computeDXYNegativeTransactions(
				TidList[] negativeTransactions, int index, TidList X,
				TidList Y, boolean add) {
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
	}

	private final PlainItemDB itemDB;
	private List<PlainTransaction> transactions;

	private TidList[] negativeTransactionsAsBitSets;

	private boolean isInitializedWithContex;
	private boolean isPosNegDB;
	private boolean biasesAreSet;

	private final StarOperation star;
	private int cardinalityPos;
	private int cardinalityNeg;
	private Map<Integer, Double> biasesMap;
	private Bias biasComputer;
	private SSS sss;
	private boolean isStop;

	public SequentialSubsetSampler(PlainItemDB itemDB, StarOperation star) {
		this.itemDB = itemDB;
		this.star = star;
		this.isPosNegDB = false;
		this.biasesAreSet = false;
		isInitializedWithContex = false;
	}

	public SequentialSubsetSampler(PlainItemDB itemDB, StarOperation star,
			int cardinalityPos, int cardinalityNeg) {
		this(itemDB, star);
		this.cardinalityPos = cardinalityPos;
		this.cardinalityNeg = cardinalityNeg;
		this.isPosNegDB = true;
		this.biasesAreSet = false;
	}

	public SequentialSubsetSampler(PlainItemDB itemDB, StarOperation star,
			int cardinalityPos, int cardinalityNeg,
			Map<Integer, Double> biasesMap) {
		this(itemDB, star, cardinalityPos, cardinalityNeg);
		this.biasesMap = biasesMap;
		this.biasesAreSet = true;
	}

	@Override
	public void setContext(List<PlainTransaction> transactions) {
		this.transactions = transactions;
		if (!isPosNegDB) {
			this.negativeTransactionsAsBitSets = new TidList[] {};

		} else {
			this.negativeTransactionsAsBitSets = getNegatives();
		}
		isInitializedWithContex = true;
	}

	@Override
	public PlainItemSet getNext() {
		if (!isInitializedWithContex) {
			throw new SamplerWithInput.NoContextSetException();
		}
		if (biasComputer == null) {
			biasComputer = getBiasComputer();
		}
		if (sss == null) {
			sss = new SSS(biasComputer);
		}
		if (isStop) {
			return null;
		}
		return convertToPlainItemSet(sss.drawSubset(intersectPositives(),
				negativeTransactionsAsBitSets));
	}

	@Override
	public void setStop(boolean isStop) {
		this.isStop = isStop;
		this.sss.setStop(isStop);
	}

	private Bias getBiasComputer() {
		if (biasesAreSet) {
			return bias(star, 1, biasesMap);
		}
		return bias(star, 1);
	}

	private PlainItemSet convertToPlainItemSet(int[] set) {
		if (isStop) {
			return null;
		}
		PlainItemSet plainItemSet = new PlainItemSet();
		for (int item : set) {
			plainItemSet.add(itemDB.get(item));
		}
		return plainItemSet;
	}

	private TidList intersectPositives() {
		if (cardinalityPos == 0) {
			return new TidList();
		}
		Iterator<PlainTransaction> iterator = transactions.iterator();
		TidList tl = (TidList) iterator.next().getItemsAsBitSet().clone();
		for (int i = 1; i < cardinalityPos; i++) {
			tl.and(iterator.next().getItemsAsBitSet());
		}
		return tl;
	}

	private TidList[] getNegatives() {
		TidList[] negatives = new TidList[cardinalityNeg];
		Iterator<PlainTransaction> iterator = transactions.iterator();
		for (int i = 0; i < cardinalityPos; i++) {
			iterator.next();
		}
		for (int i = 0; i < cardinalityNeg; i++) {
			negatives[i] = (TidList) iterator.next().getItemsAsBitSet().clone();
		}
		return negatives;
	}
}
