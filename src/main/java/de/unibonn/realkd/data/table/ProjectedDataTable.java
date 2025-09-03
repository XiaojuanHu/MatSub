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
package de.unibonn.realkd.data.table;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * Implements a projected view on an existing data table. Similar to databases
 * this shows only a selection of data tables from the underlying data table.
 * 
 * @author Sandy Moens
 *
 * @since 0.4.0
 *
 * @version 0.5.0
 *
 */
class ProjectedDataTable implements DataTable, HasSerialForm<DataTable> {

	private static class ProjectedDataTableSerialForm implements IdentifiableSerialForm<DataTable> {

		@JsonProperty("id")
		private final Identifier id;

		@JsonProperty("name")
		private final String name;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("tableId")
		private final Identifier tableId;

		@JsonProperty("hiddenAttributeNames")
		private final Collection<String> hiddenAttributeNames;

		@JsonCreator
		public ProjectedDataTableSerialForm(@JsonProperty("id") Identifier id, @JsonProperty("name") String name,
				@JsonProperty("description") String description, @JsonProperty("tableId") Identifier tableId,
				@JsonProperty("hiddenAttributeNames") Collection<String> hiddenAttributeNames) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.tableId = tableId;
			this.hiddenAttributeNames = hiddenAttributeNames;
		}

		@Override
		public DataTable build(Workspace workspace) {
			Optional<DataTable> dataTable = workspace.get(this.tableId, DataTable.class);
			return new ProjectedDataTable(this.id, this.name, this.description, dataTable.get(),
					this.hiddenAttributeNames);
		}

		@Override
		public Identifier identifier() {
			return id;
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return ImmutableSet.of(this.tableId);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ProjectedDataTableSerialForm)) {
				return false;
			}
			ProjectedDataTableSerialForm otherSerialForm = (ProjectedDataTableSerialForm) other;
			return this.id.equals(otherSerialForm.id) && this.name.equals(otherSerialForm.name)
					&& this.description.equals(otherSerialForm.description)
					&& this.tableId.equals(otherSerialForm.tableId)
					&& this.hiddenAttributeNames.equals(otherSerialForm.hiddenAttributeNames);
		}

		public int hashCode() {
			return Objects.hash(this.id, this.name, this.description, this.tableId, this.hiddenAttributeNames);
		}

	}

	private final DataTable originalTable;
	private final Collection<String> hiddenAttributeNames;
	private final DefaultDataTable wrappedTable;

	ProjectedDataTable(Identifier id, String name, String description, DataTable dataTable,
			Collection<String> hiddenAttributeNames) {
		this.originalTable = dataTable;
		this.hiddenAttributeNames = hiddenAttributeNames;
		Set<Integer> hiddenIndices = hiddenAttributeNames.stream().map(n -> dataTable.attributeNames().indexOf(n))
				.collect(Collectors.toSet());
		List<Attribute<?>> attributes = IntStream.range(0, dataTable.attributes().size()).filter(n -> !hiddenIndices.contains(n))
				.mapToObj(n -> dataTable.attribute(n)).collect(Collectors.toList());
		List<AttributeGroup> groups=originalTable.attributeGroups().stream().filter(g->attributes.containsAll(g.elements())).collect(toList());
		this.wrappedTable=new DefaultDataTable(id, name, description, originalTable.population(), attributes, groups); 
	}

	@Override
	public Identifier identifier() {
		return this.wrappedTable.identifier();
	}

	@Override
	public String caption() {
		return this.wrappedTable.caption();
	}

	@Override
	public String description() {
		return this.wrappedTable.description();
	}

	@Override
	public IdentifiableSerialForm<DataTable> serialForm() {
		return new ProjectedDataTableSerialForm(this.identifier(), this.caption(), this.description(), this.originalTable.identifier(),
				this.hiddenAttributeNames);
	}

	@Override
	public boolean atLeastOneAttributeValueMissingFor(int objectId) {
		return atLeastOneAttributeValueMissingFor(objectId, attributes());
	}

	@Override
	public boolean atLeastOneAttributeValueMissingFor(int objectId, List<? extends Attribute<?>> testAttributes) {
		return wrappedTable.atLeastOneAttributeValueMissingFor(objectId, testAttributes);
	}

	@Override
	public List<Attribute<?>> attributes() {
		return this.wrappedTable.attributes();
	}

	@Override
	public Population population() {
		return wrappedTable.population();
	}

	@Override
	public Attribute<?> attribute(int attributeIndex) {
		return attributes().get(attributeIndex);
	}
	
	@Override
	public Optional<? extends Attribute<?>> attribute(Identifier identifier) {
		return wrappedTable.attribute(identifier);
	}

	@Override
	public List<AttributeGroup> attributeGroups() {
		return wrappedTable.attributeGroups();
	}

	@Override
	public Collection<AttributeGroup> attributeGroupsOf(Attribute<?> attribute) {
		return wrappedTable.attributeGroupsOf(attribute);

	}

	@Override
	public boolean containsDependencyBetweenAnyOf(Attribute<?> attribute,
			Collection<? extends Attribute<?>> otherAttributes) {
		return wrappedTable.containsDependencyBetweenAnyOf(attribute, otherAttributes);
	}

	@Override
	public boolean containsDependencyBetween(Attribute<?> attribute, Attribute<?> otherAttribute) {
		return wrappedTable.containsDependencyBetween(attribute, otherAttribute);
	}

	@Override
	public Collection<Entity> dependencies() {
		return ImmutableList.of(originalTable);
	}


}
