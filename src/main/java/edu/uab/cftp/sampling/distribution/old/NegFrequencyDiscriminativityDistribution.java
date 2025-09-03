package edu.uab.cftp.sampling.distribution.old;

import java.util.ArrayList;
import java.util.List;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import edu.uab.cftp.sampling.distribution.DistributionTester;
import edu.uab.cftp.sampling.distribution.LabeledOrderedBaseObject;
import edu.uab.cftp.sampling.distribution.LabeledStateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import edu.uab.cftp.sampling.distribution.State;
import edu.uab.cftp.sampling.distribution.TwoStepSamplingDistribution;
import edu.uab.cftp.sampling.distribution.tool.RandomList;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemSet;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public class NegFrequencyDiscriminativityDistribution extends
		LabeledStateSpaceSamplingDistribution {

	/*
	 * =========================================================================
	 * 
	 * STATIC
	 * 
	 * =========================================================================
	 */

	public static class NegFrequencyDiscriminativityTester extends
			DistributionTester {

		private final PosNegDbInterface posNegDb;

		public NegFrequencyDiscriminativityTester(PosNegDbInterface db) {
			super();
			this.posNegDb = db;
		}

		@Override
		public double computeMeasureValue(PlainItemSet itemSet) {
			return this.posNegDb.getPosItemSet(itemSet).getTIDs().cardinality()
					* Math.pow(this.posNegDb.getTransactionsNeg().size()
							- this.posNegDb.getNegItemSet(itemSet).getTIDs()
									.cardinality(), 2);
		}
	}

	/*
	 * =========================================================================
	 * 
	 * NON-STATIC
	 * 
	 * =========================================================================
	 */

	public NegFrequencyDiscriminativityDistribution(PosNegDbInterface posNegDB,
			LabeledNextStateProposer nextStateProposer,
			boolean initializeCumulatedWeights) {
		super(posNegDB, nextStateProposer, posNegDB.getTransactionsPos().size()
				* posNegDB.getTransactionsNeg().size()
				* posNegDB.getTransactionsNeg().size());

		if (initializeCumulatedWeights) {
			initialize();
			computeCumulatedWeights();
		}
	}

	public NegFrequencyDiscriminativityDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer) {
		super(db, nextStateProposer);

		DebugPrinter.println(this, "Number of BaseObjects: "
				+ numberOfBaseObjects());
	}

	private void initialize() {
		int endPos = this.posNegDB.getTransactionsPos().size();
		int endNeg = this.posNegDB.getTransactionsNeg().size();
		int c = 0;
		for (int i = 0; i < endPos; i++) {
			for (int j = 0; j < endNeg; j++) {
				for (int k = 0; k < endNeg; k++) {
					this.baseObjects[c++] = new LabeledOrderedBaseObject(
							new int[] { i, j, k });
				}
			}
		}
	}

	@Override
	public double getPowersetBias(TidList transaction) {
		if (transaction == null) {
			return 0;
		}

		return Math.pow(2, transaction.cardinality());
	}

	@Override
	public double getSingletonsBias(TidList transaction) {
		if (transaction == null) {
			return 0;
		}

		return transaction.cardinality() * 1;
	}

	@Override
	public double getEmptySetBias() {
		return 1;
	}

	@Override
	protected double computeNormalizationFactor() {
		double normalizationFactor = 0;
		int endPos = this.posNegDB.getTransactionsPos().size();
		int endNeg = this.posNegDB.getTransactionsNeg().size();
		for (int i = 0; i < endPos; i++) {
			for (int j = 0; j < endNeg; j++) {
				for (int k = 0; k < endNeg; k++) {
					normalizationFactor += baseObjectWeight(this.nextStateProposer
							.createBaseObject(new int[] { i, j, k }));
				}
			}
		}
		return normalizationFactor;
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
			DebugPrinter.println(this, "HardJupp: " + indexI + " " + -1 + " "
					+ -1);
			OrderedBaseObject b = this.nextStateProposer.createBaseObject(new int[] {
					indexI, -1, -1 });
			this.hardestJupp = new State(baseObjectWeight(b), b);
		}
		return this.hardestJupp;
	}

	@Override
	public TidList[] getBitSetRepresentations(int[] indices) {
		TidList[] tidLists = new TidList[3];
		tidLists[0] = this.posNegDB.getTransactionsPos().get(indices[0])
				.getItemsAsBitSet();
		if (indices[1] == -1) {
			tidLists[1] = new TidList();
			tidLists[2] = new TidList();
		} else {
			tidLists[1] = this.posNegDB.getTransactionsNeg().get(indices[1])
					.getItemsAsBitSet();
			tidLists[2] = this.posNegDB.getTransactionsNeg().get(indices[2])
					.getItemsAsBitSet();
		}
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
		double posSize = Math.pow(2, transactions[0].cardinality());
		double second = 0, third = 0, fourth = 0;
		if (transactions[1] != null) {
			TidList intersection = TidList.intersect(transactions[0],
					transactions[1]);
			second = Math.pow(2, intersection.cardinality());
		}
		if (transactions[2] != null) {
			TidList intersection = TidList.intersect(transactions[0],
					transactions[2]);
			third = Math.pow(2, intersection.cardinality());
		}
		if (transactions[1] != null && transactions[2] != null) {
			TidList intersection = TidList.intersect(
					TidList.intersect(transactions[0], transactions[1]),
					transactions[2]);
			fourth = Math.pow(2, intersection.cardinality());
		}
		double singleTons = TidList.difference(
				TidList.difference(transactions[0], transactions[1]),
				transactions[2]).cardinality();

		return posSize - second - third + fourth - singleTons;
	}

	public double getWeight(TidList[] transactions) {
		double posSize = Math.pow(2, transactions[0].cardinality());
		double second = 0, third = 0, fourth = 0;
		if (transactions[1] != null) {
			TidList intersection = TidList.intersect(transactions[0],
					transactions[1]);
			second = Math.pow(2, intersection.cardinality());
		}
		if (transactions[2] != null) {
			TidList intersection = TidList.intersect(transactions[0],
					transactions[2]);
			third = Math.pow(2, intersection.cardinality());
		}
		if (transactions[1] != null && transactions[2] != null) {
			TidList intersection = TidList.intersect(
					TidList.intersect(transactions[0], transactions[1]),
					transactions[2]);
			fourth = Math.pow(2, intersection.cardinality());
		}
		return posSize - second - third + fourth;
	}

	public double getConditionedWeight(TidList[] transactions, int index) {
		TidList t1 = new TidList(transactions[0]);
		TidList t2;
		TidList t3;
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
		if (transactions[2] == null) {
			t3 = null;
		} else {
			t3 = new TidList(transactions[2]);
			if (!t3.get(index)) {
				t3 = null;
			} else {
				t3.set(index, false);
			}
		}
		return getWeight(new TidList[] { t1, t2, t3 }) * 1;
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
		PlainItemSet itemSet;

		TidList t1 = new TidList(this.posNegDB.getTransactionsPos()
				.get(baseObject.getIndices()[0]).getItemsAsBitSet());
		TidList t2 = new TidList(this.posNegDB.getTransactionsNeg()
				.get(baseObject.getIndices()[1]).getItemsAsBitSet());
		TidList t3 = new TidList(this.posNegDB.getTransactionsNeg()
				.get(baseObject.getIndices()[2]).getItemsAsBitSet());
		itemSet = new PlainItemSet();

		int index = -1;
		double prob = 0, conditionalWeight = 0, weight = 0, singletonBias = 0, singletonsBias = 0, emptySetBias = 0;

		RandomList<Integer> randomList = createRandomList(t1);
		TidList[] tidLists;
		while (randomList.size() > 0) {
			tidLists = new TidList[] { t1, t2, t3 };
			index = randomList.selectAndRemove();
			// if (index == 1) {
			// System.out.println("Moet accepted worden");
			// }
			conditionalWeight = getConditionedWeight(tidLists, index);
			emptySetBias = (itemSet.size() > 1 || (t2 != null || t3 != null) ? 0
					: 1);
			weight = getWeight(tidLists);
			singletonBias = (itemSet.size() > 0
					|| (t2 != null && t2.get(index) || t3 != null
							&& t3.get(index)) ? 0 : 1);
			singletonsBias = (itemSet.size() > 0 ? 0 : TidList.difference(
					TidList.difference(t1, t2), t3).cardinality() * 1);

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
			if (t3 != null) {
				if (!t3.get(index) && add) {
					t3 = null;
				} else {
					t3.set(index, false);
				}
			}
		}
		return itemSet;
	}

	@Override
	public double numberOfBaseObjects() {
		return this.posNegDB.getTransactionsPos().size()
				* this.posNegDB.getTransactionsNeg().size()
				* this.posNegDB.getTransactionsNeg().size();
	}

	@Override
	public DistributionTester getDistributionTester() {
		return new NegFrequencyDiscriminativityTester(this.posNegDB);
	}

	public String toString() {
		return "NegFreq x Discriminativity";
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
		return 2;
	}
}
