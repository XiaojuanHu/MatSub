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

import static de.unibonn.realkd.common.base.ArrayValues.array;
import static de.unibonn.realkd.common.base.IntegerValues.intValue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.testing.EqualsTester;

import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.base.ArrayValues.GenericArray;
import de.unibonn.realkd.common.base.IntegerValues.IntegerValue;
import de.unibonn.realkd.common.testing.JsonSerializationTesting;

/**
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class ArrayValuesTest {

	public static class TestClassWithSomeGenericField implements JsonSerializable {

		@JsonProperty("values")
		private GenericArray<? extends GenericArray<? extends IntegerValue>> values;

		@JsonCreator
		public TestClassWithSomeGenericField(@JsonProperty("values") GenericArray<GenericArray<IntegerValue>> values) {
			this.values = values;
		}

		@JsonCreator
		public boolean equals(Object other) {
			if (!(other instanceof TestClassWithSomeGenericField)) {
				return false;
			}
			return values.equals(((TestClassWithSomeGenericField) other).values);
		}

	}

	private static GenericArray<IntegerValue> array1 = array(intValue(3), intValue(10));

	private static GenericArray<IntegerValue> array2 = array(intValue(3), intValue(10));

	private static GenericArray<IntegerValue> array3 = array(intValue(-3), intValue(10));

	@Test
	public void equalityTest() {
		new EqualsTester().addEqualityGroup(array1, array2).addEqualityGroup(array3).testEquals();
	}

	@Test
	public void testSerialization() throws IOException {
		GenericArray<GenericArray<IntegerValue>> nested = array(array1, array1);

		TestClassWithSomeGenericField testObject = new TestClassWithSomeGenericField(nested);

		JsonSerializationTesting.testJsonSerialization(testObject, TestClassWithSomeGenericField.class);

		// String serialString = JsonSerialization.serialString(testObject);
		// System.out.println(serialString);
		// TestClassWithSomeGenericField deserialization = JsonSerialization
		// .deserialization(new StringReader(serialString),
		// TestClassWithSomeGenericField.class);
		// System.out.println(deserialization);
	}

}
