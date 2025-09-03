package ua.ac.be.mime.plain;

import java.util.List;

public interface TransactionDBInterface {

	public PlainItemDB getItemDB();

	public List<PlainTransaction> getTransactions();

	public boolean isRectangular();
}