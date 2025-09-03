package edu.uab.cftp.sampling;

import java.util.List;

import edu.uab.cftp.sampling.distribution.LabeledOrderedBaseObject;
import edu.uab.cftp.sampling.distribution.LabeledStateSpaceSamplingDistribution;
import edu.uab.cftp.sampling.distribution.OrderedBaseObject;
import ua.ac.be.mime.plain.PlainTransaction;

public abstract class LabeledNextStateProposer extends NextStateProposer {

	protected LabeledStateSpaceSamplingDistribution labeledDistribution;

	protected List<PlainTransaction> transactionsPos;
	protected List<PlainTransaction> transactionsNeg;

	protected int transactionsSize;
	protected int transactionsSizePos;
	protected int transactionsSizeNeg;

	protected int totalCardinality;
	protected int cardinality;
	protected int cardinalityPos;
	protected int cardinalityNeg;

	public void setDistribution(
			LabeledStateSpaceSamplingDistribution labeledDistribution) {
		this.labeledDistribution = labeledDistribution;

		this.cardinality = this.labeledDistribution.cardinality();
		this.cardinalityPos = this.labeledDistribution.cardinalityPos();
		this.cardinalityNeg = this.labeledDistribution.cardinalityNeg();
		this.totalCardinality = this.cardinality + this.cardinalityPos
				+ this.cardinalityNeg;
	}

	@Override
	public void setTransactions(List<PlainTransaction> transactions) {
		this.transactions = transactions;
		this.transactionsSize = this.transactions.size();
	}

	public void setPosTransactions(List<PlainTransaction> transactionsPos) {
		this.transactionsPos = transactionsPos;
		this.transactionsSizePos = this.transactionsPos.size();
	}

	public void setNegTransactions(List<PlainTransaction> transactionsNeg) {
		this.transactionsNeg = transactionsNeg;
		this.transactionsSizeNeg = this.transactionsNeg.size();
	}

	@Override
	public OrderedBaseObject createBaseObject(int[] indices) {
		return new LabeledOrderedBaseObject(indices);
	}
}