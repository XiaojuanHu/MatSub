/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.table;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;

/**
 * Data artifact that aggregates a list of attributes over some fixed population
 * of objects and optional group information for attributes that have a semantic
 * relation.
 * 
 * @see DataTables
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 *
 */
public interface DataTable extends Entity {

	/**
	 * Checks if object has at least one missing values.
	 */
	public abstract boolean atLeastOneAttributeValueMissingFor(int objectId);

	/**
	 * Checks if object has at least one missing values for any of the specified
	 * test attributes.
	 */
	public abstract boolean atLeastOneAttributeValueMissingFor(int objectId,
			List<? extends Attribute<?>> testAttributes);

	/**
	 * Provides a list of all attributes aggregated by this table in fixed order.
	 * 
	 * @return list of all attributes
	 * 
	 */
	public abstract List<? extends Attribute<?>> attributes();

	public default List<? extends Attribute<?>> attributes(String... ids) {
		return Arrays.stream(ids).map(i -> Identifier.identifier(i)).map(i -> attribute(i)).filter(o -> o.isPresent())
				.map(o -> o.get()).collect(Collectors.toList());
	}
	
	public default List<? extends Attribute<?>> attributes(Identifier[] ids) {
		return Arrays.stream(ids).map(i -> attribute(i)).filter(o -> o.isPresent())
				.map(o -> o.get()).collect(Collectors.toList());
	}
	
	public abstract Attribute<?> attribute(int attributeIndex);

	/**
	 * Finds attribute through identifier.
	 * 
	 * @param identifier
	 *            of attribute
	 * @return optional of attribute with specified identifier or empty if no such
	 *         attribute contained in table
	 */
	public abstract Optional<? extends Attribute<?>> attribute(Identifier identifier);

	/**
	 * 
	 * @return the common population of objects to which all aggregated attributes
	 *         refer to
	 */
	public abstract Population population();

	/**
	 * Convenience method providing the number of attributes aggregated by this
	 * table, i.e., is equal to getAttributes().size().
	 * 
	 * @return number of attributes
	 */
	public default int numberOfAttributes() {
		return attributes().size();
	}

	/**
	 * Convenience method that returns a list with the names of all attributes in
	 * the same order as they would be returned by getAttributes().
	 * 
	 * @return list of all attribute names
	 * 
	 */
	public default List<String> attributeNames() {
		return attributes().stream().map(a -> a.caption()).collect(Collectors.toList());
	}

	/**
	 * 
	 * @return map of indices to attributes contained in table if index valid and to
	 *         empty optional otherwise
	 */
	public default Function<? super Integer, Optional<Attribute<?>>> indexToAttributeMap() {
		return i -> (0 <= i && i < attributes().size()) ? Optional.of(attribute(i)) : Optional.empty();
	}

	public abstract List<AttributeGroup> attributeGroups();

	public abstract Collection<AttributeGroup> attributeGroupsOf(Attribute<?> attribute);

	public abstract boolean containsDependencyBetweenAnyOf(Attribute<?> attribute,
			Collection<? extends Attribute<?>> otherAttributes);

	public abstract boolean containsDependencyBetween(Attribute<?> attribute, Attribute<?> otherAttribute);

}