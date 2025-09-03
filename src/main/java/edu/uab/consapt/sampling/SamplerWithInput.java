package edu.uab.consapt.sampling;

public interface SamplerWithInput<T, V> extends Sampler<V> {

	static class NoContextSetException extends RuntimeException {

		private static final long serialVersionUID = 286480985422187907L;

		@Override
		public String getMessage() {
			return "SamplerWithInput not set with proper context prior to sampling";
		}
	}

	public void setContext(T t);

}
