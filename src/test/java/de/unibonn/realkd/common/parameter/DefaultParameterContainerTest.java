package de.unibonn.realkd.common.parameter;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.subListParameter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.util.Predicates;

/**
 * Test case with complex parameter container with four parameters: a,b,c, and
 * d.
 * 
 * Parameters a and b are simple integer parameters that accept positive values.
 * 
 * Parameter c is a complex range enumerable parameter that contains two
 * options, each of which are again parameter container that contain a
 * sub-parameter called sub-option. These sub-parameters are range-enumerable
 * with the available range being the integers 1 to value-of-a (for the first
 * container) and 1 to value-of-b (for the second container). Thus, c depends on
 * a and b.
 * 
 * Finally, parameter d is a sublist parameter with range being the possible
 * strings from 1 to value-of-subparameter-of-value-of-c.
 * 
 * @author mboley
 * 
 */
public class DefaultParameterContainerTest {

	private static final String D_PARAMETER_NAME = "d";

	private static final String SUB_PARAMETER_NAME = "sub-option";

	private static final String A_PARAM_NAME = "a";

	private static final String B_PARAM_NAME = "b";

	private static final String C_PARAM_NAME = "c";

	private Parameter<Integer> a;

	private Parameter<Integer> b;

	private RangeEnumerableParameter<ParameterContainer> c;

	private DefaultParameterContainer firstPossibleCValue;

	private DefaultParameterContainer secondPossibelCValue;

	private SubCollectionParameter<String, List<String>> d;

	private DefaultParameterContainer container;

	// private class MySublistParam extends
	// AbstractNonEmptySubListParameter<String> {
	//
	// public MySublistParam() {
	// super(D_PARAMETER_NAME, "", List.class, null, "", c);
	// }
	//
	// @Override
	// protected List<String> getConcreteRange() {
	// int rangeBound = Integer.parseInt(c.getCurrentValue()
	// .findParameterByName(SUB_PARAMETER_NAME).getCurrentValue()
	// .toString());
	// List<String> result = new ArrayList<>();
	// for (int i = 0; i < rangeBound; i++) {
	// result.add(new Integer(i).toString());
	// }
	// return result;
	// }
	//
	// }

	@Before
	public void setUp() {

		a = new DefaultParameter<Integer>(id("a"), A_PARAM_NAME, "", Integer.class, 1,
				input->Integer.valueOf(input),
				Predicates.largerThan(0), "");

		b = new DefaultParameter<Integer>(id("b"), B_PARAM_NAME, "", Integer.class, 1,
				input->Integer.valueOf(input),
				Predicates.largerThan(0), "");

		firstPossibleCValue = new DefaultParameterContainer();
		firstPossibleCValue.addParameter(createRangeEnumParamDependingOn(a));
		secondPossibelCValue = new DefaultParameterContainer();
		secondPossibelCValue.addParameter(createRangeEnumParamDependingOn(b));

		c = Parameters.rangeEnumerableParameter(id("c"),C_PARAM_NAME, "", ParameterContainer.class, new RangeComputer<ParameterContainer>() {
			@Override
			public List<ParameterContainer> get() {
				return Arrays.asList(
						(ParameterContainer) firstPossibleCValue,
						(ParameterContainer) secondPossibelCValue);
			}
		}, firstPossibleCValue.findParameterByName(SUB_PARAMETER_NAME),
				secondPossibelCValue.findParameterByName(SUB_PARAMETER_NAME));

		// d = new MySublistParam();
		Supplier<List<String>> dCollectionComputer = new Supplier<List<String>>() {

			@Override
			public List<String> get() {
				int rangeBound = Integer.parseInt(c.current()
						.findParameterByName(SUB_PARAMETER_NAME)
						.current().toString());
				List<String> result = new ArrayList<>();
				for (int i = 0; i < rangeBound; i++) {
					result.add(Integer.valueOf(i).toString());
				}
				return result;
			}
		};
		Predicate<List<String>> dValidator = value -> !value.isEmpty()
				&& dCollectionComputer.get().containsAll(value);
		d = subListParameter(id("d"),
				D_PARAMETER_NAME, "", dCollectionComputer, dValidator, c);

		container = new DefaultParameterContainer();

		container.addParameter(a);
		container.addParameter(b);
		container.addParameter(c);
		container.addParameter(d);
	}

	private DefaultRangeEnumerableParameter<Integer> createRangeEnumParamDependingOn(
			final Parameter<Integer> depParam) {
		return Parameters.rangeEnumerableParameter(id("sub_option"), SUB_PARAMETER_NAME, "", Integer.class, new RangeComputer<Integer>() {

			@Override
			public List<Integer> get() {
				List<Integer> result = new ArrayList<>();
				for (int i = 0; i < depParam.current(); i++) {
					result.add(i);
				}
				return result;
			}
		}, depParam);
	}

	@Test
	public void testBlockAssignment() {
		Map<String, String> keyValues = new HashMap<>();
		keyValues.put(A_PARAM_NAME, "4");
		keyValues.put(B_PARAM_NAME, "5");
		keyValues.put(SUB_PARAMETER_NAME, "3");
		keyValues.put(D_PARAMETER_NAME, "[1,2]");
		container.passValuesToParameters(keyValues);

		assertEquals(Arrays.asList("1", "2"),
				container.findParameterByName(D_PARAMETER_NAME)
						.current());

		assertTrue(container.isStateValid());
	}

	@Test
	public void testIndividualAssignment() {
		container.findParameterByName(A_PARAM_NAME).setByString("4");
		RangeEnumerableParameter<?> subParam = (RangeEnumerableParameter<?>) c
				.current().findParameterByName(SUB_PARAMETER_NAME);
		assertNotNull(subParam);
		assertEquals(4, subParam.getRange().size());
		assertTrue(subParam == container
				.findParameterByName(SUB_PARAMETER_NAME));

		container.findParameterByName(SUB_PARAMETER_NAME).setByString("3");
		container.findParameterByName(D_PARAMETER_NAME).setByString("[1,2]");
		assertEquals(Arrays.asList("1", "2"),
				container.findParameterByName(D_PARAMETER_NAME)
						.current());

		assertTrue(container.isStateValid());
	}

	@Test
	public void testFindTopLevelParameterByString() {
		assertNotNull(container.findParameterByName(A_PARAM_NAME));
	}

	@Test
	public void testGetTopLevelParameters() {
		assertEquals(4, container.getTopLevelParameters().size());
	}

	@Test
	public void testFindNestedParameterByString() {
		assertNotNull(container.findParameterByName(SUB_PARAMETER_NAME));
	}

	@Test
	public void testNonStateValidIfOneParemeterInvalid() {
		container.findParameterByName(A_PARAM_NAME).set(null);
		assertFalse(container.isStateValid());
	}

}
