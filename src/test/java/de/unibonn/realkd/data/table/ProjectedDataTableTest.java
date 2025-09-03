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
package de.unibonn.realkd.data.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.testing.JsonSerializationTesting;
import de.unibonn.realkd.common.testing.TestConstants;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;

/**
 * @author Mario Boley
 * 
 * @since 0.4.0
 * 
 * @version 0.4.0
 *
 */
public class ProjectedDataTableTest {

	private static final Workspace WORKSPACE = TestConstants.getGermanyWorkspace();

	private static final DataTable ORIGINAL_DATATABLE = WORKSPACE.get(TestConstants.TABLE_ID, DataTable.class).get();

	private static final DataTable PROKECTED_TABLE = DataTables.projectedTable(Identifier.id("projected"), "", "",
			ORIGINAL_DATATABLE, ImmutableList.of("CDU 2005"));

	public ProjectedDataTableTest() {
		;
	}

	@Test
	public void testHasSerialForm() {
		assertTrue(PROKECTED_TABLE instanceof HasSerialForm);
	}

	@Test
	public void numberOfAttributes() {
		assertEquals(ORIGINAL_DATATABLE.numberOfAttributes() - 1, PROKECTED_TABLE.numberOfAttributes());
	}

	@Test
	public void numberOfAttributeGroups() {
		assertEquals(ORIGINAL_DATATABLE.attributeGroups().size() - 2, PROKECTED_TABLE.attributeGroups().size());
	}

	@Test
	public void testSerialFormSerialization() throws IOException {
		SerialForm<?> serialForm = ((HasSerialForm<?>) PROKECTED_TABLE).serialForm();
		JsonSerializationTesting.testJsonSerialization(serialForm, SerialForm.class);
	}

	@Test
	public void testSerialForm() {
		Workspace secondWorkspace = Workspaces.workspace();
		secondWorkspace.add(PROKECTED_TABLE);
		SerialForm<?> serialForm = ((HasSerialForm<?>) PROKECTED_TABLE).serialForm();
		DataTable reconstructed = (DataTable) serialForm.build(secondWorkspace);
		assertEquals(PROKECTED_TABLE.attributes(), reconstructed.attributes());
	}

}
