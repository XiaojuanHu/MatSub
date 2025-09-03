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

import org.junit.Test;

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
public class HeaderOptionsTest {

	public static final String XARF_HEADER_YES = "src/test/resources/data/dummies/dummyHeaderYes.xarf";
	public static final String XARF_HEADER_AUTO_WITH_HEADER = "src/test/resources/data/dummies/dummyHeaderAutoWithHeader.xarf";
	public static final String XARF_HEADER_AUTO_WITHOUT_HEADER = "src/test/resources/data/dummies/dummyHeaderAutoWithoutHeader.xarf";
	public static final String XARF_HEADER_NO_WITH_HEADER = "src/test/resources/data/dummies/dummyHeaderNoWithHeader.xarf";

	
	private final static XarfImport BUILDER_DUMMY_HEADER = XarfImport.xarfImport().dataFilename(XARF_HEADER_YES);
	private final static DataTable DUMMY_TABLE_HEADER_YES = BUILDER_DUMMY_HEADER.get();

	private final static XarfImport BUILDER_DUMMY_HEADER_AUTO_WITH_HEADER = XarfImport.xarfImport()
			.dataFilename(XARF_HEADER_AUTO_WITH_HEADER);
	private final static DataTable DUMMY_TABLE_HEADER_AUTO_WITH_HEADER = BUILDER_DUMMY_HEADER_AUTO_WITH_HEADER.get();

	private final static XarfImport BUILDER_DUMMY_HEADER_AUTO_WITHOUT_HEADER = XarfImport.xarfImport()
			.dataFilename(XARF_HEADER_AUTO_WITHOUT_HEADER);
	private final static DataTable DUMMY_TABLE_HEADER_AUTO_WITHOUT_HEADER = BUILDER_DUMMY_HEADER_AUTO_WITHOUT_HEADER
			.get();
	
	private final static XarfImport BUILDER_DUMMY_HEADER_NO_WITH_HEADER = XarfImport.xarfImport()
			.dataFilename(XARF_HEADER_NO_WITH_HEADER);
	private final static DataTable DUMMY_TABLE_HEADER_NO_WITH_HEADER = BUILDER_DUMMY_HEADER_NO_WITH_HEADER.get();
	

	@Test
	public void testCorrectParsingOfHeaderBasedOnOptions() {
		assertEquals(3, DUMMY_TABLE_HEADER_YES.population().size());
		assertEquals(3, DUMMY_TABLE_HEADER_AUTO_WITH_HEADER.population().size());
		assertEquals(3, DUMMY_TABLE_HEADER_AUTO_WITHOUT_HEADER.population().size());
		assertEquals(4, DUMMY_TABLE_HEADER_NO_WITH_HEADER.population().size());

	}

}
