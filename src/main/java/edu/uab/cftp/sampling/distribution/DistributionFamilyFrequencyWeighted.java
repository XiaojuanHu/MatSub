package edu.uab.cftp.sampling.distribution;

import static com.google.common.collect.Maps.newHashMap;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.Utils;
import ua.ac.be.mime.plain.weighting.WeightedTransactionDBInterface;

public class DistributionFamilyFrequencyWeighted extends
		StateSpaceSamplingDistribution {

	public static class Tester extends DistributionTester {
		private final WeightedTransactionDBInterface wdb;

		int cardinality;

		public Tester(WeightedTransactionDBInterface db, int cardinality) {
			super(db);
			wdb = db;
			this.cardinality = cardinality;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			double weightedSupport = getWeightedSupport(itemSet);

			return Math.pow(weightedSupport, cardinality);
		}

		private double getWeightedSupport(PlainItemSet itemSet) {
			BitSet tids = itemSet.getTIDs();
			int ix = -1;
			double weightedSupport = 0;
			while ((ix = tids.nextSetBit(ix + 1)) != -1) {
				weightedSupport += wdb.getTransactionWeight(ix);
			}
			return weightedSupport;
		}
	}

	private final int numberOfExtraFreq;

	private final WeightedTransactionDBInterface wdb;

	private Map<TidList, Double> transactionWeights;

	public DistributionFamilyFrequencyWeighted(
			WeightedTransactionDBInterface db,
			NextStateProposer nextStateProposer, int numberOfExtraFreq) {
		super(db);
		wdb = db;
		this.numberOfExtraFreq = numberOfExtraFreq;
		intializeTransactionWeights();
		super.setNextStateProposer(nextStateProposer);
	}

	private void intializeTransactionWeights() {
		transactionWeights = newHashMap();
		for (int i = 0; i < wdb.getTransactions().size(); i++) {
			double weight = wdb.getTransactionWeight(i);
			if (weight != 1) {
				transactionWeights.put(wdb.getTransactions().get(i)
						.getItemsAsBitSet(), weight);
			}
		}
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return 1;
	}

	@Override
	public double getWeightInDistribution(TidList transaction) {
		return getWeightInDistribution(transaction,
				getTransactionWeight(transaction));
	}

	private Double getTransactionWeight(TidList transaction) {
		Double weight = transactionWeights.get(transaction);
		if (weight == null) {
			weight = 1.0;
		}
		return weight;
	}

	private double getWeight(TidList transaction) {
		double size = transaction.cardinality();
		return (Math.pow(2, size) - size - 1);
	}

	private double getWeightInDistribution(TidList transaction, double weight) {
		double size = transaction.cardinality();
		return (Math.pow(2, size) - size - 1) * weight;
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		return Utils.drawUniformSubSetNoSingletons(this.db.getItemDB(), Utils
				.intersectAll(Utils.getTransactions(this.transactions,
						baseObject.getIndices())));
	}

	@Override
	public double baseObjectWeight(OrderedBaseObject baseObject) {
		if (baseObject.getWeight() != -1) {
			return baseObject.getWeight();
		} else {
			List<PlainTransaction> transactions = Utils.getTransactions(
					this.transactions, baseObject.getIndices());

			TidList transactionIntersection = Utils.intersectAll(transactions);
			double weight = getWeight(transactionIntersection)
					* getBaseTransactionWeights(baseObject);

			baseObject.setWeight(weight);
			return weight;
		}
	}

	private double getBaseTransactionWeights(OrderedBaseObject baseObject) {
		double d = 1;
		for (int tid : baseObject.getIndices()) {
			d *= wdb.getTransactionWeight(tid);
		}
		return d;
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new Tester(wdb, cardinality());
	}

	@Override
	protected double computeNormalizationFactor() {
		return 0;
	}

	@Override
	public double numberOfBaseObjects() {
		return 0;
	}

	@Override
	public int cardinality() {
		return 1 + numberOfExtraFreq;
	}

	@Override
	public String toString() {
		String str = "";
		if (numberOfExtraFreq != 0) {
			str += "Freq";
			if (numberOfExtraFreq != 1) {
				str += "^" + numberOfExtraFreq;
			}
			str += " x ";
		}
		return str + "Weighted Freq";
	}

}
