package edu.uab.cftp.sampling.distribution;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ua.ac.be.mime.mining.ResultDataStructure;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DebugPrinter;
import ua.ac.be.mime.tool.Utils;

/**
 * Abstract class representing the two step sampling distribution framework. The
 * two step sampling framework consist of a step drawing a base object according
 * to a distribution and then drawing a subset of this base object. This
 * procedure is based on
 * "Direct Local Pattern Sampling by Efficient Two-Step Random Procedures" by
 * Boley, Lucchese, Paurat and Gaertner
 * 
 * @author Sandy Moens
 */

public abstract class TwoStepSamplingDistribution {

	public static Random random = new Random(System.currentTimeMillis());

	protected TransactionDBInterface db;
	protected OrderedBaseObject[] baseObjects = null;
	protected double[] cumulatedWeights = null;
	protected double normalizationFactor = -1;
	protected List<PlainTransaction> transactions;

	/**
	 * Constructs a new TwoStepSamplingDistribution object. This method also
	 * initializes the base objects with the number specified. Use this
	 * constructor if you want to use the exact method for drawing samples.
	 * 
	 * @param db
	 *            the weighted database from which the distribution draws
	 *            samples
	 * @param numberOfBaseObjects
	 *            this indicates the number of base objects that need to be
	 *            created
	 */
	public TwoStepSamplingDistribution(TransactionDBInterface db,
			int numberOfBaseObjects) {
		this.db = db;
		this.baseObjects = new OrderedBaseObject[numberOfBaseObjects];
		this.transactions = db.getTransactions();
	}

	/**
	 * Constructs a new TwoStepSamplingDistribution object. This method does not
	 * initialize the base objects with the number specified. Use this
	 * constructor if you do not want to use the exact method for drawing
	 * samples (For instance if the number of base objects is too large to fit
	 * in main memory).
	 * 
	 * @param db
	 *            the weighted database from which the distribution draws
	 *            samples
	 */
	public TwoStepSamplingDistribution(TransactionDBInterface db) {
		if (db != null) {
			this.db = db;
			this.transactions = db.getTransactions();
			this.baseObjects = null;
		}
	}

	/**
	 * Draws a number of samples using the exact method for drawing a sample
	 * from the distribution.
	 * 
	 * @param numberOfSamples
	 *            the number of samples that need to be drawn
	 * @return the list of item sets that have been drawn
	 */
	public List<PlainItemSet> drawSamples(int numberOfSamples) {
		if (this.cumulatedWeights == null) {
			computeCumulatedWeights();
		}

		List<PlainItemSet> itemSets = new LinkedList<PlainItemSet>();
		PlainItemSet itemSet;
		int nn = numberOfSamples / 10;
		while (itemSets.size() != numberOfSamples) {
			itemSet = drawSample();
			if (itemSet.size() != 0) {
				itemSets.add(itemSet);
			}
			if (itemSets.size() % nn == 0) {
				DebugPrinter.print(".");
			}
		}
		DebugPrinter.println();
		return itemSets;
	}

	public void drawSamplesNoReturn(int numberOfSamples) {
		if (this.cumulatedWeights == null) {
			computeCumulatedWeights();
		}

		int i = 0;
		PlainItemSet itemSet;
		int nn = numberOfSamples / 10;
		while (i < numberOfSamples) {
			itemSet = drawSample();
			if (itemSet.size() != 0) {
				i++;
			}
			if (i % nn == 0) {
				DebugPrinter.print(".");
			}
		}
		DebugPrinter.println();
	}

	/**
	 * Returns the array of base objects
	 * 
	 * @return the array of base objects
	 */
	public OrderedBaseObject[] getBaseObjects() {
		return this.baseObjects;
	}

