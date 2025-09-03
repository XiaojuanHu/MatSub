package edu.uab.cftp.sampling.distribution;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static edu.uab.cftp.sampling.distribution.tool.Biases.bias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import edu.uab.cftp.sampling.GeneralWeightBasedLabeledNextStateProposer;
import edu.uab.cftp.sampling.ImpossibleToSampleException;
import edu.uab.cftp.sampling.distribution.tool.Bias;
import edu.uab.cftp.sampling.distribution.tool.SequentialSubsetSampler;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.cftp.sampling.distribution.tool.SubsetSampler;
import edu.uab.consapt.sampling.DataPortion;
import edu.uab.consapt.sampling.DistributionFactor;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;

public class GeneralDistribution extends LabeledStateSpaceSamplingDistribution {

	public static enum SupportMeasure {
		POSITIVE, NEGATIVE;

		public static SupportMeasure getSupportMeasure(String string) {
			if (string.equalsIgnoreCase("positive")) {
				return POSITIVE;
			}
			return NEGATIVE;
		}
	}

	private double defaultBias = 1;
	private final Map<Integer, Double> itemBiases;

	private StarOperation starOperation = StarOperation.MULTIPLICATIVE;

	private final PosNegDbInterface posNegDb;

	private final List<DistributionFactor> qFunctions = new LinkedList<DistributionFactor>();

	private Bias biasComputer;
	private SubsetSampler subsetSampler;

	public GeneralDistribution(PosNegDbInterface db) {
		super(db);
		this.posNegDb = db;
		itemBiases = new HashMap<Integer, Double>();
	}

	public void initializeForDirectSampling() {
		this.baseObjects = new OrderedBaseObject[(int) numberOfBaseObjects()];
		initialize();
		computeCumulatedWeights();
	}

	public void initializeForCFTP() throws ImpossibleToSampleException {
		this.biasComputer = bias(starOperation, this.defaultBias, this.itemBiases);
		this.subsetSampler = new SequentialSubsetSampler(this.biasComputer);

		GeneralWeightBasedLabeledNextStateProposer nsp = new GeneralWeightBasedLabeledNextStateProposer();

		nsp.setDistribution(this);
		nsp.setPosTransactions(this.posNegDB.getTransactionsPos());
		nsp.setNegTransactions(this.posNegDB.getTransactionsNeg());
		nsp.initialize(this.qFunctions, this.biasComputer);

		this.nextStateProposer = nsp;
	}

	private int[] toArray(List<Integer> list) {
		int[] array = new int[list.size()];
		int index = 0;
		for (Integer i : list) {
			array[index++] = i;
		}
		return array;
	}

	private int init(int indexInPortions, int indexInList, List<Integer> ints) {
		int end = this.qFunctions.get(indexInPortions).getDataPortion().size();
		for (int i = 0; i < end; i++) {
			ints.add(i);
			if (indexInPortions == cardinality() - 1) {
				this.baseObjects[indexInList] = new LabeledOrderedBaseObject(
						toArray(ints));
				indexInList++;
			} else {
				indexInList = init(indexInPortions + 1, indexInList, ints);
			}
			ints.remove(ints.size() - 1);
		}
		return indexInList;
	}

	private void initialize() {
		init(0, 0, new ArrayList<Integer>(cardinality()));
	}

	public void addQFunction(DataPortion dataPortion,
			SupportMeasure supportMeasure) {
		this.qFunctions
				.add(new DistributionFactor(dataPortion, supportMeasure));
	}

	public void setDefaultBias(double defaultBias) {
		this.defaultBias = defaultBias;
	}

	public void addBias(int id, double bias) {
		this.itemBiases.put(id, bias);
	}

	public void addBiases(Map<Integer, Double> itemBiases) {
		this.itemBiases.putAll(itemBiases);
	}

	public double getDefaultBias() {
		return this.defaultBias;
	}

	public void setStarOperation(StarOperation starOperation) {
		this.starOperation = starOperation;
		this.biasComputer = bias(starOperation, this.defaultBias);
		this.subsetSampler = new SequentialSubsetSampler(this.biasComputer);
	}

	public StarOperation getStarOperation() {
		return this.starOperation;
	}

	private double computeNormalizationFactorRecursive(int indexInPortions,
			List<Integer> ints) {
		int end = this.qFunctions.get(indexInPortions).getDataPortion().size();
		double normalizationFactorPortion = 0;
		for (int i = 0; i < end; i++) {
			ints.add(i);
			if (indexInPortions == cardinality() - 1) {
				normalizationFactorPortion += baseObjectWeight(new LabeledOrderedBaseObject(
						toArray(ints)));
			} else {
				normalizationFactorPortion += computeNormalizationFactorRecursive(
						indexInPortions + 1, ints);
			}
			ints.remove(ints.size() - 1);
		}
		return normalizationFactorPortion;
	}

	@Override
	protected double computeNormalizationFactor() {
		return computeNormalizationFactorRecursive(0,
				Lists.<Integer> newArrayListWithCapacity(cardinality()));
	}

