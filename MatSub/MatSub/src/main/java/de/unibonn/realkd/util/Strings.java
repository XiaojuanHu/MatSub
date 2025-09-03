package de.unibonn.realkd.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * String utility functions.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.0
 *
 */
public class Strings {

	private static final Collector<CharSequence, ?, String> AS_JOINED_STRING = Collectors.joining(",", "[", "]");

	private Strings() {
		; // not to be instantiated
	}

	/**
	 * Converts a string in the form [e1, e2, e3,...] to a list of strings with
	 * entries 'e1, e2, e3,...' the elements of which are trimmed of leading and
	 * trailing white-spaces.
	 * 
	 */
	public static List<String> jsonArrayToStringList(String argument) {
		argument = argument.substring(1, argument.length() - 1);
		if (argument.trim().length() == 0) {
			return Arrays.asList();
		}
		String[] split = argument.split(",");
		List<String> result = new ArrayList<>();
		for (String string : split) {
			String trimmed = string.trim();
			result.add(trimmed);
		}
		return result;
	}

	/**
	 * 
	 * @param input
	 *            the input string
	 * @param i
	 *            the number of characters to be chopped off the end
	 * @return input string minus the last i characters
	 */
	public static String chopped(String input, int i) {
		return input.substring(0, input.length() - i);
	}

	/**
	 * Converts object to string by recursively applying to contained elements
	 * if object is collection. In the process each floating number is converted
	 * according to some provided format.
	 * 
	 * @param object
	 *            object to be formatted
	 * @param format
	 *            format string to apply encountered floating point numbers
	 * @return string representation of object with contained floating point
	 *         numbers formatted
	 */
	public static String recursivelyFormatted(Object object, String format) {
		if (object instanceof Double || object instanceof Float) {
			return String.format(format, object);
		} else if (object instanceof Collection<?>) {
			return ((Collection<?>) object).stream().map(o -> recursivelyFormatted(o, format)).collect(AS_JOINED_STRING)
					.toString();
		} else if (object instanceof double[][]) {
			return Arrays.stream((double[][]) object).map(r -> recursivelyFormatted(r, format))
					.collect(AS_JOINED_STRING).toString();
		} else if (object instanceof double[]) {
			return Arrays.stream((double[]) object).mapToObj(d -> recursivelyFormatted(d, format))
					.collect(AS_JOINED_STRING).toString();
		} else {
			return object.toString();
		}
	}

}
