package edu.uab.consapt.sampling;

import java.util.List;

import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.Utils;

public class UniformSubsetSampler implements
		StoppableSamplerWithInput<List<PlainTransaction>, PlainItemSet> {

	private boolean isInitializedWithContex;
	private final PlainItemDB itemDB;

	private TidList intersectionAsBitSet;

	private boolean isStop;

	public UniformSubsetSampler(PlainItemDB itemDB) {
		this.itemDB = itemDB;
		isInitializedWithContex = false;
	}

	@Override
	public void setContext(List<PlainTransaction> transactions) {
		intersectionAsBitSet = Utils.intersectAll(transactions);
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
		return drawUniformSubSetNoSingletons(itemDB, intersectionAsBitSet);
	}

	public PlainItemSet drawUniformSubSetNoSingletons(PlainItemDB itemDB,
			TidList transaction) {
		PlainItemSet itemSet;

		if (transaction.cardinality() == 2) {
			itemSet = new PlainItemSet();
			int i;
			itemSet.add(itemDB.get(i = transaction.nextSetBit(0)));
			itemSet.add(itemDB.get(transaction.nextSetBit(i + 1)));
			return itemSet;
		}

		do {
			if (isStop) {
				return null;
			}
			itemSet = Utils.drawUniformSubSet(itemDB, transaction);
		} while (itemSet.size() <= 1);

		return itemSet;
	}

	@Override
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

}
