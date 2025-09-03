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

import static de.unibonn.realkd.common.base.Identifier.identifier;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.util.Predicates;

public final class FractionOfOutliersParameter extends DefaultParameter<Double> {

	private static final Identifier ID = identifier("max_outlier_frac");

	private static final String HINT = "Specify number between 0 and 1";

	private static final Class<Double> TYPE = Double.class;

	private static final double DEFAULT_VALUE = 0.01;

	private static final String DESCRIPTION = "The maximal fraction of outliers in a patterns";

	private static final String NAME = "Max fraction outliers";

	public FractionOfOutliersParameter(ParameterContainer algorithm) {
		super(ID, NAME, DESCRIPTION, TYPE, DEFAULT_VALUE, input -> Double
				.valueOf(input), Predicates.inOpenRange(
				0.0, 1.0), HINT);
	}

	// @Override
	// protected final boolean concretelyValid() {
	// return getCurrentValue() < 1 && getCurrentValue() > 0;
	// }

}
