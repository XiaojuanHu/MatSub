/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.base;

import static de.unibonn.realkd.common.base.IntegerValues.intValue;
import static de.unibonn.realkd.common.testing.JsonSerializationTesting.testJsonSerialization;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.common.testing.EqualsTester;

import de.unibonn.realkd.common.base.IntegerValues.EvenIntegerValue;
import de.unibonn.realkd.common.base.IntegerValues.IntegerValue;
import de.unibonn.realkd.common.base.IntegerValues.OddIntegerValue;
import de.unibonn.realkd.common.base.IntegerValues.PositiveIntegerValue;

/**
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class IntegerValuesTest {

	private static final IntegerValue zero = intValue(0);
	private static final IntegerValue one = intValue(1);
	private static final IntegerValue negOne = intValue(-1);
	private static final IntegerValue two = intValue(2);
	private static final IntegerValue negTwo = intValue(-2);

	@Test
	public void testEquals() {
		new EqualsTester().addEqualityGroup(zero, intValue(0)).addEqualityGroup(intValue(-10), intValue(-10))
				.addEqualityGroup(intValue(+1), one).testEquals();
	}

	@Test
	public void testTypes() {
		assertTrue(zero instanceof EvenIntegerValue);
		assertTrue(one instanceof PositiveIntegerValue);
		assertTrue(one instanceof OddIntegerValue);
		assertTrue(negOne instanceof OddIntegerValue);
		assertTrue(two instanceof PositiveIntegerValue);
		assertTrue(two instanceof EvenIntegerValue);
		assertTrue(negTwo instanceof EvenIntegerValue);
	}

	@Test
	public void serializationTest() throws IOException {
		testJsonSerialization(zero, IntegerValue.class);
		testJsonSerialization(one, IntegerValue.class);
		testJsonSerialization(negOne, IntegerValue.class);
		testJsonSerialization(two, IntegerValue.class);
		testJsonSerialization(negTwo, IntegerValue.class);
	}

}
