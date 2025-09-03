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
package de.unibonn.realkd.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * @author Mario Boley
 * 
 * @since 0.4.1
 * 
 * @version 0.4.1
 *
 */
public class IndexSetTest {

	@Test
	public void basicVarArgsCreation() {
		IndexSet s = IndexSets.of(23, 50, 1000);
		assertTrue(s.contains(23));
		assertTrue(s.contains(50));
		assertTrue(s.contains(1000));
		assertFalse(s.contains(50000));
		assertEquals(3, s.size());
	}

	@Test
	public void hashCodeTest() {
		IndexSet s1 = IndexSets.of(23, 50, 1000, 4213, 5535);
		IndexSet s2 = IndexSets.of(23, 50, 1000, 4213, 5535);
		int s1hash = s1.hashCode();
		assertEquals(s1hash, s2.hashCode());
		assertEquals(s1hash, s1.hashCode());
		assertNotEquals(0, s1hash);
	}

	@Test
	public void equals() {
		IndexSet s1 = IndexSets.of(12, 24, 564, 3535);
		IndexSet s2 = IndexSets.of(12, 24, 564, 3535);
		IndexSet s3 = IndexSets.of(23, 50, 1000, 4213, 5535);
		assertEquals(s1, s2);
		assertNotEquals(s1, s3);
	}

	@Test
	public void difference() {
		IndexSet s1 = IndexSets.of(3, 10, 20, 34);
		IndexSet s2 = IndexSets.of(3, 20, 100);
		IndexSet difference = IndexSets.difference(s1, s2);
		assertEquals(difference, IndexSets.of(10, 34));
	}

	@Test
	public void containsAll() {
		IndexSet s1 = IndexSets.of(0, 1, 32, 124, 564);

		// non IndexSet operators
		assertTrue(s1.containsAll(ImmutableList.of()));
		assertTrue(s1.containsAll(ImmutableList.of(0, 124)));
		assertFalse(s1.containsAll(ImmutableList.of(5, 124)));

		// IndexSet operators
		assertTrue(s1.containsAll(IndexSets.of()));
		assertTrue(s1.containsAll(IndexSets.of(0, 124)));
		assertFalse(s1.containsAll(IndexSets.of(5, 124)));
		assertFalse(s1.containsAll(IndexSets.of(5, 26, 64, 123, 124)));
	}

	@Test
	public void empty() {
		IndexSet empty = IndexSets.empty();
		assertEquals(0, empty.size());
		assertEquals(IndexSets.of(), empty);
	}

}
