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
package de.unibonn.realkd.algorithms;

import java.util.Collection;
import java.util.List;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Mock algorithm with configurable parameters and results for setting up test
 * cases of clients of algorithms.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public class MockAlgorithm extends AbstractMiningAlgorithm {

	private final String name;

	private final String description;

	private final AlgorithmCategory category;

	private final Collection<Pattern<?>> results;

	public MockAlgorithm(String name, String description,
			AlgorithmCategory category, List<Parameter<?>> parameters,
			Collection<Pattern<?>> results) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.results = results;
		parameters.forEach(this::registerParameter);
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public AlgorithmCategory getCategory() {
		return category;
	}

	@Override
	protected Collection<Pattern<?>> concreteCall() {
		return results;
	}

}
