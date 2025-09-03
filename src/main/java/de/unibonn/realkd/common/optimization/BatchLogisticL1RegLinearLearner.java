package de.unibonn.realkd.common.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Pair;
import de.unibonn.realkd.util.Lists;

/**
 * <p>
 * Maintains a pattern utility model based on batch learning via stochastic
 * coordinate descent. This version uses Logistic Loss based on the differences
 * between provided pairwise comparisons.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.3.0
 * 
 */
public class BatchLogisticL1RegLinearLearner<T> implements RegressionModelFromPreferenceLearner<T> {

	private static final Logger LOGGER = Logger.getLogger(BatchLogisticL1RegLinearLearner.class.getName());

	private static final int ITERATIONS_FACTOR = 1000;

	private List<Pair<T, T>> trainingData;

	private List<List<Double>> differences;

	private static final double GAMMA = 0.01;

	private LinearModel<T> model;

	public BatchLogisticL1RegLinearLearner(LinearFeatureSpace<T> optimizationSpace) {
		this.model = new LinearModel<T>(optimizationSpace);
		this.trainingData = new ArrayList<>();
		this.differences = new ArrayList<>();
	}

	@Override
	public void tellPreference(T superior, T inferior) {
		LOGGER.fine("Received relative training example; prefer " + superior + " over " + inferior);

		Pair<T, T> pair = Pair.pair(superior, inferior);

		trainingData.add(pair);
		differences.add(getDifference(pair));
	}

	private List<Double> getDifference(Pair<T, T> pair) {
		ArrayList<Double> newDifferenceVector = new ArrayList<>(
				((LinearFeatureSpace<T>) this.model.getFeatureSpace()).getFeatures().size());

		for (LinearFeature<T> feature : this.model.getFeatureSpace().getFeatures()) {
			newDifferenceVector.add(feature.value(pair._1()) - feature.value(pair._2()));
		}

		return newDifferenceVector;
	}

	public void doUpdate() {
		if (differences.size() == 0) {
			return;
		}

		DoubleBinaryOperator lossDerivative = (score, label) -> (-label) / (1 + Math.exp(label * score));
		List<Double> weights = model.getWeights();
		List<Double> labels = Lists.generatorBackedList(i -> 1.0, weights.size());
		List<Double> aprioriWeights = model.getFeatureSpace().getFeatures().stream().map(f -> f.getDefaultCoefficient())
				.collect(Collectors.toList());
		double betaInverse = 4.0;
		double gamma = GAMMA;
		int maxIterations = weights.size() * ITERATIONS_FACTOR;
		// IntToDoubleFunction partialDerivative = i ->
		// computePartialDerivative(i, weights, differences);

		StochasticCoordinateDescent.stochasticCoordinateDescent(lossDerivative, weights, differences, labels,
				aprioriWeights, betaInverse, gamma, maxIterations);
	}

	public String toString() {
		return this.getClass().toString();
	}

	public LinearModel<T> getModel() {
		return model;
	}

}
