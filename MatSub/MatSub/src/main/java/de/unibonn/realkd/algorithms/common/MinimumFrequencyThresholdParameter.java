/**
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
 *
 */
package de.unibonn.realkd.algorithms.common;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultParameter;

/**
 * @author Mario Boley
 * 
 * @since 0.2.0
 * 
 * @version 0.5.0
 * 
 */
public class MinimumFrequencyThresholdParameter extends DefaultParameter<Double> {

	private static final Identifier ID = identifier("min_freq");

	private static final String HINT = "Must be between 0 and 1.";

	private static final double DEFAULT = 0.1;

	private static final String DESCRIPTION = "The minimum frequency a pattern has to have in order to be part of the result.";

	private static final String NAME = "Frequency threshold";

	public MinimumFrequencyThresholdParameter() {
		super(ID, NAME, DESCRIPTION, Double.class, DEFAULT, input -> Double.valueOf(input),
				value -> (value <= 1.0 && value >= 0.0), HINT);
	}

}
