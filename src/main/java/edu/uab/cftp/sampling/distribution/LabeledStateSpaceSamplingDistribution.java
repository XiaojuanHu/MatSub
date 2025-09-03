package edu.uab.cftp.sampling.distribution;

import java.util.List;

import edu.uab.cftp.sampling.LabeledNextStateProposer;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.tool.DebugPrinter;

public abstract class LabeledStateSpaceSamplingDistribution extends
		StateSpaceSamplingDistribution {

	protected PosNegDbInterface posNegDB;

	public LabeledStateSpaceSamplingDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer, int numberOfObjects) {
		super(db, null, numberOfObjects);

		this.posNegDB = db;
		this.nextStateProposer = nextStateProposer;
		nextStateProposer.setDistribution(this);
		nextStateProposer.setTransactions(this.posNegDB.getTransactions());
		nextStateProposer
				.setPosTransactions(this.posNegDB.getTransactionsPos());
		nextStateProposer
				.setNegTransactions(this.posNegDB.getTransactionsNeg());
	}

	public LabeledStateSpaceSamplingDistribution(PosNegDbInterface db,
			LabeledNextStateProposer nextStateProposer,
			boolean... setNextStateProposer) {
		super(db, null);

		this.posNegDB = db;
		this.nextStateProposer = nextStateProposer;
		nextStateProposer.setDistribution(this);
		nextStateProposer.setTransactions(getTransactions());
		nextStateProposer
				.setPosTransactions(this.posNegDB.getTransactionsPos());
		nextStateProposer
				.setNegTransactions(this.posNegDB.getTransactionsNeg());
	}

	public void setNextStateProposer(LabeledNextStateProposer nextStateProposer) {
		this.nextStateProposer = nextStateProposer;
		nextStateProposer.setDistribution(this);
		nextStateProposer.setTransactions(getTransactions());
		nextStateProposer.setPosTransactions(posNegDB.getTransactionsPos());
		nextStateProposer.setNegTransactions(posNegDB.getTransactionsNeg());
	}

	public LabeledStateSpaceSamplingDistribution(PosNegDbInterface db) {
		super(db);
		this.posNegDB = db;
	}

	@Override
	public double getWeightInDistribution(TidList transaction) {
		return 0;
	}

	public abstract double getWeightInDistribution(TidList[] transactions);

	protected abstract TidList[] getBitSetRepresentations(int[] indices);

	@Override
	public State getHardJupp() {
		if (this.hardestJupp == null) {
			int indexI = 0, indexJ = 0;
			double currentValue = 0, tempValue;
			int endI = this.posNegDB.getTransactionsPos().size();
			int endJ = this.posNegDB.getTransactionsNeg().size();
			for (int i = 0; i < endI; i++) {
				for (int j = 0; j < endJ; j++) {
					if ((tempValue = getWeightInDistribution(getBitSetRepresentations(new int[] {
							i, j }))) >= currentValue) {
						currentValue = tempValue;
						indexI = i;
						indexJ = j;
					}
				}
			}
			DebugPrinter.println(this, "HardJupp: " + indexI + " " + indexJ);
			OrderedBaseObject b = this.nextStateProposer.createBaseObject(new int[] {
					indexI, indexJ });
			this.hardestJupp = new State(baseObjectWeight(b), b);
		}
		return this.hardestJupp;
	}

	@Override
	public double baseObjectWeight(OrderedBaseObject baseObject) {
		if (baseObject.getWeight() != -1) {
			return baseObject.getWeight();
		} else {
			double weight = getWeightInDistribution(getBitSetRepresentations(baseObject
					.getIndices()));
			baseObject.setWeight(weight);
			return weight;
		}
	}

	public PosNegDbInterface getPosNegDB() {
		return this.posNegDB;
	}

	public abstract int cardinalityPos();

	public abstract int cardinalityNeg();

	public double getPowersetBias(TidList transaction) {
		// TODO
		return 0;
	}

	public double getSingletonsBias(TidList transaction) {
		// TODO
		return 0;
	}

	public double getEmptySetBias() {
		// TODO
		return 0;
	}

	public List<PlainTransaction> getTransactions() {
		return posNegDB.getTransactions();
	}

}
