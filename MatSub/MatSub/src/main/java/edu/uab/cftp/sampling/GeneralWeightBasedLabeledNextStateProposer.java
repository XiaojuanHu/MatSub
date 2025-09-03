package edu.uab.cftp.sampling;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uab.cftp.sampling.distribution.GeneralDistribution.SupportMeasure;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import edu.uab.cftp.sampling.distribution.tool.Bias;
import edu.uab.consapt.sampling.DistributionFactor;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.tool.Utils;

public class GeneralWeightBasedLabeledNextStateProposer extends
		LabeledNextStateProposer {

	public boolean useMariosProposal = true;

	private List<DistributionFactor> qFunctions;
	private Bias biasComputer;

	private double[][] cumulatedValues;
	private double[][] values;

	private TidList maxWeightTidList;
	private double maxWeight = -1;

	private double powerSetBiasE = -1;

	@Override
	public void setTransactions(List<PlainTransaction> transactions) {
		this.transactions = transactions;
	}

	@Override
	public void setPosTransactions(List<PlainTransaction> transactionsPos) {
	}

	public void initialize(List<DistributionFactor> qFunctions, Bias biasComputer)
			throws ImpossibleToSampleException {
		this.qFunctions = qFunctions;
		this.biasComputer = biasComputer;

		computeValues();
	}

	public double getMaxWeight() {
		double weight = -1;
		TidList tidList = getMaxTransactionAsTidList(this.labeledDistribution
				.getDB().getTransactions());
		double w = this.biasComputer.powerSetBias(tidList);
		if (w > weight) {
			weight = w;
			this.maxWeightTidList = tidList;
		}
		return weight;
	}

	private void computeValues() throws ImpossibleToSampleException {
		this.maxWeight = getMaxWeight();

		this.cumulatedValues = new double[this.qFunctions.size()][];
		this.values = new double[this.qFunctions.size()][];

		int ix = 0;
		for (DistributionFactor qFunction : this.qFunctions) {
			double[] qFunctionValues;
			if (qFunction.getSupportMeasure().equals(SupportMeasure.POSITIVE)) {
				qFunctionValues = computePosValues(qFunction.getDataPortion()
						.getTransactions());
			} else {
				qFunctionValues = computeNegValues(qFunction.getDataPortion()
						.getTransactions());
			}
			this.values[ix] = qFunctionValues;
			this.cumulatedValues[ix] = computeCumulatedValues(qFunctionValues);
			ix++;
		}
	}

	private double[] computeCumulatedValues(double[] values)
			throws ImpossibleToSampleException {
		double normalizationFactor = 0;
		for (double value : values) {
			normalizationFactor += value;
		}
		if (normalizationFactor == 0) {
			throw new ImpossibleToSampleException(
					"Normalization factor is zero. Unable to sample.");
		}

		double[] cumulatedValues = new double[values.length];
		int ix = 0;
		double oldValue = 0;
		for (double value : values) {
			cumulatedValues[ix++] = (oldValue += (value / normalizationFactor));
		}
		return cumulatedValues;
	}

	private double[] computePosValues(List<PlainTransaction> transactions) {
		double[] values = new double[transactions.size()];

		int ix = 0;

		for (PlainTransaction transaction : transactions) {
			TidList transactionAsBitSet = transaction.getItemsAsBitSet();

			double patternsInDBias = this.biasComputer
					.powerSetBias(transactionAsBitSet);
			double singletonsBias = this.biasComputer
					.singletonsBias(transactionAsBitSet);
			double emptySetBias = this.biasComputer.emptySetBias();
			double patternNotInLanguageBias = singletonsBias + emptySetBias;
			values[ix++] = Math.pow(
					(Math.max(0, patternsInDBias - patternNotInLanguageBias)),
					1.0 / this.qFunctions.size());
		}

		return values;
	}

	private double[] computeNegValues(List<PlainTransaction> transactions) {
		double[] values = new double[transactions.size()];

		int ix = 0;

		for (PlainTransaction transaction : transactions) {
			if (this.useMariosProposal) {
				values[ix] = proposalBoundMario(transaction);
			} else {
				values[ix] = proposalBoundSandy(transaction);
			}
			ix++;
		}

		return values;
	}

	private double proposalBoundSandy(PlainTransaction transaction) {
		TidList transactionAsBitSet = transaction.getItemsAsBitSet();

		double powerSetBias = this.biasComputer
				.powerSetBias(transactionAsBitSet);
		double patternsInEMinusDBias = this.maxWeight - powerSetBias;
		double patternsNotInLanguageBias = this.biasComputer
				.singletonsBias(TidList.difference(this.maxWeightTidList,
						transactionAsBitSet));
		{
			int car = this.maxWeightTidList.cardinality()
					- transactionAsBitSet.cardinality();
			TidList tl = new TidList();
			tl.set(0, car);
			patternsNotInLanguageBias = this.biasComputer.singletonsBias(tl);
		}

		return Math.pow(this.maxWeight + patternsInEMinusDBias
				- patternsNotInLanguageBias, 1.0 / this.qFunctions.size());
	}

	private double proposalBoundMario(PlainTransaction transaction) {
		double powerSetBias = this.biasComputer.powerSetBias(transaction
				.getItemsAsBitSet());
		return Math.pow(
				Math.min(this.maxWeight, getPowerSetBiasE() - powerSetBias),
				1.0 / this.qFunctions.size());
	}

	private double getPowerSetBiasE() {
		if (this.powerSetBiasE == -1) {
			TidList e = new TidList();
			Iterator<PlainItem> it = this.labeledDistribution.getDB()
					.getItemDB().iterator();
			while (it.hasNext()) {
				e.set(it.next().getId());
			}
			this.powerSetBiasE = this.biasComputer.powerSetBias(e);
		}
		return this.powerSetBiasE;
	}

	@Override
	public void setNegTransactions(List<PlainTransaction> transactionsNeg) {
	}

	private TidList getMaxTransactionAsTidList(
			List<PlainTransaction> transactions) {
		TidList allItems = new TidList();
		for (PlainTransaction transaction : transactions) {
			if (transaction.size() > allItems.cardinality()) {
				allItems = transaction.getItemsAsBitSet();
			}
		}
		return allItems;
	}

	private int getIndexInList(double[] list, double d) {
		return Utils.logIndexSearch(list, d);
	}

	@Override
	public State nextState(Random random) {
		int[] indices = new int[this.qFunctions.size()];
		for (int ix = 0; ix < this.qFunctions.size(); ix++) {
			indices[ix] = getIndexInList(this.cumulatedValues[ix],
					random.nextDouble());
		}

		OrderedBaseObject baseObject = createBaseObject(indices);
		return new State(this.labeledDistribution.baseObjectWeight(baseObject),
				baseObject);
	}

	static boolean printed = false;

	@Override
	public double getPotential(State stateFrom, State stateTo) {
		int[] indices = stateTo.getBaseObject().getIndices();
		double product = 1;
		for (int ix = 0; ix < this.qFunctions.size(); ix++) {
			product *= this.values[ix][indices[ix]];
		}
		return product;
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public boolean isUniform() {
		return false;
	}

	@Override
	public int randomBitsForNextState() {
		return this.qFunctions.size() + 1;
	}

}
