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
package de.unibonn.realkd.common.base;

import static java.lang.Character.isDigit;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;

/**
 * <p>
 * Special char sequence that can only contain word characters plus the
 * special symbol '$', which is used by some clients to refer to special
 * non-user entities.
 * </p>
 * <p>
 * Additionally, identifiers may not be empty, may not start with a digit, and
 * may not only consist of the underscore symbol '_'.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("identifier")
@KdonDoc("String that only contains alpha-numeric characters and underscores and that does not start with a digit.")
public class Identifier implements CharSequence, Comparable<Identifier> {

	private static final String FORBIDDEN_CHARS = "[\\W&&[^$]]+";
	private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile(FORBIDDEN_CHARS);
	private static final Logger LOGGER = Logger.getLogger(Identifier.class.getName());

	/**
	 * Converts a string into an identifier. If input string does not obey
	 * identifier conventions then it is altered and a warning is logged.
	 * 
	 * @param string
	 *            the input string
	 * @return an identifier based on string
	 */
	public static Identifier identifier(String string) {
		String validString = string.replaceAll(FORBIDDEN_CHARS, "_");
		if (validString.isEmpty() || validString.equals("_")) {
			validString = "id";
		} else if (isDigit(validString.charAt(0))) {
			validString = "id_" + validString;
		}
		if (!validString.equals(string)) {
			LOGGER.warning("Invalid identifier string " + string + " had to be altered to " + validString);
		}
		return new Identifier(validString);
	}
	
	public static Identifier id(String string) {
		return identifier(string);
	}

	/**
	 * Checks if string can be turned into a valid identifier as is.
	 * 
	 * @param string
	 *            the input string
	 * @return true iff identifier(string).toString().equals(string)
	 */
	public static boolean isValidIdentifier(String string) {
		return !string.isEmpty() && !string.equals("_") && !Character.isDigit(string.charAt(0))
				&& !FORBIDDEN_CHARS_PATTERN.matcher(string).find();
	}

	private final String string;

	private Identifier(@JsonProperty("value") String string) {
		this.string = string;
	}

	@JsonProperty("value")
	public String toString() {
		return string;
	}

	@Override
	public int compareTo(Identifier o) {
		return string.compareTo(o.string);
	}

	@Override
	public int length() {
		return string.length();
	}

	@Override
	public char charAt(int index) {
		return string.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return string.subSequence(start, end);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Identifier)) {
			return false;
		}
		return this.string.equals(((Identifier) other).string);
	}

}
