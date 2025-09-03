package edu.uab.consapt.sampling.cftp;

import static edu.uab.cftp.sampling.distribution.tool.Biases.bias;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import edu.uab.cftp.sampling.distribution.tool.Bias;
import edu.uab.cftp.sampling.distribution.tool.StarOperation;
import edu.uab.consapt.sampling.PotentialFunction;
import ua.ac.be.mime.mining.TidList;
import ua.ac.be.mime.plain.PlainTransaction;
import ua.ac.be.mime.plain.weighting.Utils;

public class IntersectingAdditivePotential implements
		PotentialFunction<List<PlainTransaction>> {

	private static final double EMPTY_SET_BIAS = 0;

	private final boolean singletonBiasesAreSet;
	private Map<Integer, Double> singletonBiases;
	protected final int cardinalityPos;
	private final int cardinalityNeg;
	private final Bias biasComputer;

	public IntersectingAdditivePotential(int cardinalityPos, int cardinalityNeg) {
		this.singletonBiasesAreSet = false;
		this.cardinalityPos = cardinalityPos;
		this.cardinalityNeg = cardinalityNeg;

		biasComputer = bias(StarOperation.ADDITIVE, 1);
	}

	public IntersectingAdditivePotential(int cardinalityPos,
			int cardinalityNeg, Map<Integer, Double> singletonBiases) {
		this.cardinalityPos = cardinalityPos;
		this.cardinalityNeg = cardinalityNeg;
		this.singletonBiases = singletonBiases;
		this.singletonBiasesAreSet = true;

		biasComputer = bias(StarOperation.ADDITIVE, 1, this.singletonBiases);
	}

	@Override
	public double getPotential(List<PlainTransaction> transactions) {
		TidList posIntersection = intersectPositiveTransactionsUsingBitSets(transactions);
		if (cardinalityNeg == 0) {
			return computePotentialWithoutNegatives(posIntersection);
		}
		TidList[] negativeParts = getNegativeParts(transactions);
		TidList posIntersectionDifference = getPosIntersectionDifference(
				posIntersection, negativeParts);

		return this.biasComputer.getWeight(posIntersection, negativeParts)
				- getSingletonsBias(posIntersectionDifference);
	}

	private double computePotentialWithoutNegatives(TidList posIntersection) {
		double setBias = getSingletonsBias(posIntersection);
		return setBias * Math.pow(2, posIntersection.cardinality() - 1)
				- setBias - EMPTY_SET_BIAS;
	}

	private double getSingletonsBias(TidList itemsAsTids) {
		if (!singletonBiasesAreSet) {
			return itemsAsTids.cardinality();
		}
		return getWeightedSingletonsBias(itemsAsTids);
	}

	private TidList getPosIntersectionDifference(TidList posIntersection,
			TidList[] negativeParts) {
		TidList posIntersectionDifference = new TidList(posIntersection);
		for (TidList negativePart : negativeParts) {
			posIntersectionDifference.andNot(negativePart);
		}
		return posIntersectionDifference;
	}

	private TidList[] getNegativeParts(List<PlainTransaction> transactions) {
		TidList[] negativeParts = new TidList[cardinalityNeg];
		for (int i = 0; i < cardinalityNeg; i++) {
			negativeParts[i] = transactions.get(cardinalityPos + i)
					.getItemsAsBitSet();
		}
		return negativeParts;
	}

	private TidList intersectPositiveTransactionsUsingBitSets(
			List<PlainTransaction> transactions) {
		if(cardinalityNeg == 0) {
			return Utils.intersectAll(transactions);
		}
		return Utils.intersectAll(transactions.subList(0, cardinalityPos));
	}

	private double getWeightedSingletonsBias(BitSet intersection) {
		double singletonsBias = 0;
		int ix = 0;
		while ((ix = intersection.nextSetBit(ix + 1)) != -1) {
			Double bias = singletonBiases.get(ix);
			if (bias != null) {
				singletonsBias += bias;
			} else {
				singletonsBias += 1;
			}
		}
		return singletonsBias;
	}

}
