package edu.uab.cftp.sampling.distribution.tool;

import java.util.Random;

import edu.uab.cftp.sampling.CouplingFromThePast;
import ua.ac.be.mime.mining.TidList;

public class SequentialSubsetSampler implements SubsetSampler {

	private final Bias biasComputer;

	public SequentialSubsetSampler(Bias biasComputer) {
		this.biasComputer = biasComputer;
	}

	@Override
	public int[] drawSubset(TidList posIntersection,
			TidList[] negativeTransactions) {
		TidList difference = difference(posIntersection, negativeTransactions);

		int index;
		double prob = 0, conditionalWeight = 0, weight = 0;

		TidList X = new TidList(), Y = new TidList();

		Random random = new Random(CouplingFromThePast.nextRandomSeed());

		while (posIntersection.cardinality() > 0) {
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
			if (computeDXYNegativeTransactions(negativeTransactions, index, X,
					Y, add)) {
				difference = difference(posIntersection, negativeTransactions);
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
}
