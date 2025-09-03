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
package de.unibonn.realkd.common.measures;

import static de.unibonn.realkd.common.measures.Measures.measurement;
import static de.unibonn.realkd.patterns.Frequency.FREQUENCY;
import static de.unibonn.realkd.patterns.Support.SUPPORT;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.testing.JsonSerializationTesting;

/**
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.6.0
 * 
 * @version 0.7.1
 *
 */
public class MeasurementTest {

	@Test
	public void serializationSimple() throws IOException {
		JsonSerializationTesting.testJsonSerialization(measurement(FREQUENCY, 0.9), Measurement.class);
	}

	@Test
	public void serializationWithAuxMeasurements() throws IOException {
		JsonSerializationTesting.testJsonSerialization(
				measurement(FREQUENCY, 0.9, ImmutableList.of(measurement(SUPPORT, 500))),
				Measurement.class);
	}

}
