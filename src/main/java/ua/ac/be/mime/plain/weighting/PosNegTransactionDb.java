package ua.ac.be.mime.plain.weighting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.PlainTransactionDB;
import ua.ac.be.mime.plain.TransactionDBInterface;

/**
 * A binary dataset with positive and negative labeled data.
 * 
 * @author Sandy Moens
 */
public class PosNegTransactionDb extends PlainTransactionDB implements
		PosNegDbInterface, TransactionDBInterface {

	// indicates if the labels in the datasetfile should be kept in the
	// transactions in memory
	private boolean keepLabels;

	// the tids of the positive labeled data
	protected final TidList posSupportSet = new TidList();
	// the tids of the negative labeled data
	protected final TidList negSupportSet = new TidList();

	private List<PlainTransaction> posTransactions;
	private List<PlainTransaction> negTransactions;

	protected PosNegTransactionDb(PlainItemDB itemDB) {
		super(itemDB);
	}

	public PosNegTransactionDb() {
	}

	public PosNegTransactionDb(String fileName) {
		this(fileName, false);
	}

	public PosNegTransactionDb(String fileName, boolean keepLabels) {
		this.keepLabels = keepLabels;
		try {
			populateFromFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PosNegTransactionDb(String fileNamePos, String fileNameNeg) {
		populateTransactionsFromFile(fileNamePos, this.posSupportSet);
		populateTransactionsFromFile(fileNameNeg, this.negSupportSet);
	}

	@Override
	public BitSet getLabelSupportSet(Label label) {
		switch (label) {
		case POSITIVE:
			return this.posSupportSet;
		case NEGATIVE:
			return this.negSupportSet;
		}
		return null;
	}

	@Override
	protected void populateFromFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		int size = -1;

		String labelPositive = null;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}

			StringTokenizer st = new StringTokenizer(line, " ");

			String label = null;
			int tid = this.transactions.size();
			PlainTransaction t = newTransaction();

			label = st.nextToken();
			if (this.keepLabels) {
				PlainItem item = this.itemsDB.get(label);
				item.setTID(tid);
				t.add(item);
				this.fullSize++;
			}
			while (st.hasMoreElements()) {
				PlainItem item = this.itemsDB.get(st.nextToken());
				item.setTID(tid);
				t.add(item);
				this.fullSize++;
			}

			if (labelPositive == null || labelPositive.equals(label)) {
				labelPositive = label;
				this.posSupportSet.set(tid);
			} else {
				this.negSupportSet.set(tid);
			}
			this.transactions.add(t);

			if (size == -1) {
				size = t.size();
			}
			if (t.size() != size) {
				this.isRectangular = false;
			}
		}

		reader.close();
	}

	protected void populateTransactionsFromFile(String fileName,
			TidList supportSet) throws IllegalArgumentException {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(fileName));

			PlainTransaction t;
			PlainItem item;
			int size = -1;

			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (line.length() == 0) {
					continue;
				}
				String[] splittedLine = line.split(Delimiter);

				t = newTransaction();
				for (String itemStr : splittedLine) {
					{
						item = this.itemsDB.get(itemStr);
						item.setTID(this.transactions.size());
						t.add(item);
					}
					{
						supportSet.set(this.transactions.size());
					}
					this.fullSize++;
				}
				this.transactions.add(t);

				if (size == -1) {
					size = t.size();
				}
				if (t.size() != size) {
					this.isRectangular = false;
				}
			}

			sc.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public PlainTransaction addTransaction(String[] itemNames, boolean isPos) {
		PlainTransaction tx = newTransaction();

		for (String itemName : itemNames) {
			PlainItem item = getItemByName(itemName);
			item.setTID(transactions.size());
			tx.add(item);
		}
		if (isPos) {
			this.posSupportSet.set(transactions.size());
		} else {
			this.negSupportSet.set(transactions.size());
		}
		transactions.add(tx);

		return tx;
	}

	public PlainTransaction addTransaction(Collection<String> itemNames,
			boolean isPos) {
		PlainTransaction tx = newTransaction();

		for (String itemName : itemNames) {
			PlainItem item = getItemByName(itemName);
			item.setTID(transactions.size());
			tx.add(item);
		}
		if (isPos) {
			this.posSupportSet.set(transactions.size());
		} else {
			this.negSupportSet.set(transactions.size());
		}
		transactions.add(tx);

		return tx;
	}

	private PlainItem getItemByName(String name) {
		return this.itemsDB.getByName(name);
	}

	@Override
	public List<PlainTransaction> getTransactionsPos() {
		if (this.posTransactions == null) {
			this.posTransactions = new ArrayList<PlainTransaction>(
					this.posSupportSet.cardinality());
			int ix = -1;
			while ((ix = this.posSupportSet.nextSetBit(ix + 1)) != -1) {
				this.posTransactions.add(this.transactions.get(ix));
			}
		}
		return this.posTransactions;
	}

	@Override
	public List<PlainTransaction> getTransactionsNeg() {
		if (this.negTransactions == null) {
			this.negTransactions = new ArrayList<PlainTransaction>(
					this.posSupportSet.cardinality());
			int ix = -1;
			while ((ix = this.negSupportSet.nextSetBit(ix + 1)) != -1) {
				this.negTransactions.add(this.transactions.get(ix));
			}
		}
		return this.negTransactions;
	}

	@Override
	public int getLabelSupportSetSize(Label label) {
		return getLabelSupportSet(label).size();
	}

	@Override
	public PlainItemSet getCompleteDbItemSet(PlainItemSet itemSet) {
		PlainItemSet completeItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			completeItemSet.add(this.itemsDB.get(item.getId()));
		}
		return completeItemSet;
	}

	@Override
	public PlainItemSet getPosItemSet(PlainItemSet itemSet) {
		PlainItemSet completeItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			PlainItem posItem = new PlainItem(item.getId(), TidList.intersect(
					this.posSupportSet,
					new TidList(this.itemsDB.get(item.getId()).getTIDs())));
			completeItemSet.add(posItem);
		}
		return completeItemSet;
	}

	@Override
	public PlainItemSet getNegItemSet(PlainItemSet itemSet) {
		PlainItemSet completeItemSet = new PlainItemSet();
		for (PlainItem item : itemSet) {
			PlainItem negItem = new PlainItem(item.getId(), TidList.intersect(
					this.negSupportSet,
					new TidList(this.itemsDB.get(item.getId()).getTIDs())));
			completeItemSet.add(negItem);
		}
		return completeItemSet;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("PosNeg TransactionDB: #transactions=" + transactions.size()
				+ " #pos=" + posSupportSet.cardinality() + " #neg="
				+ negSupportSet.cardinality() + "\n");
		buf.append(itemsDB + "\n");
		return buf.toString();
	}
}