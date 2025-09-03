package ua.ac.be.mime.plain.weighting;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import edu.uab.cftp.sampling.WeightReader;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.PlainTransactionDB;

/**
 * Class that implements a database of weighted transactions
 * 
 * @author Sandy Moens
 */
public class WeightedTransactionDB extends PlainTransactionDB implements
		WeightedTransactionDBInterface {

	private Map<Integer, Double> weightsMap;

	public WeightedTransactionDB() {
		super(new WeightedItemDb());
	}

	public WeightedTransactionDB(String fileName) {
		this();
		try {
			populateFromFile(fileName);
			weightsMap = newHashMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public WeightedTransactionDB(String fileName, String weightsFile) {
		this();
		try {
			populateFromFile(fileName);
			readAndSetWeights(weightsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readAndSetWeights(String weightsFile) {
		weightsMap = new WeightReader(weightsFile, "=").getWeightMap();
		setWeightsOnTransactions();
	}

	private void setWeightsOnTransactions() {
		for (Entry<Integer, Double> entry : weightsMap.entrySet()) {
			setWeightOnly(entry.getKey(), entry.getValue());
		}
	}

	private void setWeightOnly(Integer tid, Double weight) {
		((WeightedTransaction) transactions.get(tid)).setWeight(weight);
	}

	public void setWeight(Integer tid, Double weight) {
		setWeightOnly(tid, weight);
		weightsMap.put(tid, weight);
	}

	@Override
	public PlainTransaction newTransaction() {
		return new WeightedTransaction(transactions.size());
	}

	@Override
	public double getTransactionWeight(int tid) {
		Double weight = weightsMap.get(tid);
		if (weight != null) {
			return weight;
		}
		return 1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WeightedTransactionDB: #transactions="
				+ transactions.size() + "\n" + itemsDB);
		builder.append("WeightsMap: " + weightsMap + "\n");
		return builder.toString();
	}
}