/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.algorithms.emm;

import java.io.IOException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import de.unibonn.realkd.algorithms.emm.BalancedCoveragePositiveMeanShiftOptimisticEstimator.SelectionEstimation;

/**
 * @author Janis Kalofolias
 *
 */
public class UnbalancedCoveragePositiveMeanShiftOptimisticEstimatorTest {

	private static final String TEST_DATA_PATH_UNBALANCED = "/configurations/BCPMSOEstimatorTestUnbalanced.data";
	
	static class ConfigTestUnbalanced {
		public String name;
		// Input Data
		double[] targetIn;
		int[] controlIn;
		int[] isValidIn;
		int[] isMemberSel;
		int numCat;
		// Optimisation configuration
		double[] controlWeights;
		double[] controlClass0Probabilities;
		// Results, one entry per configuration above
		double[] fValOptPerWeight;
		int[][] optCntsPerWeight;
		
		@Override
		public String toString() {
			return String.format("Config[%s](%d elements)", name, targetIn.length);
		}
	}

	@Test
	public void testUnbalanced() throws NoSuchFieldException, IllegalAccessException, IOException {
		

		ConfigTestUnbalanced[] configs = SimpleConfigParser.readConfig(TEST_DATA_PATH_UNBALANCED, ConfigTestUnbalanced.class, ConfigTestUnbalanced[]::new,
				ConfigTestUnbalanced::new);
		final double eps = Math.ulp(1) * 8; // Comparison precision, when math
											// operations are expected.
		for (int ci = 0; ci < configs.length; ++ci) {
			ConfigTestUnbalanced c = configs[ci];
			System.err.format("** Testing unbalanced data against config: %s\n", c.name);

			PopulationData dataPop;
			dataPop = new PopulationData(DoubleStream.of(c.targetIn), IntStream.of(c.controlIn),
					IntStream.of(c.isValidIn));
			SelectionData dataSel = new SelectionData(dataPop, i -> c.isMemberSel[i] != 0);
			BalancedCoveragePositiveMeanShiftOptimisticEstimator oest =
					new BalancedCoveragePositiveMeanShiftOptimisticEstimator(dataPop);
			
			for(int iw=0;iw<c.controlWeights.length;++iw) {
				final double controlWeight = c.controlWeights[iw];
				final double controlClass0Probability = c.controlClass0Probabilities[iw];
				final double optValue = c.fValOptPerWeight[iw];
				final int[] optCnts = c.optCntsPerWeight[iw];

				oest.setExponentRepr(controlWeight);
				if(!Double.isNaN(controlClass0Probability)) {
					oest.setControlClass0Probability(controlClass0Probability);
				} else {
					oest.setControlClass0Probability();
				}

				System.err.format(" + Testing optima for weight: %5f, class 0 probability: %5f\n", controlWeight, controlClass0Probability);
				{
					SelectionEstimation selEst = oest.new SelectionEstimation(dataSel);
					Assert.assertEquals("Selection Estimation Optimal Value", optValue, selEst.optValue, eps);
					Assert.assertArrayEquals("Selection Estimation Optimal Index", optCnts, selEst.optCounts);
					Assert.assertEquals("Estimator Operator Value", optValue, oest.applyAsDouble(dataSel), eps);
				}
			}
			System.err.println(" Done");
		}
	}

}

