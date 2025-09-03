package edu.uab.cftp.sampling.distribution;

import java.util.Random;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DebugPrinter;
import ua.ac.be.mime.tool.Utils;

/**
 * Abstract class representing the two step sampling distribution framework
 * using transactions and intersections as states in a state space distribution.
 * 
 * @author Sandy Moens
 */
public abstract class StateSpaceSamplingDistribution extends
		TwoStepSamplingDistribution implements
		StateSpaceProbabilityDistribution {

	// the next state proposer that proposes new states for the MCMC chain
	protected NextStateProposer nextStateProposer;
	// the state in the state space that is the hardest to convince
	protected State hardestJupp;

	/**
	 * Constructs a new TwoStepStateSpaceSamplingDistribution objects. This
	 * method also initializes the base objects with the number specified. Use
	 * this constructor if you want to use the exact method for drawing samples.
	 * 
	 * @param db
	 *            the weighted database from which the distribution draws
	 *            samples
	 * @param nextStateProposer
	 *            the next state proposer is used do propose a new state for the
	 *            MCMC method
	 * @param numberOfBaseObjects
	 *            this indicates the number of base objects that need to be
	 *            created
	 */
	public StateSpaceSamplingDistribution(TransactionDBInterface db,
			NextStateProposer nextStateProposer, int numberOfBaseObjects) {
		super(db, numberOfBaseObjects);
		setNextStateProposer(nextStateProposer);
	}

	/**
	 * Constructs a new TwoStepStateSpaceSamplingDistribution objects. This
	 * method does not initialize the base objects with the number specified.
	 * Use this constructor if you do not want to use the exact method for
	 * drawing samples (For instance if the number of base objects is too large
	 * to fit in main memory).
	 * 
	 * @param db
	 *            the weighted database from which the distribution draws
	 *            samples
	 * @param nextStateProposer
	 *            the next state proposer is used do propose a new state for the
	 *            MCMC method
	 */
	public StateSpaceSamplingDistribution(TransactionDBInterface db,
			NextStateProposer nextStateProposer) {
		this(db);
		setNextStateProposer(nextStateProposer);
	}

	/**
	 * Constructs a new TwoStepStateSpaceSamplingDistribution objects. This
	 * method does not initialize the base objects with the number specified.
	 * Use this constructor if you do not want to use the exact method for
	 * drawing samples (For instance if the number of base objects is too large
	 * to fit in main memory).
	 * 
	 * @param db
	 *            the weighted database from which the distribution draws
	 *            samples
	 */
	public StateSpaceSamplingDistribution(TransactionDBInterface db) {
		super(db);
	}

	// STUFF FOR DISTRIBUTION WEIGHTS

	public abstract double getWeightInDistribution(PlainItem item);

	/**
	 * Gets the weight of a state in the state space.
	 * 
	 * @param transaction
	 *            the state in the state space
	 * @return the weight of the state
	 */
	public abstract double getWeightInDistribution(TidList transaction);

	public double[] getSingletonWeightsInDistribution(TidList tids) {
		double[] weights = new double[tids.cardinality()];
		int index = 0;
		int i = -1;
		while ((i = tids.nextSetBit(i + 1)) != -1) {
			weights[index++] = getWeightInDistribution(this.db.getItemDB().get(
					i));
		}
		return weights;
	}

	// STUFF FOR PROPOSAL WEIGHTS, DEFAULT THEY ARE THE SAME AS THE DISTRIBUTION

	public double getWeightInProposal(PlainItem item) {
		return getWeightInDistribution(item);
	}

	public double getWeightInProposal(TidList transaction) {
		return getWeightInDistribution(transaction);
	}

	public double[] getSingletonWeightsInProposal(TidList tids) {
		double[] weights = new double[tids.cardinality()];
		int index = 0;
		int i = -1;
		while ((i = tids.nextSetBit(i + 1)) != -1) {
			weights[index++] = getWeightInProposal(this.db.getItemDB().get(i));
		}
		return weights;
	}

	public boolean useProposal() {
		return false;
	}

	public double getValue(TidList transaction) {
		if (!this.nextStateProposer.isUniform()) {
			if (useProposal()) {
				return getWeightInDistribution(transaction)
						/ getWeightInProposal(transaction);
			} else {
				return 1;
			}
		} else {
			if (this.nextStateProposer.isSparse()) {
				return getWeightInDistribution(transaction)
						* Utils.combination(this.transactions.size()
								+ cardinality() - 1, cardinality());
			} else {
				System.out
						.println(getWeightInDistribution(transaction)
								+ "\n"
								+ Math.pow(this.transactions.size(),
										this.cardinality()));
				return getWeightInDistribution(transaction)
						* Math.pow(this.transactions.size(), this.cardinality());
			}
		}
	}

	@Override
	public State getHardJupp() {
		if (this.hardestJupp == null) {
			int index = 0;
			double currentValue = 0, tempValue;
			int i = 0;
			for (PlainTransaction newState : this.db.getTransactions()) {
				if ((tempValue = getValue(newState.getItemsAsBitSet())) >= currentValue) {
					currentValue = tempValue;
					index = i;
				}
				i++;
			}
			DebugPrinter.println(this, "HardJupp: " + index + " ["
					+ currentValue + "]");
			int[] indices = new int[this.cardinality()];
			for (i = 0; i < this.cardinality(); i++) {
				indices[i] = index;
			}
			OrderedBaseObject b = this.nextStateProposer.createBaseObject(indices);
			this.hardestJupp = new State(baseObjectWeight(b), b);
		}
		return this.hardestJupp;
	}

	public double baseObjectWeight(OrderedBaseObject baseObject) {
		if (baseObject.getWeight() != -1) {
			return baseObject.getWeight();
		} else {
			double weight = getWeightInDistribution(ua.ac.be.mime.plain.weighting.Utils
					.intersectAll(ua.ac.be.mime.plain.weighting.Utils.getTransactions(
							this.transactions, baseObject.getIndices())));

			baseObject.setWeight(weight);
			return weight;
		}
	}

	/**
	 * Sets the next state proposer to the distribution and sets this
	 * distribution to the next state proposer
	 * 
	 * @param nextStateProposer
	 *            the next state proposer that will be used to draw new sample
	 *            states for the distribution
	 */
	public void setNextStateProposer(NextStateProposer nextStateProposer) {
		if (nextStateProposer != null) {
			this.nextStateProposer = nextStateProposer;
			this.nextStateProposer.setDistribution(this);
			this.nextStateProposer.setTransactions(getTransactions());
		}
	}

	/**
	 * Gets the next state proposer used by the distribution
	 * 
	 * @return the next state proposer used
	 */
	public NextStateProposer getNextStateProposer() {
		return this.nextStateProposer;
	}

	@Override
	public int randomBitsForNextState() {
		return this.nextStateProposer.randomBitsForNextState();
	}

	@Override
	public State drawNextState(Random random) {
		return this.nextStateProposer.nextState(random);
	}

	public abstract DistributionTester getDistributionTester();
}
