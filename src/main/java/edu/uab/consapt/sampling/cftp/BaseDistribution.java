package edu.uab.consapt.sampling.cftp;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static ua.ac.be.mime.tool.Utils.logIndexSearch;

import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.uab.consapt.sampling.AbstractDistribution;

public class BaseDistribution<T> extends AbstractDistribution<T> {

	protected final List<T> objects;
	protected Map<T, Integer> index;

	protected double[] potentials;
	protected double[] cPotentials;

	private T last;
	private int lastIx;

	protected BaseDistribution(List<T> objects) {
		this.objects = objects;
		this.index = newHashMapWithExpectedSize(objects.size());

		initializeIndex();
	}

	public BaseDistribution(List<T> objects, double[] potentials) {
		this(objects);

		this.potentials = potentials;
		this.cPotentials = new double[potentials.length];

		initializeCumulatedPotentials();
	}

	private void initializeIndex() {
		int ix = 0;
		for (T t : objects) {
			this.index.put(t, ix++);
		}
	}

	protected void initializeCumulatedPotentials() {
		double total = 0;
		for (int ix = 0; ix < potentials.length; ix++) {
			cPotentials[ix] = (total += potentials[ix]);
		}
	}

	private double getValueToSearch(Random random) {
		try {
			return random.nextDouble() * cPotentials[cPotentials.length - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
		}
		return random.nextDouble() * cPotentials[cPotentials.length - 1];
	}

	@Override
	public T getNext(Random random) {
		lastIx = logIndexSearch(cPotentials, getValueToSearch(random));
		last = objects.get(lastIx);
		return last;
	}

	@Override
	public double getPotential(T t) {
		if (t == last) {
			return potentials[lastIx];
		}
		return potentials[index.get(t)];
	}
}
