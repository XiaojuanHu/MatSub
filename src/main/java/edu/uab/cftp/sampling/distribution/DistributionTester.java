package edu.uab.cftp.sampling.distribution;

import java.util.Collection;

import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.TransactionDBInterface;

public abstract class DistributionTester {

	protected TransactionDBInterface db;

	public DistributionTester(TransactionDBInterface db) {
		this.db = db;
	}

	protected DistributionTester() {
	}

	public abstract double computeMeasureValue(PlainItemSet itemSet);

	public double computeNormalizationFactor(Collection<PlainItemSet> itemSets) {
		double normalizationFactor = 0;

		for (PlainItemSet itemSet : itemSets) {
			normalizationFactor += computeMeasureValue(itemSet);
		}

		return normalizationFactor;
	}
}