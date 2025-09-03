package edu.uab.cftp.sampling.distribution;

/**
 * Class that represents a state for a state space probability distribution
 * 
 * @author Sandy Moens
 */
public class State {

	// the probability of this state happening in the state space
	private double probability;
	// the base object that this state represents
	private OrderedBaseObject baseObject;

	/**
	 * Creates a new state object with a specified base object and a probability
	 * 
	 * @param probability
	 *          the probability of the state happening in the state space
	 * @param baseObject
	 *          the base object represented by the state
	 */
	public State(double probability, OrderedBaseObject baseObject) {
		this.probability = probability;
		this.baseObject = baseObject;
	}

	/**
	 * Gets the probability of the state happening in the state space
	 * 
	 * @return the probability of the state
	 */
	public double getProbability() {
		return this.probability;
	}

	/**
	 * Gets the base object that is represented by the state
	 * 
	 * @return the base object
	 */
	public OrderedBaseObject getBaseObject() {
		return this.baseObject;
	}
}