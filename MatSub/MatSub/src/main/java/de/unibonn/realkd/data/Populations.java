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
package de.unibonn.realkd.data;

import static com.google.common.base.Preconditions.checkElementIndex;
import static de.unibonn.realkd.common.base.ArrayValues.arrayFromList;
import static de.unibonn.realkd.common.base.IntegerValues.posIntValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.ArrayValues.GenericArray;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.IntegerValues.PositiveIntegerValue;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * Provides static factory methods for the creation of basic entities.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.3.0
 *
 */
public final class Populations {

	public static Population population(Identifier identifier, String name, String description,
			List<String> entityNames) {

		PopulationSerialForm serialForm = new PopulationSerialFormWithNames(identifier, name, description,
				arrayFromList(entityNames));
		return new DefaultPopulation(identifier, name, description, entityNames, serialForm);
	}

	/**
	 * <p>
	 * Creates an anonymous population, i.e., one with object names generated from 0
	 * to m-1, where m is desired number of objects.
	 * </p>
	 * <p>
	 * Like {@link #population(String, String, String, int)} but with default
	 * caption and description.
	 * </p>
	 * 
	 * @param identifier
	 *            identifier of population
	 * @param numberOfObjects
	 *            number of objects/individuals in population
	 * @return population with desired properties
	 * 
	 */
	public static Population population(Identifier identifier, int numberOfObjects) {
		List<String> objectNames = new ArrayList<>();
		for (int j = 0; j < numberOfObjects; j++) {
			objectNames.add(String.valueOf(j));
		}
		return population(identifier, identifier.toString(), "", objectNames);
	}

	/**
	 * Creates an anonymous population, i.e., one with object names generated from 0
	 * to m-1, where m is desired number of objects.
	 * 
	 * @param identifier
	 *            identifier of population
	 * @param name
	 *            name of population
	 * @param description
	 *            description of population
	 * @param numberOfObjects
	 *            number of objects/individuals in population
	 * @return population with desired properties
	 */
	public static Population population(Identifier identifier, String name, String description, int numberOfObjects) {
		List<String> objectNames = new ArrayList<>();
		for (int j = 0; j < numberOfObjects; j++) {
			objectNames.add(String.valueOf(j));
		}

		PopulationSerialForm serialForm = new AnonymousPopulationSerialForm(identifier, name, description,
				posIntValue(numberOfObjects));

		return new DefaultPopulation(identifier, name, description, objectNames, serialForm);
	}

	@KdonTypeName("population")
	public interface PopulationSerialForm extends IdentifiableSerialForm<Population> {
	}

	@KdonTypeName("anonymousPopulation")
	private static class AnonymousPopulationSerialForm implements PopulationSerialForm {

		private final Identifier identifier;

		@JsonProperty("caption")
		private final String caption;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("size")
		private final PositiveIntegerValue size;

		@JsonCreator
		private AnonymousPopulationSerialForm(@JsonProperty("identifier") Identifier identifier,
				@JsonProperty("caption") String name, @JsonProperty("description") String description,
				@JsonProperty("size") PositiveIntegerValue size) {
			this.identifier = identifier;
			this.caption = name;
			this.description = description;
			this.size = size;
		}

		@Override
		@JsonProperty("identifier")
		public Identifier identifier() {
			return identifier;
		}

		@Override
		public String toString() {
			return identifier().toString();
		}

		@Override
		public Population build(Workspace workspace) {
			return population(identifier, caption, description, size.asInt());
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AnonymousPopulationSerialForm)) {
				return false;
			}
			AnonymousPopulationSerialForm otherForm = (AnonymousPopulationSerialForm) other;
			return (this.identifier.equals(otherForm.identifier) && (this.caption.equals(otherForm.caption)))
					&& (this.description.equals(otherForm.description) && (this.size.equals(otherForm.size)));
		}

		@Override
		public int hashCode() {
			return Objects.hash(identifier, caption, description, size);
		}

	}

	@KdonTypeName("populationOfNamedIndividuals")
	private static class PopulationSerialFormWithNames implements PopulationSerialForm {

		private final Identifier identifier;

		@JsonProperty("caption")
		private final String caption;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("names")
		private final GenericArray<String> names;

		@JsonCreator
		private PopulationSerialFormWithNames(@JsonProperty("identifier") Identifier identifier,
				@JsonProperty("caption") String name, @JsonProperty("description") String description,
				@JsonProperty("names") GenericArray<String> names) {
			this.identifier = identifier;
			this.caption = name;
			this.description = description;
			this.names = names;
		}

		@Override
		@JsonProperty("identifier")
		public Identifier identifier() {
			return identifier;
		}

		@Override
		public String toString() {
			return identifier().toString();
		}

		@Override
		public Population build(Workspace workspace) {
			return population(identifier, caption, description, names.elements());
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof PopulationSerialFormWithNames)) {
				return false;
			}
			PopulationSerialFormWithNames otherForm = (PopulationSerialFormWithNames) other;
			return (this.identifier.equals(otherForm.identifier) && (this.caption.equals(otherForm.caption)))
					&& (this.description.equals(otherForm.description) && (this.names.equals(otherForm.names)));
		}

		@Override
		public int hashCode() {
			return Objects.hash(identifier, caption, description, names);
		}

	}

	private static class DefaultPopulation implements Population, HasSerialForm<Population> {

		private final ImmutableList<String> entityNames;

		private final Identifier identifier;

		private final String caption;

		private final String description;

		private final IndexSet indexSet;

		private final PopulationSerialForm serialForm;

		@JsonCreator
		private DefaultPopulation(Identifier identifier, String caption, String description, List<String> entityNames,
				PopulationSerialForm serialForm) {
			this.identifier = identifier;
			this.caption = caption;
			this.description = description;
			this.entityNames = ImmutableList.copyOf(entityNames);
			this.serialForm = serialForm;

			this.indexSet = IndexSets.full(entityNames.size() - 1);
		}

		@Override
		public int size() {
			return entityNames.size();
		}

		@Override
		@JsonProperty
		public Identifier identifier() {
			return identifier;
		}

		@Override
		@JsonProperty
		public String caption() {
			return caption;
		}

		@Override
		public IndexSet objectIds() {
			return indexSet;
		}

		@Override
		@JsonProperty
		public String description() {
			return description;
		}

		@Override
		public String objectName(int id) {
			checkElementIndex(id, size());
			return entityNames.get(id);
		}

		@Override
		@JsonProperty
		public List<String> objectNames() {
			return entityNames;
		}

		public String toString() {
			return identifier() + "::Population";
		}

		@Override
		public PopulationSerialForm serialForm() {
			return serialForm;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultPopulation)) {
				return false;
			}
			DefaultPopulation otherPop = (DefaultPopulation) other;
			return (this.identifier.equals(otherPop.identifier) && this.caption.equals(otherPop.caption)
					&& this.description.equals(otherPop.description) && this.indexSet.equals(otherPop.indexSet)
					&& this.entityNames.equals(otherPop.entityNames));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(identifier);
		}

	}

	private Populations() {
		;
	}

}
