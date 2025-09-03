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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.common.IndexSets.copyOf;
import static de.unibonn.realkd.data.constraints.Constraints.lowerQuantileBound;
import static de.unibonn.realkd.data.constraints.Constraints.lowerQuantileBoundNegation;
import static de.unibonn.realkd.data.constraints.Constraints.upperQuantileBound;
import static de.unibonn.realkd.data.constraints.Constraints.upperQuantileBoundNegation;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * Factory methods for the creation of propositional logics.
 * 
 * @author Mario Boley
 * 
 * @since 0.5.1
 * 
 * @version 0.5.1
 *
 */
public class Propositions {

	private Propositions() {
		; // not to be instantiated
	}

	/**
	 * 
	 * @param table
	 *            the input table
	 * @return propositional logic for table based on default attribute to
	 *         proposition mapping scheme
	 */
	public static TableBasedPropositionalContext propositionalContext(DataTable table) {
		return new PropositionalContextFromTableBuilder().apply(table);
	}

	/**
	 * @param result
	 * @param ordinal
	 * @param c
	 */
	public static <T> void addLowerQuantileBoundBasedPropositions(DataTable table,
			List<AttributeBasedProposition<?>> result, OrdinalAttribute<T> ordinal, double c) {
		T threshold = ordinal.quantile(c);
		result.add(Propositions.proposition(table, ordinal, lowerQuantileBound(ordinal, threshold)));
		result.add(Propositions.proposition(table, ordinal, lowerQuantileBoundNegation(ordinal, threshold)));
	}

	/**
	 * @param result
	 * @param ordinal
	 * @param c
	 */
	public static <T> void addUpperQuantileBoundBasedPropositions(DataTable table,
			List<AttributeBasedProposition<?>> result, OrdinalAttribute<T> ordinal, double c) {
		T threshold = ordinal.quantile(c);
		result.add(Propositions.proposition(table, ordinal, upperQuantileBoundNegation(ordinal, threshold)));
		result.add(Propositions.proposition(table, ordinal, upperQuantileBound(ordinal, threshold)));
	}

	public static Proposition proposition(int id, IndexSet supportSet) {
		return new SetBackedProposition(id, supportSet);
	}

	public static Proposition proposition(int id, String name, IndexSet supportSet) {
		return new SetBackedProposition(id, name, supportSet);
	}

	private static class SetBackedPropositionSerialForm implements SerialForm<Proposition> {

		@JsonProperty("id")
		private Integer id;

		@JsonProperty("name")
		private String name;

		@JsonProperty("supportSet")
		private List<Integer> supportSet;

		public SetBackedPropositionSerialForm(@JsonProperty("id") Integer id, @JsonProperty("name") String name,
				@JsonProperty("supportSet") List<Integer> supportSet) {
			this.id = id;
			this.name = name;
			this.supportSet = supportSet;
		}

