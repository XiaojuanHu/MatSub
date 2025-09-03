package de.unibonn.realkd.common.optimization;

/**
 * Simple real-valued feature map for generic input type that provides a default
 * weight for linear models that use this feature.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public interface LinearFeature<T> {

	public double value(T element);

	/**
	 * Default value (model prior) for model coefficient of this feature in a
	 * linear model. For most features this should be 0, unless there are strong
	 * reasons that a typical user will have a positive (or negative) preference
	 * for positive values of this feature.
	 * 
	 * This mechanism is likely to be removed or modified in future versions for
	 * its not equal treatment of linear and kernelized leaners.
	 */
	public double getDefaultCoefficient();

}
