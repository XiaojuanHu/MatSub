package edu.uab.cftp.sampling.distribution;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.plain.weighting.Utils;

public class DistributionFamilyArea extends StateSpaceSamplingDistribution {

	private final int numberOfExtraFreq;

	public DistributionFamilyArea(TransactionDBInterface db,
			NextStateProposer nextStateProposer, int numberOfExtraFreq) {
		super(db);
		this.numberOfExtraFreq = numberOfExtraFreq;
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
		return Utils.drawSequentialSubsetNoSingletons(this.db.getItemDB(),
				Utils.intersectAll(Utils.getTransactions(this.transactions,
						baseObject.getIndices())));
	}

	public static class Tester extends DistributionTester {
		int cardinality;

		public Tester(TransactionDBInterface db, int cardinality) {
			super(db);
			this.cardinality = cardinality;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {

			return itemSet.size()
					* Math.pow(itemSet.getTIDs().cardinality(),
							this.cardinality);
		}
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new Tester(db, cardinality());
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
		return 1 + numberOfExtraFreq;
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
		return str + "Area";
	}

}
