package de.unibonn.realkd.common.optimization;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.math.types.InnerProductSpace;

/**
 * <p>
 * Inner product space that represents objects of specific type by mapping it
 * into an n dimensional real space defined by a list of n features.
 * </p>
 * <p>
 * The inner product is the simple dot product between the feature
 * representations.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public class LinearFeatureSpace<T> implements InnerProductSpace<T> {

	private final List<LinearFeature<T>> features;

	private final T zeroElement;

	public LinearFeatureSpace(List<LinearFeature<T>> features, T zeroElement) {
		this.features = features;
		this.zeroElement = zeroElement;
	}

	public List<LinearFeature<T>> getFeatures() {
		return features;
	}

	public List<Double> getFeatureValues(T p) {
		List<Double> values = new ArrayList<>(features.size());

		for (LinearFeature<T> feature : features) {
			values.add(feature.value(p));
		}

		return values;
	}

	@Override
	public double innerProduct(T p1, T p2) {
		if (zeroElement.equals(p1) || zeroElement.equals(p2)) {
			return 0.0;
		}
		List<Double> features1 = getFeatureValues(p1);
		List<Double> features2 = getFeatureValues(p2);

		double res = 0.;
		for (int i = 0; i < features1.size(); i++) {
			res += features1.get(i) * features2.get(i);
		}
		return res;
	}

	@Override
	public T getZeroElement() {
		return zeroElement;
	}

}
