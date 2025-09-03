package edu.uab.cftp.sampling;

import java.util.List;
import java.util.Random;

import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import ua.ac.be.mime.plain.PlainTransaction;

public abstract class NextStateProposer {

	protected StateSpaceSamplingDistribution distribution;
	protected List<PlainTransaction> transactions;

	public abstract State nextState(Random random);

	public abstract double getPotential(State stateFrom, State stateTo);

	public abstract OrderedBaseObject createBaseObject(int[] indices);

	public int randomBitsForNextState() {
		return 1;
	}

	public void setDistribution(StateSpaceSamplingDistribution distribution) {
		this.distribution = distribution;
	}

	public void setTransactions(List<PlainTransaction> transactions) {
		this.transactions = transactions;
	}

	public abstract boolean isSparse();

	public abstract boolean isUniform();
}
