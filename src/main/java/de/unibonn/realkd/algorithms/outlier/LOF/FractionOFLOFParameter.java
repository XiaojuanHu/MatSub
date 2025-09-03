/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.algorithms.outlier.LOF;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;

/**
 * <p>
 * paramaeter K that is passed to the Local Outlier factor algorithm
 * </p>
 * 
 * @author amr Koura
 *
 */
public class FractionOFLOFParameter extends DefaultParameter<Integer> {

	private static final String HINT = "Specify number between 0 and 1";

	private static final Class<Integer> TYPE = Integer.class;

	private static final int DEFAULT_VALUE = 10;

	private static final String DESCRIPTION = "The value of K that is used in the Algorithm";

	private static final String NAME = "KValue";

	public FractionOFLOFParameter(ParameterContainer algorithm) {
		super(identifier("k_value"), NAME, DESCRIPTION, TYPE, DEFAULT_VALUE, input -> Integer.valueOf(input), value -> value >= 1, HINT);
	}

}
