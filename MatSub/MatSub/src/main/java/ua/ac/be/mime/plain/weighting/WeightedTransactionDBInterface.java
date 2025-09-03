package ua.ac.be.mime.plain.weighting;

import ua.ac.be.mime.plain.TransactionDBInterface;

public interface WeightedTransactionDBInterface extends TransactionDBInterface {

	public double getTransactionWeight(int tid);
}
