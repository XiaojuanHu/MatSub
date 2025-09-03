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
package de.unibonn.realkd.data.table.attribute;

import static de.unibonn.realkd.common.base.Identifier.identifier;
import static java.util.Comparator.naturalOrder;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.base.Identifier;

/**
 * Provides static factory methods for creating attributes.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.0
 *
 */
public class Attributes {

	private Attributes() {
		; // not to be instantiated
	}

	/**
	 * Creates a new categorical attribute of string values.
	 * 
	 * @param identifier
	 *            identifier of result attribute
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @return categorical attribute with provided parameters
	 */
	public static CategoricAttribute<String> categoricalAttribute(Identifier identifier, String caption,
			String description, List<String> values) {
		return new DefaultCategoricAttribute(identifier, caption, description, values);
	}

	/**
	 * Like {@link #categoricalAttribute(Identifier, String, String, List)}
	 * except that identifier is created from caption.
	 * 
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @return categorical attribute with provided parameters
	 */
	public static CategoricAttribute<String> categoricalAttribute(String caption, String description,
			List<String> values) {
		return categoricalAttribute(identifier(caption), caption, description, values);
	}

	public static <T> OrdinalAttribute<T> ordinalAttribute(String name, String description, List<T> values,
			Comparator<T> comparator, Class<T> type) {
		return new DefaultOrdinalAttribute<T>(identifier(name), name, description, values, comparator, type);
	}

	/**
	 * Creates a new ordinal attribute of values from a type with natural order.
	 * 
	 * @param identifier
	 *            identifier of result attribute
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @param type
	 *            type of result attribute values
	 * @return ordinal attribute with provided parameters
	 */
	public static <T extends Comparable<T>> OrdinalAttribute<T> ordinalAttribute(Identifier identifier, String caption,
			String description, List<T> values, Class<T> type) {
		return new DefaultOrdinalAttribute<T>(identifier, caption, description, values, naturalOrder(), type);
	}

	/**
	 * Like {@link #ordinalAttribute(Identifier, String, String, List, Class)}
	 * except that identifier is created from caption.
	 * 
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @param type
	 *            type of result attribute values
	 * @return ordinal attribute with provided parameters
	 */
	public static <T extends Comparable<T>> OrdinalAttribute<T> ordinalAttribute(String caption, String description,
			List<T> values, Class<T> type) {
		return ordinalAttribute(identifier(caption), caption, description, values, type);
	}

	public static <T> OrderedCategoricAttribute<T> orderedCategoricAttribute(String name, String description,
			List<T> values, Comparator<T> comparator, Class<? extends T> type) {
		return new OrderedCategoricAttribute<T>(identifier(name), name, description, values, comparator, type);
	}
	
	public static <T> OrderedCategoricAttribute<T> orderedCategoricAttribute(Identifier identifier, String name, String description,
			List<T> values, Comparator<T> comparator, Class<? extends T> type) {
		return new OrderedCategoricAttribute<T>(identifier, name, description, values, comparator, type);
	}

	public static <T extends Comparable<T>> OrderedCategoricAttribute<T> orderedCategoricAttribute(Identifier identifier, String name,
			String description, List<T> values, Class<T> type) {
		return new OrderedCategoricAttribute<T>(identifier, name, description, values, naturalOrder(), type);
	}
	
	public static <T extends Comparable<T>> OrderedCategoricAttribute<T> orderedCategoricAttribute(String name,
			String description, List<T> values, Class<T> type) {
		return orderedCategoricAttribute(identifier(name), name, description, values, type);
	}

	/**
	 * Creates a new metric attribute with double values from provided
	 * parameters.
	 * 
	 * @param identifier
	 *            identifier of result attribute
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @return metric attribute with provided parameters
	 */
	public static MetricAttribute metricDoubleAttribute(Identifier identifier, String caption, String description,
			List<Double> values) {
		return new DefaultMetricAttribute(identifier, caption, description, values);
	}

	/**
	 * Like {@link #metricDoubleAttribute(Identifier, String, String, List)}
	 * except that identifier is created from caption.
	 * 
	 * @param caption
	 *            caption of result attribute
	 * @param description
	 *            description of result attribute
	 * @param values
	 *            values of result attribute (null encodes missing)
	 * @return metric attribute with provided parameters
	 */
	public static MetricAttribute metricDoubleAttribute(String caption, String description, List<Double> values) {
		return metricDoubleAttribute(identifier(caption), caption, description, values);
	}

	public static DefaultDateAttribute dateAttribute(Identifier identifier, String name, String description, List<Date> values) {
		return new DefaultDateAttribute(identifier, name, description, values);
	}
	
	public static DefaultDateAttribute dateAttribute(String name, String description, List<Date> values) {
		return dateAttribute(identifier(name), name, description, values);
	}

	public static void main(String[] args) {
		// Population population = Populations.population("testPop", 3);
		CategoricAttribute<String> categoricAttribute = categoricalAttribute("Categoric attribute", "For testing",
				ImmutableList.of("male", "male", "female"));
		String json = JsonSerialization.toPrettyJson(categoricAttribute);
		System.out.println(json);
	}

}
