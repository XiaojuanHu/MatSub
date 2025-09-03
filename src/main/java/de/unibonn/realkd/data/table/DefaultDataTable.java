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

package de.unibonn.realkd.data.table;

import static com.google.common.base.Preconditions.checkElementIndex;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;

/**
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0
 * 
 */
public final class DefaultDataTable extends AbstractDataTable implements DataTable, HasSerialForm<DataTable> {

	private static class DefaultDataTableSerialFom implements IdentifiableSerialForm<DataTable> {

		public final Identifier populationId;

		public final String name;

		public final String description;

		public final List<AttributeGroup> groups;

		public final List<Attribute<?>> attributes;

		public final Identifier id;

		@JsonCreator
		public DefaultDataTableSerialFom(@JsonProperty("populationId") Identifier populationId,
				@JsonProperty("id") Identifier id, @JsonProperty("name") String name,
				@JsonProperty("description") String description,
				@JsonProperty("attributes") List<Attribute<?>> attributes,
				@JsonProperty("groups") List<AttributeGroup> groups) {
			this.populationId = populationId;
			this.id = id;
			this.name = name;
			this.description = description;
			this.attributes = attributes;
			this.groups = groups;
		}

		@Override
		public DataTable build(Workspace workspace) {
			Optional<Population> pop = workspace.get(populationId, Population.class);
			return DataTables.table(id, name, description, pop.get(), attributes, groups);
		}

		@Override
		public Identifier identifier() {
			return id;
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return ImmutableSet.of(populationId);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultDataTableSerialFom)) {
				return false;
			}
			DefaultDataTableSerialFom otherSerialForm = (DefaultDataTableSerialFom) other;
			return populationId.equals(otherSerialForm.populationId) && id.equals(otherSerialForm.id)
					&& name.equals(otherSerialForm.name) && description.equals(otherSerialForm.description)
					&& attributes.equals(otherSerialForm.attributes) && groups.equals(otherSerialForm.groups);
		}

		public int hashCode() {
			return Objects.hash(id, populationId, name, description, attributes, groups);
		}

	}

	final List<Attribute<?>> attributes;

	private final ImmutableList<Entity> dependencies;

	private final Map<Identifier, Attribute<?>> attributeFromId;

	DefaultDataTable(Identifier id, String name, String description, Population population, List<Attribute<?>> attributes,
			List<AttributeGroup> attributeGroups) {
		super(id, name, description, population, attributeGroups);
		this.attributes = ImmutableList.copyOf(attributes);
		this.attributeFromId = attributes.stream().collect(toMap(Attribute::identifier, a -> a));
		this.dependencies = ImmutableList.of(population);
	}

	@Override
	public List<Attribute<?>> attributes() {
		return attributes;
	}

	@Override
	public Attribute<?> attribute(int attributeIndex) {
		checkElementIndex(attributeIndex, attributes.size());
		return attributes.get(attributeIndex);
	}

	@Override
	public Optional<? extends Attribute<?>> attribute(Identifier identifier) {
		return Optional.ofNullable(attributeFromId.get(identifier));
	}

	@Override
	public IdentifiableSerialForm<DataTable> serialForm() {
		return new DefaultDataTableSerialFom(population().identifier(), identifier(), caption(), description(), attributes,
				attributeGroups());
	}

	@Override
	public List<Entity> dependencies() {
		return dependencies;
	}

}
