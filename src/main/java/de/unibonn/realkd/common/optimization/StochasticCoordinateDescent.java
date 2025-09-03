/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.common.optimization;

import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;

/**
 * Implementation of stochastic coordinate descent based on <i>Stochastic
 * methods for l1-regularized loss minimization</i> by Shalev-Shwartz, Shai and
 * Tewari, Ambuj.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class StochasticCoordinateDescent {

	// public interface LossFunction {
	//
	// public DoubleBinaryOperator lossDerivative();
	//
	// public double betaInverse();
	//
	// }

	private static Logger LOGGER = Logger.getLogger(StochasticCoordinateDescent.class.getName());

	private static double getScoreForExample(List<Double> weights, List<Double> example) {
		double res = 0.0;

		// for (int j = 0; j < model.getWeights().size(); j++) {
		// res += model.getWeights().get(j) * differences.get(i).get(j);
		// }

		for (int j = 0; j < weights.size(); j++) {
			res += weights.get(j) * example.get(j);
			if (Math.abs(example.get(j)) > 1) {
				LOGGER.warning("absolute value of data entry larger than one---search likely to diverge");
			}
		}

		return res;
	}

	private static double computePartialDerivative(int coordinate, List<Double> weights, List<List<Double>> data,
			List<Double> labels, DoubleBinaryOperator lossDerivative) {
		// double y = 1.0;
		double res = 0.0;

		// for (int i = 0; i < differences.size(); i++) {
		for (int i = 0; i < data.size(); i++) {
			double score = getScoreForExample(weights, data.get(i));
			Double label = labels.get(i);
//			 System.out.println("score, label "+score+","+label);
			res += data.get(i).get(coordinate) * lossDerivative.applyAsDouble(score, label);
			if (Math.abs(label) > 1) {
				LOGGER.warning("absolute label value larger than one---search likely to diverge");
			}

			// res += data.get(i).get(coordinate) * (-y) / (1 + Math.exp(y *
			// getScoreForExample(weights, data.get(i))));
			// res += differences.get(i).get(coordinate) / (1 + Math.exp(y *
			// getScoreForExample(i)));
		}
//		 System.out.println("unormalized partial derivative: "+res);
		 res = res / data.size();
//		System.out.println("normalized partial derivative: " + res);

		return res;

		// return -y * res / differences.size();
	}

	/**
	 * @param lossDerivative
	 * @param weights
	 * @param labels
	 * @param aprioriWeights
	 * @param betaInverse
	 * @param gamma
	 * @param maxIterations
	 */
	public static void stochasticCoordinateDescent(DoubleBinaryOperator lossDerivative, List<Double> weights,
			List<List<Double>> data, List<Double> labels, List<Double> aprioriWeights, double betaInverse, double gamma,
			int maxIterations) {
		LOGGER.info("Starting model parameter optimization by coordiante descent with " + String.valueOf(data.size())
				+ " examples (max " + String.valueOf(maxIterations) + " iterations)...");

		int t = -1;
		int freeWeights = weights.size();
		boolean[] freeToChange = new boolean[weights.size()];
		for (int i = 0; i < weights.size(); i++) {
			freeToChange[i] = true;
		}

		int skipCounter = 0;
		while (t < maxIterations && freeWeights > 0) {
			t++;
			int coordinate = (int) (Math.random() * weights.size());
			// System.out.println("updating weight: " + coordinate);

			if (!freeToChange[coordinate]) {
				// System.out.println("cannot change---skip");
				skipCounter++;
				continue;
			}

			double oldValue = weights.get(coordinate);
			// double g = computePartialDerivative(coordinate);
			double g = computePartialDerivative(coordinate, weights, data, labels, lossDerivative);

			// System.out.println("partial derivative: " + g);

			// double g = partialDerivative.applyAsDouble(coordinate);
			double wAfterGradStep = weights.get(coordinate) - g * betaInverse;

			// move weight closer to zero to account for L1 regularization
			// set weight to prior if we cross prior while updating

			double aprioriWeight = aprioriWeights.get(coordinate);
			// double aprioriWeight =
			// this.model.getFeatureSpace().getFeatures().get(coordinate).getDefaultCoefficient();

			// TODO why is update sign not relevant for case distinction?
			if (wAfterGradStep > (betaInverse * gamma) + aprioriWeight) {
				weights.set(coordinate, wAfterGradStep - betaInverse * gamma);
			} else if (wAfterGradStep < (-betaInverse * gamma) + aprioriWeight) {
				weights.set(coordinate, wAfterGradStep + betaInverse * gamma);
			} else {
				weights.set(coordinate, aprioriWeight);
			}

			if (weights.get(coordinate).equals(oldValue)) {
				freeToChange[coordinate] = false;
				freeWeights--;
			} else {
				for (int i = 0; i < weights.size(); i++) {
					freeToChange[i] = true;
				}
				freeWeights = weights.size();
			}

//			System.out.println("updated weights: " + weights);

		}

		LOGGER.info("Done model computation (after " + String.valueOf(t - skipCounter)
				+ " iterations of coordinate descent)");
	}

}
