package ua.ac.be.mime.plain;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

import java.util.Iterator;
import java.util.Set;

import ua.ac.be.mime.mining.TidList;

public class PlainTransaction implements Iterable<PlainItem> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlainTransaction other = (PlainTransaction) obj;
		if (tid != other.tid)
			return false;
		return true;
	}

	private final int tid;

	private final Set<PlainItem> items = newHashSet();
	private final TidList itemsBitSet = new TidList();

	public PlainTransaction(int tid) {
		this.tid = tid;
	}

	public int getTid() {
		return this.tid;
	}

	public void add(PlainItem item) {
		this.items.add(item);
		this.itemsBitSet.set(item.getId());
	}

	public Set<PlainItem> getItems() {
		return items;
	}

	@Override
	public Iterator<PlainItem> iterator() {
		return this.items.iterator();
	}

	public boolean contains(PlainItem item) {
		return this.items.contains(item);
	}

	public int size() {
		return this.items.size();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (PlainItem item : newTreeSet(this.items)) {
			buf.append(item.id + "   ");
		}
		return buf.toString();
	}

	public TidList getItemsAsBitSet() {
		return this.itemsBitSet;
	}

}