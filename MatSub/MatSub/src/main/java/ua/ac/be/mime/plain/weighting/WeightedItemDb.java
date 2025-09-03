package ua.ac.be.mime.plain.weighting;

import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemDB;

/**
 * Class that implements a database of weighted items
 * 
 * @author Sandy
 */
public class WeightedItemDb extends PlainItemDB {

	@Override
	protected PlainItem newItem(int itemId) {
		return new WeightedItem(itemId);
	}
}
