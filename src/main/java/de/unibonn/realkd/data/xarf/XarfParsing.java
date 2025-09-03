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
package de.unibonn.realkd.data.xarf;

import static de.unibonn.realkd.common.base.Identifier.id;
import static java.util.Optional.empty;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.FiniteOrder;

/**
 * 
 * @author Panagiotis Mandros
 * @author Michael Hedderich
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.7.0
 *
 */
public class XarfParsing {

	static final Logger LOGGER = Logger.getLogger(XarfParsing.class.getName());

	public static final String LABEL_RELATION = "@relation";
	public static final String LABEL_DATA = "@data";
	public static final String LABEL_GROUP = "@group";
	public static final String LABEL_ATTRIBUTE = "@attribute";
	public static final String LABEL_INPUT = "@input";
	public static final String LABEL_OUTPUT = "@output";

	public static final String COMMENTTOKEN = "%";
	public static final String DECLARATIONTOKEN = "@";
	public static final String DEFAULT_DELIMITER = ",";

	private static final String MISSING_VALUE = "?";

	public static final Function<String, Integer> AS_INTEGER = s -> {
		try {
			return s == null ? null : Integer.parseInt(s);
		} catch (NumberFormatException e) {
			LOGGER.warning("Value for integer attribute could not be parsed as integer: " + s);
			try {
				double d = Double.parseDouble(s);
				if (d != (int) d) {
					throw new NumberFormatException(d + " not an integer value");
				}
				return (int) d;
			} catch (NumberFormatException e2) {
				LOGGER.warning(String.format("Could not parse '%s' as a number", s));
				throw e2;
			}
		}
	};

	public static final Function<String, String> AS_STRING = s -> s;

	public static final Function<String, Double> AS_DOUBLE = s -> s == null ? null : Double.parseDouble(s);

	static class Token {

		public boolean caseInsensitiveStartsWith(String value) {
			return false;
		}

		public Optional<Comparator<String>> asStringOrder() {
			return empty();
		}

		public Optional<Collection<String>> asStringCollection() {
			return empty();
		}

	}

	static class AssignmentToken extends Token {

		public String toString() {
			return "ASSIGNMENT";
		}

	}

	static abstract class ValueToken extends Token {
		public abstract Object value();
	}

	static class SetToken extends ValueToken {

		private final Set<String> value;

		public SetToken(Set<String> value) {
			this.value = value;
		}

		@Override
		public Collection<String> value() {
			return value;
		}

		public Optional<Collection<String>> asStringCollection() {
			return Optional.of(value);
		}

		@Override
		public String toString() {
			return "SET(" + value.toString() + ")";
		}

	}

	static class SequenceToken extends ValueToken {

		private final List<String> value;

		public SequenceToken(List<String> value) {
			this.value = value;
		}

		@Override
		public List<String> value() {
			return value;
		}

		public Optional<Comparator<String>> asStringOrder() {
			return Optional.of(new FiniteOrder(value));
		}

		public Optional<Collection<String>> asStringCollection() {
			return Optional.of(value);
		}

	}

	static class StringToken extends ValueToken {

		private final String value;

		StringToken(String value) {
			this.value = value;
		}

		public boolean caseInsensitiveStartsWith(String value) {
			return this.value.toLowerCase().startsWith(value.toLowerCase());
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public String value() {
			return value;
		}

	}

	private static final String SEQ_EXPRESSION = "\\[([^\\]]*.?)\\]";
	private static final String SET_EXPRESSION = "\\{([^}]*.?)\\}";
	private static final String UNQUOTED_STRING_EXPRESSION = "([\\S&&[^=]]+)";
	private static final String SINGLEQUOTED_STRING_EXPRESSION = "'([^']*)'";
	private static final String DOUBLEQUOTED_STRING_EXPRESSION = "\"([^\"]*)\"";
	private static final String ASSIGNMENT_SYMBOL = "(=)";
	private static final Pattern TOKEN_PATTERN = Pattern
			.compile(ASSIGNMENT_SYMBOL + "|" + DOUBLEQUOTED_STRING_EXPRESSION + "|" + SINGLEQUOTED_STRING_EXPRESSION
					+ "|" + SET_EXPRESSION + "|" + SEQ_EXPRESSION + "|" + UNQUOTED_STRING_EXPRESSION);

	private static String[] collectionTokens(String contentExpression) {
		return contentExpression.replaceAll("\"", "").replaceAll("\'", "").split(",");
	}

	public static Token[] tokens(String line) {
		List<Token> results = new ArrayList<>();
		Matcher m = TOKEN_PATTERN.matcher(line);
		while (m.find()) {
			if (m.group(1) != null) {
				results.add(new AssignmentToken());
			} else if (m.group(2) != null) {
				results.add(new StringToken(m.group(2)));
			} else if (m.group(3) != null) {
				results.add(new StringToken(m.group(3)));
			} else if (m.group(4) != null) {
				ImmutableSet<String> set = ImmutableSet.copyOf(collectionTokens(m.group(4)));
				results.add(new SetToken(set));
			} else if (m.group(5) != null) {
				ImmutableList<String> list = ImmutableList.copyOf(collectionTokens(m.group(5)));
				results.add(new SequenceToken(list));
			} else {
				results.add(new StringToken(m.group()));
			}
		}
		return results.toArray(new Token[results.size()]);
	}

