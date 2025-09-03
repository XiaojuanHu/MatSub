package de.unibonn.realkd.common.optimization;


/**
 * Replicates the inner product of a given linear feature space.
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class LinearKernel<T> implements Kernel<T> {

	private final LinearFeatureSpace<T> inputSpace;

	public LinearKernel(LinearFeatureSpace<T> inputSpace) {
		this.inputSpace = inputSpace;
	}

	@Override
	public double value(T p1, T p2) {
		return inputSpace.innerProduct(p1, p2);
	}

	@Override
	public T getZeroElement() {
		return inputSpace.getZeroElement();
	}
}
