package ua.ac.be.mime.plain;

import static com.google.common.collect.Sets.newTreeSet;
import static java.util.Collections.unmodifiableSet;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class PlainItemSet implements Iterable<PlainItem> {

	private SortedSet<PlainItem> items;

	public PlainItemSet() {
		items = newTreeSet();
	}

	public PlainItemSet(PlainItem... newItems) {
		this();
		for (PlainItem newItem : newItems) {
			items.add(newItem);
		}
	}

	public PlainItemSet(PlainItemSet aPlainItemSet) {
		this.items = newTreeSet(aPlainItemSet.items);
	}

	public PlainItemSet(Iterable<PlainItem> items) {
		this();
		for (PlainItem item : items) {
			this.items.add(item);
		}
	}

	public BitSet getTIDs() {
		BitSet tids = new BitSet();
		Iterator<PlainItem> it = items.iterator();
		if (it.hasNext()) {
			tids.or(it.next().getTIDs()); // setup

			for (; it.hasNext();) {
				tids.and(it.next().getTIDs());
			}
		}
		return tids;
	}

	public void add(PlainItem newItem) {
		items.add(newItem);
	}

	public int size() {
		return items.size();
	}

	public boolean contains(PlainItem item) {
		return items.contains(item);
	}

	@Override
	public String toString() {
		return items.toString();
	}

	@Override
	public Iterator<PlainItem> iterator() {
		return unmodifiableSet(items).iterator();
	}

	@Override
	public int hashCode() {
		return items.hashCode() * (int) Math.pow(items.size(), 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlainItemSet other = (PlainItemSet) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (other.items.size() != this.items.size()) {
			return false;
		} else {
			for (PlainItem plainItem : other.items) {
				if (!this.items.contains(plainItem)) {
					return false;
				}
			}
		}
		return true;
	}

	public final static class ItemSetSizeComparator implements
			Comparator<PlainItemSet> {
		@Override
		public int compare(PlainItemSet o1, PlainItemSet o2) {
			int overSize = o2.size() - o1.size();
			if (overSize == 0) {
				int overCardinality = o2.getTIDs().cardinality()
						- o1.getTIDs().cardinality();
				if (overCardinality == 0) {
					if (o1.equals(o2)) {
						return 0;
					}

					Iterator<PlainItem> firstIterator = o1.iterator();
					Iterator<PlainItem> secondIterator = o2.iterator();

					for (; firstIterator.hasNext();) {
						PlainItem item2 = secondIterator.next();
						PlainItem item1 = firstIterator.next();
						int overItems = item1.compareTo(item2);
						if (overItems != 0) {
							return overItems;
						}
					}
					return 0;
				}
				return overCardinality;
			}
			return overSize;
		}
	}

	public PlainItemSet difference(PlainItemSet itemSet) {
		PlainItemSet diff = new PlainItemSet();
		diff.items = newTreeSet(this.items);
		diff.items.removeAll(itemSet.items);
		return diff;
	}

	public PlainItemSet union(PlainItemSet itemSet) {
		PlainItemSet union = new PlainItemSet();
		union.items = newTreeSet(this.items);
		union.items.addAll(itemSet.items);
		return union;
	}

	public PlainItemSet union(PlainItem item) {
		PlainItemSet union = new PlainItemSet();
		union.items = newTreeSet(this.items);
		union.items.add(item);
		return union;
	}

	public PlainItemSet intersection(PlainItemSet itemSet) {
		PlainItemSet intersection = new PlainItemSet();
		intersection.items = newTreeSet(this.items);
		intersection.items.retainAll(itemSet.items);
		return intersection;
	}
}
