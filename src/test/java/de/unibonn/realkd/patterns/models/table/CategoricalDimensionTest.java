/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.models.table;

import static de.unibonn.realkd.patterns.models.table.ContingencyTables.categoricalDimension;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.assertEquals;

/**
 * @author Mario Boley
 * 
 * @version 0.7.2
 *
 */
public class CategoricalDimensionTest {
	
	private static final ImmutableList<String> CATEGORIES = ImmutableList.of("a", "b", "c", "d", "e");
	
	private static final CategoricalDimension DIM = categoricalDimension(CATEGORIES);
		
	@Test
	public void initTest() {
		assertEquals(CATEGORIES, DIM.binCaptions());
	}
	
	@Test
	public void mergeTest1() {
		Dimension merged = DIM.merge(1, 2);
		assertEquals(ImmutableList.of("a", "b+c", "d", "e"), merged.binCaptions());
		assertEquals(0, merged.bin("a"));
		assertEquals(1, merged.bin("b"));
		assertEquals(1, merged.bin("c"));
		assertEquals(2, merged.bin("d"));
		assertEquals(3, merged.bin("e"));
	}
	
	@Test
	public void mergeTest2() {
		Dimension merged = DIM.merge(1, 3);
		assertEquals(ImmutableList.of("a", "b+c+d", "e"), merged.binCaptions());
		assertEquals(0, merged.bin("a"));
		assertEquals(1, merged.bin("b"));
		assertEquals(1, merged.bin("c"));
		assertEquals(1, merged.bin("d"));
		assertEquals(2, merged.bin("e"));
	}
	
	@Test
	public void mergeTest3() {
		Dimension merged = DIM.merge(0, 5);
		assertEquals(ImmutableList.of("a+b+c+d+e"), merged.binCaptions());
		assertEquals(0, merged.bin("a"));
		assertEquals(0, merged.bin("b"));
		assertEquals(0, merged.bin("c"));
		assertEquals(0, merged.bin("d"));
		assertEquals(0, merged.bin("e"));
	}
	
	@Test
	public void binningTest() {
		assertEquals(0, DIM.bin("a"));
		assertEquals(1, DIM.bin("b"));
		assertEquals(2, DIM.bin("c"));
		assertEquals(3, DIM.bin("d"));
		assertEquals(4, DIM.bin("e"));	
	}

}
