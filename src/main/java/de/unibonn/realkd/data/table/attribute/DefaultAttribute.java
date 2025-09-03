/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.data.table.attribute;

import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;

/**
 * Default implementation of {@link Attribute} that handles missing values.
 * 
 * @author Mario Boley
 *
 * @param <T>
 *            the type of the attribute values
 * 
 * @since 0.1.0
 * 
 * @version 0.4.2
 *
 */
public class DefaultAttribute<T> implements Attribute<T> {

	private final Identifier identifier;

	private final String name;

	private final String description;

	@JsonProperty("values")
	private final List<T> values;

	private final Class<? extends T> type;

	private final Collection<T> nonMissingValues;

	private final IndexSet missingPositions;

	private final int hashCode;

	/**
	 * @param values
	 *            list of T-values that can contain null entries to indicate
	 *            missing values
	 */
	public DefaultAttribute(Identifier identifier, String name, String description, List<T> values,
			Class<? extends T> type) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.values = new ArrayList<>(values);
		this.nonMissingValues = new ArrayList<>();
		List<Integer> _missingPositions = new ArrayList<>();
		this.type = type;
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) == null) {
				_missingPositions.add(i);
			} else {
				nonMissingValues.add(values.get(i));
			}
		}
		this.missingPositions = IndexSets.copyOf(_missingPositions);
		hashCode = hash(identifier);
	}

	@Override
	@JsonProperty("identifier")
	public Identifier identifier() {
		return identifier;
	}

	@Override
	@JsonProperty("description")
	public String description() {
		return description;
	}

	@Override
	@JsonProperty("name")
	public String caption() {
		return name;
	}

	@Override
	public boolean valueMissing(int objectId) {
		return values.get(objectId) == null;
	}

	@Override
	public T value(int objectId) {
		if (values.get(objectId) == null) {
			throw new IllegalArgumentException("value for object " + objectId + " missing");
		}
		return values.get(objectId);
	}

	@Override
	public Collection<T> nonMissingValues() {
		return nonMissingValues;
	}

	@Override
	public int numberOfNonMissingValues() {
		return values.size() - missingPositions.size();
	}

	@Override
	public int maxIndex() {
		return this.values.size() - 1;
	}

	@Override
	public IndexSet missingPositions() {
		return missingPositions;
	}

	public String toString() {
		return name;
	}

	@Override
	@JsonProperty("value_type")
	public Class<? extends T> type() {
		return type;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Attribute)) {
			return false;
		}
		Attribute<?> otherAttr = (Attribute<?>) other;
		return identifier.equals(otherAttr.identifier());
	}

	public int hashCode() {
		return hashCode;
	}

}
