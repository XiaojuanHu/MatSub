package edu.uab.cftp.sampling.distribution.old;

import java.util.ArrayList;
import java.util.List;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import edu.uab.cftp.sampling.distribution.DistributionTester;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.TwoStepSamplingDistribution;
import edu.uab.cftp.sampling.distribution.tool.RandomList;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.Utils;

public class DiscriminativityDistribution extends
		SinglePosSingleNegDistribution {

	/*
	 * =========================================================================
	 * 
	 * STATIC
	 * 
	 * =========================================================================
	 */

	public static class DiscriminativityTester extends DistributionTester {

		private final PosNegDbInterface posNegDb;

		public DiscriminativityTester(PosNegDbInterface db) {
			super();
			this.posNegDb = db;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			return ((double) this.posNegDb.getPosItemSet(itemSet).getTIDs()
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

	public DiscriminativityDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer,
			boolean initializeCumulatedWeights) {
		super(db, nextStateProposer, initializeCumulatedWeights);
	}

	public DiscriminativityDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer) {
		super(db, nextStateProposer);
	}

	@Override
	public double getWeightInDistribution(PlainItem item) {
		return 1;
	}

	public static boolean general = true;

	@Override
	public double getWeightInDistribution(TidList[] transactions) {
		if (!general) {
			double posSize = transactions[0].cardinality();
			double intSize = transactions[1].cardinality();
			double onlyInPosSize = posSize - intSize;
			return (Math.pow(2, onlyInPosSize) - 1) * Math.pow(2, intSize)
					- onlyInPosSize;
		} else {
			double posSize = Math.pow(2, transactions[0].cardinality());
			if (transactions[1] != null) {
				TidList intersection = TidList.intersect(transactions[0],
						transactions[1]);
				double negSize1 = Math.pow(2, intersection.cardinality());
				return posSize - negSize1 - transactions[0].cardinality()
						+ intersection.cardinality();
			} else {
				return posSize - transactions[0].cardinality() - 1;
			}
		}
	}

	public double getWeightInDistribution2(TidList[] transactions) {
		double posSize = Math.pow(2, transactions[0].cardinality());
		if (transactions[1] != null) {
			TidList intersection = TidList.intersect(transactions[0],
					transactions[1]);
			double negSize1 = Math.pow(2, intersection.cardinality());
			return posSize - negSize1;
		} else {
			return posSize;
		}
	}

	public double getConditionedWeight(TidList[] transactions, int index) {
		TidList t1 = new TidList(transactions[0]);
		TidList t2;
		t1.set(index, false);
		if (transactions[1] == null) {
			t2 = null;
		} else {
			t2 = new TidList(transactions[1]);

			if (!t2.get(index)) {
				t2 = null;
			} else {
				t2.set(index, false);
			}
		}
		return getWeightInDistribution2(new TidList[] { t1, t2 }) * 1;
	}

	private RandomList<Integer> createRandomList(TidList t) {
		List<Integer> values = new ArrayList<Integer>();
		int index = -1;
		while ((index = t.nextSetBit(index + 1)) != -1) {
			values.add(index);
		}
		return new RandomList<Integer>(values);
	}

	@Override
	public PlainItemSet drawSubSet(OrderedBaseObject baseObject) {
		if (!general) {
			TidList[] transactions = getBitSetRepresentations(baseObject
					.getIndices());
			TidList difference = TidList.difference(transactions[0],
					transactions[1]);

			return Utils.drawDiscriminativeSubSetNoSingletons(
					this.posNegDB.getItemDB(), transactions[1], difference);
		} else {
			PlainItemSet itemSet;

			do {
				TidList t1 = new TidList(this.posNegDB.getTransactionsPos()
						.get(baseObject.getIndices()[0]).getItemsAsBitSet());
				TidList t2 = new TidList(this.posNegDB.getTransactionsNeg()
						.get(baseObject.getIndices()[1]).getItemsAsBitSet());
				itemSet = new PlainItemSet();

				int index = -1;
				double prob = 0, conditionalWeight = 0, weight = 0, singletonBias = 0, singletonsBias = 0, emptySetBias = 0;

				RandomList<Integer> randomList = createRandomList(t1);
				TidList[] tidLists;
				while (randomList.size() > 0) {
					tidLists = new TidList[] { t1, t2 };
					index = randomList.selectAndRemove();
					conditionalWeight = getConditionedWeight(tidLists, index);
					emptySetBias = (itemSet.size() > 1 || (t2 != null) ? 0 : 1);
					weight = getWeightInDistribution2(tidLists);
					singletonBias = (itemSet.size() > 0
							|| (t2 != null && t2.get(index)) ? 0 : 1);
					singletonsBias = (itemSet.size() > 0 ? 0 : TidList
							.difference(t1, t2).cardinality() * 1);

					prob = (conditionalWeight - singletonBias)
							/ (weight - singletonsBias - emptySetBias);
					boolean add = false;

					if (TwoStepSamplingDistribution.random.nextDouble() < prob) {
						itemSet.add(this.db.getItemDB().get(index));
						add = true;
					}

					t1.set(index, false);
					if (t2 != null) {
						if (!t2.get(index) && add) {
							t2 = null;
						} else {
							t2.set(index, false);
						}
					}
				}
			} while (itemSet.size() == 0);
			return itemSet;
		}
	}

	@Override
	public TidList[] getBitSetRepresentations(int[] indices) {
		if (!general) {
			return super.getBitSetRepresentations(indices);
		} else {
			TidList[] tidLists = new TidList[2];
			tidLists[0] = this.posNegDB.getTransactionsPos().get(indices[0])
					.getItemsAsBitSet();
			tidLists[1] = indices[1] != -1 ? this.posNegDB.getTransactionsNeg()
					.get(indices[1]).getItemsAsBitSet() : new TidList();
			return tidLists;
		}
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new DiscriminativityTester(this.posNegDB);
	}

	public double getWeightInDistribution(TidList transaction) {
		return ua.ac.be.mime.tool.Utils.pow(2, transaction.cardinality())
				- transaction.cardinality() - 1;
	}

}
