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
package de.unibonn.realkd.computations.core;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.measures.Measures.measurement;

import de.unibonn.realkd.algorithms.ComputationMeasure;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measurement;

/**
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.6.2
 *
 */
public enum ComputationTime implements ComputationMeasure {

	COMPUTATION_TIME;

	private final String caption;

	private final String description;

	private final Identifier id;

	private ComputationTime() {
		this.id = id("computation_time");
		this.caption = "Computation time";
		this.description = "The total time of the computation.";
	}

	@Override
	public String caption() {
		return caption;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	@Override
	public Measurement perform(Object obj) {
		if (!(obj instanceof Computation)) {
			return measurement(this, Double.NaN);
		}
		Computation<?> c = (Computation<?>) obj;
		double res = c.startTime().map(s -> (double) (c.terminationTime().orElse(System.currentTimeMillis()) - s))
				.orElse(Double.NaN);
		return measurement(this, res);
	}

}
