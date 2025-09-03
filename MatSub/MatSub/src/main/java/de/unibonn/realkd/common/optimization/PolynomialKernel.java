package de.unibonn.realkd.common.optimization;

import de.unibonn.realkd.common.math.types.InnerProductSpace;

/**
 * Based on an "input space" with inner product k' and numerical parameters c
 * and d, the kernel is given by the function k(x,y)=(c+k'(x,y))^d.
 * 
 * @author Pavel Tokmakov
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class PolynomialKernel<T> implements Kernel<T> {

	private final int d;

	private final double c;

	private final InnerProductSpace<T> inputSpace;

	private final T zeroElement;

	public PolynomialKernel(InnerProductSpace<T> inputSpace, int d, double c,
			T zeroElement) {
		this.inputSpace = inputSpace;
		this.d = d;
		this.c = c;
		this.zeroElement = zeroElement;
	}

	public double value(T p1, T p2) {
		if (zeroElement.equals(p1) || zeroElement.equals(p2)) {
			return 0.0;
		}

		return Math.pow(c + inputSpace.innerProduct(p1, p2), d);
	}

	@Override
	public T getZeroElement() {
		return zeroElement;
	}
}