		@Override
		public Proposition build(Workspace workspace) {
			return Propositions.proposition(this.id, this.name, IndexSets.copyOf(this.supportSet));
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SetBackedPropositionSerialForm)) {
				return false;
			}
			SetBackedPropositionSerialForm that = (SetBackedPropositionSerialForm) other;
			return this.name.equals(that.name) && this.id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, id);
		}

	}

	/**
	 * @author Mario Boley
	 * 
	 * @since 0.2.0
	 * 
	 * @version 0.6.0
	 *
	 */
	public static class SetBackedProposition implements Proposition {

		private final int id;

		private final IndexSet supportSet;

		private final String name;

		SetBackedProposition(int id, IndexSet supportSet) {
			this.id = id;
			this.name = String.valueOf(id);
			this.supportSet = supportSet;
		}

		SetBackedProposition(int id, String name, IndexSet supportSet) {
			this.id = id;
			this.name = name;
			this.supportSet = supportSet;
		}

		@Override
		public boolean holdsFor(int i) {
			return supportSet.contains(i);
		}

		public int getId() {
			return id;
		}

		@Override
		public IndexSet supportSet() {
			return supportSet;
		}

		@Override
		public int supportCount() {
			return supportSet.size();
		}

		public String toString() {
			return this.name;
		}

		@Override
		public String name() {
			return name;
		}

		private static List<Integer> convertToList(IndexSet supportSet) {
			return StreamSupport.stream(supportSet.spliterator(), false).collect(Collectors.toList());
		}

		@Override
		public SerialForm<? extends Proposition> serialForm() {
			return new SetBackedPropositionSerialForm(id, name, convertToList(supportSet));
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SetBackedProposition)) {
				return false;
			}
			SetBackedProposition that = (SetBackedProposition) other;
			return this.name.equals(that.name) && this.id == that.id;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, id);
		}

	}

	public static <T> AttributeBasedProposition<T> proposition(DataTable table, Attribute<? extends T> attribute,
			Constraint<T> constraint) {
		return new DefaultAttributeBasedProposition<T>(table, attribute, constraint);
	}

	private static class DefaultAttributeBasedProposition<T> implements AttributeBasedProposition<T> {

		private final DataTable table;

		private final Attribute<? extends T> attribute;

		private final Constraint<T> constraint;

		private final String name;

		private IndexSet supportSet;

		DefaultAttributeBasedProposition(DataTable table, Attribute<? extends T> attribute, Constraint<T> constraint) {
			this.table = table;
			this.attribute = attribute;
			this.constraint = constraint;
			this.name = attribute.caption() + constraint.suffixNotationName();
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public Attribute<? extends T> attribute() {
			return attribute;
		}

		@Override
		public Constraint<T> constraint() {
			return constraint;
		}

		@Override
		public boolean implies(Proposition anotherProposition) {
			if (!(anotherProposition instanceof AttributeBasedProposition<?>)) {
				return false;
			}
			if (this.attribute() == ((AttributeBasedProposition<?>) anotherProposition).attribute()) {
				// following cast is safe because propositons refer to same
				// attribute
				@SuppressWarnings("unchecked")
				Constraint<T> otherConstraint = (Constraint<T>) ((AttributeBasedProposition<T>) anotherProposition)
						.constraint();
				return this.constraint.implies(otherConstraint);
			}
			return false;
		}

		/**
		 * Buffers the support set after first request and uses {@link ImmutableSet} as
		 * return type.
		 * 
		 */
		@Override
		public IndexSet supportSet() {
			// TODO should use Lazy<IndexSet> here
			if (supportSet == null) {
				List<Integer> result = new ArrayList<>();
				for (int i = 0; i <= attribute.maxIndex(); i++) {
					if (holdsFor(i)) {
						result.add(i);
					}
				}
				supportSet = copyOf(result);
			}

			return supportSet;
		}

		@Override
		public int supportCount() {
			return supportSet().size();
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public SerialForm<? extends AttributeBasedProposition<T>> serialForm() {
			return new DefaultAttributeBasedPropositionSerialForm<>(table.identifier(), attribute.identifier(), constraint);
		}

		@Override
		public DataTable table() {
			return table;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultAttributeBasedProposition<?>)) {
				return false;
			}
			DefaultAttributeBasedProposition<?> that = (DefaultAttributeBasedProposition<?>) other;
			return (this.attribute.equals(that.attribute) && this.constraint.equals(that.constraint));
		}

		@Override
		public int hashCode() {
			return hash(attribute, constraint);
		}

	}

	private static class DefaultAttributeBasedPropositionSerialForm<T>
			implements SerialForm<AttributeBasedProposition<T>> {

		@JsonProperty("tableId")
		private final Identifier tableId;

		@JsonProperty("attributeId")
		private final Identifier attributeId;

		private final List<Identifier> dependencyIds;

		@JsonProperty("constraint")
		private final Constraint<T> constraint;

		@JsonCreator
		public DefaultAttributeBasedPropositionSerialForm(@JsonProperty("tableId") Identifier tableId,
				@JsonProperty("attributeId") Identifier attributeId,
				@JsonProperty("constraint") Constraint<T> constraint) {
			this.tableId = tableId;
			this.attributeId = attributeId;
			this.constraint = constraint;
			this.dependencyIds = ImmutableList.of(tableId);
		}

		@Override
		public AttributeBasedProposition<T> build(Workspace workspace) {
			DataTable table = workspace.get(tableId, DataTable.class).get();
			@SuppressWarnings("unchecked")
			Attribute<? extends T> attribute = (Attribute<? extends T>) table.attribute(attributeId).get();
			return Propositions.proposition(table, attribute, constraint);
		}

		@Override
		public List<Identifier> dependencyIds() {
			return dependencyIds;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultAttributeBasedPropositionSerialForm<?>)) {
				return false;
			}
			DefaultAttributeBasedPropositionSerialForm<?> that = (DefaultAttributeBasedPropositionSerialForm<?>) other;
			return (this.tableId.equals(that.tableId) && this.attributeId.equals(that.attributeId)
					&& this.constraint.equals(that.constraint));
		}

		@Override
		public int hashCode() {
			return hash(tableId, attributeId, constraint);
		}

	}

	/**
	 * Proposition predicate that evaluates to true if input proposition does not
	 * relate to any of a collection of provided attributes.
	 * 
	 * @return a predicate for propositions that is false if and only if proposition
	 *         relates to at least one of a set of target attributes
	 *         
	 */
	public static Predicate<Proposition> isNotRelatedTo(DataTable dataTable, Collection<? extends Attribute<?>> attributes) {
		return p -> {
			if (!(p instanceof AttributeBasedProposition)) {
				return true;
			}
	
			Attribute<?> attribute = ((AttributeBasedProposition<?>) p).attribute();
			if (attributes.contains(attribute) || dataTable.containsDependencyBetweenAnyOf(attribute, attributes)) {
				return false;
			}
			return true;
		};
	}

}
