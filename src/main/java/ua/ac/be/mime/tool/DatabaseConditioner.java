package ua.ac.be.mime.tool;

import java.util.BitSet;

import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.PlainTransactionDB;

public class DatabaseConditioner {

	public static PlainTransactionDB conditionDB(PlainTransactionDB db,
			PlainItemSet items, BitSet tids) {
		PlainTransactionDB newDb = new PlainTransactionDB();
		int ix = 0;
		for (PlainTransaction t : db.getTransactions()) {
			boolean remove = false;
			if (tids.get(ix)) {
				remove = true;
			}

			PlainTransaction newT = new PlainTransaction(ix);
			for (PlainItem item : t) {
				PlainItem newItem = newDb.getItem(item.getId());
				if (!remove
						|| (remove && (!tids.get(ix) || !items.contains(item)))) {
					newItem.setTID(ix);
					newT.add(newItem);
				}
			}
			newDb.add(newT);
			ix++;
		}
		return newDb;
	}

	public static void print(PlainTransactionDB db) {
		StringBuilder builder = new StringBuilder();
		for (PlainTransaction transaction : db.getTransactions()) {
			for (PlainItem item : transaction) {
				builder.append(item.getId() + " ");
			}
			builder.append("\n");
		}
		System.out.println(builder.toString());
	}

	public static void main(String[] args) {
		PlainTransactionDB db = new PlainTransactionDB("testData/smallDb.txt");
		print(db);

		PlainItemSet items = new PlainItemSet(db.getItem(2), db.getItem(4));
		BitSet tids = new BitSet();
		tids.set(0);
		tids.set(1);

		System.out.println("==========================");
		System.out.println("Filter:");
		System.out.println("Items:" + items);
		System.out.println("Tids:" + tids);
		System.out.println("==========================");

		PlainTransactionDB conditionedDb = conditionDB(db, items, tids);
		print(conditionedDb);

		items = new PlainItemSet(db.getItem(1), db.getItem(5));
		tids = new BitSet();
		tids.set(2);
		tids.set(3);

		System.out.println("==========================");
		System.out.println("Filter:");
		System.out.println("Items:" + items);
		System.out.println("Tids:" + tids);
		System.out.println("==========================");

		conditionedDb = conditionDB(conditionedDb, items, tids);
		print(conditionedDb);
	}
}
