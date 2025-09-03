package de.unibonn.realkd.data.table.attribute;

import static de.unibonn.realkd.common.base.Identifier.identifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DefaultAttributeTest {

	private static final List<String> TEST_ATTRIBUTE_VALUES = Arrays.asList("A", "B", null, "A");
	private static final String TEST_ATTRIBUTE_DESCRIPTION = "This attribute has been added for unit testing DefaultAttribute.java";
	private static final String TEST_ATTRIBUTE_NAME = "Test Attribute";
	private DefaultAttribute<String> testAttribute;

	@Before
	public void setUp() {
		this.testAttribute = new DefaultAttribute<String>(identifier(TEST_ATTRIBUTE_NAME), TEST_ATTRIBUTE_NAME,
				TEST_ATTRIBUTE_DESCRIPTION, TEST_ATTRIBUTE_VALUES, String.class);
	}

	@Test
	public void testIsValueMissing() {
		assertTrue(testAttribute.valueMissing(2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetValueIllegalAccess() {
		testAttribute.value(2);
	}

	@Test
	public void testGetValue() {
		assertEquals("B", testAttribute.value(1));
	}

	@Test
	public void testGetValues() {
		Collection<String> nonMissingValues = testAttribute.nonMissingValues();
		assertEquals(3, nonMissingValues.size());
		int aCount = 0;
		int bCount = 0;
		for (String value : nonMissingValues) {
			if (value.equals("A")) {
				aCount++;
			}
			if (value.equals("B")) {
				bCount++;
			}
		}
		assertEquals(2, aCount);
		assertEquals(1, bCount);
	}

	@Test
	public void getNonMissing() {
		assertEquals(3, testAttribute.numberOfNonMissingValues());
	}

}
