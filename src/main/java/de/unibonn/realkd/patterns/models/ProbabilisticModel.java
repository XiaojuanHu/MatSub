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

package de.unibonn.realkd.patterns.models;

import java.util.logging.Logger;

public interface ProbabilisticModel extends Model {

	public static final Logger LOGGER = Logger.getLogger(ProbabilisticModel.class.getName());

	public default double totalVariationDistance(ProbabilisticModel q) {
		LOGGER.warning(this.getClass().getSimpleName()
				+ " does not provide implementation for total variation distance; result NaN");
		return Double.NaN;
	}

	public default double hellingerDistance(ProbabilisticModel q) {
		LOGGER.warning(this.getClass().getSimpleName()
				+ " does not provide implementation for Hellinger distance; result NaN");
		return Double.NaN;
	}

	/**
	 * KL divergence from this to some other probabilistic model.
	 * 
	 * @param q
	 *            the other model
	 * @return D(this|q)
	 */
	public default double kullbackLeiblerDivergence(ProbabilisticModel q) {
		LOGGER.warning(
				this.getClass().getSimpleName() + " does not provide implementation for KL divergence; result NaN");
		return Double.NaN;
	}

}
