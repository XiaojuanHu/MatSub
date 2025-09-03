package edu.uab.cftp.sampling;

import java.util.Random;

import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;

public class UniformLabeledNextStateProposer extends LabeledNextStateProposer {

	@Override
	public State nextState(Random random) {
		int[] indices = new int[this.totalCardinality];
		int index = 0;
		for (int i = 0; i < this.cardinality; i++) {
			indices[index++] = random.nextInt(this.transactionsSize);
		}
		for (int i = 0; i < this.cardinalityPos; i++) {
			indices[index++] = random.nextInt(this.transactionsSizePos);
		}
		for (int i = 0; i < this.cardinalityNeg; i++) {
			indices[index++] = random.nextInt(this.transactionsSizeNeg);
		}
		OrderedBaseObject baseObject = createBaseObject(indices);
		return new State(this.labeledDistribution.baseObjectWeight(baseObject),
				baseObject);
	}

	@Override
	public double getPotential(State stateFrom, State stateTo) {
		// double first = this.cardinality != 0 ? 1 /
		// Math.pow(this.transactionsSize,
		// this.cardinality) : 1;
		// double second = this.cardinalityPos != 0 ? 1 / Math.pow(
		// this.transactionsSizePos, this.cardinalityPos) : 1;
		// double third = this.cardinalityNeg != 0 ? 1 / Math.pow(
		// this.transactionsSizeNeg, this.cardinalityNeg) : 1;
		double first = this.cardinality == 0 ? 1 : 1;
		double second = this.cardinalityPos == 0 ? 1 : 1;
		double third = this.cardinalityNeg == 0 ? 1 : 1;

		return first * second * third;
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public boolean isUniform() {
		return true;
	}

}
