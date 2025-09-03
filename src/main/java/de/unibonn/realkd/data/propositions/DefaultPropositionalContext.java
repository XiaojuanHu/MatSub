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
package de.unibonn.realkd.data.propositions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;

/**
 * @author Mario Boley
 * 
 * @since 0.2.0
 * 
 * @version 0.3.0
 *
 */
public class DefaultPropositionalContext implements PropositionalContext, HasSerialForm<PropositionalContext> {

	private static class DefaultPropositionalContextSerialForm implements IdentifiableSerialForm<PropositionalContext> {

		@JsonProperty("identifier")
		private final Identifier identifier;

		@JsonProperty("name")
		private final String name;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("propositions")
		private final List<SerialForm<? extends Proposition>> propositionSerialForms;

		private final ImmutableList<Identifier> dependencies;

		@JsonProperty("population")
		private Population population;

		@JsonCreator
		public DefaultPropositionalContextSerialForm(@JsonProperty("identifier") Identifier identifier,
				@JsonProperty("name") String name, @JsonProperty("description") String description,
				@JsonProperty("population") Population population,
				@JsonProperty("propositions")  List<SerialForm<? extends Proposition>> propositionSerialForms) {
			this.identifier = identifier;
			this.name = name;
			this.description = description;
			this.population = population;
			this.propositionSerialForms = propositionSerialForms;
			this.dependencies = ImmutableList.of();
		}

		@Override
		public Identifier identifier() {
			return identifier;
		}

		public PropositionalContext build(Workspace workspace) {
			List<Proposition> propositions = this.propositionSerialForms.stream()
					.map(p -> p.build(workspace))
					.collect(Collectors.toList());
			return new DefaultPropositionalContext(this.identifier, this.name, this.description, this.population, propositions);
		}

		public Collection<Identifier> dependencyIds() {
			return dependencies;
		}

	}

	// private static final Logger LOGGER = Logger
	// .getLogger(DefaultPropositionalLogic.class.getName());

	private final ImmutableList<Proposition> propositions;

	private final Map<Proposition, Integer> propToIndex;

	private final Set<Integer>[] truthSets;

	private final String name;

	private final Identifier id;

	private final String description;

	private final Population population;

	@SuppressWarnings("unchecked")
	public DefaultPropositionalContext(Identifier id, String name, String description, Population population,
			List<Proposition> propositions) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.population = population;
		this.propositions = ImmutableList.copyOf(propositions);
		this.truthSets = ((Set<Integer>[]) new Set[population.size()]);
		this.propToIndex = new HashMap<>(propositions.size());
		for (int i = 0; i < propositions.size(); i++) {
			propToIndex.put(propositions.get(i), i);
		}
	}

	public DefaultPropositionalContext(String name, String description, Population population,
			List<Proposition> propositions) {
		this(Identifier.id(name), name, description, population, propositions);
	}

	@Override
	public List<Proposition> propositions() {
		return propositions;
	}

	@Override
	public String toString() {
		return caption();
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public IndexSet supportSet(int basePropositionIndex) {
		return propositions.get(basePropositionIndex).supportSet();
	}

	@Override
	public Set<Integer> truthSet(int objectId) {
		if (truthSets[objectId] == null) {
			List<Integer> list = range(0, propositions.size())
					.filter(i -> propositions.get(i).holdsFor(objectId)).mapToObj(Integer::valueOf)
					.collect(toList());
			truthSets[objectId] = ImmutableSet.copyOf(list);
		}
		return truthSets[objectId];
	}

	@Override
	public Population population() {
		return population;
	}

	@Override
	public SerialForm<? extends PropositionalContext> serialForm() {
		List<SerialForm<? extends Proposition>> propositionSerialForms = this.propositions.stream()
				.map(p -> p.serialForm())
				.collect(Collectors.toList());

		return new DefaultPropositionalContextSerialForm(this.id, this.name, this.description, this.population,
				propositionSerialForms);
	}

	@Override
	public Optional<Integer> index(Proposition p) {
		return Optional.ofNullable(propToIndex.get(p));
	}

}
