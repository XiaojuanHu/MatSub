package edu.uab.consapt.sampling;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static edu.uab.cftp.sampling.distribution.tool.StarOperation.MULTIPLICATIVE;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.consapt.sampling.cftp.AdditivePotential;
import edu.uab.consapt.sampling.cftp.BaseDistribution;
import edu.uab.consapt.sampling.cftp.CFTPSampler;
import edu.uab.consapt.sampling.cftp.DistributionPotentialsAllSizeOfMaxTransaction;
import edu.uab.consapt.sampling.cftp.DistributionPotentialsComputerFromTransactions;
import edu.uab.consapt.sampling.cftp.IntersectingAdditivePotential;
import edu.uab.consapt.sampling.cftp.IntersectingMultiplicativePotential;
import edu.uab.consapt.sampling.cftp.IntersectingWeightedAdditivePotential;
import edu.uab.consapt.sampling.cftp.IntersectingWeightedMultiplicativePotential;
import edu.uab.consapt.sampling.cftp.MultiplicativePotential;
import edu.uab.consapt.sampling.cftp.PotentialsComputer;
import edu.uab.consapt.sampling.cftp.ProductDistribution;
import edu.uab.consapt.sampling.cftp.WeightedBaseDistribution;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.Pair;

public class TwoStepPatternSampler implements StoppableSampler<PlainItemSet> {

	public static class Builder {

		boolean singletonWeightsAreSet = false;
		boolean transactionWeightsAreSet = false;

		final TransactionDBInterface db;
		double defaultSingletonWeight = 1;
		Map<Integer, Double> singletonWeights;
		List<Map<PlainTransaction, Double>> transactionWeights;

		StarOperation starOperation = StarOperation.ADDITIVE;
		List<DistributionFactor> distributionFactors = newArrayList();

		public Builder(TransactionDBInterface db) {
			this.db = db;
			this.transactionWeights = newArrayList();
		}

		public Builder setDefaultSingletonWeight(double weight) {
			defaultSingletonWeight = weight;
			return this;
		}

		public Builder addSingletonWeights(
				Map<Integer, Double> singletonsWeights) {
			singletonWeights = newHashMap(singletonsWeights);
			singletonWeightsAreSet = true;
			return this;
		}

		public Builder setStarOperation(StarOperation starOperation) {
			this.starOperation = starOperation;
			return this;
		}

		public Builder addPositiveFactor(DataPortion dataPortion) {
			addPositiveFactor(dataPortion,
					Maps.<PlainTransaction, Double> newHashMap());
			return this;
		}

		public Builder addPositiveFactor(DataPortion dataPortion,
				Map<PlainTransaction, Double> weights) {
			distributionFactors.add(new DistributionFactor(dataPortion,
					SupportMeasure.POSITIVE));
			transactionWeights.add(weights);
			transactionWeightsAreSet = true;
			return this;
		}

		public Builder addNegativeFactor(DataPortion dataPortion) {
			addNegativeFactor(dataPortion,
					Maps.<PlainTransaction, Double> newHashMap());
			return this;
		}

		public Builder addNegativeFactor(DataPortion dataPortion,
				Map<PlainTransaction, Double> weights) {
			distributionFactors.add(new DistributionFactor(dataPortion,
					SupportMeasure.NEGATIVE));
			transactionWeights.add(weights);
			transactionWeightsAreSet = true;
			return this;
		}

		public TwoStepPatternSampler build() throws Exception {
			if (transactionWeightsAreSet) {
				sortQFunctionsAndWeights();
			} else {
				sortQFunctions();
			}
			transactionWeightsAreSet = false;
			for (Map<PlainTransaction, Double> m : transactionWeights) {
				if (!m.isEmpty()) {
					transactionWeightsAreSet = true;
					break;
				}
			}
			return new TwoStepPatternSampler(this);
		}

		private void sortQFunctionsAndWeights() {
			List<Pair<DistributionFactor, Map<PlainTransaction, Double>>> listToSort = newArrayList();
			for (int i = 0; i < distributionFactors.size(); i++) {
				listToSort.add(new Pair(distributionFactors.get(i),
						transactionWeights.get(i)));
			}
			Collections
					.sort(listToSort,
							new Comparator<Pair<DistributionFactor, Map<PlainTransaction, Double>>>() {
								@Override
								public int compare(
										Pair<DistributionFactor, Map<PlainTransaction, Double>> o1,
										Pair<DistributionFactor, Map<PlainTransaction, Double>> o2) {
									int c = o1
											.getFirst()
											.getSupportMeasure()
											.compareTo(
													o2.getFirst()
															.getSupportMeasure());
									return c;
								}
							});
			List<DistributionFactor> sortedQFunctions = newArrayList();
			List<Map<PlainTransaction, Double>> sortedWeights = newArrayList();
			for (Pair<DistributionFactor, Map<PlainTransaction, Double>> entry : listToSort) {
				sortedQFunctions.add(entry.getFirst());
				sortedWeights.add(entry.getSecond());
			}
			distributionFactors = sortedQFunctions;
			transactionWeights = sortedWeights;
		}

