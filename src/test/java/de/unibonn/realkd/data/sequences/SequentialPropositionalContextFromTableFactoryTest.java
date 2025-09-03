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
package de.unibonn.realkd.data.sequences;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Sandy Moens
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 */
public class SequentialPropositionalContextFromTableFactoryTest {
	
	@Test
	public void testLoadOrdinal() {
		SequentialPropositionalContextFromTableFactory f = new SequentialPropositionalContextFromTableFactory();
		f.groupingAttributeName("id");
		f.distanceAttributeName("timestamp");
		
		SequentialPropositionalContext context = f.build(TestConstants.DATATABLE_ORDINAL);
		
		assertEquals("Sequences for statements about ordinal_sequence", context.caption());
		assertEquals("Sequential database for data table ordinal_sequence", context.description());
		
		assertEquals(7, context.sequences().size());
		
		assertEquals(8, context.sequences().get(0).events().size());
		assertEquals(4, context.sequences().get(1).events().size());
		assertEquals(6, context.sequences().get(2).events().size());
		assertEquals(4, context.sequences().get(3).events().size());
		assertEquals(2, context.sequences().get(4).events().size());
		assertEquals(8, context.sequences().get(5).events().size());
		assertEquals(4, context.sequences().get(6).events().size());
		
		assertEquals(10, context.propositions().size());
		
		assertEquals(5, context.proposition(0).supportCount());
		assertEquals(5, context.proposition(1).supportCount());
		assertEquals(2, context.proposition(2).supportCount());
		assertEquals(2, context.proposition(3).supportCount());
		assertEquals(2, context.proposition(4).supportCount());
		assertEquals(3, context.proposition(5).supportCount());
		assertEquals(3, context.proposition(6).supportCount());
		assertEquals(2, context.proposition(7).supportCount());
		assertEquals(2, context.proposition(8).supportCount());
		assertEquals(2, context.proposition(9).supportCount());
	}
	
	@Test
	public void testLoadDate() {
		SequentialPropositionalContextFromTableFactory f = new SequentialPropositionalContextFromTableFactory();
		f.groupingAttributeName("id");
		f.distanceAttributeName("timestamp");
		
		SequentialPropositionalContext context = f.build(TestConstants.DATATABLE_DATE);
		
		assertEquals("Sequences for statements about date_based_sequence", context.caption());
		assertEquals("Sequential database for data table date_based_sequence", context.description());
		
		assertEquals(7,context.sequences().size());
		assertEquals(8, context.sequences().get(0).events().size());
		assertEquals(4, context.sequences().get(1).events().size());
		assertEquals(6, context.sequences().get(2).events().size());
		assertEquals(4, context.sequences().get(3).events().size());
		assertEquals(2, context.sequences().get(4).events().size());
		assertEquals(8, context.sequences().get(5).events().size());
		assertEquals(4, context.sequences().get(6).events().size());
		
		assertEquals(10, context.propositions().size());
		
		assertEquals(5, context.proposition(0).supportCount());
		assertEquals(5, context.proposition(1).supportCount());
		assertEquals(2, context.proposition(2).supportCount());
		assertEquals(2, context.proposition(3).supportCount());
		assertEquals(2, context.proposition(4).supportCount());
		assertEquals(3, context.proposition(5).supportCount());
		assertEquals(3, context.proposition(6).supportCount());
		assertEquals(2, context.proposition(7).supportCount());
		assertEquals(2, context.proposition(8).supportCount());
		assertEquals(2, context.proposition(9).supportCount());

	}
	
}
