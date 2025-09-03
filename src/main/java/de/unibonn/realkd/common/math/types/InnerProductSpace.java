package de.unibonn.realkd.common.math.types;

/**
 * Represents vector spaces of objects of some specific type. We require vector
 * spaces to be equipped with an inner product; consequently the interface also
 * includes the derived functions norm and cosine as convenience for clients and
 * in order to allow efficient implementations.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 * 
 * @param <T>
 *            the input type that is structured in this inner product space
 * 
 * 
 */
public interface InnerProductSpace<T> extends MetricSpace<T> {

	public double innerProduct(T p1, T p2);

	/**
	 * Norm induced by the inner product, i.e., square root
	 * of {@code <p,p>}.
	 * 
	 */
	public default double norm(T p) {
		return Math.sqrt(innerProduct(p, p));
	}

	/**
	 * Cosine induced by inner product, i.e., {@code <p1,p2>} divided by norms of {@code p1} and
	 * {@code p2}, again using the norm induced by inner product.
	 */
	public default double cosine(T p1, T p2) {
		return innerProduct(p1, p2) / (norm(p1) * norm(p2));
	}

	/**
	 * Distance induced by the inner product (norm of p1-p2).
	 * 
	 */
	@Override
	public default double distance(T a, T b) {
		return Math.sqrt(innerProduct(a, a) + innerProduct(b, b) - 2
				* innerProduct(a, b));
	}
	
	public T getZeroElement();

}
