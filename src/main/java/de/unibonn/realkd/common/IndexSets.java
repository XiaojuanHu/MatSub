/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.common;

import static java.util.Collections.max;

import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import com.google.common.primitives.Ints;

/**
 * <p>
 * Factory methods and set operations for {@link IndexSet} objects.
 * </p>
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.4.1
 * 
 * @version 0.4.1
 *
 */
public class IndexSets {

	private static final BitSetBackedIndexSet EMPTY_INDEX_SET = new BitSetBackedIndexSet(new BitSet());

	public static IndexSet empty() {
		return EMPTY_INDEX_SET;
	}

	public static IndexSet full(int maxIndex) {
		BitSet resultBitSet = new BitSet(maxIndex + 1);
		resultBitSet.set(0, maxIndex + 1, true);
		return new BitSetBackedIndexSet(resultBitSet);
	}

	public static IndexSet copyOf(Collection<? extends Integer> c) {
		if (c.isEmpty()) {
			return empty();
		}
		int maxIndex = max(c);
		BitSet bs = new BitSet(maxIndex + 1);
		for (int i : c) {
			bs.set(i);
		}
		return new BitSetBackedIndexSet(bs);
	}

	public static IndexSet of(int... elements) {
		if (elements.length == 0) {
			return empty();
		}
		int maxIndex = Ints.max(elements);
		BitSet bs = new BitSet(maxIndex + 1);
		for (int i : elements) {
			bs.set(i);
		}
		return new BitSetBackedIndexSet(bs);
	}

	public static IndexSet intersection(BitSetBackedIndexSet i1, BitSetBackedIndexSet i2) {
		BitSet resultBitSet;
		if (i1.bitSet.length() <= i2.bitSet.length()) {
			resultBitSet = (BitSet) i1.bitSet.clone();
			resultBitSet.and(i2.bitSet);
		} else {
			resultBitSet = (BitSet) i2.bitSet.clone();
			resultBitSet.and(i1.bitSet);
		}
		return new BitSetBackedIndexSet(resultBitSet);
	}

	public static IndexSet intersection(IndexSet i1, IndexSet i2) {
		if (i1.isEmpty() || i2.isEmpty()) {
			return empty();
		}
		if (i1 instanceof BitSetBackedIndexSet && i2 instanceof BitSetBackedIndexSet) {
			return intersection((BitSetBackedIndexSet) i1, (BitSetBackedIndexSet) i2);
		}
		// TODO default implementation here

		throw new IllegalArgumentException();
	}

	public static IndexSet union(BitSetBackedIndexSet i1, BitSetBackedIndexSet i2) {
		BitSet resultBitSet;
		if (i1.bitSet.length() >= i2.bitSet.length()) {
			resultBitSet = (BitSet) i1.bitSet.clone();
			resultBitSet.or(i2.bitSet);
		} else {
			resultBitSet = (BitSet) i2.bitSet.clone();
			resultBitSet.or(i1.bitSet);
		}
		return new BitSetBackedIndexSet(resultBitSet);
	}

	public static IndexSet union(IndexSet i1, IndexSet i2) {
		if (i1 instanceof BitSetBackedIndexSet && i2 instanceof BitSetBackedIndexSet) {
			return union((BitSetBackedIndexSet) i1, (BitSetBackedIndexSet) i2);
		}
		// TODO default implementation here

		throw new IllegalArgumentException();
	}

	public static IndexSet difference(BitSetBackedIndexSet i1, BitSetBackedIndexSet i2) {
		if (i2.isEmpty()) {
			return i1;
		}
		BitSet resultBitSet = (BitSet) i1.bitSet.clone();
		resultBitSet.andNot(i2.bitSet);

		return new BitSetBackedIndexSet(resultBitSet);
	}

	public static IndexSet difference(IndexSet i1, IndexSet i2) {
		if (i1 instanceof BitSetBackedIndexSet && i2 instanceof BitSetBackedIndexSet) {
			return difference((BitSetBackedIndexSet) i1, (BitSetBackedIndexSet) i2);
		}
		// TODO default implementation here

		throw new IllegalArgumentException();
	}