	/**
	 * Computes the cumulated weights of the base objects
	 */
	protected void computeCumulatedWeights() {
		double[] weights = new double[this.baseObjects.length];
		int index = 0;

		this.normalizationFactor = 0;
		for (OrderedBaseObject baseObject : this.baseObjects) {
			this.normalizationFactor += (weights[index++] = baseObjectWeight(baseObject));
		}

		DebugPrinter.println(this, "Number of base objects:  "
				+ this.baseObjects.length);
		DebugPrinter.println(this, "Normalizationfactor: "
				+ this.normalizationFactor);

		for (int i = 0; i < weights.length; i++) {
			weights[i] /= this.normalizationFactor;
		}
		this.cumulatedWeights = new double[this.baseObjects.length];

		double weight = 0.0;
		index = 0;
		for (double w : weights) {
			this.cumulatedWeights[index++] = (weight += w);
		}
	}

	/**
	 * Draws a single sample from the distribution
	 * 
	 * @return a new sample item set that drawn exactly from the distribution
	 */
	private PlainItemSet drawSample() {
		return drawSubSet(drawBaseObject());
	}

	/**
	 * Draws a new base object from the distribution using a cumulated method.
	 * All weights are cumulated to one and a random number if drawn between 0
	 * and 1. The first base object with cumulated weight greater than the
	 * random number is returned
	 * 
	 * @return a new sample base object drawn exactly from the distribution
	 */
	protected OrderedBaseObject drawBaseObject() {
		return this.baseObjects[Utils.logIndexSearch(
				this.cumulatedWeights, random.nextDouble())];
	}

	/**
	 * Prints the individual weights of the base objects to the output stream
	 */
	public void printBaseObjectWeights() {
		int i = 0;
		double d, highest = 0;
		for (OrderedBaseObject baseObject : this.baseObjects) {
			DebugPrinter.println(this, i++ + ": " + baseObject + " "
					+ (d = baseObjectWeight(baseObject)));
			if (d > highest) {
				highest = d;
			}
		}
		DebugPrinter.println(this, "Highest " + highest);
	}

	public void printNormalizedWeights() {
		DebugPrinter.println("NormalizedWeights");
		for (double d : this.cumulatedWeights) {
			DebugPrinter.println(d + "");
		}
	}

	/**
	 * Computes the normalization factor of the distribution
	 * 
	 * @return the normalization factor
	 */
	protected abstract double computeNormalizationFactor();

	/**
	 * Gets the normalization factor of the individual weights of the base
	 * objects
	 * 
	 * @return the normalization factor
	 */
	public final double getNormalizationFactor() {
		return this.normalizationFactor == -1 ? (this.normalizationFactor = computeNormalizationFactor())
				: this.normalizationFactor;
	}

	/**
	 * Gets the weight of an individual base object, following the distribution
	 * 
	 * @param baseObject
	 *            the base object whose weight needs to be computed
	 * @return the weight of the base object
	 */
	public abstract double baseObjectWeight(OrderedBaseObject baseObject);

	/**
	 * Uniformly draws a subset of the base object
	 * 
	 * @param baseObject
	 *            the base object from which a subset needs to be drawn
	 * @return a sample subset of the base object
	 */
	public abstract PlainItemSet drawSubSet(OrderedBaseObject baseObject);

	/**
	 * Uniformly draws a subsets from the base object
	 * 
	 * @param baseObject
	 *            the base object from which a subset needs to be drawn
	 * @param data
	 *            datastructure to which the subset is added
	 */
	public void drawSubSet(OrderedBaseObject baseObject,
			ResultDataStructure data) {
		data.add(drawSubSet(baseObject));
	}

	/**
	 * Gets the weighted database from which the distribution samples new
	 * samples
	 * 
	 * @return the weighted database
	 */
	public TransactionDBInterface getDB() {
		return this.db;
	}

	/**
	 * Gets the number of base object. This is influenced by the cardinality of
	 * the distribution (single, squared, ...). The type of data (labeled or
	 * unlabeled) and the next state proposer (full, sparse)
	 * 
	 * @return the number of base object
	 */
	public abstract double numberOfBaseObjects();

	/**
	 * Gets the cardinality of the distribution
	 * 
	 * @return the cardinality of the distribution
	 */
	public abstract int cardinality();

	public List<PlainTransaction> getTransactions() {
		return this.transactions;
	}
}
