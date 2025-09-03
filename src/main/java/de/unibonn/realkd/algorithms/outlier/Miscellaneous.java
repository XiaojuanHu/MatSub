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

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Miscellaneous {

	public Properties readProperties(String file) {
		try {
			InputStream is = getClass().getResourceAsStream(file);
			Properties prop = new Properties();
			prop.load(is);
			return prop;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to read from " + file + " file.");
		}
		return null;
	}

	public static double sum(double... values) {
		double result = 0;
		for (double value : values)
			result += value;
		return result;
	}

	public static double l2Distance(List<Double> vectorA, List<Double> vectorB) {
		double prod = 0.;
		for (int i = 0; i < vectorA.size(); i++) {
			prod += Math.pow(vectorA.get(i) - vectorB.get(i), 2.);
		}
		return Math.sqrt(prod);
	}

	// public static double cosineDistance(List<Double> vectorA, List<Double>
	// vectorB) {
	// return 1.0 - Miscellaneous.cosineSimilarity(vectorA, vectorB);
	// }

	public static double logit(double x) {
		return 1.0 / (1.0 + Math.exp(-1.0 * x));
	}

	public static double tanh(double x) {
		return (1.0 - Math.exp(-2.0 * x)) / (1.0 + Math.exp(-2.0 * x));
	}

	public static void waitForAwhile(long miliSeconds) {
		try {
			Thread.sleep(miliSeconds);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

}
