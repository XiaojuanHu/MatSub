package de.unibonn.realkd.discovery;

import de.unibonn.realkd.common.optimization.RegressionModelFromPreferenceLearner;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Interface for classes that feed online pattern preference learners with
 * examples based on the implicit user feedback that amounts during a discovery
 * process.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public interface DiscoveryProcessBasedPreferenceTrainer extends
		DiscoveryProcessObserver {

	public abstract RegressionModelFromPreferenceLearner<Pattern<?>> getLearner();

}
