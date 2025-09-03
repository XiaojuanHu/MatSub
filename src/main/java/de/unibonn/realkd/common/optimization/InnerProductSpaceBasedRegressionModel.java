package de.unibonn.realkd.common.optimization;

import java.util.Comparator;

import de.unibonn.realkd.common.math.types.InnerProductSpace;

/**
 * Interface implemented by pattern scoring functions that are used for ranking
 * output patterns and currently also for parameter selection. Currently we
 * require all models to be based on an underlying Hilbert space of patterns,
 * which can be obtained by clients in order to compute, e.g., pattern distances
 * compatible with the model
 * 
 * @author Mario Boley
 * 
 */
public interface InnerProductSpaceBasedRegressionModel<T> {

	/**
	 * on the abstract level, currently score is a real utility value (can be
	 * turned into a probability using the sigmoid function)
	 */
	public abstract double value(T p);

	/**
	 * Feature space that the model is consistent with;
	 */
	public abstract InnerProductSpace<T> getFeatureSpace();

	/**
	 * returns a comparator compatible with the score function; after Java 8
	 * update, we can provide a default implementation within the interface
	 */
	public default Comparator<T> getElementComparator() {
		return (a, b) -> Double.compare(value(a), value(b));
	}

}