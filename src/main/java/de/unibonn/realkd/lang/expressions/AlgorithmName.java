/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.lang.expressions;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.MiningAlgorithmFactory;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * @author mboley
 *
 */
public class AlgorithmName implements Expression<MiningAlgorithm> {
	
	private final MiningAlgorithmFactory factory;
	
	public AlgorithmName(MiningAlgorithmFactory factory) {
		this.factory=factory;
	}

	@Override
	public MiningAlgorithm evaluate(Workspace workspace) throws InterpretationException {
		return factory.create(workspace);
	}

	@Override
	public Class<? extends MiningAlgorithm> resultType() {
		return MiningAlgorithm.class;
	}

}
