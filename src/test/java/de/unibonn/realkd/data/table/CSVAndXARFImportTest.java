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
package de.unibonn.realkd.data.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.DefaultCategoricAttribute;
import de.unibonn.realkd.data.table.attribute.DefaultMetricAttribute;
import de.unibonn.realkd.data.xarf.XarfImport;

/**
 * 
 * Tests the unified csv-xarf importing
 * 
 * @author Panagiotis Mandros
 * 
 * @version 0.7.0
 * 
 */
public class CSVAndXARFImportTest {

	public static final String CSV_FILE = "src/test/resources/data/binaries/octet_binaries_2.1.1.csv";
	public static final String CSV_FILE_DATA_TAG = "src/test/resources/data/binaries/octet_binaries_2.1.1_with_atdata_tag.csv";
	public static final String METADATA_FILE = "src/test/resources/data/binaries/octet_binaries_2.1.1_metadata.xarf";

	private final static XarfImport BUILDER_CSV = XarfImport.xarfImport().dataFilename(CSV_FILE);
	private final static DataTable DATA_TABLE_CSV = BUILDER_CSV.get();

	private final static XarfImport BUILDER_CSV_ATDATA = XarfImport.xarfImport().dataFilename(CSV_FILE_DATA_TAG);
	private final static DataTable DATA_TABLE_CSV_ATDATA = BUILDER_CSV_ATDATA.get();

	private final static XarfImport BUILDER_CSV_METADATA = XarfImport.xarfImport(CSV_FILE, METADATA_FILE);
	private final static DataTable DATA_TABLE_CSV_METADATA = BUILDER_CSV_METADATA.get();

	@Test
	public void testNoNullTables() {
		assertNotNull("Csv only table should not be null", DATA_TABLE_CSV);
		assertNotNull("Csv with @data tag table should not be null", DATA_TABLE_CSV_ATDATA);
		assertNotNull("Csv with metadata table should not be null", DATA_TABLE_CSV_METADATA);
	}

	@Test
	public void testNumberSamples() {
		assertEquals("Csv only table should have 82 data points", DATA_TABLE_CSV.population().size(), 82);
		assertEquals("Csv with @data tag should have 82 data points", DATA_TABLE_CSV_ATDATA.population().size(), 82);
		assertEquals("Csv with metadata table should have 82 data points", DATA_TABLE_CSV_METADATA.population().size(),
				82);
	}

	@Test
	public void testNumberAttributes() {
		assertEquals("Csv only table should have 57 attributes", DATA_TABLE_CSV.attributes().size(), 57);
		assertEquals("Csv with @data tag should have 57 attributes", DATA_TABLE_CSV_ATDATA.attributes().size(), 57);

		// name attributes are skipped
		assertEquals("Csv with metadata table should have 57 attributes", DATA_TABLE_CSV_METADATA.attributes().size(),
				56);
	}

	@Test
	public void checkFirstAttributeTypes() {
		assertEquals("Csv only table should have a categoric first attribute ",
				DATA_TABLE_CSV.attributes().get(0).getClass(), DefaultCategoricAttribute.class);
		assertEquals("Csv with @data tag table should have a categoric first attribute ",
				DATA_TABLE_CSV_ATDATA.attributes().get(0).getClass(), DefaultCategoricAttribute.class);

		// name attributes are skipped
		assertEquals("Csv with metadata table should have a numeric first attribute ",
				DATA_TABLE_CSV_METADATA.attributes().get(0).getClass(), DefaultMetricAttribute.class);

	}

	@Test
	public void checkSniffedAttributeID() {
		assertEquals("Last attribute should have id: " + Identifier.id("Attribute_" + 57),
				DATA_TABLE_CSV.attributes().get(56).identifier(), Identifier.id("Attribute_" + 57));
		assertEquals("Last attribute should have id: " + Identifier.id("Attribute_" + 57),
				DATA_TABLE_CSV_ATDATA.attributes().get(56).identifier(), Identifier.id("Attribute_" + 57));

	}

	@Test
	public void testGroupParsed() {
		assertEquals(1, DATA_TABLE_CSV_METADATA.attributeGroups().size());
	}

	@Test
	public void testNamesParsed() {
		assertEquals("BeO", DATA_TABLE_CSV_METADATA.population().objectName(4));
	}

	@Test
	public void testTableNameParsed() {
		assertEquals("Octet Binaries", DATA_TABLE_CSV_METADATA.caption());
	}

	@Test
	public void testAttributeIdentifiersAccess() {
		assertTrue(DATA_TABLE_CSV_METADATA.attribute(Identifier.identifier("lasso_1")).isPresent());
		assertFalse(DATA_TABLE_CSV_METADATA.attribute(Identifier.identifier("no_such_identifier")).isPresent());

	}
}
