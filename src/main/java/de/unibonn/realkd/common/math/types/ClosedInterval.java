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
package de.unibonn.realkd.common.math.types;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a closed interval of double values (i.e., containing the end
 * points).
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class ClosedInterval implements Domain<Double> {

	public static ClosedInterval closedInterval(double lower, double upper) {
		checkArgument(upper >= lower);
		return new ClosedInterval(lower, upper);
	}

	private final Double lower;

	private final Double upper;

	private ClosedInterval(Double lower, Double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public double lower() {
		return lower;
	}

	public double upper() {
		return upper;
	}

	public double width() {
		return upper - lower;
	}
	
	@Override
	public boolean contains(Double x) {
		return lower.compareTo(x) <= 0 && upper.compareTo(x) >= 0;
	}

	public String toString() {
		return String.format("ClosedInterval[%f, %f]",lower(), upper());
	}

}
