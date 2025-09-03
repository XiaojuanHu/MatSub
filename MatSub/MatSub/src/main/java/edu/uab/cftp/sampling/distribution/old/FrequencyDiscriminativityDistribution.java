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

public class FrequencyDiscriminativityDistribution extends
		LabeledStateSpaceSamplingDistribution {

	/*
	 * =========================================================================
	 * 
	 * STATIC
	 * 
	 * =========================================================================
	 */

	public static class FrequencyDiscriminativityTester extends
			DistributionTester {

		private PosNegDbInterface posNegDb;

		public FrequencyDiscriminativityTester(PosNegDbInterface db) {
			super();
			this.posNegDb = db;
		}

		public double computeMeasureValue(PlainItemSet itemSet) {
			return ((double) this.posNegDb.getCompleteDbItemSet(itemSet)
					.getTIDs().cardinality())
					* ((double) this.posNegDb.getPosItemSet(itemSet).getTIDs()
							.cardinality())
					* (this.posNegDb.getTransactionsNeg().size() - this.posNegDb
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

	public FrequencyDiscriminativityDistribution(PosNegDbInterface posNegDB,
			LabeledNextStateProposer nextStateProposer,
			boolean initializeCumulatedWeights) {
		super(posNegDB, nextStateProposer, posNegDB.getTransactions().size()
				* posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size());

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());

		if (initializeCumulatedWeights) {
			initialize();
			computeCumulatedWeights();
		}
	}

	public FrequencyDiscriminativityDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer) {
		super(db);

		setNextStateProposer(nextStateProposer);
		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());
	}

	private void initialize() {
		int end = this.posNegDB.getTransactions().size();
		int endPos = this.posNegDB.getTransactionsPos().size();
		int endNeg = this.posNegDB.getTransactionsNeg().size();
		int c = 0;
		for (int i = 0; i < end; i++) {
			for (int j = 0; j < endPos; j++) {
				for (int k = 0; k < endNeg; k++) {
					this.baseObjects[c++] = new LabeledOrderedBaseObject(
							new int[] { i, j, k });
				}
			}
		}
	}

	@Override
	protected double computeNormalizationFactor() {
		double normalizationFactor = 0;
		int end = this.posNegDB.getTransactions().size();
		int endPos = this.posNegDB.getTransactionsPos().size();
		int endNeg = this.posNegDB.getTransactionsNeg().size();
		for (int i = 0; i < end; i++) {
			for (int j = 0; j < endPos; j++) {
				for (int k = 0; k < endNeg; k++) {
					normalizationFactor += baseObjectWeight(this.nextStateProposer
							.createBaseObject(new int[] { i, j, k }));
				}
			}
		}
		return normalizationFactor;
	}

	@Override
	public double numberOfBaseObjects() {
		return (double) this.posNegDB.getTransactions().size()
				* this.posNegDB.getTransactionsPos().size()
				* this.posNegDB.getTransactionsNeg().size();
	}

	@Override
	public TidList[] getBitSetRepresentations(int[] indices) {
		TidList[] tidLists = new TidList[2];
		tidLists[0] = TidList.intersect(
				this.posNegDB.getTransactions().get(indices[0])
						.getItemsAsBitSet(), this.posNegDB.getTransactionsPos()
						.get(indices[1]).getItemsAsBitSet());
		tidLists[1] = indices[2] != -1 ? TidList.intersect(tidLists[0],
				this.posNegDB.getTransactionsNeg().get(indices[2])
						.getItemsAsBitSet()) : new TidList();
		return tidLists;
	}

	@Override
	public State getHardJupp() {
		if (this.hardestJupp == null) {
			int indexI = 0;
			double currentValue = 0, tempValue;
			int endPos = this.posNegDB.getTransactionsPos().size();
			for (int i = 0; i < endPos; i++) {
				if ((tempValue = this.posNegDB.getTransactionsPos().get(i)
						.size()) >= currentValue) {
					currentValue = tempValue;
					indexI = i;
				}
			}
			DebugPrinter.println(this, "HardJupp: " + indexI + " " + indexI
					+ " " + -1);
			OrderedBaseObject b = this.nextStateProposer.createBaseObject(new int[] {
					indexI, indexI, -1 });
			this.hardestJupp = new State(baseObjectWeight(b), b);
		}
		return this.hardestJupp;
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
	public DistributionTester getDistributionTester() {
		return new FrequencyDiscriminativityTester(this.posNegDB);
	}

	@Override
	public int cardinality() {
		return 1;
	}

	@Override
	public int cardinalityPos() {
		return 1;
	}

	@Override
	public int cardinalityNeg() {
		return 1;
	}
}
