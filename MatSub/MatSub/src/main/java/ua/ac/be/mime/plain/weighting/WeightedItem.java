package ua.ac.be.mime.plain.weighting;

import java.util.BitSet;

import ua.ac.be.mime.plain.PlainItem;

/**
 * Class that implements a weighted item
 * 
 * @author Sandy Moens
 */
public class WeightedItem extends PlainItem {

	private double weight;

	/**
	 * Creates a new Weighted item with the specified id and a weight of 1
	 * 
	 * @param itemId
	 *            the id of this item
	 */
	public WeightedItem(int itemId) {
		this(itemId, 1);
	}

	/**
	 * Creates a new Weighted item with the specified id and weight
	 * 
	 * @param itemId
	 *            the id of this item
	 * @param weight
	 *            the initial weight of the item
	 */
	public WeightedItem(int itemId, double weight) {
		this(itemId, new BitSet(), weight);
	}

	/**
	 * Creates a new Weighted item with the specified id, bitset and weight
	 * 
	 * @param itemId
	 *            the id of this item
	 * @param tids
	 *            the transaction list covered by this item
	 * @param weight
	 *            the initial weight of the item
	 */
	public WeightedItem(int itemId, BitSet tids, double weight) {
		super(itemId, tids);

		this.weight = weight;
	}

	/**
	 * Gets the weight of the item
	 * 
	 * @return the weight
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Sets the weight of the item
	 * 
	 * @param weight
	 *            the new weight of the item
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WeightedItem) {
			if (super.equals(obj)) {
				return this.weight == ((WeightedItem) obj).weight;
			} else {
				return false;
			}
		}
		return false;
	}
}
