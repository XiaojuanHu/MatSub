package edu.uab.consapt.sampling.cftp;

import java.util.List;
import java.util.Map;

public class WeightedBaseDistribution<T> extends BaseDistribution<T> {

	private final Map<T, Double> weights;

	public WeightedBaseDistribution(List<T> objects, double[] potentials,
			Map<T, Double> transactionWeights) {
		super(objects);
		this.weights = transactionWeights;
		this.potentials = potentials;
		this.cPotentials = new double[potentials.length];

		initializeCumulatedPotentials();
	}

	@Override
	protected void initializeCumulatedPotentials() {
		double total = 0;
		int i = 0;
		for (T t : objects) {
			cPotentials[i] = (total += (potentials[i] * getWeight(t)));
			i++;
		}
	}

	private double getWeight(T t) {
		Double weight = weights.get(t);
		if (weight == null) {
			return 1;
		}
		return weight;
	}

	@Override
	public double getPotential(T t) {
		return super.getPotential(t) * getWeight(t);
	}

}
