package edu.uab.cftp.sampling.distribution;

import java.util.Random;

import edu.uab.cftp.sampling.NextStateProposer;

/**
 * Abstract class representing a state space probability distribution that is
 * used by MCMC methods.
 * 
 * @author Sandy Moens
 */

public interface StateSpaceProbabilityDistribution {

	/**
	 * Gets the state in the state space distribution that is the most difficult
	 * to leave. This can be used as the hardest to convince state in the Coupling
	 * From The Past algorithm. The starting state needs to be monitored in order
	 * to check for mixing of the chain.
	 * 
	 * @return the state that is hardest to leave
	 */
	public State getHardJupp();

	/**
	 * TODO
	 */
	public State drawNextState(Random random);

	/**
	 * Gets the number of bits that are used
	 * 
	 * @return
	 */
	public int randomBitsForNextState();

	/**
	 * Gets the next state proposer for the state space distribution.
	 * 
	 * @return the next state proposer
	 */
	public NextStateProposer getNextStateProposer();
}
