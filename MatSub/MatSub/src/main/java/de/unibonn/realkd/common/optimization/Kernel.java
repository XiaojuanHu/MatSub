package de.unibonn.realkd.common.optimization;

/**
 * Kernel-function for inducing some implicitly defined inner-product space.
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 * 
 */
public interface Kernel<T> {

	/**
	 * Inner product of v1 and v2 corresponding to dot product in some implicit
	 * feature space.
	 */
	public abstract double value(T v1, T v2);

	/**
	 * Identifies one object that is mapped to the zero element in the implicit
	 * feature space. Since the implicit feature map is not necessarily
	 * injective, this might not be the only T object for which all kernel
	 * evaluations will result in 0.
	 * 
	 * @return one T object z for which the {@link #value(Object, Object)} will evaluate to
	 *         0 for all x.
	 */
	public abstract T getZeroElement();

}
