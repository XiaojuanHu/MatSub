package de.unibonn.realkd.common.parameter;

import static de.unibonn.realkd.common.base.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.util.Predicates;

public class DefaultRangeEnumerableParameterTest {

	private static final String UPSTREAM_PARAMETER = "a";

	private static final String DEFAULT_RANGE_ENUMERABLE_PARAM_NAME = "b";

	private Parameter<Integer> upstreamParameter;

	private DefaultRangeEnumerableParameter<Integer> defaultRangeEnumerableParameter;

	@Before
	public void setUp() {
		upstreamParameter = new DefaultParameter<Integer>(id("a"), UPSTREAM_PARAMETER, "", Integer.class, 3,
				input -> Integer.valueOf(input), Predicates.largerThan(0), "");

		RangeComputer<Integer> rangeComputer = new RangeComputer<Integer>() {

			@Override
			public List<Integer> get() {
				List<Integer> result = new ArrayList<>();

				for (int i = 0; i < upstreamParameter.current(); i++) {
					result.add(Integer.valueOf(i));
				}
				return result;
			}

		};

		defaultRangeEnumerableParameter = Parameters.rangeEnumerableParameter(id("b"),
				DEFAULT_RANGE_ENUMERABLE_PARAM_NAME, "", ParameterContainer.class, rangeComputer, upstreamParameter);

	}

	@Test
	public void testRangeIteration() {
		Iterator<? extends Integer> iterator1 = defaultRangeEnumerableParameter.getRange().iterator();
		Iterator<? extends Integer> iterator2 = defaultRangeEnumerableParameter.getRange().iterator();
		assertTrue("Must return different iterator objects", iterator1 != iterator2);
	}

	@Test
	public void testRangeInitializiation() {
		assertEquals(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)),
				defaultRangeEnumerableParameter.getRange());
	}

	@Test
	public void testValueInitializiation() {
		assertEquals(Integer.valueOf(0), defaultRangeEnumerableParameter.current());
	}

	@Test
	public void testRangeReInitialization() {
		upstreamParameter.set(5);
		assertEquals(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3),
				Integer.valueOf(4)), defaultRangeEnumerableParameter.getRange());
	}

	@Test
	public void testRangeReInitializationInInvalidContext() {
		upstreamParameter.set(-1);
		assertEquals(Arrays.asList(), defaultRangeEnumerableParameter.getRange());
	}

	@Test
	public void testSetToValidValue() {
		Integer value = 2;
		defaultRangeEnumerableParameter.set(value);
		assertEquals(value, defaultRangeEnumerableParameter.current());
	}

	@Test
	public void testSetToInvalidValue() {
		Integer value = 10;
		defaultRangeEnumerableParameter.set(value);
		assertEquals(value, defaultRangeEnumerableParameter.current());
	}

	@Test
	public void testSetByStringToValueValue() {
		Integer value = 2;
		defaultRangeEnumerableParameter.setByString(String.valueOf(value));
		assertEquals(value, defaultRangeEnumerableParameter.current());
	}

	@Test
	public void testValidationOfInvalidValue() {
		Integer value = 10;
		defaultRangeEnumerableParameter.set(value);
		assertEquals(false, defaultRangeEnumerableParameter.isValid());
	}

	@Test
	public void testValidationOfValidValue() {
		Integer value = 1;
		defaultRangeEnumerableParameter.set(value);
		assertEquals(true, defaultRangeEnumerableParameter.isValid());
	}

	@Test
	public void testContextValidationOfInvalidContext() {
		upstreamParameter.set(-1);
		assertEquals(false, defaultRangeEnumerableParameter.isContextValid());
	}

	@Test
	public void testContextValidationOfValidContext() {
		upstreamParameter.set(3);
		assertEquals(true, defaultRangeEnumerableParameter.isContextValid());
	}

}
