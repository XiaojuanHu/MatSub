package edu.uab.cftp.sampling.distribution;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.plain.weighting.Utils;

public class DistributionFamilyFrequency extends StateSpaceSamplingDistribution {

	public static class Tester extends DistributionTester {
		int cardinality;

		public Tester(TransactionDBInterface db, int cardinality) {
			super(db);
			this.cardinality = cardinality;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			return Math.pow(itemSet.getTIDs().cardinality(), cardinality);
		}
	}

	private final int numberOfExtraFreq;

	public DistributionFamilyFrequency(TransactionDBInterface db,
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
		return Math.pow(2, size) - size - 1;
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		return Utils.drawUniformSubSetNoSingletons(this.db.getItemDB(), Utils
				.intersectAll(Utils.getTransactions(this.transactions,
						baseObject.getIndices())));
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
		String str = "Freq";
		if (numberOfExtraFreq != 0) {
			str += "^" + (1 + numberOfExtraFreq);
		}
		return str;
	}

}