		private void sortQFunctions() {
			Collections.sort(distributionFactors,
					new Comparator<DistributionFactor>() {
						@Override
						public int compare(DistributionFactor o1,
								DistributionFactor o2) {
							int c = o1.getSupportMeasure().compareTo(
									o2.getSupportMeasure());
							return c;
						}
					});
		}

	}

	private static StoppableSampler<List<PlainTransaction>> createStepOneSampler(
			Builder builder) throws Exception {
		return new CFTPSampler(createProductDistribution(builder),
				createIntersectionDistribution(builder));
	}

	private static StoppableSamplerWithInput<List<PlainTransaction>, PlainItemSet> createStepTwoSampler(
			Builder builder) {
		if (canSampleUniformly(builder)) {
			return new UniformSubsetSampler(builder.db.getItemDB());
		} else if (!builder.singletonWeightsAreSet
				&& builder.starOperation.equals(StarOperation.ADDITIVE)
				&& getNegSupportCount(builder) == 0) {
			return new SequentialSubsetSamplerAdditiveNoNegatives(
					builder.db.getItemDB());
		} else if (builder.singletonWeightsAreSet) {
			return new SequentialSubsetSampler(builder.db.getItemDB(),
					builder.starOperation, getPosSupportCount(builder),
					getNegSupportCount(builder), builder.singletonWeights);
		}
		return new SequentialSubsetSampler(builder.db.getItemDB(),
				builder.starOperation, getPosSupportCount(builder),
				getNegSupportCount(builder));
	}

	private static PotentialFunction<List<PlainTransaction>> createIntersectionDistribution(
			Builder builder) throws Exception {
		if (builder.singletonWeightsAreSet) {
			if (builder.transactionWeightsAreSet) {
				if (builder.starOperation.equals(MULTIPLICATIVE)) {
					return new IntersectingWeightedMultiplicativePotential(
							getPosSupportCount(builder),
							getNegSupportCount(builder),
							builder.singletonWeights,
							builder.transactionWeights);
				}
				return new IntersectingWeightedAdditivePotential(
						getPosSupportCount(builder),
						getNegSupportCount(builder), builder.singletonWeights,
						builder.transactionWeights);
			}
			if (builder.starOperation.equals(MULTIPLICATIVE)) {
				return new IntersectingMultiplicativePotential(
						getPosSupportCount(builder),
						getNegSupportCount(builder), builder.singletonWeights);
			}
			return new IntersectingAdditivePotential(
					getPosSupportCount(builder), getNegSupportCount(builder),
					builder.singletonWeights);
		}
		if (builder.transactionWeightsAreSet) {
			if (builder.starOperation.equals(MULTIPLICATIVE)) {
				return new IntersectingWeightedMultiplicativePotential(
						getPosSupportCount(builder),
						getNegSupportCount(builder), builder.transactionWeights);
			}
			return new IntersectingWeightedAdditivePotential(
					getPosSupportCount(builder), getNegSupportCount(builder),
					builder.transactionWeights);
		}
		if (builder.starOperation.equals(MULTIPLICATIVE)) {
			return new IntersectingMultiplicativePotential(
					getPosSupportCount(builder), getNegSupportCount(builder));
		}
		return new IntersectingAdditivePotential(getPosSupportCount(builder),
				getNegSupportCount(builder));
	}

	private static AbstractDistribution<List<PlainTransaction>> createProductDistribution(
			Builder builder) throws Exception {
		BaseDistribution<PlainTransaction>[] baseDistributions = new BaseDistribution[builder.distributionFactors
				.size()];
		int i = 0;
		for (DistributionFactor qFunction : builder.distributionFactors) {
			if (builder.transactionWeightsAreSet) {
				baseDistributions[i] = createWeightedBaseDistribution(builder,
						qFunction, builder.transactionWeights.get(i));
			} else {
				baseDistributions[i] = createBaseDistribution(builder,
						qFunction);
			}
			i++;
		}
		return new ProductDistribution(baseDistributions);
	}

