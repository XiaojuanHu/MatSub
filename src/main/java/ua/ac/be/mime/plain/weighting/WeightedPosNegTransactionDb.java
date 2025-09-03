package ua.ac.be.mime.plain.weighting;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import edu.uab.cftp.sampling.WeightReader;
import ua.ac.be.mime.plain.PlainTransaction;

/**
 * A binary dataset with positive and negative labeled data.
 * 
 * @author Sandy Moens
 */
public class WeightedPosNegTransactionDb extends PosNegTransactionDb implements
		WeightedPosNegDbInterface {

	public WeightedPosNegTransactionDb() {
		super(new WeightedItemDb());
	}

	public WeightedPosNegTransactionDb(String fileName) {
		this();
		try {
			populateFromFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public WeightedPosNegTransactionDb(String fileName, String weightsFile) {
		this();
		try {
			populateFromFile(fileName);
			readAndSetWeights(weightsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAndSetWeights(String weightsFile) {
		Map<Integer, Double> weightsMap = new WeightReader(weightsFile, "=")
				.getWeightMap();
		setWeightsOnTransactions(weightsMap);
	}

	private void setWeightsOnTransactions(Map<Integer, Double> weightsMap) {
		for (Entry<Integer, Double> entry : weightsMap.entrySet()) {
			setWeightOnly(entry.getKey(), entry.getValue());
		}
	}

	private void setWeightOnly(Integer tid, Double weight) {
		((WeightedTransaction) getTransactions().get(tid)).setWeight(weight);
	}

	@Override
	public PlainTransaction newTransaction() {
		return new WeightedTransaction(transactions.size());
	}

	@Override
	public double getTransactionWeight(int tid) {
		return ((WeightedTransaction) getTransactions().get(tid)).getWeight();
	}

	@Override
	public double getPosTransactionWeight(int tid) {
		return ((WeightedTransaction) getTransactionsPos().get(tid))
				.getWeight();
	}

	@Override
	public double getNegTransactionWeight(int tid) {
		return ((WeightedTransaction) getTransactionsNeg().get(tid))
				.getWeight();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Weighted PosNeg TransactionDB: #transactions="
				+ transactions.size() + " #pos=" + posSupportSet.cardinality()
				+ " #neg=" + negSupportSet.cardinality() + "\n");
		buf.append(itemsDB + "\n");
		return buf.toString();
	}

}