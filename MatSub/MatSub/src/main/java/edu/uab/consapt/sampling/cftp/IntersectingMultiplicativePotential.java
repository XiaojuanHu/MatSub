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

public class IntersectingMultiplicativePotential implements
		PotentialFunction<List<PlainTransaction>> {

	private static final double EMPTY_SET_BIAS = 1;

	private final boolean singletonBiasesAreSet;
	private Map<Integer, Double> singletonBiases;
	protected final int cardinalityPos;
	private final int cardinalityNeg;
	private final Bias biasComputer;

	public IntersectingMultiplicativePotential(int cardinalityPos,
			int cardinalityNeg) {
		this.singletonBiasesAreSet = false;
		this.cardinalityPos = cardinalityPos;
		this.cardinalityNeg = cardinalityNeg;

		biasComputer = bias(StarOperation.MULTIPLICATIVE, 1);
	}

	public IntersectingMultiplicativePotential(int cardinalityPos,
			int cardinalityNeg, Map<Integer, Double> singletonBiases) {
		this.cardinalityPos = cardinalityPos;
		this.cardinalityNeg = cardinalityNeg;
		this.singletonBiases = singletonBiases;
		this.singletonBiasesAreSet = true;

		biasComputer = bias(StarOperation.MULTIPLICATIVE, 1, this.singletonBiases);
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
		if (singletonBiasesAreSet) {
			return this.biasComputer.getWeight(posIntersection,
					new TidList[] {})
					- getWeightedSingletonsBias(posIntersection)
					- EMPTY_SET_BIAS;
		}
		int size = posIntersection.cardinality();
		return Math.pow(2, size) - size - EMPTY_SET_BIAS;
	}

	private double getSingletonsBias(TidList posIntersectionDifference) {
		if (!singletonBiasesAreSet) {
			return posIntersectionDifference.cardinality();
		}
		return getWeightedSingletonsBias(posIntersectionDifference);
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
