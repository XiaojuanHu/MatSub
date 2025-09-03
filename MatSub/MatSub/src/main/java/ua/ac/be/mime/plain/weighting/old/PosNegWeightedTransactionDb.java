package ua.ac.be.mime.plain.weighting.old;

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.WeightedItemDb;
import ua.ac.be.mime.plain.weighting.WeightedTransaction;
import ua.ac.be.mime.plain.weighting.WeightedTransactionDB;

@Deprecated
public class PosNegWeightedTransactionDb extends WeightedTransactionDB {

	private final List<PlainTransaction> posWeightedTransactions = new ArrayList<PlainTransaction>();
	private final List<PlainTransaction> negWeightedTransactions = new ArrayList<PlainTransaction>();

	private final PlainItemDB posItemDB;
	private final PlainItemDB negItemDB;

	private boolean isRectangularPos;
	private boolean isRectangularNeg;

	protected static final String Delimiter = " ";

	public PosNegWeightedTransactionDb() {
		this.posItemDB = new WeightedItemDb();
		this.negItemDB = new WeightedItemDb();
		this.isRectangularPos = true;
		this.isRectangularNeg = true;
	}

	public PosNegWeightedTransactionDb(String fileNamePos) {
		this.posItemDB = new WeightedItemDb();
		this.isRectangularPos = true;
		populatePosTransactionsFromFile(fileNamePos);

		this.negItemDB = new WeightedItemDb();
		this.isRectangularNeg = true;
		populateNegTransactionsFromFile("empty.txt");
	}

	public PosNegWeightedTransactionDb(String fileNamePos, String fileNameNeg) {
		this.posItemDB = new WeightedItemDb();
		this.isRectangularPos = true;
		populatePosTransactionsFromFile(fileNamePos);

		this.negItemDB = new WeightedItemDb();
		this.isRectangularNeg = true;
		populateNegTransactionsFromFile(fileNameNeg);
	}

	protected void populatePosTransactionsFromFile(String fileName)
			throws IllegalArgumentException {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(fileName));

			WeightedTransaction t, tx;
			PlainItem item;
			int size = -1;

			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (line.length() == 0) {
					continue;
				}
				String[] splittedLine = line.split(Delimiter);

				t = new WeightedTransaction(this.transactions.size());
				tx = new WeightedTransaction(
						this.posWeightedTransactions.size());
				for (String itemStr : splittedLine) {
					{
						item = this.itemsDB.get(itemStr);
						item.setTID(this.transactions.size());
						t.add(item);
					}
					{
						item = this.posItemDB.get(itemStr);
						item.setTID(this.posWeightedTransactions.size());
						tx.add(item);
					}
					this.fullSize++;
				}
				this.transactions.add(t);
				this.posWeightedTransactions.add(tx);

				if (size == -1) {
					size = t.size();
				}
				if (t.size() != size) {
					this.isRectangularPos = false;
				}
			}

			sc.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected void populateNegTransactionsFromFile(String fileName)
			throws IllegalArgumentException {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(fileName));

			WeightedTransaction t, tx;
			PlainItem item;
			int size = -1;
			int cSize = this.isRectangularPos ? this.posWeightedTransactions
					.get(0).size() : -1;

			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (line.length() == 0) {
					continue;
				}
				String[] splittedLine = line.split(Delimiter);

				t = new WeightedTransaction(this.transactions.size());
				tx = new WeightedTransaction(
						this.negWeightedTransactions.size());
				for (String itemStr : splittedLine) {
					{
						item = this.itemsDB.get(itemStr);
						item.setTID(this.transactions.size());
						t.add(item);
					}
					{
						item = this.negItemDB.get(itemStr);
						item.setTID(this.negWeightedTransactions.size());
						tx.add(item);
					}
					this.fullSize++;
				}
				this.transactions.add(t);
				this.negWeightedTransactions.add(tx);

				if (size == -1) {
					size = t.size();
				}
				if (t.size() != size) {
					this.isRectangularNeg = false;
				} else {
					size = t.size();
				}
				if (t.size() != cSize) {
					this.isRectangular = false;
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public PlainTransaction addTransaction(String[] itemNames, boolean isPos) {
		PlainTransaction t = new PlainTransaction(this.transactions.size());
		PlainTransaction tx;
		if (isPos) {
			tx = new PlainTransaction(posWeightedTransactions.size());
		} else {
			tx = new PlainTransaction(negWeightedTransactions.size());
		}

		for (String itemName : itemNames) {
			{
				PlainItem item = itemsDB.getByName(itemName);
				item.setTID(transactions.size());
				t.add(item);
				if (transactions.size() > 0) {
					isRectangularPos = isRectangular
							&& t.size() == transactions.get(0).size();
				}
			}
			{
				PlainItem item = getItemByName(itemName, isPos);
				item.setTID(size(isPos));
				tx.add(item);
				updateIsRectangular(tx.size(), isPos);
			}
		}
		transactions.add(t);
		add(tx, isPos);

		return tx;
	}

	private void updateIsRectangular(int size, boolean pos) {
		if (pos) {
			if (posWeightedTransactions.size() < 1) {
				return;
			}
			isRectangularPos = isRectangularNeg
					&& size == posWeightedTransactions.get(0).size();
			return;
		}
		if (negWeightedTransactions.size() < 1) {
			return;
		}
		isRectangularNeg = isRectangularNeg
				&& size == negWeightedTransactions.get(0).size();
	}

	private void add(PlainTransaction transaction, boolean pos) {
		if (pos) {
			posWeightedTransactions.add(transaction);
			return;
		}
		negWeightedTransactions.add(transaction);
	}

	private int size(boolean pos) {
		if (pos) {
			return posWeightedTransactions.size();
		}
		return negWeightedTransactions.size();
	}

	private PlainItem getItemByName(String itemName, boolean isPos) {
		PlainItemDB itemDB = posItemDB;
		if (!isPos) {
			itemDB = negItemDB;
		}
		return itemDB.getByName(itemName);
	}

	public List<PlainTransaction> getTransactionsPos() {
		return unmodifiableList(this.posWeightedTransactions);
	}

	public List<PlainTransaction> getTransactionsNeg() {
		return unmodifiableList(this.negWeightedTransactions);
	}

	public PlainItemSet getCompleteDbItemSet(PlainItemSet itemSet) {
		PlainItemSet completeItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			completeItemSet.add(this.itemsDB.get(item.getId()));
		}
		return completeItemSet;
	}

	public PlainItemSet getPosItemSet(PlainItemSet itemSet) {
		PlainItemSet posItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			posItemSet.add(this.posItemDB.get(item.getId()));
		}
		return posItemSet;
	}

	public PlainItemSet getNegItemSet(PlainItemSet itemSet) {
		PlainItemSet negItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			negItemSet.add(this.negItemDB.get(item.getId()));
		}
		return negItemSet;
	}

	public boolean isRectangularPos() {
		return this.isRectangularPos;
	}

	public boolean isRectangularNeg() {
		return this.isRectangularNeg;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("PosNeg (Weighted) TransactionDB: #transactions="
				+ transactions.size() + " #pos="
				+ posWeightedTransactions.size() + " #neg="
				+ negWeightedTransactions.size() + "\n");
		buf.append(itemsDB + "\n");
		return buf.toString();
	}
}
