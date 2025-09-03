package edu.uab.cftp.sampling;

import java.util.List;

import edu.uab.cftp.sampling.distribution.State;

/**
 * Interface for interruptible perfect sampling algorithms. Interruptible
 * sampling algorithms can be run for a specified amount of time.
 * 
 * @author Sandy Moens
 */

public interface InterruptiblePerfectSamplingAlgorithm {

	/**
	 * This method simulates the chain for a specified amount of time using the
	 * random seed to generate random bits.
	 * 
	 * @param time
	 *            amount of time the chain must be simulated
	 * @param randomSeed
	 *            a random seed that is used to generate random bits
	 * @return the state of the chain after simulation of time timesteps, or
	 *         null if no state has been accepted during simulation.
	 */
	public State run(long time, long randomSeed);

	/**
	 * This method simulates multiple chain simulations in a row starting at a
	 * given moment in time. One chain simulation is specified by a simulation
	 * time and a random seed.
	 * 
	 * @param beginIndex
	 *            the index of the first chain that must be simulated.
	 * @param randomSeeds
	 *            list of chain simulation information.
	 * @return the state of the chain after simulation of the chains starting at
	 *         beginIndex, or null if no state has been accepted during
	 *         simulation.
	 */
	public State run(int beginIndex, List<long[]> randomSeeds);

	public void reset();
}
