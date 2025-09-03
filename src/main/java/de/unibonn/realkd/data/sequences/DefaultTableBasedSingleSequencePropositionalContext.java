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
package de.unibonn.realkd.data.sequences;

import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.data.sequences.SequenceEvents.newSequenceEvent;
import static java.util.stream.Collectors.toList;

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

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.Populations;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.DataTable;

/**
 * Sequences of binary statements about data objects 
 * 
 * @author Sandy Moens
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class DefaultTableBasedSingleSequencePropositionalContext implements TableBasedSingleSequencePropositionalContext, HasSerialForm<SingleSequencePropositionalContext> {

	public static SingleSequencePropositionalContext singleSequencePropositionalContext(DataTable dataTable, List<SequenceEvent<?>> events, List<Proposition> propositions) {
			return new DefaultTableBasedSingleSequencePropositionalContext(dataTable, events, propositions);
	}
	
	@KdonTypeName("tableBasedSingleSequencePropositionContext")
	private static class DefaultSingleSequencePropositionalContextSerialForm implements IdentifiableSerialForm<SingleSequencePropositionalContext> {

		public final Identifier identifier;

		public final String name;

		public final String description;
		
		public final Identifier tableIdentifier;

		public final List<SerialForm<? extends Proposition>> propositions;

		public final List<SequenceEventSerialForm<?>> events;
		
		private final ImmutableList<Identifier> dependencies;

		@JsonCreator
		public DefaultSingleSequencePropositionalContextSerialForm(@JsonProperty("identifier") Identifier identifier,
				@JsonProperty("name") String name, @JsonProperty("description") String description,
				@JsonProperty("tableIdentifier") Identifier tableIdentifier,
				@JsonProperty("propositions") List<SerialForm<? extends Proposition>> propositions,
				@JsonProperty("sequences") List<SequenceEventSerialForm<?>> events) {
			this.identifier = identifier;
			this.name = name;
			this.description = description;
			this.tableIdentifier = tableIdentifier;
			this.propositions = propositions;
			this.dependencies = ImmutableList.of(tableIdentifier);
			this.events = events;
		}

		@Override
		public Identifier identifier() {
			return identifier;
		}

		@Override
		public SingleSequencePropositionalContext build(Workspace workspace) {
			DataTable table = workspace.get(tableIdentifier, DataTable.class).get();
			List<Proposition> props = this.propositions.stream().map(p -> p.build(workspace)).collect(Collectors.toList());
			List<SequenceEvent<?>> sequences = this.events.stream().map(s -> {
				Comparable<?> value = (Comparable<?>)s.orderValue;
				Proposition proposition = props.get(s.element);
				return newSequenceEvent(value, proposition);}
					).collect(toList());
			return new DefaultTableBasedSingleSequencePropositionalContext(table, identifier, name, description, sequences, props);
		}

		public Collection<Identifier> dependencyIds() {
			return dependencies;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof DefaultSingleSequencePropositionalContextSerialForm)) {
				return false;
			}
			DefaultSingleSequencePropositionalContextSerialForm otherBuilder = (DefaultSingleSequencePropositionalContextSerialForm) other;
			return (this.identifier.equals(otherBuilder.identifier)
					&& this.name.equals(otherBuilder.name)
					&& this.description.equals(otherBuilder.description)
					&& this.propositions.equals(otherBuilder.propositions)
					&& this.events.equals(otherBuilder.events));
		}

	}
	
	private static class SequenceEventSerialForm<T extends Comparable<?>> {
	
		@JsonProperty("orderValue")
		private T orderValue;
		
		@JsonProperty("element")
		private Integer element;

		private SequenceEventSerialForm(@JsonProperty("orderValue") T orderValue, @JsonProperty("element") Integer element) {
			this.orderValue = orderValue;
			this.element = element;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SequenceEventSerialForm)) {
				return false;
			}
			SequenceEventSerialForm<?> that = (SequenceEventSerialForm<?>) other;
			return this.orderValue.equals(that.orderValue) && this.element.equals(that.element);
		}

	}
	
	private final DataTable dataTable;
	private final Identifier identifier;
	private final String name;
	private final String description;
	private final List<SequenceEvent<?>> events;
	private final List<Proposition> propositions;
	private final Map<Proposition, Integer> propToIndex;
	
	private DefaultTableBasedSingleSequencePropositionalContext(DataTable dataTable, List<SequenceEvent<?>> events, List<Proposition> propositions) {
		this(dataTable, Identifier.id("single_sequence_for_statements_about_" + dataTable.identifier()), "Single sequence for statements about " + dataTable.caption(),
				"Single sequence database for data table " + dataTable.caption(), events, propositions);
	}
	
	private DefaultTableBasedSingleSequencePropositionalContext(DataTable dataTable, Identifier identifier, String name, String description, List<SequenceEvent<?>> events, List<Proposition> propositions) {
		this.dataTable = dataTable;
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.events = ImmutableList.copyOf(events);
		this.propositions = ImmutableList.copyOf(propositions);
		this.propToIndex = new HashMap<>(propositions.size());
		for (int i = 0; i < propositions.size(); i++) {
			propToIndex.put(propositions.get(i), i);
		}
	}
	
	public Identifier identifier() {
		return this.identifier;
	}
	
	@Override
	public String caption() {
		return this.name;
	}

	@Override
	public String description() {
		return this.description;
	}

	@Override
	public List<SequenceEvent<?>> events() {
		return this.events;		
	}

	@Override
	public List<Proposition> propositions() {
		return this.propositions;
	}

	@Override
	public Population population() {
		return Populations.population(Identifier.id("Sequences"), 1);
	}

	@Override
	public IndexSet supportSet(int basePropositionIndex) {
		//TODO check why this is empty
		return IndexSets.empty();
	}

	@Override
	public Set<Integer> truthSet(int objectId) {
		return newHashSet();
	}

	@Override
	public IdentifiableSerialForm<SingleSequencePropositionalContext> serialForm() {
		List<SerialForm<? extends Proposition>> propositions = this.propositions.stream().map(p -> p.serialForm()).collect(toList());
		List<SequenceEventSerialForm<?>> sequencesSer = this.events.stream().map(s -> {
			Optional<Integer> element = this.index(s.proposition());
			return element.isPresent() ? 
				new SequenceEventSerialForm<Comparable<?>>(s.value(), element.get()): null;
		}).collect(toList());
		
		return new DefaultSingleSequencePropositionalContextSerialForm(this.identifier, this.name, this.description, this.dataTable.identifier(), propositions, sequencesSer);
	}

	@Override
	public Optional<Integer> index(Proposition p) {
		return Optional.ofNullable(propToIndex.get(p));
	}

	@Override
	public DataTable datatable() {
		return dataTable;
	}


}