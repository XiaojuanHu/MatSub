/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.table;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * @author Mario Boley
 * 
 * @since 0.5.3.
 * 
 * @version 0.5.3
 *
 */
public class XarfOrdinalAttributeTest {

	public static final String FILENAME = "src/main/resources/data/breast-cancer/breast-cancer_ord.arff";

	public static final DataTable TABLE = XarfImport.xarfImport(FILENAME).get();

	public static final Identifier ATTRIBUTE_ID = Identifier.identifier("age");

	public static final List<String> VALUES = ImmutableList.of("10-19", "20-29", "30-39", "40-49", "50-59", "60-69",
			"70-79", "80-89", "90-99");

	@Test
	public void ordinalAttributeRead() {
		assertTrue(String.format("Attribute %s not read", ATTRIBUTE_ID), TABLE.attribute(ATTRIBUTE_ID).isPresent());
	}

	@Test
	public void ordinalAttributeReadAsOrdinal() {
		assertTrue(TABLE.attribute(ATTRIBUTE_ID).get() instanceof OrdinalAttribute<?>);
	}
	
	@Test
	public void testOrder() {
		OrdinalAttribute<?> attribute=(OrdinalAttribute<?>) TABLE.attribute(ATTRIBUTE_ID).get(); 
		Comparator<String> comparator = (Comparator<String>) attribute.valueComparator();
		for (int i=1; i<VALUES.size(); i++) {
			for (int j=0; j<i; j++) {
				assertTrue(comparator.compare(VALUES.get(j), VALUES.get(i))<0);
			}
		}
		
		assertTrue(attribute.compare(0, 1)<0);
		assertTrue(attribute.compare(1, 2)==0);		
	}

}
