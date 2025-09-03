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
package de.unibonn.realkd.data.constraints;

import static de.unibonn.realkd.data.constraints.Constraints.inClosedInterval;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the constraints produced by {@link Constraints#equalTo(Object)} for
 * Double objects.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class EqualToBoxedDoubleTest {

	private static Double value = 2.3;
	private static Double equalValue = 2.3;
	private static Double largerValue = 3.0;
	private static Double smallerValue = -2.0;
	private static Constraint<Double> equalToTwoPointThree = Constraints.equalTo(value);

	@Test
	public void holdsForIdenticalValue() {
		assertTrue(equalToTwoPointThree.holds(value));
	}

	@Test
	public void holdsForEqualValue() {
		assertTrue(equalToTwoPointThree.holds(equalValue));
	}

	@Test
	public void holdsNotForUnequalValue() {
		assertFalse(equalToTwoPointThree.holds(value + 0.0000001));
	}

	@Test
	public void impliesLessOrEqualsWithLargerThreshold() {
		Constraint<Double> lessOrEqualsThree = Constraints.lessOrEquals(largerValue);
		assertTrue(equalToTwoPointThree.implies(lessOrEqualsThree));
	}

	@Test
	public void impliesLessOrEqualsWithSameThreshold() {
		Constraint<Double> lessOrEqualsThree = Constraints.lessOrEquals(equalValue);
		assertTrue(equalToTwoPointThree.implies(lessOrEqualsThree));
	}

	@Test
	public void impliesNotLessOrEqualsWithSmallerThreshold() {
		Constraint<Double> lessOrEqualsThree = Constraints.lessOrEquals(smallerValue);
		assertFalse(equalToTwoPointThree.implies(lessOrEqualsThree));
	}

	@Test
	public void impliesInterval() {
		Constraint<Double> inInterval = inClosedInterval(smallerValue, largerValue);
		assertTrue(equalToTwoPointThree.implies(inInterval));
	}

	@Test
	public void impliesNotInterval() {
		Constraint<Double> inInterval = inClosedInterval(largerValue, largerValue + 1.0);
		assertFalse(equalToTwoPointThree.implies(inInterval));
	}

}
