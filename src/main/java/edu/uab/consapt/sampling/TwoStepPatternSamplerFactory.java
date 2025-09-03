package edu.uab.consapt.sampling;

import static com.google.common.collect.Maps.newHashMap;

import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;

import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;

public class TwoStepPatternSamplerFactory {

	public static TwoStepPatternSampler createFreqTimesFreqDistribution(
			TransactionDBInterface db) throws Exception {
		return createFreqTimesFreqDistribution(db, 0);
	}

	public static TwoStepPatternSampler createFreqTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount)
			throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.MULTIPLICATIVE);
		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(new DataPortion(db
					.getTransactions()));
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createRareTimesFreqDistribution(
			TransactionDBInterface db) throws Exception {
		return createRareTimesFreqDistribution(db, 0);
	}

	public static TwoStepPatternSampler createRareTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount)
			throws Exception {
		Map<Integer, Double> inverseBiasesMap = getInverseBiasesMap(db);
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1)
				.setStarOperation(StarOperation.MULTIPLICATIVE)
				.addSingletonWeights(inverseBiasesMap);

		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(new DataPortion(db
					.getTransactions()));
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createAreaTimesFreqDistribution(
			TransactionDBInterface db) throws Exception {
		return createAreaTimesFreqDistribution(db, 0);
	}

	public static TwoStepPatternSampler createAreaTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount)
			throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.ADDITIVE);
		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(new DataPortion(db
					.getTransactions()));
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createDiscrTimesFreqDistribution(
			PosNegDbInterface db) throws Exception {
		return createDiscrTimesFreqDistribution(db, 0, 0, 0);
	}

	public static TwoStepPatternSampler createDiscrTimesFreqDistribution(
			PosNegDbInterface db, int extraFreqFactorCount,
			int extraPosFreqFactorCount, int extraNegFreqFactorCount)
			throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.MULTIPLICATIVE);
		for (int i = 0; i < extraFreqFactorCount; i++) {
			builder = builder.addPositiveFactor(new DataPortion(db
					.getTransactions()));
		}
		for (int i = 0; i < extraPosFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(new DataPortion(db
					.getTransactionsPos()));
		}
		for (int i = 0; i < extraNegFreqFactorCount + 1; i++) {
			builder.addNegativeFactor(new DataPortion(db.getTransactionsNeg()));
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createWeightedFreqTimesFreqDistribution(
			TransactionDBInterface db, Map<PlainTransaction, Double> weights)
			throws Exception {
		return createWeightedFreqTimesFreqDistribution(db, 0, weights);
	}

	public static TwoStepPatternSampler createWeightedFreqTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount,
			Map<PlainTransaction, Double> weights) throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.MULTIPLICATIVE);
		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(
					new DataPortion(db.getTransactions()), weights);
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createWeightedRareTimesFreqDistribution(
			TransactionDBInterface db, Map<PlainTransaction, Double> weights)
			throws Exception {
		return createWeightedRareTimesFreqDistribution(db, 0, weights);
	}

	public static TwoStepPatternSampler createWeightedRareTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount,
			Map<PlainTransaction, Double> weights) throws Exception {
		Map<Integer, Double> inverseBiasesMap = getWeightedInverseBiasesMap(db,
				weights);
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1)
				.setStarOperation(StarOperation.MULTIPLICATIVE)
				.addSingletonWeights(inverseBiasesMap);
		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(
					new DataPortion(db.getTransactions()), weights);
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createWeightedAreaTimesFreqDistribution(
			TransactionDBInterface db, Map<PlainTransaction, Double> weights)
			throws Exception {
		return createWeightedAreaTimesFreqDistribution(db, 0, weights);
	}

	public static TwoStepPatternSampler createWeightedAreaTimesFreqDistribution(
			TransactionDBInterface db, int extraFreqFactorCount,
			Map<PlainTransaction, Double> weights) throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.ADDITIVE);
		for (int i = 0; i < extraFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(
					new DataPortion(db.getTransactions()), weights);
		}
		return builder.build();
	}

	public static TwoStepPatternSampler createWeightedDiscrTimesFreqDistribution(
			PosNegDbInterface db, Map<PlainTransaction, Double> weights)
			throws Exception {
		return createWeightedDiscrTimesFreqDistribution(db, 0, 0, 0, weights);
	}

	public static TwoStepPatternSampler createWeightedDiscrTimesFreqDistribution(
			PosNegDbInterface db, int extraFreqFactorCount,
			int extraPosFreqFactorCount, int extraNegFreqFactorCount,
			Map<PlainTransaction, Double> weights) throws Exception {
		TwoStepPatternSampler.Builder builder = new TwoStepPatternSampler.Builder(
				db);
		builder = builder.setDefaultSingletonWeight(1).setStarOperation(
				StarOperation.MULTIPLICATIVE);
		for (int i = 0; i < extraFreqFactorCount; i++) {
			builder = builder.addPositiveFactor(
					new DataPortion(db.getTransactions()), weights);
		}
		for (int i = 0; i < extraPosFreqFactorCount + 1; i++) {
			builder = builder.addPositiveFactor(
					new DataPortion(db.getTransactionsPos()), weights);
		}
		for (int i = 0; i < extraNegFreqFactorCount + 1; i++) {
			builder.addNegativeFactor(new DataPortion(db.getTransactionsNeg()),
					weights);
		}
		return builder.build();
	}

	private static Map<Integer, Double> getInverseBiasesMap(
			TransactionDBInterface pdb) {
		Map<Integer, Double> biases = newHashMap();
		for (PlainItem item : pdb.getItemDB()) {
			biases.put(item.getId(), 1 - 1.0 * (item.getTIDs().cardinality())
					/ pdb.getTransactions().size());
		}

		return biases;
	}

	private static Map<Integer, Double> getWeightedInverseBiasesMap(
			TransactionDBInterface pdb, Map<PlainTransaction, Double> weights) {
		double totalWeight = 0;
		for (Entry<PlainTransaction, Double> weight : weights.entrySet()) {
			totalWeight += weight.getValue();
		}
		Map<Integer, Double> biases = newHashMap();
		for (PlainItem item : pdb.getItemDB()) {
			biases.put(item.getId(),
					1 - getWeightedSupport(pdb, item.getTIDs(), weights)
							/ totalWeight);
		}
		return biases;
	}

	private static double getWeightedSupport(TransactionDBInterface pdb,
			BitSet tids, Map<PlainTransaction, Double> weights) {
		int ix = -1;
		double weightedSupport = 0;
		while ((ix = tids.nextSetBit(ix + 1)) != -1) {
			weightedSupport += weights.get(pdb.getTransactions().get(ix));
		}
		return weightedSupport;
	}

}
