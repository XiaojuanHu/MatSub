package edu.uab.consapt.sampling;

import static ua.ac.be.mime.mining.TidList.difference;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;

public class SequentialSubsetSamplerAdditiveNoNegatives implements
		StoppableSamplerWithInput<List<PlainTransaction>, PlainItemSet> {

	private final Random random;
	private final PlainItemDB itemDB;
	private List<PlainTransaction> transactions;

	private boolean isInitializedWithContex;
	private final boolean biasesAreSet;

	private Map<Integer, Double> biasesMap;
	private TidList intersection;
	private boolean isStop;

	public SequentialSubsetSamplerAdditiveNoNegatives(PlainItemDB itemDB) {
		this.itemDB = itemDB;
		this.biasesAreSet = false;
		this.random = new Random(System.currentTimeMillis());
		isInitializedWithContex = false;
	}

	@Override
	public void setContext(List<PlainTransaction> transactions) {
		this.transactions = transactions;
		this.intersection = intersectTransactions();
		isInitializedWithContex = true;
	}

	@Override
	public PlainItemSet getNext() {
		if (!isInitializedWithContex) {
			throw new SamplerWithInput.NoContextSetException();
		}
		if (isStop) {
			return null;
		}
		return drawSequentialSubsetNoSingletons(intersection);
	}

	@Override
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	private PlainItemSet drawSequentialSubsetNoSingletons(TidList itemsAsTids) {
		PlainItemSet itemSet;

		do {
			if (isStop) {
				return null;
			}
			itemSet = drawSequentialSubset(itemsAsTids);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	private PlainItemSet drawSequentialSubset(TidList itemsAsTids) {
		TidList P = new TidList();
		TidList N = new TidList();

		double alfa, tmp;
		int index = -1;
		while ((index = itemsAsTids.nextSetBit(index + 1)) != -1) {
			tmp = getWeight(difference(itemsAsTids, N));
			alfa = tmp + getWeight(P, index);
			alfa /= 2 * (tmp + getWeight(P));
			if (random.nextDouble() < alfa) {
				P.set(index);
			} else {
				N.set(index);
			}
		}

		PlainItemSet itemSet = new PlainItemSet();
		index = -1;
		while ((index = P.nextSetBit(index + 1)) != -1) {
			itemSet.add(itemDB.get(index));
		}

		return itemSet;
	}

	private static double getWeight(TidList tids, int i) {
		return (tids.cardinality() + 1) * 0.5;
	}

	private static double getWeight(TidList tids) {
		return tids.cardinality() * 0.5;
	}

	private TidList intersectTransactions() {
		if (transactions.size() == 0) {
			return new TidList();
		}
		Iterator<PlainTransaction> iterator = transactions.iterator();
		TidList tl = (TidList) iterator.next().getItemsAsBitSet().clone();
		while (iterator.hasNext()) {
			tl.and(iterator.next().getItemsAsBitSet());
		}
		return tl;
	}

}
