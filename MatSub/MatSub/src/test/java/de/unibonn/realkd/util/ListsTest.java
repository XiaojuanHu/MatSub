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
package de.unibonn.realkd.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class ListsTest {
	
	@Test
	public void testValueSequence() {
		assertEquals(ImmutableList.of(3.0, 1.0), Lists.valueSequence(ImmutableList.of(3.0, 1.0, 1.0)));
		assertEquals(ImmutableList.of(0.3, 0.7, 9.0), Lists.valueSequence(ImmutableList.of(0.3, 0.7, 9.0)));
		assertEquals(ImmutableList.of(-3.0, 0.3, 0.1), Lists.valueSequence(ImmutableList.of(-3.0, 0.3, 0.1, 0.1)));
		assertEquals(ImmutableList.of(0.0, 0.2, 0.1, 0.2), Lists.valueSequence(ImmutableList.of(0.0, 0.2, 0.1, 0.2)));
		assertEquals(ImmutableList.of(1.0, 2.0, 3.0), Lists.valueSequence(ImmutableList.of(1.0, 2.0, 3.0, 3.0)));
		assertEquals(ImmutableList.of(3.0), Lists.valueSequence(ImmutableList.of(3.0, 3.0, 3.0, 3.0)));
		assertEquals(ImmutableList.of(1.0, 2.0, 3.0, 4.0),
				Lists.valueSequence(ImmutableList.of(1.0, 2.0, 3.0, 3.0, 4.0)));
	}
	
	@Test
	public void testKmeansCutPoints() {
		List<Double> values = IntStream.rangeClosed(1, 100).mapToDouble(i -> i).boxed().collect(Collectors.toList());
		MetricAttribute a = Attributes.metricDoubleAttribute("Test", "", values);
		List<Double> kMeansCutPoints = Lists.kMeansCutPoints(a.nonMissingValuesInOrder(), 5, 20);
		assertEquals(4, kMeansCutPoints.size());
		double[] expected = { 20.0, 40.0, 60.0, 80.0 };
		assertArrayEquals(expected, kMeansCutPoints.stream().mapToDouble(x -> x).toArray(), 1.0);
	}

}
