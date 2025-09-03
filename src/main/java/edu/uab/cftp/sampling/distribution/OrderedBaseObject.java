package edu.uab.cftp.sampling.distribution;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class OrderedBaseObject implements Comparable<OrderedBaseObject> {

	protected int[] indices;
	protected double weight = -1;

	public OrderedBaseObject(int[] indices) {
		this.indices = indices;
	}

	public OrderedBaseObject copy() {
		return new OrderedBaseObject(this.indices.clone());
	}

	public int size() {
		return this.indices.length;
	}

	public boolean isUnity() {
		int index = this.indices[0];
		for (int i = 1; i < this.indices.length; i++) {
			if (index != this.indices[i]) {
				return false;
			}
		}
		return true;

	}

	public int[] getIndices() {
		return this.indices;
	}

	public Set<Integer> getUniqueIndices() {
		Set<Integer> uniqueIndices = new TreeSet<Integer>();
		for (int i : this.indices) {
			uniqueIndices.add(i);
		}
		return uniqueIndices;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return this.weight;
	}

	@Override
	public int compareTo(OrderedBaseObject baseObject) {
		if (this.indices.length == baseObject.indices.length) {
			for (int i = 0; i < this.indices.length; i++) {
				if (this.indices[i] != baseObject.indices[i]) {
					return Integer.valueOf(this.indices[i]).compareTo(
							baseObject.indices[i]);
				}
			}
			return 0;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.indices);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof OrderedBaseObject) {
			int[] i1 = this.indices.clone();
			Arrays.sort(i1);
			int[] i2 = ((OrderedBaseObject) o).getIndices();
			Arrays.sort(i2);
			return Arrays.equals(i1, i2);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (int i : this.indices) {
			b.append(i + " ");
		}
		return b.toString();
	}
}
