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
package de.unibonn.realkd.algorithms.derived;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.subSetParameter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.MockAlgorithm;
import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Simple test case for parameter facade algorithm produced by
 * {@link DerivedAlgorithms#getAlgorithmWithWrappedParameters)} wrapping core
 * algorithm with two parameters a and b that are terminated with two textual
 * terminators passed to factory method in order incompatible with their
 * dependency.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public class GetFacadeAlgorithmTestUnorderedWrappers {

	private Parameter<Set<Integer>> a;

	private Parameter<Set<Integer>> b;

	private ParameterTerminator aTerminator;

	private ParameterTerminator bTerminator;

	private StoppableMiningAlgorithm wrappedAlgorithm;

	private MiningAlgorithm wrapper;

	@Before
	public void setUp() {
		a = subSetParameter(id("a"),"a", "",
				() -> ImmutableSet.of(1, 2, 3, 4, 5), () -> ImmutableSet.of());
		b = subSetParameter(id("b"),"b", "",
				() -> a.current(), () -> ImmutableSet.of(), a);
		wrappedAlgorithm = new MockAlgorithm("Inner algorithm", "",
				AlgorithmCategory.OTHER, ImmutableList.of(a, b),
				new ArrayList<Pattern<?>>());

		aTerminator = new StringValueTerminator(a, () -> "[1,2,3]");
		bTerminator = new StringValueTerminator(b, () -> "[3]");

		wrapper = DerivedAlgorithms.getAlgorithmWithWrappedParameters(
				wrappedAlgorithm, ImmutableList.of(bTerminator, aTerminator),
				DerivedAlgorithms.HIDE_AND_WARN);
	}

	@Test
	public void testNotSetBeforeExecution() {
		assertEquals(a.current(), ImmutableSet.of());
		assertEquals(b.current(), ImmutableSet.of());
	}

	@Test
	public void testValidOnExecution() throws ValidationException {
		wrapper.call();
		assertEquals(a.current(), ImmutableSet.of(1, 2, 3));
		assertEquals(b.current(), ImmutableSet.of(3));
	}

	@Test
	public void testNoPublishedParameters() {
		assertTrue(wrapper.getAllParameters().isEmpty());
	}

}