	/**
	 * @param indexSet
	 *            X
	 * @return a new index set representing the complement X
	 */
	public static IndexSet complement(IndexSet indexSet) {
		if (indexSet instanceof BitSetBackedIndexSet) {
			BitSet bitSet = ((BitSetBackedIndexSet) indexSet).bitSet;
			BitSet resultBitSet = (BitSet) bitSet.clone();
			resultBitSet.flip(0, bitSet.length());
			return new BitSetBackedIndexSet(resultBitSet);
		}
		throw new IllegalArgumentException("not yet implemented for general index sets");
	}

	public static class BitSetBackedIndexSet implements IndexSet {

		private class BitSetIterator implements PrimitiveIterator.OfInt {

			private BitSet bitSet;
			private int ix;
			private int highestIndex;

			public BitSetIterator(BitSet bitSet) {
				this.bitSet = bitSet;
				this.ix = -1;
				this.highestIndex = this.bitSet.previousSetBit(this.bitSet.size());
			}

			@Override
			public boolean hasNext() {
				return ix != highestIndex;
			}

			// @Override
			// public Integer next() {
			// return ix = this.bitSet.nextSetBit(ix + 1);
			// }

			@Override
			public void forEachRemaining(IntConsumer action) {
				while (hasNext()) {
					action.accept(nextInt());
				}
			}

			@Override
			public int nextInt() {
				return ix = this.bitSet.nextSetBit(ix + 1);
			}

		}

		private final BitSet bitSet;

		private BitSetBackedIndexSet(BitSet bs) {
			bitSet = bs;
		}

		@Override
		public boolean isEmpty() {
			return this.bitSet.isEmpty();
		}

		@Override
		public int size() {
			return this.bitSet.cardinality();
		}

		@Override
		public boolean contains(int i) {
			return this.bitSet.get(i);
		}

		@Override
		public PrimitiveIterator.OfInt iterator() {
			return new BitSetIterator(this.bitSet);
		}

		@Override
		public IntStream stream() {
			return this.bitSet.stream();
		}

		@Override
		public boolean containsAll(Iterable<Integer> c) {
			for (int i : c) {
				if (!this.bitSet.get(i)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean containsAll(IndexSet other) {
			if (this.size() < other.size()) {
				return false;
			}
			return containsAll((Iterable<Integer>) other);

			// TODO: here fast implementation in the future
			// if (!(other instanceof BitSetBackedIndexSet)) {
			// return containsAll((Iterable<Integer>) other);
			// }
			// BitSetBackedIndexSet operand = (BitSetBackedIndexSet) other;
			// BitSet intersection = (BitSet) operand.bitSet.clone();
			// intersection.and(this.bitSet);
			// return intersection.equals(operand.bitSet);
		}

		// @Override
		// public Comparator<? super Integer> comparator() {
		// return Comparator.naturalOrder();
		// }
		//
		// @Override
		// public SortedSet<Integer> subSet(Integer fromElement, Integer
		// toElement) {
		// return new BitSetBackedIndexSet(this.bitSet.get(fromElement,
		// toElement));
		// }
		//
		// @Override
		// public SortedSet<Integer> headSet(Integer toElement) {
		// return subSet(0, toElement);
		// }
		//
		// @Override
		// public SortedSet<Integer> tailSet(Integer fromElement) {
		// return subSet(fromElement, size());
		// }
		//
		//
		// /**
		// *
		// * @return first contained element or -1 if index set empty
		// */
		// private int first() {
		// return this.bitSet.nextSetBit(0);
		// }
		//
		// @Override
		// public Integer last() {
		// return this.bitSet.previousSetBit(this.bitSet.size());
		// }

		private boolean hashCodeSet = false;

		private int hashCode = 0;

		@Override
		public int hashCode() {
			if (hashCodeSet) {
				return hashCode;
			}
			hashCodeSet = true;
			return hashCode = Objects.hash(bitSet);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof BitSetBackedIndexSet)) {
				return false;
			}

			BitSetBackedIndexSet bs = (BitSetBackedIndexSet) o;
			if (this.hashCodeSet && bs.hashCodeSet) {
				return (this.hashCode == bs.hashCode && this.bitSet.equals(bs.bitSet));
			}
			return this.bitSet.equals(bs.bitSet);
		}

		@Override
		public String toString() {
			return this.bitSet.toString();
		}

	}

	private IndexSets() {
		;
	}

}
