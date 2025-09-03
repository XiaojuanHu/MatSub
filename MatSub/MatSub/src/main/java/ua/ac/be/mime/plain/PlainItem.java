package ua.ac.be.mime.plain;

import java.util.BitSet;

public class PlainItem implements Comparable<PlainItem> {

	protected int id;
	protected String name;
	private final BitSet tids;

	public PlainItem(int itemId) {
		this.id = itemId;
		this.tids = new BitSet();
	}

	public PlainItem(int id, BitSet tids) {
		this.id = id;
		BitSet newTIDs = new BitSet();
		newTIDs.or(tids);
		this.tids = newTIDs;
	}

	public void setTID(int txCounter) {
		this.tids.set(txCounter);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int compareTo(PlainItem o) {
		return this.id - o.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlainItem other = (PlainItem) obj;
		if (this.id == other.id)
			return true;
		return false;
	}

	public BitSet getTIDs() {
		return this.tids;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public String toString() {
		if (name != null) {
			return name + "(id=" + id + ")";
		}
		return id + "";
	}
}
