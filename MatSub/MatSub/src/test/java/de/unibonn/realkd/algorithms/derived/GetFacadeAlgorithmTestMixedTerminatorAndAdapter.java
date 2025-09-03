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
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.MockAlgorithm;
import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Complex test case for parameter facade algorithm produced by
 * {@link DerivedAlgorithms#getAlgorithmWithWrappedParameters}. This test case
 * consists of a core algorithm with 5 parameters that is wrapped using 3
 * parameter wrappers, two of which are terminators and one is an adapter. The
 * parameters of the core algorithm are:
 * </p>
 * <p>
 * <li><b>a</b>, subset parameter with range set [1,2,3,4,5],</li>
 * <li><b>b</b>, subset parameter with range set [1,2,3,4,5],</li>
 * <li><b>c</b>, subset parameter with range set equal to the intersection of a
 * and b,</li>
 * <li><b>d</b>, integer parameter with valid range between 0 and the number of
 * elements selected for c,</li>
 * <li><b>e</b>, integer parameter with valid range between 0 and d, and</li>
 * <li><b>f</b>, range enumerable integer parameter with range the integers from
 * 0 through e.</li>
 * </p>
 * <p>
 * Then the wrapper algorithm is defined by a parameter adapter that exposes a
 * deep copy of <b>d</b> in place of <b>d</b> and 3 terminators:
 * </p>
 * <p>
 * <li><b>a</b> to [1,2,3],</li>
 * <li><b>b</b> to [2,3,4],</li>
 * <li><b>f</b> to the current value of <b>e</b>.
 * </p>
 * The wrapped algorithm is required to call the terminators for <b>a</b> and
 * <b>b</b> on the construction of the algorithm because the exposed parameter
 * <b>d</b> transitively depends on them. In contrast, the terminator for
 * <b>f</b> must be called on execution only, because <b>f</b> depends on the
 * user choice for <b>d</b>.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 *
 */
public class GetFacadeAlgorithmTestMixedTerminatorAndAdapter {

	private Parameter<Set<Integer>> a;

	private Parameter<Set<Integer>> b;

	private SubCollectionParameter<Integer, Set<Integer>> c;

	private Parameter<Integer> d;

	private Parameter<Integer> e;

	private RangeEnumerableParameter<Integer> f;

	private ParameterTerminator aTerminator;

	private ParameterTerminator bTerminator;

	private ParameterAdapter dAdapter;

	private ParameterTerminator fTerminator;

	private StoppableMiningAlgorithm wrappedAlgorithm;

	private MiningAlgorithm wrapper;

	private DefaultParameter<Integer> outerD;

	@Before
	public void setUp() {
		a = Parameters.subSetParameter(id("a"), "a", "",
				() -> ImmutableSet.of(1, 2, 3, 4, 5), () -> ImmutableSet.of());
		b = Parameters.subSetParameter(id("b"), "b", "",
				() -> ImmutableSet.of(1, 2, 3, 4, 5), () -> ImmutableSet.of());
		c = Parameters.subSetParameter(id("c"),
				"c",
				"",
				() -> Sets.intersection(a.current(),
						b.current()),
				() -> Sets.intersection(a.current(),
						b.current()), a, b);
		d = new DefaultParameter<Integer>(identifier("d"),"d", "", Integer.class, null,
				str -> Integer.parseInt(str), val -> (0 <= val && val <= c
						.current().size()), "", c);
		e = new DefaultParameter<Integer>(identifier("e"),"e", "", Integer.class,
				input -> Integer.valueOf(input),
				val -> val <= d.current(),
				"value must be less than or equal to value of d",
				() -> d.current(), d);
		f = Parameters.rangeEnumerableParameter(identifier("f"),"f", "", Integer.TYPE, new RangeComputer<Integer>() {
			@Override
			public List<Integer> get() {
				List<Integer> result = new ArrayList<>();
				for (int i = 0; i <= e.current(); i++) {
					result.add(i);
				}
				return result;
			}
		}, e);

		wrappedAlgorithm = new MockAlgorithm("Inner algorithm", "",
				AlgorithmCategory.OTHER, ImmutableList.of(a, b, c, d, e, f),
				new ArrayList<Pattern<?>>());

		aTerminator = new StringValueTerminator(a, () -> "[1,2,3]");
		bTerminator = new StringValueTerminator(b, () -> "[2,3,4]");
		outerD = new DefaultParameter<Integer>(identifier("d"),"d", "", Integer.class, null,
				str -> Integer.parseInt(str), val -> val <= c.current()
						.size(), "", c);
		dAdapter = new SimpleParameterAdapter<Integer>(d, outerD);
		fTerminator = new StringValueTerminator(f, () -> e.current()
				.toString());

		wrapper = DerivedAlgorithms.getAlgorithmWithWrappedParameters(
				wrappedAlgorithm, ImmutableList.of(dAdapter, bTerminator,
						aTerminator, fTerminator),
				DerivedAlgorithms.HIDE_AND_WARN);
	}

	@Test
	public void testWrapperParameterConfiguration() {
		assertEquals(1, wrapper.getAllParameters().size());
		assertEquals(ImmutableList.of(c), outerD.getDependsOnParameters());
	}

	@Test
	public void testInit() {
		assertEquals(ImmutableSet.of(2, 3), c.getCollection());
		assertEquals(ImmutableSet.of(2, 3), c.current());
		assertFalse("outer d was initialized to null and must not be valid",
				outerD.isValid());
		assertTrue(
				"outer d must have valid context because dependency c must have gotten auto-initialized in background",
				outerD.isContextValid());
		outerD.set(3);
		assertFalse("3 must be an invalid value for d", outerD.isValid());
		assertFalse("e must have invalid context as long as d is invalid",
				e.isContextValid());
		assertFalse("f must have invalid context as long as e is invalid",
				f.isContextValid());

		outerD.set(2);
		assertTrue("2 must be a valid value", outerD.isValid());
		assertTrue("e must have valid context as long as d is valid",
				e.isContextValid());
		assertEquals("e must be initialized to value of d", Integer.valueOf(2),
				e.current());
		assertTrue("e must be valid after init", e.isValid());
		assertTrue("f must have valid context as long e is valid",
				f.isContextValid());
		assertEquals("f must be initialized to 0", Integer.valueOf(0),
				f.current());
	}

	@Test
	public void testOnExecutationSetting() throws ValidationException {
		outerD.set(2);
		wrapper.call();
		assertEquals("f must have been set to 2 by terminator on execution",
				Integer.valueOf(2), f.current());
	}

}
