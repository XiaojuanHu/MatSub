package edu.uab.consapt.sampling;

public interface StoppableSamplerWithInput<T, V> extends SamplerWithInput<T, V> {

	public void setStop(boolean stop);

}
