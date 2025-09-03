package edu.uab.consapt.sampling;

public interface StoppableSampler<T> extends Sampler<T> {

	public void setStop(boolean stop);

}
