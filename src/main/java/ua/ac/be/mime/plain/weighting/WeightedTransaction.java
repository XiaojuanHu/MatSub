package ua.ac.be.mime.plain.weighting;

import ua.ac.be.mime.plain.PlainTransaction;

/**
 * Class that implements a weighted transaction
 * 
 * @author Sandy Moens
 */
public class WeightedTransaction extends PlainTransaction {

	private double weight = 1;
	
	public WeightedTransaction(int tid) {
		super(tid);
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
