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
package de.unibonn.realkd.patterns.logical;

import static de.unibonn.realkd.common.testing.JsonSerializationTesting.testJsonSerialization;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import de.unibonn.realkd.common.testing.AbstractBufferedInputTest;
import de.unibonn.realkd.common.workspace.SerialForm;

/**
 * @author Mario Boley
 *
 * @since 0.1.2
 * 
 * @version 0.1.2.1
 * 
 */
public class LogicalDescriptorTest extends AbstractBufferedInputTest<LogicalDescriptorTestInput> {

	@Parameters(name = "{0}")
	public static Iterable<Object[]> getValues() {
		return Arrays.asList(new Object[][] { { new RandomLogicalDescriptorInputSpec(10, 20, 0) },
				{ new RandomLogicalDescriptorInputSpec(10, 20, 5) },
				{ new RandomLogicalDescriptorInputSpec(10, 20, 10) } });
	}

	public LogicalDescriptorTest(RandomLogicalDescriptorInputSpec inputSpec) {
		super(inputSpec);
	}

	@Test
	public void testToBuilderConsistency() {
		SerialForm<LogicalDescriptor> builder = getCurrentInput().descriptor.serialForm();
		LogicalDescriptor builderProduct = builder.build(getCurrentInput().dataWorkspace);
		assertEquals(getCurrentInput().descriptor, builderProduct);
	}

	@Test
	public void testBuilderJsonSerialization() throws IOException {
		SerialForm<LogicalDescriptor> source = getCurrentInput().descriptor.serialForm();
		testJsonSerialization(source, SerialForm.class);
	}

	@Test
	public void testDescriptorIsEqualToEquivalent() {
		SerialForm<LogicalDescriptor> builder = getCurrentInput().descriptor.serialForm();
		LogicalDescriptor clone = builder.build(getCurrentInput().dataWorkspace);
		assertEquals(getCurrentInput().descriptor, clone);
	}

	@Test
	public void testBuilderIsEqualToEquivalent() {
		SerialForm<LogicalDescriptor> builder = getCurrentInput().descriptor.serialForm();
		SerialForm<LogicalDescriptor> clone = builder.build(getCurrentInput().dataWorkspace).serialForm();
		assertEquals(builder, clone);
	}

	@Test
	public void testApproximateShortestGenerator() {
		LogicalDescriptor descriptor = getCurrentInput().descriptor;
		LogicalDescriptor generator = LogicalDescriptors.approximateShortestGenerator(descriptor);
		assertEquals(descriptor.supportSet(), generator.supportSet());
		if (descriptor.minimal()) {
			assertEquals(descriptor.size(), generator.size());
		} else {
			assertTrue(descriptor.size() > generator.size());
		}
	}

}
