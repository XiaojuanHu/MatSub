package ua.ac.be.mime.plain;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Sandy Moens
 */
public class PlainTransactionDB implements TransactionDBInterface {
	protected static final String Delimiter = " ";

	protected List<PlainTransaction> transactions = newArrayList();

	protected PlainItemDB itemsDB;

	protected boolean isRectangular;
	protected long fullSize;

	protected PlainTransactionDB(PlainItemDB itemsDB) {
		this.itemsDB = itemsDB;
		this.isRectangular = true;
		this.fullSize = 0;
	}

	public PlainTransactionDB() {
		this(new PlainItemDB());
	}

	public PlainTransactionDB(String fileName) {
		this();
		try {
			populateFromFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected PlainTransaction newTransaction() {
		return new PlainTransaction(transactions.size());
	}

	protected void populateFromFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		int size = -1;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}
			String[] splittedLine = line.split(Delimiter);

			PlainTransaction tx = newTransaction();
			for (String itemStr : splittedLine) {
				PlainItem item = this.itemsDB.get(itemStr);
				item.setTID(this.transactions.size());
				tx.add(item);
				this.fullSize++;
			}
			this.transactions.add(tx);

			if (size == -1) {
				size = tx.size();
			}
			if (tx.size() != size) {
				this.isRectangular = false;
			}
		}
		reader.close();
	}

	public PlainTransaction addTransaction(String[] itemNames) {
		PlainTransaction tx = newTransaction();

		for (String itemName : itemNames) {
			PlainItem item = getItemByName(itemName);
			item.setTID(transactions.size());
			tx.add(item);
		}
		transactions.add(tx);

		return tx;
	}

	private PlainItem getItemByName(String name) {
		return this.itemsDB.getByName(name);
	}

	public PlainItem getItem(int itemId) {
		return this.itemsDB.get(itemId);
	}

	public PlainItem getItem(String itemId) {
		return this.itemsDB.get(itemId);
	}

	public void add(PlainTransaction plainTransaction) {
		for (PlainItem item : plainTransaction) {
			item.setTID(this.transactions.size());
		}
		this.transactions.add(plainTransaction);
	}

	@Override
	public boolean isRectangular() {
		return this.isRectangular;
	}

	public PlainItemSet getItemSet(List<Integer> items) {
		PlainItemSet itemSet = new PlainItemSet();

		for (Integer item : items) {
			itemSet.add(this.itemsDB.get(item));
		}

		return itemSet;
	}

	public long getFullSize() {
		return this.fullSize;
	}

	@Override
	public String toString() {
		return "TransactionDB: #transactions=" + transactions.size() + "\n"
				+ itemsDB;
	}

	@Override
	public List<PlainTransaction> getTransactions() {
		return unmodifiableList(this.transactions);
	}

	@Override
	public PlainItemDB getItemDB() {
		return this.itemsDB;
	}

}
