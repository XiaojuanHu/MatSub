/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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

import static de.unibonn.realkd.data.table.attribute.Attributes.categoricalAttribute;
import static de.unibonn.realkd.data.table.attribute.Attributes.metricDoubleAttribute;
import static de.unibonn.realkd.data.table.attribute.Attributes.orderedCategoricAttribute;
import static de.unibonn.realkd.data.table.attribute.Attributes.ordinalAttribute;
import static java.lang.Boolean.parseBoolean;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;
import de.unibonn.realkd.data.xarf.XarfParsing.SetToken;
import de.unibonn.realkd.data.xarf.XarfParsing.StringToken;
import de.unibonn.realkd.data.xarf.XarfParsing.Token;
import de.unibonn.realkd.data.xarf.XarfParsing.ValueToken;

public class AttributeDeclaration {

	private static final Logger LOGGER = Logger.getLogger(AttributeDeclaration.class.getName());

	public static final String DESCRIPTION_PARAMETER_NAME = "description";

	public static final String CAPTION_PARAMETER_NAME = "caption";

	static final String LABEL_OF_ATTRIBUTE_NAME = "name";
	static final String LABEL_OF_CATEGORIC_ATTRIBUTE = "categoric";
	static final String LABEL_OF_INTEGER_ATTRIBUTE = "integer";
	static final String LABEL_OF_NUMERIC_ATTRIBUTE = "numeric";
	static final String LABEL_OF_REAL_ATTRIBUTE = "real";
	static final String LABEL_OF_DATE_ATTRIBUTE = "date";

	private final Identifier id;

	private final ValueToken domainSpecification;

	private final Optional<String> potentialDataFormat;

	private final Map<String, Object> parameters;

	public AttributeDeclaration(Identifier id, ValueToken domainSpec, Optional<String> potentialDataFormat,
			Map<String, Object> parameters) {
		this.id = id;
		this.domainSpecification = domainSpec;
		this.potentialDataFormat = potentialDataFormat;
		this.parameters = parameters;
	}

	public static Optional<AttributeDeclaration> attributeDeclaration(String line) {
		Token[] tokens = XarfParsing.tokens(line);
		if (tokens.length < 3 || !(tokens[1] instanceof StringToken) || !(tokens[2] instanceof ValueToken)) {
			return empty();
		}
		Identifier identifier = Identifier.identifier(((StringToken) tokens[1]).value());
		ValueToken domainSpecifier = (ValueToken) tokens[2];
		Optional<String> potentialDateFormat = (tokens.length >= 4 && tokens[3] instanceof StringToken)
				? Optional.of(((StringToken) tokens[3]).value())
				: empty();
		Map<String, Object> parameters = XarfParsing.parameters(tokens);
		return Optional.of(new AttributeDeclaration(identifier, domainSpecifier, potentialDateFormat, parameters));
	}

	public Identifier id() {
		return id;
	}

	public String caption() {
		return XarfParsing.valueOrElse(CAPTION_PARAMETER_NAME, parameters, id.toString());
	}

	public String description() {
		return XarfParsing.valueOrElse(DESCRIPTION_PARAMETER_NAME, parameters, "");
	}

	public boolean nameAttributeDeclaration() {
		return domainSpecification.caseInsensitiveStartsWith(LABEL_OF_ATTRIBUTE_NAME);
//		return (domainSpecification instanceof StringToken
//				&& ((StringToken) domainSpecification).value().toLowerCase().equals(LABEL_OF_ATTRIBUTE_NAME));
		// return domainSpecification.toLowerCase().startsWith(LABEL_OF_ATTRIBUTE_NAME);
	}

	public boolean categoricAttributeDeclaration() {
		return domainSpecification.caseInsensitiveStartsWith(LABEL_OF_CATEGORIC_ATTRIBUTE)
				|| (domainSpecification instanceof SetToken);
	}

	public boolean integerAttributeDeclaration() {
		return domainSpecification.caseInsensitiveStartsWith(LABEL_OF_INTEGER_ATTRIBUTE);
	}

	public boolean metricAttributeDeclaration() {
		return domainSpecification.caseInsensitiveStartsWith(LABEL_OF_NUMERIC_ATTRIBUTE)
				|| domainSpecification.caseInsensitiveStartsWith(LABEL_OF_REAL_ATTRIBUTE);
	}

	public boolean dateAttributeDeclaration() {
		return domainSpecification.caseInsensitiveStartsWith(LABEL_OF_DATE_ATTRIBUTE);
	}

	public Optional<Attribute<?>> attribute(List<String> data) {
		if (metricAttributeDeclaration()) {
			List<Double> values = data.stream().map(XarfParsing.AS_DOUBLE).collect(toList());
			return Optional.of(metricDoubleAttribute(id, caption(), description(), values));
		} else if (integerAttributeDeclaration()) {
			List<Integer> values = data.stream().map(XarfParsing.AS_INTEGER).collect(toList());
			boolean categoric = parseBoolean(XarfParsing.valueOrElse("categorical", parameters, "false"));
			OrdinalAttribute<Integer> attribute = categoric
					? orderedCategoricAttribute(id, caption(), description(), values, Integer.class)
					: ordinalAttribute(id, caption(), description(), values, Integer.class);
			return Optional.of(attribute);
		} else if (domainSpecification.asStringOrder().isPresent()) {
			List<String> values = data.stream().map(XarfParsing.AS_STRING).collect(toList());
			return Optional.of(Attributes.orderedCategoricAttribute(id, caption(), description(), values,
					domainSpecification.asStringOrder().get(), String.class));
		} else if (categoricAttributeDeclaration()) {
			List<String> values = data.stream().map(XarfParsing.AS_STRING).collect(toList());
			return Optional.of(categoricalAttribute(id, caption(), description(), values));
		} else if (dateAttributeDeclaration()) {
			if (!potentialDataFormat.isPresent()) {
				return empty();
			}
			SimpleDateFormat formatter = new SimpleDateFormat(potentialDataFormat.get());
			List<Date> values = data.stream().map(s -> {
				try {
					return formatter.parse(s);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return null;
			}).collect(toList());
			return Optional.of(Attributes.dateAttribute(id, caption(), description(), values));
		} else if (nameAttributeDeclaration()) {
			return Optional.empty(); // name attribute declarations are not turned into attributes
		} else {
			LOGGER.warning(String.format("Skipping attribute '%s' with unknown domain specifier '%s'", id,
					domainSpecification));
			return Optional.empty();
		}
	}

}