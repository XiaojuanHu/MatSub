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

package de.unibonn.realkd.data.propositions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

public class DefaultTableBasedPropositionalLogic
		implements TableBasedPropositionalContext, HasSerialForm<PropositionalContext> {

	private static class DefaultTableBasedPropositionalLogicSerialForm
			implements IdentifiableSerialForm<PropositionalContext> {

		public final Identifier identifier;

		public final String name;

		public final String description;

		public final Identifier tableIdentifier;

		public final List<Integer> attributeIndices;

		public final List<Constraint<?>> constraints;

		private final ImmutableList<Identifier> dependencies;

		@JsonCreator
		public DefaultTableBasedPropositionalLogicSerialForm(@JsonProperty("identifier") Identifier identifier,
				@JsonProperty("name") String name, @JsonProperty("description") String description,
				@JsonProperty("tableIdentifier") Identifier tableIdentifier,
				@JsonProperty("attributeIndices") List<Integer> attributeIndices,
				@JsonProperty("constraints") List<Constraint<?>> constraints) {
			this.identifier = identifier;
			this.name = name;
			this.description = description;
			this.tableIdentifier = tableIdentifier;
			this.attributeIndices = attributeIndices;
			this.constraints = constraints;
			dependencies = ImmutableList.of(tableIdentifier);
		}

		@Override
		public Identifier identifier() {
			return identifier;
		}

		private <T> AttributeBasedProposition<T> propositionOfIndex(DataTable table, int i) {
			@SuppressWarnings("unchecked")
			Attribute<T> attribute = (Attribute<T>) table.attributes().get(attributeIndices.get(i));
			@SuppressWarnings("unchecked")
			Constraint<T> constraint = (Constraint<T>) constraints.get(i);
			return Propositions.proposition(table, attribute, constraint);
		}

		@Override
		public PropositionalContext build(Workspace workspace) {
			DataTable table = workspace.get(tableIdentifier, DataTable.class).get();
			List<AttributeBasedProposition<?>> props = IntStream.range(0, constraints.size())
					.mapToObj(i -> propositionOfIndex(table, i)).collect(Collectors.toList());
			return new DefaultTableBasedPropositionalLogic(table, props, identifier, name, description);
		}

		public Collection<Identifier> dependencyIds() {
			return dependencies;
		}

	}

	private final DataTable dataTable;

	private final DefaultPropositionalContext propositionalLogic;

	public DefaultTableBasedPropositionalLogic(DataTable dataTable, List<AttributeBasedProposition<?>> propositions) {
		this(dataTable, propositions, Identifier.id("statements_about_" + dataTable.identifier()),
				"Statements about " + dataTable.caption(), "");
	}

	public DefaultTableBasedPropositionalLogic(DataTable dataTable, List<AttributeBasedProposition<?>> propositions,
			Identifier id, String name, String description) {
		this.dataTable = dataTable;
		List<Proposition> plainProps = propositions.stream().map(p -> (Proposition) p).collect(Collectors.toList());
		this.propositionalLogic = new DefaultPropositionalContext(id, name, description, dataTable.population(),
				plainProps);
	}

	@Override
	public List<Proposition> propositions() {
		return propositionalLogic.propositions();
	}

	@Override
	public List<AttributeBasedProposition<?>> getAttributeBasedPropositionsAbout(Attribute<?> attribute) {
		List<AttributeBasedProposition<?>> result = new ArrayList<>();
		for (Proposition prop : this.propositions()) {
			if (((AttributeBasedProposition<?>) prop).attribute() == attribute) {
				result.add((AttributeBasedProposition<?>) prop);
			}
		}
		return result;
	}

	@Override
	public DataTable getDatatable() {
		return dataTable;
	}

	@Override
	public String toString() {
		return propositionalLogic.toString();
	}

	@Override
	public Identifier identifier() {
		return propositionalLogic.identifier();
	}

	@Override
	public String caption() {
		return propositionalLogic.caption();
	}

	@Override
	public String description() {
		return propositionalLogic.description();
	}

	@Override
	public IndexSet supportSet(int basePropositionIndex) {
		return this.propositionalLogic.supportSet(basePropositionIndex);
	}

	@Override
	public Set<Integer> truthSet(int objectId) {
		return this.propositionalLogic.truthSet(objectId);
	}

	@Override
	public Population population() {
		return dataTable.population();
	}

	@Override
	public IdentifiableSerialForm<PropositionalContext> serialForm() {
		List<Integer> attributeIndices = propositionalLogic.propositions().stream()
				.map(p -> ((AttributeBasedProposition<?>) p).attribute()).map(a -> dataTable.attributes().indexOf(a))
				.collect(Collectors.toList());
		List<Constraint<?>> constraints = propositionalLogic.propositions().stream()
				.map(p -> ((AttributeBasedProposition<?>) p).constraint()).collect(Collectors.toList());
		return new DefaultTableBasedPropositionalLogicSerialForm(this.identifier(), this.caption(), this.description(),
				this.dataTable.identifier(), attributeIndices, constraints);
	}

	@Override
	public Optional<Integer> index(Proposition p) {
		return propositionalLogic.index(p);
	}

}
