/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.parameter;

import static de.unibonn.realkd.common.base.Identifier.id;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test case for parameters created by
 * {@link DefaultSubCollectionParameter#getDefaultSubListParameter}. Contains
 * one parameter with a base-collection of strings with different amounts of
 * leading and trailing spaces trailing (which are supposed to be ignored for
 * parsing values from string).
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public class DefaultSubCollectionParameterTest {

	private SubCollectionParameter<String, List<String>> testParameter;

	@Before
	public void setUp() {
		testParameter = Parameters.subListParameter(id("test_param"),"Test parameter", "",
				() -> ImmutableList.of("stripped element 1", "stripped element 2", " element with leading space",
						"element with trailing space "));
	}

	@Test
	public void testSetByStringSimple() {
		testParameter.setByString("[stripped element 1]");
		assertEquals(ImmutableList.of("stripped element 1"), testParameter.current());
	}

	@Test
	public void testSetByStringWithTrailingAndLeadingSpace() {
		testParameter.setByString("[     stripped element 1        ]");
		assertEquals(ImmutableList.of("stripped element 1"), testParameter.current());
	}

	@Test
	public void testSetByStringElementOmittingLeadingSpace() {
		testParameter.setByString("[element with leading space]");
		assertEquals(ImmutableList.of(" element with leading space"), testParameter.current());
	}

	@Test
	public void testSetByStringElementOmittingTrailingSpace() {
		testParameter.setByString("[element with trailing space]");
		assertEquals(ImmutableList.of("element with trailing space "), testParameter.current());
	}

	@Test
	public void testSetByStringTwoElementsWithSpaceAroundComma() {
		testParameter.setByString("[stripped element 1          , stripped element 2]");
		assertEquals(ImmutableList.of("stripped element 1", "stripped element 2"), testParameter.current());
	}

	@Test
	public void testSetByStringTwoElementsWoSpaceAroundComma() {
		testParameter.setByString("[stripped element 1,stripped element 2]");
		assertEquals(ImmutableList.of("stripped element 1", "stripped element 2"), testParameter.current());
	}

}
