package edu.uab.consapt.sampling.cftp;

import static java.lang.Math.pow;

import java.util.Collection;
import java.util.Map;

import edu.uab.consapt.sampling.PotentialFunction;
import ua.ac.be.mime.plain.PlainItem;

public class AdditivePotential implements
		PotentialFunction<Collection<PlainItem>> {

	private final double power;
	private boolean biasesSet;
	private Map<Integer, Double> biases;

	public AdditivePotential(int cardinality) {
		power = 1.0 / cardinality;
		biasesSet = false;
	}

	public AdditivePotential(int cardinality, Map<Integer, Double> biases) {
		this(cardinality);
		this.biases = biases;
		biasesSet = true;
	}

	@Override
	public double getPotential(Collection<PlainItem> set) {
		return pow(getBasePotential(set), power);
	}

	private double getBasePotential(Collection<PlainItem> set) {
		if (!biasesSet) {
			return set.size() * Math.pow(2, set.size() - 1) - set.size();
		}
		double setBias = getSetBias(set);
		return setBias * Math.pow(2, set.size() - 1) - setBias;
	}

	private double getSetBias(Collection<PlainItem> set) {
		if (!biasesSet) {
			return set.size();
		}

		double setBias = 0;
		for (PlainItem item : set) {
			Double bias = biases.get(item.getId());
			if (bias != null) {
				setBias += bias;
			} else {
				setBias += 1;
			}
		}
		return setBias;
	}

}
