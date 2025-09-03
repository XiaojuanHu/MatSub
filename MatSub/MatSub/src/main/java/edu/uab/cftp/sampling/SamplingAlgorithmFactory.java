package edu.uab.cftp.sampling;

import edu.uab.cftp.sampling.distribution.StateSpaceProbabilityDistribution;

public interface SamplingAlgorithmFactory {

	public class MetropolisHastingsFactory implements SamplingAlgorithmFactory {
		@Override
		public InterruptiblePerfectSamplingAlgorithm createAlgorithm(
				StateSpaceProbabilityDistribution distribution) {
			return new MetropolisHastingsAlgorithm(distribution);
		}
	}

	public class MetropolisFactory implements SamplingAlgorithmFactory {
		@Override
		public InterruptiblePerfectSamplingAlgorithm createAlgorithm(
				StateSpaceProbabilityDistribution distribution) {
			return new MetropolisAlgorithm(distribution);
		}
	}

	public InterruptiblePerfectSamplingAlgorithm createAlgorithm(
			StateSpaceProbabilityDistribution distribution);
}
