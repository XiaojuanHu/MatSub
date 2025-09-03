package edu.uab.cftp.sampling.distribution.old;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import edu.uab.cftp.sampling.distribution.DistributionTester;
import edu.uab.cftp.sampling.distribution.LabeledOrderedBaseObject;
import edu.uab.cftp.sampling.distribution.LabeledStateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.Utils;
import ua.ac.be.mime.tool.DebugPrinter;

public class PosFrequencyDiscriminativityDistribution extends
		LabeledStateSpaceSamplingDistribution {

	/*
	 * =========================================================================
	 * 
	 * STATIC
	 * 
	 * =========================================================================
	 */

	public static class PosFrequencyDiscriminativityTester extends
			DistributionTester {

		private final PosNegDbInterface posNegDb;

		public PosFrequencyDiscriminativityTester(PosNegDbInterface db) {
			super();
			posNegDb = db;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			return Math.pow(posNegDb.getPosItemSet(itemSet).getTIDs()
					.cardinality(), 2)
					* (posNegDb.getTransactionsNeg().size() - posNegDb
							.getNegItemSet(itemSet).getTIDs().cardinality());
		}
	}

	/*
	 * =========================================================================
	 * 
	 * NON-STATIC
	 * 
	 * =========================================================================
	 */

	public PosFrequencyDiscriminativityDistribution(PosNegDbInterface posNegDB,
			LabeledNextStateProposer nextStateProposer,
			boolean initializeCumulatedWeights) {
		super(posNegDB, nextStateProposer, posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size());

		if (initializeCumulatedWeights) {
			initialize();
			computeCumulatedWeights();
		}
	}

	public PosFrequencyDiscriminativityDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer) {
		super(db, nextStateProposer);

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());
	}

	private void initialize() {
		int endPos = posNegDB.getTransactionsPos().size();
		int endNeg = posNegDB.getTransactionsNeg().size();
		int c = 0;
		for (int i = 0; i < endPos; i++) {
			for (int j = 0; j < endPos; j++) {
				for (int k = 0; k < endNeg; k++) {
					baseObjects[c++] = new LabeledOrderedBaseObject(new int[] {
							i, j, k });
				}
			}
		}
	}

	@Override
	protected double computeNormalizationFactor() {
		double normalizationFactor = 0;
		int endPos = posNegDB.getTransactionsPos().size();
		int endNeg = posNegDB.getTransactionsNeg().size();
		for (int i = 0; i < endPos; i++) {
			for (int j = 0; j < endPos; j++) {
				for (int k = 0; k < endNeg; k++) {
					normalizationFactor += baseObjectWeight(nextStateProposer
							.createBaseObject(new int[] { i, j, k }));
				}
			}
		}
		return normalizationFactor;
	}

	@Override
	public State getHardJupp() {
		if (hardestJupp == null) {
			int indexI = 0;
			double currentValue = 0, tempValue;
			int endPos = posNegDB.getTransactionsPos().size();
			for (int i = 0; i < endPos; i++) {
				if ((tempValue = posNegDB.getTransactionsPos().get(i).size()) >= currentValue) {
					currentValue = tempValue;
					indexI = i;
				}
			}
			DebugPrinter.println(this, "HardJupp: " + indexI + " " + indexI
					+ " " + -1);
			OrderedBaseObject b = nextStateProposer.createBaseObject(new int[] {
					indexI, indexI, -1 });
			hardestJupp = new State(baseObjectWeight(b), b);
		}
		return hardestJupp;
	}

	@Override
	public TidList[] getBitSetRepresentations(int[] indices) {
		TidList[] tidLists = new TidList[2];
		tidLists[0] = TidList.intersect(
				posNegDB.getTransactionsPos().get(indices[0])
						.getItemsAsBitSet(),
				posNegDB.getTransactionsPos().get(indices[1])
						.getItemsAsBitSet());
		tidLists[1] = indices[2] != -1 ? TidList.intersect(tidLists[0],
				posNegDB.getTransactionsNeg().get(indices[2])
						.getItemsAsBitSet()) : new TidList();
		return tidLists;
	}

	@Override
	public int randomBitsForNextState() {
		return 3;
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return 1;
	}

	@Override
	public double getWeightInDistribution(TidList[] transactions) {
		double posSize = transactions[0].cardinality();
		double intSize = transactions[1].cardinality();
		double onlyInPosSize = posSize - intSize;
		return (Math.pow(2, onlyInPosSize) - 1) * Math.pow(2, intSize)
				- onlyInPosSize;
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		TidList[] transactions = getBitSetRepresentations(baseObject
				.getIndices());
		TidList difference = TidList.difference(transactions[0],
				transactions[1]);

		return Utils.drawDiscriminativeSubSetNoSingletons(
				this.posNegDB.getItemDB(), transactions[1], difference);
	}

	@Override
	public double numberOfBaseObjects() {
		return posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size();
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new PosFrequencyDiscriminativityTester(posNegDB);
	}

	@Override
	public int cardinality() {
		return 0;
	}

	@Override
	public int cardinalityPos() {
		return 2;
	}

	@Override
	public int cardinalityNeg() {
		return 1;
	}
}
