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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.data.constraints.Constraints.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import de.unibonn.realkd.common.testing.JsonSerializationTesting;
import de.unibonn.realkd.common.testing.TestConstants;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class DefaultAttributeBasedPropositionTest {

	public static final PropositionalContext CONTEXT = TestConstants.getGermanyWorkspace().propositionalContexts()
			.get(0);

	@Test
	public void testEquals() {
		DataTable table = TestConstants.getGermanyDataTable();
		Attribute<?> attribute = table.attribute(identifier("Type")).get();
		AttributeBasedProposition<Object> p = Propositions.proposition(table, attribute, equalTo("Rural"));
		AttributeBasedProposition<Object> q = Propositions.proposition(table, attribute, equalTo("Rural"));
		assertEquals(p, q);
	}

	@Test
	public void testSerialFormSerialization() throws IOException {
		for (Proposition p : CONTEXT.propositions()) {
			SerialForm<?> serialForm = ((HasSerialForm<?>) p).serialForm();
			JsonSerializationTesting.testJsonSerialization(serialForm, SerialForm.class);
		}
	}

}
