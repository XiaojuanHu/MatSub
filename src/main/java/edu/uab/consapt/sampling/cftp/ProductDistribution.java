package edu.uab.consapt.sampling.cftp;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;
import java.util.Random;

import edu.uab.consapt.sampling.AbstractDistribution;

public class ProductDistribution<T> extends AbstractDistribution<List<T>> {

	private final BaseDistribution<T>[] baseDistributions;

	public ProductDistribution(BaseDistribution<T>... baseDistributions) {
		this.baseDistributions = baseDistributions;
	}

	@Override
	public List<T> getNext(Random random) {
		List<T> list = newArrayListWithCapacity(baseDistributions.length);
		for (BaseDistribution<T> b : baseDistributions) {
			list.add(b.getNext(random));
		}
		return list;
	}

	@Override
	public double getPotential(List<T> t) {
		double potential = 1;
		for (int i = 0; i < t.size(); i++) {
			potential *= baseDistributions[i].getPotential(t.get(i));
		}
		return potential;
	}

}
