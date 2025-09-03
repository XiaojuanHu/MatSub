/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.workspace;

import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.common.base.Identifier.isValidIdentifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.unibonn.realkd.common.base.Identifier;

/**
 * Tests basic properties of {@link Identifier} and identifier creation through
 * {@link Identifier#identifier}.
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public class IdentifierBasicTest {

	public static final String VALID_STRING = "attribute47";

	public static final String VALID_STRING_WITH_DOLLAR_PREFIX = "$attribute47";

	public static final String INVALID_STRING = "-(attribute 47**)";

	public static final String ID_STRING_FROM_INVALID_STRING = "_attribute_47_";

	public static final String EQUIVALENT_INVALID_STRING = "!(attribute    47**)";

	public static final String INVALID_STRING_WITH_LEADING_NUMBER = "47id";

	private final Identifier idFromValidString;

	private final Identifier secondIdFromValidString;

	private final Identifier idFromValidStringWithDollarPrefix;

	private final Identifier idFromInvalidString;

	private final Identifier idFromEquivalentInvalidString;

	private final Identifier idFromInvalidStringWithLeadingNumber;

	public IdentifierBasicTest() {
		this.idFromValidString = identifier(VALID_STRING);
		this.secondIdFromValidString = identifier(VALID_STRING);
		this.idFromValidStringWithDollarPrefix = identifier(VALID_STRING_WITH_DOLLAR_PREFIX);
		this.idFromInvalidString = identifier(INVALID_STRING);
		this.idFromEquivalentInvalidString = identifier(EQUIVALENT_INVALID_STRING);
		this.idFromInvalidStringWithLeadingNumber = identifier(INVALID_STRING_WITH_LEADING_NUMBER);
	}

	@Test
	public void toStringTest() {
		assertEquals(VALID_STRING, idFromValidString.toString());
		assertEquals(VALID_STRING, secondIdFromValidString.toString());
		assertEquals(VALID_STRING_WITH_DOLLAR_PREFIX, idFromValidStringWithDollarPrefix.toString());
		assertEquals(ID_STRING_FROM_INVALID_STRING, idFromInvalidString.toString());
		assertEquals(ID_STRING_FROM_INVALID_STRING, idFromEquivalentInvalidString.toString());
	}

	@Test
	public void equalsTest() {
		assertEquals(idFromValidString, secondIdFromValidString);
	}

	@Test
	public void nonEqualityWhenAddingDollarPrefix() {
		assertNotEquals(idFromValidString, idFromValidStringWithDollarPrefix);
	}

	@Test
	public void hashCodeTest() {
		assertEquals(idFromValidString.hashCode(), secondIdFromValidString.hashCode());
		assertEquals(idFromInvalidString.hashCode(), idFromEquivalentInvalidString.hashCode());
	}

	@Test
	public void avoidLeadingNumberTest() {
		assertFalse(Character.isDigit(idFromInvalidStringWithLeadingNumber.charAt(0)));
	}
	
	@Test
	public void isValidIdentifierTest() {
		assertTrue(isValidIdentifier(VALID_STRING));
		assertTrue(isValidIdentifier(VALID_STRING_WITH_DOLLAR_PREFIX));
		assertFalse(isValidIdentifier(INVALID_STRING));
		assertFalse(isValidIdentifier(INVALID_STRING_WITH_LEADING_NUMBER));
		assertFalse(isValidIdentifier(EQUIVALENT_INVALID_STRING));
	}

}
