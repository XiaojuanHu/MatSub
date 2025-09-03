package de.unibonn.realkd.common.optimization;

import de.unibonn.realkd.common.math.types.InnerProductSpace;

/**
 * Implicit feature space that is defined solely by a kernel function.
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class KernelInducedFeatureSpace<T> implements InnerProductSpace<T> {

	private final Kernel<T> kernel;

	public KernelInducedFeatureSpace(Kernel<T> kernel) {
		this.kernel = kernel;
	}

	@Override
	public double innerProduct(T p1, T p2) {
		return kernel.value(p1, p2);
	}

	@Override
	public T getZeroElement() {
		return kernel.getZeroElement();
	}

}