	public static Map<String, Object> parameters(Token[] tokens) {
		Map<String, Object> result = new HashMap<>();
		for (int i = 1; i < tokens.length - 1; i++) {
			if (tokens[i] instanceof AssignmentToken) {
				if (tokens[i - 1] instanceof StringToken && tokens[i + 1] instanceof ValueToken) {
					result.put(((StringToken) tokens[i - 1]).value(), ((ValueToken) tokens[i + 1]).value());
				}
			}
		}
		return result;
	}

	static Optional<Collection<String>> parseCollection(String token) {
		String strippedFromBrackets = token.substring(1, token.length() - 1);
		String[] contentTokens = strippedFromBrackets.replaceAll("\"", "").replaceAll("\'", "").split(",");
		if (token.startsWith("{") && token.endsWith("}")) {
			return Optional.of(ImmutableSet.copyOf(contentTokens));
		} else if (token.startsWith("[") && token.endsWith("]")) {
			return Optional.of(ImmutableList.copyOf(contentTokens));
		} else {
			return Optional.empty();
		}
	}

	// tries to find which attribute represents the name of the samples
	static Optional<Integer> nameAttributeIndex(List<AttributeDeclaration> attributeDeclarations) {
		for (int i = 0; i < attributeDeclarations.size(); i++) {
			if (attributeDeclarations.get(i).nameAttributeDeclaration()) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	// returns a specific column of the data
	static Optional<List<String>> column(int i, List<List<String>> table) {
		List<String> result = new ArrayList<>();
		for (List<String> row : table) {
			if (row.size() <= i) {
				return Optional.empty();
			}
			result.add(row.get(i));
		}
		return Optional.of(result);
	}

	public static <K, V> V valueOfOr(K key, Map<K, V> parameters, V defaultValue) {
		final V caption = parameters.containsKey(key) ? parameters.get(key) : defaultValue;
		return caption;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> V valueOrElse(K key, Map<K, Object> parameters, V defaultValue) {
		return Optional.ofNullable(parameters.get(key))
				.filter(v -> defaultValue.getClass().isAssignableFrom(v.getClass())).map(v -> (V) v)
				.orElse(defaultValue);
//		final V caption = parameters.containsKey(key) ? parameters.get(key) : defaultValue;
//		return caption;
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		if (str.isEmpty()) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (str.length() == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/*
	 * A class representing the CSV portion of a xarf, comprising of data and an
	 * optional header
	 * 
	 */
	public static class CsvPortion {

		public final List<List<String>> data;
		public final Optional<List<String>> headers;
		private final int numberOfColumns;

		CsvPortion(List<List<String>> data, Optional<List<String>> headers) {
			this.data = data;
			this.headers = headers;
			this.numberOfColumns = headers.map(h -> h.size()).orElse(data.isEmpty() ? 0 : data.get(0).size());
		}

		public int numberOfColumns() {
			return numberOfColumns;
		}

		public List<String> column(int i) {
			List<String> values = new ArrayList<>();
			for (int j = 0; j < data.size(); j++) {
				String cellValue = null;
				try {
					cellValue = data.get(j).get(i);
					if (cellValue.equals(XarfParsing.MISSING_VALUE)) {
						values.add(null);
					} else {
						values.add(data.get(j).get(i));
					}
				} catch (Exception e) {
					LOGGER.warning(String.format(
							"Exception when parsing value '%s' data column %d row %d (setting to missing)\nMessage: %s",
							cellValue, i, j, e.getMessage()));
					values.add(null);
				}
			}
			return values;
		}

	}

	/**
	 * Automatically detects and returns the header row in csv data portion if all
	 * of the following conditions hold:
	 * 
	 * <ol>
	 * <li>No token of the first row can be parsed as a number or missing value</li>
	 * <li>All tokens in the first row are unique (after being mapped to
	 * identifier)</li>
	 * <li>in each column, all token from the second row and below are different
	 * from the token in the first row (when leading and trailing whitespaces are
	 * stripped)</li>
	 * <ol>
	 * 
	 * Otherwise returns empty.
	 * 
	 * @param csvParsed the csv portion of the input
	 * 
	 * @return header if detected or empty
	 * 
	 */
	public static Optional<List<String>> autodetectedCSVHeader(List<List<String>> csvParsed) {
		if (csvParsed.isEmpty()) {
			return Optional.empty();
		}

		List<String> headerCandidate = csvParsed.get(0);

		// Cond 1: No number or missing in first row
		for (String token : headerCandidate) {
			if (isNumeric(token) || MISSING_VALUE.equals(token)) {
				return empty();
			}
		}

		// Cond 2: First row unique ids
		Set<Identifier> ids = new HashSet<>();
		for (String token : headerCandidate) {
			Identifier id = id(token);
			if (ids.contains(id)) {
				return empty();
			}
			ids.add(id);
		}

		// Cond 3: For all columns, first row entry unique
		for (int col = 0; col < headerCandidate.size(); col++) {
			for (int row = 1; row < csvParsed.size(); row++) {
				if (headerCandidate.get(col).equals(csvParsed.get(row).get(col))) {
					return empty();
				}
			}
		}

		LOGGER.info("A csv header was detected in the data");
		return Optional.of(headerCandidate);
	}

	// tries to identify the type of an attributes solely based on the data values
	// first checks if all values are integers. Then checks if is in general
	// numeric. if all fail then it is parsed as categoric
	public static String sniffForAttributeType(List<List<String>> data, int columnIndex) {
		boolean isInteger = true;
		boolean isNumeric = true;
		for (List<String> row : data) {
			String valueForAttribute = row.get(columnIndex);
			if (valueForAttribute.equals(MISSING_VALUE)) {
				continue;
			}

			if (!isInteger(valueForAttribute) && isInteger) {
				isInteger = false;
			}

			if (!isNumeric(valueForAttribute)) {
				isInteger = false;
				isNumeric = false;
				break;
			}
		}

		if (isInteger == true) {
			return AttributeDeclaration.LABEL_OF_INTEGER_ATTRIBUTE;
		} else if (isInteger == false && isNumeric == false) {
			return AttributeDeclaration.LABEL_OF_CATEGORIC_ATTRIBUTE;
		} else {
			return AttributeDeclaration.LABEL_OF_REAL_ATTRIBUTE;
		}
	}

	public static Xarf parse(BufferedReader input) throws IOException {
		LOGGER.info("Parsing input file");

		List<String> leadingCommentLines = new ArrayList<>();
		RelationDeclaration relationDeclaration = RelationDeclaration.DEFAULT_RELATION_DECLARATION;
		List<AttributeDeclaration> attributeDeclarations = new ArrayList<>();
		List<GroupDeclaration> groupDeclarations = new ArrayList<>();
		DataDeclaration dataDeclaration = DataDeclaration.DEFAULT_DATA_DECLARATION;
		List<String> data = new ArrayList<>();

		boolean descriptionPhase = true;
		boolean metadataPhase = false;
		boolean dataPhase = false;

		String line;

		while ((line = input.readLine()) != null) {
			// continue if empty line or comment outside the description
			// section
			if (Strings.isNullOrEmpty(line) || (descriptionPhase == false && line.startsWith(COMMENTTOKEN) == true)) {
				continue;
			}

			// get the description of the dataset if in description section
			// isDescription being true and the line not starting with
			// COMMENTTOKEN
			// indicates that the description section is over, and metadata
			// section begins
			if (descriptionPhase) {
				if (line.startsWith(COMMENTTOKEN)) {
					leadingCommentLines.add(line);
				} else {
					descriptionPhase = false;
					metadataPhase = true;
				}
			}

			if (metadataPhase) {
				if (line.startsWith(DECLARATIONTOKEN)) {
					if (line.toLowerCase().startsWith(LABEL_RELATION)) {
						if (relationDeclaration == RelationDeclaration.DEFAULT_RELATION_DECLARATION) {
							relationDeclaration = RelationDeclaration.fromLine(line);
						} else {
							LOGGER.warning("Multiple relation declarations found; ignoring");
						}
					} else if (line.toLowerCase().startsWith(LABEL_ATTRIBUTE)) {
						Optional<AttributeDeclaration> declaration = AttributeDeclaration.attributeDeclaration(line);
						if (declaration.isPresent()) {
							attributeDeclarations.add(declaration.get());
						}
					} else if (line.toLowerCase().startsWith(LABEL_GROUP)) {
						Optional<GroupDeclaration> group = GroupDeclaration.groupEntry(line);
						if (!group.isPresent()) {
							LOGGER.warning("Could not parse group from line: " + line);
						} else {
							groupDeclarations.add(group.get());
						}
					} else if (line.toLowerCase().startsWith(LABEL_INPUT)) {
						;
					} else if (line.toLowerCase().startsWith(LABEL_OUTPUT)) {
						;
					} else if (line.toLowerCase().startsWith(LABEL_DATA)) {
						if (dataDeclaration == DataDeclaration.DEFAULT_DATA_DECLARATION) {
							dataDeclaration = DataDeclaration.dataDeclaration(line);
						} else {
							LOGGER.warning("Multiple data format declarations found; ignoring");
						}
					} else {
						LOGGER.warning("Unknown declaration token; skipping: " + line);
					}
				} else {
					metadataPhase = false;
					dataPhase = true;
				}
			}

			if (dataPhase) {
				data.add(line);
			}
		}

		if (data.isEmpty()) {
			LOGGER.severe("Did not parse any data");
			// TODO do something else, e.g., terminate?
		}

		LOGGER.info("Done parsing ARFF file");

		return new Xarf(leadingCommentLines, relationDeclaration, attributeDeclarations, groupDeclarations,
				dataDeclaration, data);
	}

}
