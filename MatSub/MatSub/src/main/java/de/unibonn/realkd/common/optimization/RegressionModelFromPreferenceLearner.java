package de.unibonn.realkd.common.optimization;


/**
 * <p>
 * Maintains a regression model for generic input type based on example
 * preference pairs. Regression function is supposed to be consistent with
 * provided preference pairs, in the sense that for all pairs {@code (a,b)} it should
 * ideally hold that {@code f(a) > f(b)}. However, in order to avoid overfitting this is
 * not guaranteed.
 * </p>
 * 
 * @author Mario Boley
 *
 * @param <T>
 *            the type of elements
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public interface RegressionModelFromPreferenceLearner<T> {

	/**
	 * <p>
	 * Reveals training example pair to model learner. May trigger model update.
	 * </p>
	 * 
	 * @param superior
	 *            relatively good element
	 * @param inferior
	 *            relatively bad element
	 */
	public abstract void tellPreference(T superior, T inferior);

	/**
	 * Enforces model updates.
	 */
	public abstract void doUpdate();

	/**
	 * @return the regression model maintained by this learner
	 */
	public abstract InnerProductSpaceBasedRegressionModel<T> getModel();

}