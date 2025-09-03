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
package de.unibonn.realkd.patterns;

import static de.unibonn.realkd.common.base.Identifier.id;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.testing.JsonSerializationTesting;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;

/**
 * @author Sandy Moens
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class NamedPatternCollectionTest {
	
	private NamedPatternCollection namedPatternCollection = new NamedPatternCollection(id("TestId"), "TestName", "TestDescription", ImmutableList.of());

	public NamedPatternCollectionTest() {
		;
	}

	@Test
	public void testHasSerialForm() {
		assertTrue(namedPatternCollection instanceof HasSerialForm);
	}

	@Test
	public void testSerialFormSerialization() throws IOException {
		SerialForm<?> serialForm = ((HasSerialForm<?>) namedPatternCollection).serialForm();
		JsonSerializationTesting.testJsonSerialization(serialForm, SerialForm.class);
	}

}
