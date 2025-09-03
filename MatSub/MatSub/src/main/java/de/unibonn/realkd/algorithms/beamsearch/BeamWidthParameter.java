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

package de.unibonn.realkd.algorithms.beamsearch;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.data.propositions.PropositionalContext;

/**
 * Beam-width parameter for beam search algorithms, which is an integer
 * parameter that depends on a dataset parameter. Value must be between 1 and
 * the number of propositions available for the selected dataset.
 * 
 */
public class BeamWidthParameter extends DefaultParameter<Integer> {
	private static final String HINT = "Set to integer value between 0 and number of propositions in current datatable.";
	private static final Class<Integer> TYPE = Integer.TYPE;
	private static final String DESCRIPTION = "Number of search nodes that are expanded per level";
	private static final String NAME = "Beam width";
	private static final Integer DEFAULT_VALUE = 5;

	public BeamWidthParameter(ParameterContainer algorithm,
			Parameter<PropositionalContext> propositionalLogicParameter) {
		super(identifier("beam_width"),NAME, DESCRIPTION, TYPE, DEFAULT_VALUE, (input -> Integer
				.valueOf(input)), (bw -> bw > 0), HINT,
				propositionalLogicParameter);
	}

}