	private static BaseDistribution<PlainTransaction> createWeightedBaseDistribution(
			Builder builder, DistributionFactor qFunction,
			Map<PlainTransaction, Double> map) throws Exception {
		List<PlainTransaction> transactions = qFunction.getDataPortion()
				.getTransactions();
		PotentialFunction<Collection<PlainItem>> potentialFunction = createPotentialFunction(builder);
		PotentialsComputer potentialsComputer = createPotentialsComputer(
				builder, qFunction, potentialFunction);

		return new WeightedBaseDistribution<PlainTransaction>(transactions,
				potentialsComputer.getPotentials(transactions), map);
	}

	private static BaseDistribution<PlainTransaction> createBaseDistribution(
			Builder builder, DistributionFactor qFunction) throws Exception {
		List<PlainTransaction> transactions = qFunction.getDataPortion()
				.getTransactions();
		PotentialFunction<Collection<PlainItem>> potentialFunction = createPotentialFunction(builder);
		PotentialsComputer potentialsComputer = createPotentialsComputer(
				builder, qFunction, potentialFunction);

		return new BaseDistribution<PlainTransaction>(transactions,
				potentialsComputer.getPotentials(transactions));
	}

	private static PotentialsComputer createPotentialsComputer(Builder builder,
			DistributionFactor qFunction,
			PotentialFunction<Collection<PlainItem>> potentialFunction)
			throws Exception {
		switch (qFunction.getSupportMeasure()) {
		case POSITIVE:
			return new DistributionPotentialsComputerFromTransactions(
					potentialFunction);
		case NEGATIVE:
			return new DistributionPotentialsAllSizeOfMaxTransaction(
					potentialFunction, builder.db.getItemDB(),
					builder.db.getTransactions());
		}
		throw new Exception("Illegal support measure specified");

	}

	private static PotentialFunction<Collection<PlainItem>> createPotentialFunction(
			Builder builder) throws Exception {
		switch (builder.starOperation) {
		case MULTIPLICATIVE:
			return newMultiplicativePotential(builder);
		case ADDITIVE:
			return newAdditivePotential(builder);
		}
		throw new Exception("Invalid Configuration");
	}

	private static PotentialFunction<Collection<PlainItem>> newAdditivePotential(
			Builder builder) {
		if (builder.singletonWeightsAreSet) {
			return new AdditivePotential(builder.distributionFactors.size(),
					builder.singletonWeights);
		}
		return new AdditivePotential(builder.distributionFactors.size());
	}

	private static PotentialFunction<Collection<PlainItem>> newMultiplicativePotential(
			Builder builder) {
		if (builder.singletonWeightsAreSet) {
			return new MultiplicativePotential(
					builder.distributionFactors.size(),
					builder.singletonWeights);
		}
		return new MultiplicativePotential(builder.distributionFactors.size());
	}

	private static boolean canSampleUniformly(Builder builder) {
		return builder.starOperation.equals(MULTIPLICATIVE)
				&& getPosSupportCount(builder) == builder.distributionFactors
						.size() && !builder.singletonWeightsAreSet;
	}

	private static int getSupportMeasureCount(Builder builder,
			SupportMeasure supportMeasure) {
		int cardinality = 0;
		for (DistributionFactor qFunction : builder.distributionFactors) {
			if (qFunction.getSupportMeasure().equals(supportMeasure)) {
				cardinality++;
			}
		}
		return cardinality;
	}

	private static int getPosSupportCount(Builder builder) {
		return getSupportMeasureCount(builder, SupportMeasure.POSITIVE);
	}

	private static int getNegSupportCount(Builder builder) {
		return getSupportMeasureCount(builder, SupportMeasure.NEGATIVE);
	}

	private final StoppableSampler<List<PlainTransaction>> stepOneSampler;
	private final StoppableSamplerWithInput<List<PlainTransaction>, PlainItemSet> stepTwoSampler;
	private boolean isStop;

	private TwoStepPatternSampler(Builder builder) throws Exception {
		stepOneSampler = createStepOneSampler(builder);
		stepTwoSampler = createStepTwoSampler(builder);
		isStop = false;
	}

	@Override
	public PlainItemSet getNext() {
		List<PlainTransaction> baseObject = stepOneSampler.getNext();
		if (isStop) {
			return null;
		}
		stepTwoSampler.setContext(baseObject);
		if (isStop) {
			return null;
		}
		return stepTwoSampler.getNext();
	}

	@Override
	public void setStop(boolean isStop) {
		this.isStop = isStop;
		stepOneSampler.setStop(isStop);
	}

}
