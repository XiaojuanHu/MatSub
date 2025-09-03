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
package de.unibonn.realkd.data.xarf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.data.xarf.XarfParsing.AssignmentToken;
import de.unibonn.realkd.data.xarf.XarfParsing.SequenceToken;
import de.unibonn.realkd.data.xarf.XarfParsing.SetToken;
import de.unibonn.realkd.data.xarf.XarfParsing.StringToken;
import de.unibonn.realkd.data.xarf.XarfParsing.Token;

/**
 * 
 * @author Mario Boley
 * 
 * @author Panagiotis Mandros
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class XarfParsingTest {

	@Test
	public void testSetDomainSpecWithCaption() {
		String line = "@attribute {sunny,overcast,rainy} weather caption=\"The weather\"";
		Token[] tokens = XarfParsing.tokens(line);
		assertEquals(tokens.length, 6);
		assertTrue(tokens[1] instanceof SetToken);
		assertEquals(ImmutableSet.of("sunny", "overcast", "rainy"), ((SetToken) tokens[1]).value());
		assertTrue(tokens[4] instanceof AssignmentToken);
	}

	@Test
	public void testSequenceDomainSpecWithCaption() {
		String line = "@attribute [low,medium,high] intensity caption=\"The intensity\"";
		Token[] tokens = XarfParsing.tokens(line);
		assertEquals(tokens.length, 6);
		assertTrue(tokens[1] instanceof SequenceToken);
		assertEquals(ImmutableList.of("low", "medium", "high"), ((SequenceToken) tokens[1]).value());
		assertTrue(tokens[4] instanceof AssignmentToken);
	}

	@Test
	public void testDateWithFormatSpec() {
		String line = "@attribute date date 'YYYY MM DD' caption=\'The date\'";
		Token[] tokens = XarfParsing.tokens(line);
		assertEquals(tokens.length, 7);
		assertTrue(tokens[3] instanceof StringToken);
		assertTrue(((StringToken) tokens[3]).value().equals("YYYY MM DD"));
		assertTrue(tokens[5] instanceof AssignmentToken);
	}

}
