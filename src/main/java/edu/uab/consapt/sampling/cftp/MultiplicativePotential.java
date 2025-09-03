package edu.uab.consapt.sampling.cftp;

import static java.lang.Math.pow;

import java.util.Collection;
import java.util.Map;

import edu.uab.consapt.sampling.PotentialFunction;
import ua.ac.be.mime.plain.PlainItem;

public class MultiplicativePotential implements
		PotentialFunction<Collection<PlainItem>> {

	private final double power;
	private boolean biasesSet;
	private Map<Integer, Double> biases;

	public MultiplicativePotential(int cardinality) {
		power = 1.0 / cardinality;
	}

	public MultiplicativePotential(int cardinality, Map<Integer, Double> biases) {
		this(cardinality);
		biasesSet = true;
		this.biases = biases;
	}

	@Override
	public double getPotential(Collection<PlainItem> set) {
		return pow(getBasePotential(set), power);
	}

	private double getBasePotential(Collection<PlainItem> set) {

		return getTotalWeightInLanguage(set)
				- getWeightPatternsNotInLanguage(set);
	}

	private double getTotalWeightInLanguage(Collection<PlainItem> set) {
		if (!biasesSet) {
			return pow(2, set.size());
		}

		double setBias = 1;
		for (PlainItem item : set) {
			Double bias = biases.get(item.getId());
			if (bias != null) {
				setBias *= 1 + bias;
			} else {
				setBias *= 2;
			}
		}
		return setBias;
	}

	private double getWeightPatternsNotInLanguage(Collection<PlainItem> set) {
		return getWeightedSingletonsBias(set) + 1;
	}

	private double getWeightedSingletonsBias(Collection<PlainItem> set) {
		if (!biasesSet) {
			return set.size();
		}

		double singletonsBias = 0;
		for (PlainItem item : set) {
			Double bias = biases.get(item.getId());
			if (bias != null) {
				singletonsBias += bias;
			} else {
				singletonsBias += 1;
			}
		}
		return singletonsBias;
	}

}
