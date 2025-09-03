package edu.uab.cftp.sampling.distribution;

import edu.uab.cftp.sampling.NextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.TransactionDBInterface;
import ua.ac.be.mime.plain.weighting.Utils;

public class DistributionFamilyRare extends StateSpaceSamplingDistribution {

	public static class Tester extends DistributionTester {
		int cardinality;

		public Tester(TransactionDBInterface db, int cardinality) {
			super(db);
			this.cardinality = cardinality;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			double size = this.db.getTransactions().size();
			double value = Math.pow((itemSet.getTIDs().cardinality()),
					cardinality);
			for (PlainItem item : itemSet) {
				value *= (1 - (item.getTIDs().cardinality() / size));
			}
			return value;
		}
	}

	private final int numberOfExtraFreq;

	public DistributionFamilyRare(TransactionDBInterface db,
			NextStateProposer nextStateProposer, int numberOfExtraFreq) {
		super(db);
		this.numberOfExtraFreq = numberOfExtraFreq;
		super.setNextStateProposer(nextStateProposer);
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return 1.0 - ((double) item.getTIDs().cardinality())
				/ this.transactions.size();
	}

	@Override
	public double getWeightInDistribution(TidList transaction) {
		return Utils
				.getMultSubSetWeightNoSingletons(getSingletonWeightsInDistribution(transaction));
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		TidList intersection = Utils.intersectAll(Utils.getTransactions(
				this.transactions, baseObject.getIndices()));
		return Utils.drawMultWeightedSubSetNoSingletons(this.db.getItemDB(),
				intersection, getSingletonWeightsInDistribution(intersection));
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
		return str + "Rare";
	}

}
