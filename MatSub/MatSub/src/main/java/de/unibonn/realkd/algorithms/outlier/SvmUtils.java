/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.algorithms.outlier;

import de.unibonn.realkd.algorithms.outlier.libsvm.svm_node;

public class SvmUtils {

	/**
	 * Calculate the average distance between all pairs of points in data.
	 * @param data array of data points
	 * @return average distance between all pairs of data points
	 */
	public static final double calcAvgDist(svm_node[][] data) {
		double result = 0;
		int n = data.length;

		for (int i=0; i<n; ++i){
			for (int j=i+1; j<n; ++j){
				result += calcEuclideanDist(data[i], data[j]);				
			}
		}
		result = 2* result;
		return result / (data.length * (data.length-1));
		
	}

	public static final double[][] calcDistMatrix(svm_node[][] data) {
		double[][] result = new double[data.length][data.length];
		for (int i = 0; i < data.length; ++i) {
			for (int j = 0; j < data.length; ++j) {
				result[i][j] = calcEuclideanDist(data[i], data[j]);
			}

		}
		return result;
	}

	public static final double calcEuclideanDist(svm_node[] a, svm_node[] b) {
		double sum = 0;
		for (int i = 0; i < a.length; ++i) {
			sum += Math.pow(a[i].value - b[i].value, 2);
		}
		return Math.sqrt(sum);
	}
}
