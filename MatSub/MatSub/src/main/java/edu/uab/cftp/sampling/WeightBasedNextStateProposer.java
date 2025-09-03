package edu.uab.cftp.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.tool.Utils;

public class WeightBasedNextStateProposer extends NextStateProposer {

	private double[] cumulatedValues;
	private double[] values;

	@Override
	public void setTransactions(List<PlainTransaction> transactions) {
		this.transactions = transactions;
		this.cumulatedValues = new double[this.transactions.size()];
		this.values = new double[this.transactions.size()];
		computeCumulatedValues();
	}

	private double getWeight(PlainTransaction transaction) {
		double weight = Math.pow(this.distribution
				.getWeightInDistribution(transaction.getItemsAsBitSet()),
				1.0 / this.distribution.cardinality());
		if (Double.isNaN(weight)) {
			return 0;
		} else {
			return weight;
		}
	}

	private void computeCumulatedValues() {
		int index = 0;
		double normalizationFactor = 0;
		for (PlainTransaction transaction : this.transactions) {
			normalizationFactor += (this.values[index++] = getWeight(transaction));
		}

		double value = 0.0;
		index = 0;
		for (double v : this.values) {
			this.cumulatedValues[index++] = (value += (v / normalizationFactor));
		}
	}

	private int getIndex(double d) {
		return Utils.logIndexSearch(this.cumulatedValues, d);
	}

	@Override
	public State nextState(Random random) {
		int[] indices = new int[this.distribution.cardinality()];
		for (int index = 0; index < this.distribution.cardinality(); index++) {
			indices[index] = getIndex(random.nextDouble());
		}
		OrderedBaseObject baseObject = createBaseObject(indices);
		return new State(this.distribution.baseObjectWeight(baseObject),
				baseObject);
	}

	@Override
	public double getPotential(State stateFrom, State stateTo) {
		double product = 1.0;
		for (int index : stateTo.getBaseObject().getIndices()) {
			product *= this.values[index];
		}
		return product;
	}

	@Override
	public OrderedBaseObject createBaseObject(int[] indices) {
		List<PlainTransaction> transactions = new ArrayList<PlainTransaction>(
				this.distribution.cardinality());
		for (int index : indices) {
			transactions.add(this.transactions.get(index));
		}
		return new OrderedBaseObject(indices);
	}

	@Override
	public int randomBitsForNextState() {
		return this.distribution.cardinality();
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
