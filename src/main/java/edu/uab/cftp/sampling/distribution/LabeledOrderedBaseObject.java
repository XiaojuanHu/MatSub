package edu.uab.cftp.sampling.distribution;

import java.util.Arrays;

public class LabeledOrderedBaseObject extends OrderedBaseObject {

	public LabeledOrderedBaseObject(int[] indices) {
		super(indices);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof OrderedBaseObject) {
			return Arrays.equals(this.indices, ((OrderedBaseObject) o).getIndices());
		}
		return false;
	}
}
