package edu.uab.cftp.sampling;

import java.util.List;
import java.util.Random;

import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.tool.Utils;

public class WeightBasedLabeledNextStateProposer extends
		LabeledNextStateProposer {

	private double[] cumulatedValues;
	private double[] cumulatedValuesPos;

	private double[] values;
	private double[] valuesPos;

	private double negValue = 0;

	@Override
	public void setTransactions(List<PlainTransaction> transactions) {
		this.transactions = transactions;
		this.cumulatedValues = new double[this.transactions.size()];
		this.values = new double[this.transactions.size()];

		int i = 0;
		double normalizationFactor = 0;
		for (PlainTransaction t : this.transactions) {
			double value = Math.pow(Math.pow(2, t.size()) - t.size() - 1,
					1.0 / totalCardinality);
			values[i++] = value;
			normalizationFactor += value;
			if (!Double.isInfinite(value)) {
				negValue = Math.max(value, negValue);
			}
		}
		double cumul = 0;
		for (int ix = 0; ix < transactions.size(); ix++) {
			cumul += this.values[ix] / normalizationFactor;
			this.cumulatedValues[ix] = cumul;
		}
	}

	@Override
	public void setPosTransactions(List<PlainTransaction> transactionsPos) {
		this.transactionsPos = transactionsPos;
		this.transactionsSizePos = this.transactionsPos.size();
		this.cumulatedValuesPos = new double[this.transactionsPos.size()];
		this.valuesPos = new double[this.transactionsPos.size()];

		int i = 0;
		double normalizationFactor = 0;
		for (PlainTransaction t : this.transactionsPos) {
			double value = Math.pow(Math.pow(2, t.size()) - t.size() - 1,
					1.0 / totalCardinality);
			valuesPos[i++] = value;
			normalizationFactor += value;
			negValue = Math.max(value, negValue);
		}
		double cumul = 0;
		for (int ix = 0; ix < transactionsPos.size(); ix++) {
			cumul += this.valuesPos[ix] / normalizationFactor;
			this.cumulatedValuesPos[ix] = cumul;
		}
	}

	@Override
	public void setNegTransactions(List<PlainTransaction> transactionsNeg) {
		this.transactionsNeg = transactionsNeg;
		this.transactionsSizeNeg = this.transactionsNeg.size();
	}

	private int getIndex(double[] cumulatedValues, double d) {
		return Utils.logIndexSearch(cumulatedValues, d);
	}

	@Override
	public State nextState(Random random) {
		int[] indices = new int[this.totalCardinality];
		int index = 0;
		for (int i = 0; i < this.cardinality; i++) {
			indices[index++] = getIndex(cumulatedValues, random.nextDouble());
		}
		for (int i = 0; i < this.cardinalityPos; i++) {
			indices[index++] = getIndex(cumulatedValuesPos, random.nextDouble());
		}
		for (int i = 0; i < this.cardinalityNeg; i++) {
			indices[index++] = random.nextInt(this.transactionsSizeNeg);
		}
		OrderedBaseObject baseObject = createBaseObject(indices);
		return new State(this.labeledDistribution.baseObjectWeight(baseObject),
				baseObject);
	}

	static boolean printed = false;

	@Override
	public double getPotential(State stateFrom, State stateTo) {
		double first = 1;
		double second = 1;
		double third = 1;
		int[] indices = stateTo.getBaseObject().getIndices();
		int index = 0;
		for (int i = 0; i < this.cardinality; i++) {
			first *= values[indices[index++]];
		}
		for (int i = 0; i < this.cardinalityPos; i++) {
			second *= valuesPos[indices[index++]];
		}
		for (int i = 0; i < this.cardinalityNeg; i++) {
			third *= negValue;
		}

		return first * second * third;
	}

	public static boolean allZero(int[] indices) {
		for (int i : indices) {
			if (i != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public boolean isUniform() {
		return false;
	}

}
