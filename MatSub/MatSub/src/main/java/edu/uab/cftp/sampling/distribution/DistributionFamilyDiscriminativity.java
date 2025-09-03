package edu.uab.cftp.sampling.distribution;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.Utils;

public class DistributionFamilyDiscriminativity extends
		LabeledStateSpaceSamplingDistribution {

	public static class Tester extends DistributionTester {
		private final PosNegDbInterface posNegDb;
		private final int cardinality;
		private final int cardinalityPos;
		private final int cardinalityNeg;

		public Tester(PosNegDbInterface db, int cardinality,
				int cardinalityPos, int cardinalityNeg) {
			super(db);
			this.posNegDb = db;
			this.cardinality = cardinality;
			this.cardinalityPos = cardinalityPos;
			this.cardinalityNeg = cardinalityNeg;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			return Math.pow(this.posNegDb.getCompleteDbItemSet(itemSet)
					.getTIDs().cardinality(), cardinality)
					* Math.pow(this.posNegDb.getPosItemSet(itemSet).getTIDs()
							.cardinality(), cardinalityPos)
					* Math.pow(
							(this.posNegDb.getTransactionsNeg().size() - this.posNegDb
									.getNegItemSet(itemSet).getTIDs()
									.cardinality()), cardinalityNeg);
		}
	}

	private final int numberOfExtraFreq;
	private final int numberOfExtraPosFreq;

	protected PosNegDbInterface posNegDB2;

	public DistributionFamilyDiscriminativity(PosNegDbInterface posNegDB,
			LabeledNextStateProposer nextStateProposer, int numberOfExtraFreq,
			int numberOfExtraPosFreq) {
		super(posNegDB);
		this.posNegDB2 = posNegDB;
		this.numberOfExtraFreq = numberOfExtraFreq;
		this.numberOfExtraPosFreq = numberOfExtraPosFreq;
		super.setNextStateProposer(nextStateProposer);
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return 1;
	}

	@Override
	public double getWeightInDistribution(TidList transaction) {
		double size = transaction.cardinality();
		return (size * (Math.pow(2, size - 1))) - size;
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
		return new Tester(posNegDB, cardinality(), cardinalityPos(),
				cardinalityNeg());
	}

	@Override
	protected double computeNormalizationFactor() {
		return 0;
	}

	@Override
	public double numberOfBaseObjects() {
		return 0;
	}

	@Override
	public int cardinality() {
		return 0 + numberOfExtraFreq;
	}

	@Override
	public double getWeightInDistribution(TidList[] transactions) {
		double posSize = transactions[0].cardinality();
		double intSize = transactions[1].cardinality();
		double onlyInPosSize = posSize - intSize;
		return (Math.pow(2, onlyInPosSize) - 1) * Math.pow(2, intSize)
				- onlyInPosSize;
	}

	private TidList intersectExceptLast(int[] indices) {
		int index = numberOfExtraFreq;
		TidList intersection = new TidList(this.posNegDB.getTransactionsPos()
				.get(indices[index]).getItemsAsBitSet());
		for (; index < indices.length - 1; index++) {
			intersection.and(this.posNegDB.getTransactionsPos()
					.get(indices[index]).getItemsAsBitSet());
		}
		for (int i = 0; i < numberOfExtraFreq; i++) {
			intersection.and(this.posNegDB.getTransactions().get(indices[i])
					.getItemsAsBitSet());
		}
		return intersection;
	}

	@Override
	protected TidList[] getBitSetRepresentations(int[] indices) {
		TidList[] tidLists = new TidList[2];
		tidLists[0] = intersectExceptLast(indices);
		tidLists[1] = indices[indices.length - 1] != -1 ? TidList.intersect(
				tidLists[0],
				this.posNegDB.getTransactionsNeg()
						.get(indices[indices.length - 1]).getItemsAsBitSet())
				: new TidList();
		return tidLists;
	}

	@Override
	public int cardinalityPos() {
		return 1 + numberOfExtraPosFreq;
	}

	@Override
	public int cardinalityNeg() {
		return 1;
	}

	@Override
	public String toString() {
		String str = "";
		if (numberOfExtraFreq != 0) {
			str += "Freq";
			if (numberOfExtraFreq != 1) {
				str += "^" + numberOfExtraFreq;
			}
			str += " x ";
		}
		if (numberOfExtraPosFreq != 0) {
			str += "PosFreq";
			if (numberOfExtraPosFreq != 1) {
				str += "^" + numberOfExtraPosFreq;
			}
			str += " x ";
		}
		return str + "Discriminativity";
	}

}
