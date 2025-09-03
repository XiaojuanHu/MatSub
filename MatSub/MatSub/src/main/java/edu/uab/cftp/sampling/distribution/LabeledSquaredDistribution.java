package edu.uab.cftp.sampling.distribution;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.plain.weighting.old.PosNegWeightedTransactionDb;

public abstract class LabeledSquaredDistribution extends
		StateSpaceSamplingDistribution {
	// TODO

	protected PosNegWeightedTransactionDb posNegDB;

	public LabeledSquaredDistribution(PosNegWeightedTransactionDb posNegDB,
			NextStateProposer nextStateProposer,
			boolean initilializeCumulatedWeights) {
		super(posNegDB, nextStateProposer, posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size());

		this.posNegDB = posNegDB;

		initialize();

		if (initilializeCumulatedWeights) {
			computeCumulatedWeights();
		}
	}

	public LabeledSquaredDistribution(PosNegWeightedTransactionDb db,
			NextStateProposer nextStateProposer) {
		this(db, nextStateProposer, false);
	}

	protected void initialize() {
		int endI = this.posNegDB.getTransactionsPos().size();
		int endJ = this.posNegDB.getTransactionsNeg().size();
		for (int i = 0; i < endI; i++) {
			for (int j = 0; j < endJ; j++) {
				this.baseObjects[i] = this.nextStateProposer
						.createBaseObject(new int[] { i, j });
			}
		}
	}

	@Override
	public double numberOfBaseObjects() {
		return 2 * this.posNegDB.getTransactionsPos().size()
				* this.posNegDB.getTransactionsNeg().size();
	}

	@Override
	public int cardinality() {
		return 2;
	}

	@Override
	public int randomBitsForNextState() {
		return 4;
	}
}