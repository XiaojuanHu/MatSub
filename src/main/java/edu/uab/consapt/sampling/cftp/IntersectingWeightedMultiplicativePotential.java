package edu.uab.consapt.sampling.cftp;

import java.util.List;
import java.util.Map;

import ua.ac.be.mime.plain.PlainTransaction;

public class IntersectingWeightedMultiplicativePotential extends
		IntersectingMultiplicativePotential {

	private final List<Map<PlainTransaction, Double>> transactionWeights;

	public IntersectingWeightedMultiplicativePotential(int cardinalityPos,
			int cardinalityNeg,
			List<Map<PlainTransaction, Double>> transactionWeights) {
		super(cardinalityPos, cardinalityNeg);
		this.transactionWeights = transactionWeights;
	}

	public IntersectingWeightedMultiplicativePotential(int cardinalityPos,
			int cardinalityNeg, Map<Integer, Double> singletonBiases,
			List<Map<PlainTransaction, Double>> transactionWeights) {
		super(cardinalityPos, cardinalityNeg, singletonBiases);
		this.transactionWeights = transactionWeights;
	}

	@Override
	public double getPotential(List<PlainTransaction> transactions) {
		return getMultiplicatedWeight(transactions)
				* super.getPotential(transactions);
	}

	private double getMultiplicatedWeight(List<PlainTransaction> transactions) {
		double d = 1;
		int i = 0;
		for (PlainTransaction t : transactions) {
			d *= getValueInList(transactionWeights.get(i++), t);
		}
		return d;
	}

	private double getValueInList(Map<PlainTransaction, Double> map,
			PlainTransaction t) {
		Double value = map.get(t);
		if (value == null) {
			return 1;
		}
		return value;
	}

}
