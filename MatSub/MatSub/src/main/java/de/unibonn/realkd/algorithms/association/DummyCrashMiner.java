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

package de.unibonn.realkd.algorithms.association;

import java.util.Collection;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.NumberOfResultsParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.Pattern;

/**
 * @author bjacobs
 */
public class DummyCrashMiner extends AbstractMiningAlgorithm {

	NumberOfResultsParameter numberOfResultsParameter;

	public DummyCrashMiner(Workspace dataTub) {
		super();
		this.numberOfResultsParameter = new NumberOfResultsParameter();
		this.numberOfResultsParameter.set(10);

		registerParameter(numberOfResultsParameter);
	}

	@Override
	protected Collection<Pattern<?>> concreteCall() {
		throw new RuntimeException("Exception provoked");
	}

	@Override
	public String caption() {
		return "Always-crashing dummy algorithm";
	}

	@Override
	public String description() {
		return "Dummy algorithm that is only used in development or test of the system.";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.ASSOCIATION_MINING;
	}

	/*
	 * @Override public List<MiningParameter> getParameters() { return
	 * Arrays.asList( new MiningParameter[] { numberOfResultsParameter }); }
	 */
}
