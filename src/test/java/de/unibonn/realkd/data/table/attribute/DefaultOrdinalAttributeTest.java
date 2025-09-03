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

package de.unibonn.realkd.data.table.attribute;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class DefaultOrdinalAttributeTest {

	private OrderedCategoricAttribute<String> attribute;

	private final String[] possibleValues = new String[] { "very low", "low", "medium", "high", "very high" };
	private final String[] dummyValues = new String[] { "high", "low", "very low", "medium", "medium", "very high",
			"low", "very high" };

	@Before
	public void setUp() {
		List<String> possibleValues = Arrays.asList(this.possibleValues);
		List<String> values = Arrays.asList(dummyValues);

		FiniteOrder comparator = new FiniteOrder(possibleValues);

		attribute = Attributes.orderedCategoricAttribute("OrdinalTestName", "This is an ordinal attribute", values, comparator, String.class);
	}

	@Test
	public void testOrderNumber() {
		Assert.assertEquals(5, attribute.orderNumber("medium"));
		Assert.assertEquals(1, attribute.orderNumber("very low"));
		Assert.assertEquals(8, attribute.orderNumber("very high"));
	}

	@Test
	public void testOrderNumberOnRows() {
		Assert.assertEquals(3, attribute.orderNumberOnRows("medium", ImmutableSet.of(0, 1, 2, 3)));
		Assert.assertEquals(1, attribute.orderNumberOnRows("very low", ImmutableSet.of(0, 1, 2, 3, 4, 5, 6, 7)));
		Assert.assertEquals(4, attribute.orderNumberOnRows("very high", ImmutableSet.of(4, 5, 6, 7)));
	}

	@Test
	public void testGetMedian() {
		Assert.assertEquals(attribute.median(), "medium");
	}

	@Test
	public void testGetMax() {
		Assert.assertEquals(attribute.max(), "very high");
	}

	@Test
	public void testGetMin() {
		Assert.assertEquals(attribute.min(), "very low");
	}

	@Test
	public void testGetLowerQuartile() {
		Assert.assertEquals(attribute.lowerQuartile(), "low");
	}

	@Test
	public void testGetUpperQuartile() {
		Assert.assertEquals(attribute.upperQuartile(), "high");
	}
}