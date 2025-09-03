package ua.ac.be.mime.plain.weighting;

import java.util.BitSet;
import java.util.List;

import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.TransactionDBInterface;

/**
 * Interface for pos/neg labeled datasets
 * 
 * @author Sandy Moens
 */
public interface PosNegDbInterface extends TransactionDBInterface {

	public static enum Label {
		POSITIVE, NEGATIVE
	}

	/**
	 * This method returns the bitset of the transactions in the positive (resp.
	 * the negative) labeled data
	 * 
	 * @param label
	 *            indicates if the positive or negative transactions should be
	 *            returned
	 * @return bitsets containing the indices of the transactions in the
	 *         positive (resp. negative) labeled data
	 */
	public abstract BitSet getLabelSupportSet(Label label);

	/**
	 * Gets all transactions
	 * 
	 * @return a list of all transactions
	 */
	public abstract List<PlainTransaction> getTransactions();

	/**
	 * Gets all positive labeled transactions
	 * 
	 * @return a list of transactions of the positive data
	 */
	public abstract List<PlainTransaction> getTransactionsPos();

	/**
	 * Gets all negative labeled transactions
	 * 
	 * @return a list of transactions of the negative data
	 */
	public abstract List<PlainTransaction> getTransactionsNeg();

	/**
	 * Gets the size of the support set of the positive (resp. negative) data
	 * 
	 * @param label
	 *            the label of the data part
	 * @return the size of the support set of the positive (resp. negative)
	 *         labeled data
	 */
	public abstract int getLabelSupportSetSize(Label label);

	/**
	 * Converts the itemset to an itemset using transactions from the complete
	 * dataset
	 * 
	 * @param itemSet
	 *            the itemset that needs to be converted
	 * @return a new itemset with support set in the complete dataset
	 */
	public abstract PlainItemSet getCompleteDbItemSet(PlainItemSet itemSet);

	/**
	 * Converts the itemset to an itemset using transactions from the positive
	 * labeled dataset
	 * 
	 * @param itemSet
	 *            the itemset that needs to be converted
	 * @return a new itemset with support set in the positive labeled dataset
	 */
	public abstract PlainItemSet getPosItemSet(PlainItemSet itemSet);

	/**
	 * Converts the itemset to an itemset using transactions from the negative
	 * labeled dataset
	 * 
	 * @param itemSet
	 *            the itemset that needs to be converted
	 * @return a new itemset with support set in the negative labeled dataset
	 */
	public abstract PlainItemSet getNegItemSet(PlainItemSet itemSet);

	public abstract PlainTransaction addTransaction(String[] array,
			boolean isPos);
}