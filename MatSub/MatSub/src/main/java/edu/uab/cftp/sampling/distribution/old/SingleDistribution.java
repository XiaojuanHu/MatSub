package edu.uab.cftp.sampling.distribution.old;

import edu.uab.cftp.sampling.NextStateProposer;
import edu.uab.cftp.sampling.distribution.StateSpaceSamplingDistribution;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public abstract class SingleDistribution extends StateSpaceSamplingDistribution {

	public SingleDistribution(TransactionDBInterface db,
			NextStateProposer nextStateProposer, boolean initilializeCumulatedWeights) {
		super(db, nextStateProposer, db.getTransactions().size());

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());

		if (initilializeCumulatedWeights) {
			initialize();
			computeCumulatedWeights();
		}
	}

	public SingleDistribution(TransactionDBInterface db,
			NextStateProposer nextStateProposer) {
		super(db, nextStateProposer);

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());
	}

	private void initialize() {
		int end = db.getTransactions().size();
		for (int i = 0; i < end; i++) {
			this.baseObjects[i] = this.nextStateProposer
					.createBaseObject(new int[] { i });
		}
	}

	@Override
	protected double computeNormalizationFactor() {
		double normalizationFactor = 0;
		int end = this.db.getTransactions().size();
		for (int i = 0; i < end; i++) {
			normalizationFactor += baseObjectWeight(this.nextStateProposer
					.createBaseObject(new int[] { i }));
		}
		return normalizationFactor;
	}

	@Override
	public double numberOfBaseObjects() {
		return this.db.getTransactions().size();
	}

	public int cardinality() {
		return 1;
	}
}