package edu.uab.consapt.sampling;

import java.util.Random;

public abstract class AbstractDistribution<T> implements Distribution<T> {

	private static Random random = new Random(System.currentTimeMillis());

	public abstract T getNext(Random random);

	@Override
	public T getNext() {
		return getNext(random);
	}
}
