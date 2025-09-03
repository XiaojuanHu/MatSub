package edu.uab.cftp.sampling.distribution.old;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import edu.uab.cftp.sampling.distribution.LabeledStateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public abstract class SinglePosSingleNegDistribution extends
		LabeledStateSpaceSamplingDistribution {

	public SinglePosSingleNegDistribution(PosNegDbInterface posNegDB,
			LabeledNextStateProposer nextStateProposer,
			boolean initilializeCumulatedWeights) {
		super(posNegDB, nextStateProposer, posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size());

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());

		if (initilializeCumulatedWeights) {
			initialize();
			computeCumulatedWeights();
		}
	}

	public SinglePosSingleNegDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer) {
		super(db, nextStateProposer);

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());
	}

	private void initialize() {
		int endI = posNegDB.getTransactionsPos().size();
		int endJ = posNegDB.getTransactionsNeg().size();
		int c = 0;
		for (int i = 0; i < endI; i++) {
			for (int j = 0; j < endJ; j++) {
				baseObjects[c++] = nextStateProposer
						.createBaseObject(new int[] { i, j });
			}
		}
	}

	@Override
	protected double computeNormalizationFactor() {
		double normalizationFactor = 0;
		int endI = posNegDB.getTransactionsPos().size();
		int endJ = posNegDB.getTransactionsNeg().size();
		for (int i = 0; i < endI; i++) {
			for (int j = 0; j < endJ; j++) {
				normalizationFactor += baseObjectWeight(nextStateProposer
						.createBaseObject(new int[] { i, j }));
			}
		}
		return normalizationFactor;
	}

	@Override
	public double numberOfBaseObjects() {
		return posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size();
	}

	@Override
	public int cardinality() {
		return 0;
	}

	@Override
	public int cardinalityPos() {
		return 1;
	}

	@Override
	public int cardinalityNeg() {
		return 1;
	}

	@Override
	public TidList[] getBitSetRepresentations(int[] indices) {
		TidList[] tidLists = new TidList[2];
		tidLists[0] = posNegDB.getTransactionsPos().get(indices[0])
				.getItemsAsBitSet();
		tidLists[1] = indices[1] != -1 ? TidList.intersect(tidLists[0],
				posNegDB.getTransactionsNeg().get(indices[1])
						.getItemsAsBitSet()) : new TidList();
		return tidLists;
	}

	@Override
	public State getHardJupp() {
		if (hardestJupp == null) {
			int indexI = 0;
			double currentValue = 0, tempValue;
			int endI = posNegDB.getTransactionsPos().size();
			for (int i = 0; i < endI; i++) {
				{
					if ((tempValue = posNegDB.getTransactionsPos().get(i)
							.size()) >= currentValue) {
						currentValue = tempValue;
						indexI = i;
					}
					// DebugPrinter.println(this, i + " " + j + " " +
					// tempValue);
				}
			}
			DebugPrinter.println(this, "HardJupp: " + indexI + " " + -1);
			OrderedBaseObject b = nextStateProposer.createBaseObject(new int[] {
					indexI, -1 });
			hardestJupp = new State(baseObjectWeight(b), b);
		}
		return hardestJupp;
	}

}