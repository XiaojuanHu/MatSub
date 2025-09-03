package de.unibonn.realkd.discovery;

import de.unibonn.realkd.common.optimization.RegressionModelFromPreferenceLearner;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Abstract base class for model trainers. Each model trainer monitors the
 * development of a SessionPatternState in order to generate training example
 * for a model learner. Therefore all model trainers have to be initialized with
 * two such corresponding objects.
 * </p>
 * <p>
 * Every client can get the ModelLearner (in most cases in order to retrieve the
 * Model); concrete subclasses can get the SessionPatternState.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public abstract class AbstractDiscoveryProcessBasedPreferenceModelTrainer
		implements DiscoveryProcessBasedPreferenceTrainer {

	private final RegressionModelFromPreferenceLearner<Pattern<?>> modelLearner;

	private DiscoveryProcessState discoveryProcessState;

	public AbstractDiscoveryProcessBasedPreferenceModelTrainer(
			RegressionModelFromPreferenceLearner<Pattern<?>> utilityModel,
			DiscoveryProcessState patternState) {
		this.modelLearner = utilityModel;
		this.discoveryProcessState = patternState;
	}

	@Override
	public final RegressionModelFromPreferenceLearner<Pattern<?>> getLearner() {
		return modelLearner;
	}

	protected final DiscoveryProcessState getPatternState() {
		return discoveryProcessState;
	}

}
