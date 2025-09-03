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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.hash;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.Propositions.SetBackedProposition;
import de.unibonn.realkd.data.sequences.SequentialPropositionalContext;

/**
 * Implementation of a descriptor for a sequential list of proposition lists.
 * The ordering in the list imposes a predecessor relation either in time
 * or in distance, compared to the following lists. That is the propositions
 * in the first list, are earlier in time / closer in distance than the
 * propositions in the second list, etc. 
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.7.0
 
 */
public class DefaultSequenceDescriptor implements SequenceDescriptor {
	
	public static SequenceDescriptor create(SequentialPropositionalContext sequenceDatabase){ 
		return new DefaultSequenceDescriptor(sequenceDatabase, newArrayList());
	}
	
	public static SequenceDescriptor create(SequentialPropositionalContext sequenceDatabase, List<List<Proposition>> orderedSetsDescriptors){ 
		return new DefaultSequenceDescriptor(sequenceDatabase, orderedSetsDescriptors);
	}
	
	private SequentialPropositionalContext sequentialPropositionalLogic;
	private List<List<Proposition>> orderedSetsDescriptors;

	private DefaultSequenceDescriptor(SequentialPropositionalContext sequentialPropositionalLogic, List<List<Proposition>> orderedSetsDescriptors) {
		this.sequentialPropositionalLogic = sequentialPropositionalLogic;
		this.orderedSetsDescriptors = orderedSetsDescriptors;
	}
	
	@Override
	public SequentialPropositionalContext sequentialPropositionalLogic() {
		return this.sequentialPropositionalLogic;
	}

	@Override
	public List<List<Proposition>> orderedSets() {
		return orderedSetsDescriptors;
	}
	
	public String toString() {
		return String.join(" > ", orderedSetsDescriptors.stream().map(o -> o.toString()).collect(toList()));
	}
	
	@Override
	public int hashCode() {
		return hash(this.orderedSetsDescriptors);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DefaultSequenceDescriptor)) {
			return false;
		}
		
		DefaultSequenceDescriptor other = (DefaultSequenceDescriptor) obj;
		return this.orderedSetsDescriptors.equals(other.orderedSetsDescriptors);
	}

	@Override
	public SerialForm<SequenceDescriptor> serialForm() {
		List<List<Integer>> propositionIds = this.orderedSetsDescriptors.stream().map(o ->
			o.stream().map(p -> ((SetBackedProposition) p).getId()).collect(Collectors.toList())
		).collect(Collectors.toList());
		
		return new SetBackedSequenceDescriptorBuilder(this.sequentialPropositionalLogic.identifier(), propositionIds);
	}
	
//	/**
//	 * Creates a new sequence descriptor builder with one empty proposition list.
//	 * 
//	 * @param propositionalLogicIdentifier
//	 * 			the identifier of the propositional logic
//	 * 
//	 * @return a new sequence descriptor builder
//	 */
	public static SerialForm<SequenceDescriptor> defaultSequenceDescriptorBuilder(Identifier propositionalLogicIdentifier) {
		return new DefaultSequenceDescriptorBuilder(propositionalLogicIdentifier,
				newArrayList());
	}
	
	public static SerialForm<SequenceDescriptor> defaultSequenceDescriptorBuilder(String propositionalLogicIdentifier, 
			List<List<Integer>> orderedSetBuilders) {
//		return new DefaultSequenceDescriptorBuilder(propositionalLogicIdentifier,
//				orderedSetBuilders);
		return null;
	}
	
	private static class DefaultSequenceDescriptorBuilder implements SerialForm<SequenceDescriptor> {

		@JsonProperty("propositionalLogicIdentifier")
		private Identifier propositionalLogicIdentifier;
		
		@JsonProperty("orderedSequences")
		private List<List<SerialForm<? extends Proposition>>> orderedSequences;
		
		@JsonCreator
		public DefaultSequenceDescriptorBuilder(
				@JsonProperty("propositionalLogicIdentifier") Identifier propositionalLogicIdentifier,
				@JsonProperty("orderedSequences") List<List<SerialForm<? extends Proposition>>> orderedSequences) {
			this.propositionalLogicIdentifier = propositionalLogicIdentifier;
			this.orderedSequences = orderedSequences;
		}

		@Override
		public SequenceDescriptor build(Workspace context) {
			SequentialPropositionalContext sequentialPropositionalLogic = (SequentialPropositionalContext) context.get(propositionalLogicIdentifier);
			
			List<List<Proposition>> sequences = newArrayList();
			for(List<SerialForm<? extends Proposition>> orderedSequence: orderedSequences) {
				sequences.add(orderedSequence.stream().map(e -> e.build(context)).collect(toList()));
			}

			return new DefaultSequenceDescriptor(sequentialPropositionalLogic, sequences);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			Set<Identifier> dependencies = Sets.newHashSet();
//			orderedSetBuilders.stream().forEach(o -> dependencies.addAll(o.dependencyIds()));			
			return Sets.union(ImmutableSet.of(propositionalLogicIdentifier), dependencies);
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof DefaultSequenceDescriptorBuilder)) {
				return false;
			}
			DefaultSequenceDescriptorBuilder other = (DefaultSequenceDescriptorBuilder) obj;
			return this.propositionalLogicIdentifier.equals(other.propositionalLogicIdentifier) && this.orderedSequences.equals(other.orderedSequences);
		}
		
	}
	
