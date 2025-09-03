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
package de.unibonn.realkd.common.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * @author Mario Boley
 *
 */
public abstract class AbstractBuilderConversionTest<T> {

	private Workspace context;

	private HasSerialForm<? extends T> object;

	public AbstractBuilderConversionTest(Workspace context,
			HasSerialForm<? extends T> object) {
		this.context = context;
		this.object = object;
	}

	@Test
	public void testToBuilderConsistency() {
		SerialForm<? extends T> builder = object.serialForm();
		Object clone = builder.build(context);
		assertEquals(object, clone);
	}

	@Test
	public void testBuilderConversionProducesEquivalentButDistinctObjects() {
		SerialForm<? extends T> builder1 = object.serialForm();
		SerialForm<? extends T> builder2 = object.serialForm();
		assertTrue(builder1 != builder2);
		assertEquals(builder1, builder2);
	}

}
