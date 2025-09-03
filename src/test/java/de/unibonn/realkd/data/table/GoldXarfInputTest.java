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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * Tests correct parsing of gold dataset from xarf file.
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class GoldXarfInputTest {

	public static final String filename = "src/main/resources/data/gold/gold_stratsample12200_2.0.1.xarf";

	private final static XarfImport builder = XarfImport.xarfImport().dataFilename(filename);
	private final static DataTable dataTable = builder.get();

	@Test
	public void builtNonNullTable() {
		assertNotNull(dataTable);
	}

	@Test
	public void testNumberOfAttributes() {
		// 23 non name attributes plus 3 derived from ordered distribution
		assertEquals(26, dataTable.attributes().size());
	}

	@Test
	public void testGroupParsed() {
		assertEquals(4, dataTable.attributeGroups().size());
	}

	@Test
	public void testNamesParsed() {
		assertEquals("Au14", dataTable.population().objectName(4));
	}

	@Test
	public void testTableNameParsed() {
		assertEquals("Au5-14 Clusters", dataTable.caption());
	}

	@Test
	public void testNumberOfAtomsBothOrdinalAndCategoric() {
		Attribute<?> attribute = dataTable.attribute(Identifier.identifier("number_atoms")).get();
		assertTrue(attribute instanceof CategoricAttribute);
		assertTrue(attribute instanceof OrdinalAttribute);
	}

	@Test
	public void testAttributeIdentifiersAccess() {
		assertTrue(dataTable.attribute(Identifier.identifier("delta_hardness")).isPresent());
		assertFalse(dataTable.attribute(Identifier.identifier("no_such_identifier")).isPresent());
	}

}
