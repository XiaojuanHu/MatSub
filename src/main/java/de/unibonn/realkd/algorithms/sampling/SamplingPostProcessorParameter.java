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

package de.unibonn.realkd.algorithms.sampling;

import static de.unibonn.realkd.common.base.Identifier.identifier;

import java.util.Arrays;
import java.util.List;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;

public class SamplingPostProcessorParameter extends DefaultRangeEnumerableParameter<SinglePatternPostProcessor> {

	private static final String DESCRIPTION = "Algorithm applied to random seed after sampling";
	private static final String NAME = "Post processing strategy";
	private static final Identifier ID = identifier("post_proc");

	public SamplingPostProcessorParameter() {
		super(ID, NAME, DESCRIPTION, SinglePatternPostProcessor.class, new RangeComputer<SinglePatternPostProcessor>() {
			@Override
			public List<SinglePatternPostProcessor> get() {
				return Arrays.asList(SinglePatternPostProcessor.values());
			}
		});
	}

}
