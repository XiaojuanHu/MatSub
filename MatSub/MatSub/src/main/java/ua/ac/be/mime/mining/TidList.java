package ua.ac.be.mime.mining;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TidList extends BitSet {// HashSet<Integer> {
	private static final long serialVersionUID = 6037248886409149654L;

	public TidList() {
		super();
	}

	public TidList(BitSet t) {
		super();
		or(t);
	}

	/**
	 * Adds the first numOfTransactions transactions to this item
	 * 
	 * @param numOfTransactions
	 */
	public TidList(int numOfTransactions) {
		this.set(0, numOfTransactions);
	}

	private boolean containsAll(BitSet s1, BitSet s2) {
		int index = -1;
		while ((index = s2.nextSetBit(index + 1)) != -1) {
			if (!s1.get(index)) {
				return false;
			}
		}
		return true;
	}

	public static int containsNumber(BitSet s1, BitSet s2) {
		int total = 0;
		int index = -1;
		while ((index = s2.nextSetBit(index + 1)) != -1) {
			if (s1.get(index)) {
				total++;
			}
		}
		return total;
	}

	public boolean isSubsetOf(BitSet c) {
		return containsAll(c, this);
	}

	public void addAll(TidList t) {
		this.or(t);
	}

	public void retainAll(TidList t) {
		this.and(t);
	}

	public void removeAll(TidList t) {
		this.andNot(t);
	}

	public void add(int i) {
		this.set(i, true);
	}

	@Override
	public int size() {
		return cardinality();
	}

	public static TidList intersect(TidList t1, TidList t2) {
		TidList i;

		if (t1.cardinality() < t2.cardinality()) {
			i = (TidList) t1.clone();
			i.and(t2);
		} else {
			i = (TidList) t2.clone();
			i.and(t1);
		}
		return i;
	}

	public static TidList intersect(Collection<TidList> tidLists) {
		if (tidLists.size() == 0) {
			return null;
		} else if (tidLists.size() == 1) {
			return new TidList(tidLists.iterator().next());
		}
		Iterator<TidList> it = tidLists.iterator();
		TidList tl = intersect(it.next(), it.next());
		while (it.hasNext()) {
			tl.and(it.next());
		}
		return tl;
	}

	public static TidList difference(TidList t1, TidList t2) {
		TidList i = new TidList(t1);
		i.removeAll(t2);
		return i;
	}

	public static TidList union(TidList t1, TidList t2) {
		TidList i = new TidList(t1);
		i.addAll(t2);
		return i;
	}

	public List<Integer> toList() {
		List<Integer> l = new LinkedList<Integer>();
		int index = -1;
		while ((index = nextSetBit(index + 1)) != -1) {
			l.add(index);
		}
		return l;
	}
}