//	private static class AttributeBasedSequenceDescriptorBuilder implements SerialForm<SequenceDescriptor> {
//		
//		@JsonProperty("table")
//		private Identifier table;
//		
//		@JsonProperty("propositionalLogicIdentifier")
//		private Identifier propositionalLogicIdentifier;
//		
//		@JsonProperty("attributes")
//		private List<List<Identifier>> attributes;
//		
//		@JsonProperty("constraints")
//		private List<List<Constraint<?>>> constraints;
//		
//		@JsonCreator
//		public AttributeBasedSequenceDescriptorBuilder(
//				@JsonProperty("table") Identifier table,
//				@JsonProperty("propositionalLogicIdentifier") Identifier propositionalLogicIdentifier,
//				@JsonProperty("attributes") List<List<Identifier>> attributes,
//				@JsonProperty("constraints") List<List<Constraint<?>>> constraints) {
//			this.table = table;
//			this.propositionalLogicIdentifier = propositionalLogicIdentifier;
//			this.attributes = attributes;
//			this.constraints = constraints;
//		}
//		
//		@Override
//		public SequenceDescriptor build(Workspace workspace) {
//			DataTable dataTable = workspace.get(table, DataTable.class).get();
//			SequentialPropositionalContext sequentialPropositionalLogic = workspace.get(propositionalLogicIdentifier, SequentialPropositionalContext.class).get();
//			
//			List<List<Proposition>> sequences = getOrderedSets(dataTable);
//		
//			return new DefaultSequenceDescriptor(sequentialPropositionalLogic, sequences);
//		}
//		
//		private <T> AttributeBasedProposition<T> propositionOfIndex(DataTable table, int i, int j) {
//			@SuppressWarnings("unchecked")
//			Attribute<T> attribute = (Attribute<T>) table.attribute(attributes.get(i).get(j)).get();
//			@SuppressWarnings("unchecked")
//			Constraint<T> constraint = (Constraint<T>) constraints.get(i).get(j);
//			return Propositions.proposition(table, attribute, constraint);
//		}
//		
//		private List<List<Proposition>> getOrderedSets(DataTable dataTable) {
//			List<List<Proposition>> orderedSets = newArrayList();
//			
//			for(int i = 0; i < attributes.size(); i++) { 
//				int sI = i;
//				orderedSets.add(IntStream.range(0, constraints.get(i).size())
//						.mapToObj(j -> propositionOfIndex(dataTable, sI, j)).collect(Collectors.toList()));
//			}
//			
//			return orderedSets;
//		}
//
//		@Override
//		public Collection<Identifier> dependencyIds() {
//			return ImmutableList.of(table, propositionalLogicIdentifier);
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (!(obj instanceof AttributeBasedSequenceDescriptorBuilder)) {
//				return false;
//			}
//			AttributeBasedSequenceDescriptorBuilder other = (AttributeBasedSequenceDescriptorBuilder) obj;
//			return this.table.equals(other.table) && this.propositionalLogicIdentifier.equals(other.propositionalLogicIdentifier)
//					&& this.attributes.equals(other.attributes) && this.constraints.equals(other.constraints);
//		}
//		
//	}
	
	private static class SetBackedSequenceDescriptorBuilder implements SerialForm<SequenceDescriptor> {
		
		@JsonProperty("propositionalLogicIdentifier")
		private Identifier propositionalLogicIdentifier;
		
		@JsonProperty("propositionIds")
		private List<List<Integer>> propositionIds;
		
		@JsonProperty("constraints")
		private List<List<Constraint<?>>> constraints;
		
		@JsonCreator
		public SetBackedSequenceDescriptorBuilder(
				@JsonProperty("propositionalLogicIdentifier") Identifier propositionalLogicIdentifier,
				@JsonProperty("attributes") List<List<Integer>> propositionIds) {
			this.propositionalLogicIdentifier = propositionalLogicIdentifier;
			this.propositionIds = propositionIds;
		}
		
		@Override
		public SequenceDescriptor build(Workspace workspace) {
			SequentialPropositionalContext sequentialPropositionalLogic = workspace.get(propositionalLogicIdentifier, SequentialPropositionalContext.class).get();
			
			List<List<Proposition>> sequences = getOrderedSets(sequentialPropositionalLogic);
			
			return new DefaultSequenceDescriptor(sequentialPropositionalLogic, sequences);
		}
		
		private List<List<Proposition>> getOrderedSets(SequentialPropositionalContext sequentialPropositionalLogic) {
			return propositionIds.stream().map(l -> 
				l.stream().map(i -> sequentialPropositionalLogic.proposition(i)).collect(Collectors.toList())
			).collect(Collectors.toList());
		}
		
		@Override
		public Collection<Identifier> dependencyIds() {
			return ImmutableList.of(propositionalLogicIdentifier);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof SetBackedSequenceDescriptorBuilder)) {
				return false;
			}
			SetBackedSequenceDescriptorBuilder other = (SetBackedSequenceDescriptorBuilder) obj;
			return this.propositionalLogicIdentifier.equals(other.propositionalLogicIdentifier)
					&& this.propositionIds.equals(other.propositionIds);
		}
		
	}
	
}
