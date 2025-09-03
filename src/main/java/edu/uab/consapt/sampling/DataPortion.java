package edu.uab.consapt.sampling;

import java.util.BitSet;
import java.util.List;

import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;

public class DataPortion {

	private BitSet transactionTids;
	private final List<PlainTransaction> transactions;

	public DataPortion(List<PlainTransaction> transactions) {
		this.transactions = transactions;
	}

	public int size() {
		return getTransactions().size();
	}

	public List<PlainTransaction> getTransactions() {
		return this.transactions;
	}

	public PlainTransaction get(int index) {
		return this.transactions.get(index);
	}

	public PlainItemSet getItemSetInPortion(PlainItemSet itemSet) {
		if (transactionTids == null) {
			initializeTids();
		}

		return createProjectedItemSet(itemSet);
	}

	private PlainItemSet createProjectedItemSet(PlainItemSet itemSet) {
		PlainItemSet pItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			BitSet tids = intersect(itemSet.getTIDs(), transactionTids);
			PlainItem pItem = new PlainItem(item.getId(), tids);
			pItemSet.add(pItem);
		}
		return pItemSet;
	}

	private void initializeTids() {
		transactionTids = new BitSet();
		for (PlainTransaction transaction : transactions) {
			transactionTids.set(transaction.getTid());
		}
	}

	private static BitSet intersect(BitSet b1, BitSet b2) {
		BitSet bitSet = (BitSet) b1.clone();
		bitSet.and(b2);
		return bitSet;
	}

	@Override
	public String toString() {
		return "DataPortion[" + transactions.size() + "]";
	}

}