	private TidList posIntersection(TidList[] transactions) {
		List<TidList> tidListsToIntersect = newArrayListWithCapacity(transactions.length);
		for (int ix = 0; ix < this.qFunctions.size(); ix++) {
			if (this.qFunctions.get(ix).getSupportMeasure()
					.equals(SupportMeasure.POSITIVE)) {
				tidListsToIntersect.add(transactions[ix]);
			}
		}

		TidList intersection = TidList.intersect(tidListsToIntersect);
		return intersection;
	}

	private TidList[] negativeParts(TidList[] transactions) {
		List<TidList> negativeTidLists = newArrayListWithCapacity(transactions.length);
		for (int ix = 0; ix < this.qFunctions.size(); ix++) {
			if (this.qFunctions.get(ix).getSupportMeasure()
					.equals(SupportMeasure.NEGATIVE)) {
				negativeTidLists.add(new TidList(transactions[ix]));
			}
		}
		return negativeTidLists.toArray(new TidList[0]);
	}

	@Override
	public double getPowersetBias(TidList transaction) {
		return this.biasComputer.powerSetBias(transaction);
	}

	@Override
	public double getSingletonsBias(TidList transaction) {
		return this.biasComputer.singletonsBias(transaction);
	}

	@Override
	public double getEmptySetBias() {
		return this.biasComputer.emptySetBias();
	}

	public double getWeight(TidList[] transactions) {
		TidList posIntersection = posIntersection(transactions);
		TidList[] negativeParts = negativeParts(transactions);
		return this.biasComputer.getWeight(posIntersection, negativeParts);
	}

	private TidList[] getTransactions(int[] indices) {
		TidList[] transactions = new TidList[this.qFunctions.size()];

		for (int ix = 0; ix < this.qFunctions.size(); ix++) {
			transactions[ix] = this.qFunctions.get(ix).getDataPortion()
					.getTransactions().get(indices[ix]).getItemsAsBitSet();
		}

		return transactions;
	}

	@Override
	public double baseObjectWeight(OrderedBaseObject baseObject) {
		TidList[] tidLists = getTransactions(baseObject.getIndices());
		TidList posIntersection = posIntersection(tidLists);
		TidList posIntersectionDifference = new TidList(posIntersection);
		TidList[] negativeParts = negativeParts(tidLists);
		for (TidList negativePart : negativeParts) {
			posIntersectionDifference.andNot(negativePart);
		}
		if (negativeParts.length != 0) {
			return this.biasComputer.getWeight(posIntersection, negativeParts)
					- getSingletonsBias(posIntersectionDifference);
		} else {
			return this.biasComputer.getWeight(posIntersection, negativeParts)
					- getSingletonsBias(posIntersectionDifference)
					- getEmptySetBias();
		}
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		TidList[] tidLists = getTransactions(baseObject.getIndices());

		TidList posIntersection = posIntersection(tidLists);
		TidList[] negativeTransactions = negativeParts(tidLists);

		int[] itemIds = this.subsetSampler.drawSubset(posIntersection,
				negativeTransactions);

		PlainItemSet itemSet = new PlainItemSet();
		for (int id : itemIds) {
			itemSet.add(this.db.getItemDB().get(id));
		}
		return itemSet;
	}

	@Override
	public double numberOfBaseObjects() {
		double v = 1;
		for (DistributionFactor qFunction : this.qFunctions) {
			v *= qFunction.getDataPortion().size();
		}
		return v;
	}

	@Override
	public int cardinality() {
		return this.qFunctions.size();
	}

	@Override
	@Deprecated
	public State getHardJupp() {
		return null;
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return this.defaultBias;
	}

	@Override
	public double getWeightInDistribution(TidList transaction) {
		return 0;
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new GeneralDistributionTesterBPI(this.posNegDb, this.qFunctions,
				this.starOperation, this.defaultBias, this.itemBiases);
	}

	@Override
	public double getWeightInDistribution(TidList[] transactions) {
		return 0;
	}

	@Override
	protected TidList[] getBitSetRepresentations(int[] indices) {
		return null;
	}

	@Override
	public State drawNextState(Random random) {
		return this.nextStateProposer.nextState(random);
	}

	@Override
	public int cardinalityPos() {
		int positivePortions = 0;
		for (DistributionFactor qFunction : this.qFunctions) {
			if (qFunction.getSupportMeasure().equals(SupportMeasure.POSITIVE)) {
				positivePortions++;
			}
		}
		return positivePortions;
	}

	@Override
	public int cardinalityNeg() {
		int negativePortions = 0;
		for (DistributionFactor qFunction : this.qFunctions) {
			if (qFunction.getSupportMeasure().equals(SupportMeasure.NEGATIVE)) {
				negativePortions++;
			}
		}
		return negativePortions;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("General distribution: ");
		buf.append("bias weight=" + defaultBias + ", ");
		buf.append("star operation=" + starOperation + ", ");
		buf.append("qfunctions=");
		for (DistributionFactor qFunction : qFunctions) {
			buf.append("["
					+ qFunction.getDataPortion().getTransactions().size() + "/"
					+ qFunction.getSupportMeasure() + "]");
		}
		return buf.toString();
	}

}
