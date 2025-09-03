package edu.uab.cftp.sampling;

import edu.uab.cftp.sampling.distribution.State;
import edu.uab.cftp.sampling.distribution.StateSpaceProbabilityDistribution;

/**
 * Monte-Carlo Chain simulation method with acceptance probability equal
 * p(x)q(y) / p(y)q(x), where x is the current state and y is a newly proposed
 * state.
 * 
 * @author Sandy Moens
 */
public class MetropolisHastingsAlgorithm extends MetropolisAlgorithm {

	private final NextStateProposer nextStateProposer;

	public MetropolisHastingsAlgorithm(
			StateSpaceProbabilityDistribution distribution) {
		this.distribution = distribution;
		this.nextStateProposer = distribution.getNextStateProposer();
		reset();
	}

	@Override
	public void reset() {
		this.curState = null;
		this.currentValue = 1;
		this.accepted = false;
	}

	@Override
	protected double getValue(State state) {
		return state.getProbability()
				/ this.nextStateProposer.getPotential(null, state);
	}
}