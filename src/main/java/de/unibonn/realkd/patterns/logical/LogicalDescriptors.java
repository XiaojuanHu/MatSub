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
package de.unibonn.realkd.patterns.logical;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static de.unibonn.realkd.common.IndexSets.difference;
import static de.unibonn.realkd.common.IndexSets.intersection;
import static java.util.Objects.hash;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;

/**
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.2.0
 * 
 * @version 0.6.0
 *
 */
public class LogicalDescriptors {

	public static LogicalDescriptor create(Population population, Collection<Proposition> elements) {
		return create(population, elements, supportSet(population, elements));
	}

	private static LogicalDescriptor create(Population population, Collection<Proposition> elements,
			IndexSet supportSet) {
		Optional<DataTable> table = elements.stream().filter(p -> p instanceof AttributeBasedProposition<?>)
				.map(p -> (AttributeBasedProposition<?>) p).map(AttributeBasedProposition::table).findFirst();
		if (table.isPresent()) {
			return createDefault(table.get(), elements, supportSet);
		}
		return createSimple(population, elements, supportSet);
	}

	/**
	 * <p>
	 * Computes a approximate shortest logical descriptor with identical
	 * extension.
	 * </p>
	 * <p>
	 * This implementation (version 0.4.1) uses the greedy set cover algorithm.
	 * It does not guarantee the solution to be minimal. Therefore it is
	 * followed up by one mapping to the lexicograpically last minimal
	 * generator.
	 * </p>
	 * 
	 * @version 0.4.1
	 * 
	 * @param x
	 *            some logical descriptor
	 * @return logical descriptor describing identical extension as x
	 * 
	 */
	public static LogicalDescriptor approximateShortestGenerator(LogicalDescriptor x) {
		IndexSet toCover = difference(x.population().objectIds(), x.supportSet());
		HashSet<Proposition> candidates = new HashSet<>(x.elements());
		List<Proposition> solution = new ArrayList<>();
		while (!toCover.isEmpty()) {
			Proposition best = null;
			IndexSet smallestDifference = toCover;
			for (Proposition c : candidates) {
				IndexSet supportSet = c.supportSet(); // x.getPropositionalLogic().supportSet(c.getId());
				IndexSet difference = IndexSets.intersection(toCover, supportSet);
				if (difference.size() < smallestDifference.size()) {
					smallestDifference = difference;
					best = c;
				}
			}
			solution.add(best);
			candidates.remove(best);
			toCover = smallestDifference;
		}
		LogicalDescriptor greedyApproximation = create(x.population(), solution, x.supportSet());
		return greedyApproximation.lexicographicallyLastMinimalGenerator();
	}

	/**
	 * computes the support set of the pattern by forming the intersection of
	 * the support sets of all propositions. Note that in the special case of an
	 * empty description, the support set has to be the complete set of objects
	 */
	private static IndexSet supportSet(Population population, Collection<Proposition> elements) {
		if (elements.isEmpty()) {
			return population.objectIds();
		}

		Iterator<Proposition> iterator = elements.iterator();
		IndexSet result = iterator.next().supportSet();
		while (iterator.hasNext()) {
			result = intersection(result, iterator.next().supportSet());
		}
		return result;
	}

	private static LogicalDescriptor createSimple(Population population, Collection<Proposition> elements,
			IndexSet supportSet) {
		return new SimpleLogicalDescriptor(population, canonicalOrder(elements), supportSet);
	}

	private static LogicalDescriptor createDefault(DataTable table, Collection<Proposition> elements,
			IndexSet supportSet) {
		return new LogicalDescriptorDefaultImplementation(table, canonicalOrder(elements), supportSet);
	}

	private static List<Proposition> canonicalOrder(Collection<Proposition> elements) {
		List<Proposition> orderedElements = new ArrayList<>(elements);
		Collections.sort(orderedElements, Comparator.comparing(Proposition::toString));
		return orderedElements;
	}

	private static class SimpleLogicalDescriptor implements LogicalDescriptor {

		private final IndexSet supportSet;

		private final ImmutableSet<Proposition> elements;

		private final Population population;

		private SimpleLogicalDescriptor(Population population, Collection<Proposition> orderedElements,
				IndexSet supportSet) {
			this.population = population;
			this.elements = ImmutableSet.copyOf(orderedElements);
			this.supportSet = supportSet;
		}

		@Override
		public boolean minimal() {
			for (Proposition p : elements) {
				if (generalization(p).supportSet().size() == supportSet().size()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public LogicalDescriptor lexicographicallyLastMinimalGenerator() {
			LogicalDescriptor current = this;
			for (Proposition p : elements) {
				LogicalDescriptor generalization = current.generalization(p);
				if (generalization.supportSet().size() == supportSet().size()) {
					current = generalization;
				}
			}
			return current;
		}

		/**
		 * 
		 * @return number of contained propositions
		 */
		@Override
		public int size() {
			return this.elements.size();
		}

		@Override
		public boolean isEmpty() {
			return this.elements.isEmpty();
		}

		@Override
		public boolean empiricallyImplies(Proposition p) {
			// if (semanticallyImplies(p)) {
			// semImplCount++;
			// return true;
			// }
			return p.supportSet().containsAll(supportSet());
		}

		@Override
		public List<String> getElementsAsStringList() {
			List<String> descriptionList = new ArrayList<>();
			for (Proposition proposition : elements()) {
				descriptionList.add(proposition.toString());
			}

			return descriptionList;
		}

		@Override
		public LogicalDescriptor specialization(Proposition augmentation) {
			List<Proposition> newElements = new ArrayList<>(elements());
			newElements.add(augmentation);
			// Collections.sort(newElements, PROPOSITION_ORDER);

			IndexSet newSupportSet = intersection(supportSet, augmentation.supportSet());
			return create(population, newElements, newSupportSet);
		}

		@Override
		public LogicalDescriptor supportPreservingSpecialization(List<Proposition> augmentations) {
			List<Proposition> newElements = new ArrayList<>(elements());
			boolean augmented = false;
			for (Proposition augmentation : augmentations) {
				if (elements().contains(augmentation)) {
					continue;
				}
				if (augmentation.supportSet().containsAll(supportSet())) {
					// if (propositionalLogic.holdsFor(augmentation.getId(),
					// supportSet())) {
					newElements.add(augmentation);
					augmented = true;
				}
			}
			if (!augmented) {
				return this;
			} else {
				// Collections.sort(newElements, PROPOSITION_ORDER);
				return create(population, newElements, supportSet);
			}
		}

		@Override
		public LogicalDescriptor generalization(Proposition reductionElement) {
			if (!elements().contains(reductionElement)) {
				throw new IllegalArgumentException("reduction element not part of description");
			}
			List<Proposition> newDescription = new ArrayList<>(elements());
			newDescription.remove(reductionElement);
			return create(population, newDescription);

		}

		@Override
		@JsonProperty("elements")
		public ImmutableSet<Proposition> elements() {
			return this.elements;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof LogicalDescriptor))
				return false;

			LogicalDescriptor other = (LogicalDescriptor) o;

			return (this.elements().equals(other.elements()));
		}

		@Override
		public int hashCode() {
			return hash(elements);
		}

		@Override
		public String toString() {
			return elements.toString();
		}

		@Override
		public IndexSet supportSet() {
			return supportSet;
		}

		@Override
		public Population population() {
			return population;
		}

		@Override
		public SerialForm<LogicalDescriptor> serialForm() {
			List<SerialForm<? extends Proposition>> elements = this.elements.stream().map(e -> e.serialForm())
					.collect(toList());
			return new DefaultLogicalDescriptorSerialForm(population.identifier(), elements);
		}

		@Override
		public boolean refersToAttribute(Attribute<?> attribute) {
			for (Proposition proposition : elements()) {
				if (proposition instanceof AttributeBasedProposition
						&& ((AttributeBasedProposition<?>) proposition).attribute() == attribute) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Iterator<Proposition> iterator() {
			return elements.iterator();
		}

	}

	/**
	 * <p>
	 * Implementation that buffers support set on creation; this is likely to be
	 * removed in future versions.
	 * </p>
	 */
	private static class LogicalDescriptorDefaultImplementation extends SimpleLogicalDescriptor
			implements TableSubspaceDescriptor {

		private DataTable dataTable;

		private LogicalDescriptorDefaultImplementation(DataTable dataTable, Collection<Proposition> orderedElements,
				IndexSet supportSet) {
			super(dataTable.population(), orderedElements, supportSet);

			this.dataTable = dataTable;
		}

		@Override
		public DataTable table() {
			return this.dataTable;
		}

		@Override
		public List<Attribute<?>> getReferencedAttributes() {
			List<Attribute<?>> result = new ArrayList<>();
			for (Proposition proposition : elements()) {
				if (proposition instanceof AttributeBasedProposition<?>) {
					result.add(((AttributeBasedProposition<?>) proposition).attribute());
				}
			}
			return result;
		}

		@Override
		public SerialForm<LogicalDescriptor> serialForm() {
			Identifier[] attributes = this.elements().stream().map(e -> (AttributeBasedProposition<?>) e)
					.map(e -> e.attribute().identifier()).toArray(i -> new Identifier[i]);
			Constraint<?>[] constraints = this.elements().stream().map(e -> (AttributeBasedProposition<?>) e)
					.map(e -> e.constraint()).toArray(i -> new Constraint<?>[i]);
			return new AttributeBasedLogicalDescriptorSerialForm(dataTable.identifier(), attributes, constraints);
		}

	}

	public static SerialForm<LogicalDescriptor> attributeBasedLogicalDescriptorSerialForm(Identifier tableId,
			Identifier[] attributes,
			Constraint<?>[] constraints) {
		return new AttributeBasedLogicalDescriptorSerialForm(tableId, attributes, constraints);
	}
	
	@KdonTypeName("attributeBasedConjunction")
	public static class AttributeBasedLogicalDescriptorSerialForm implements SerialForm<LogicalDescriptor> {

		@JsonProperty("table")
		private final Identifier table;

		@JsonProperty("attributes")
		private final Identifier[] attributes;

		@JsonProperty("constraints")
		private final Constraint<?>[] constraints;

		private final ImmutableList<Identifier> dependencies;

		@JsonCreator
		private AttributeBasedLogicalDescriptorSerialForm(@JsonProperty("table") Identifier tableId,
				@JsonProperty("attributes") Identifier[] attributes,
				@JsonProperty("constraints") Constraint<?>[] constraints) {
			this.table = tableId;
			this.attributes = attributes;
			this.constraints = constraints;
			dependencies = ImmutableList.of(table);
		}

		private <T> AttributeBasedProposition<T> propositionOfIndex(DataTable table, int i) {
			@SuppressWarnings("unchecked")
			Attribute<T> attribute = (Attribute<T>) table.attribute(attributes[i]).get();
			@SuppressWarnings("unchecked")
			Constraint<T> constraint = (Constraint<T>) constraints[i];
			return Propositions.proposition(table, attribute, constraint);
		}

		@Override
		public LogicalDescriptor build(Workspace workspace) {
			DataTable tableEntity = workspace.get(table, DataTable.class).get();
			List<Proposition> props = IntStream.range(0, constraints.length)
					.mapToObj(i -> propositionOfIndex(tableEntity, i)).collect(Collectors.toList());
			return createDefault(tableEntity, props, supportSet(tableEntity.population(), props));
		}

		public Collection<Identifier> dependencyIds() {
			return dependencies;
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AttributeBasedLogicalDescriptorSerialForm)) {
				return false;
			}
			AttributeBasedLogicalDescriptorSerialForm that = (AttributeBasedLogicalDescriptorSerialForm) other;
			return (this.table.equals(that.table) && Arrays.equals(this.attributes, that.attributes)
					&& Arrays.equals(this.constraints, that.constraints));
		}
	}

	public static SerialForm<LogicalDescriptor> logicalDescriptorBuilder(Identifier propositionalLogicIdentifier) {
		return new DefaultLogicalDescriptorSerialForm(propositionalLogicIdentifier, newArrayList());
	}

	public static class DefaultLogicalDescriptorSerialForm implements SerialForm<LogicalDescriptor> {

		@JsonProperty("population")
		private final Identifier populationId;

		@JsonProperty("elements")
		private final List<SerialForm<? extends Proposition>> elements;

		@JsonCreator
		public DefaultLogicalDescriptorSerialForm(@JsonProperty("population") Identifier populationId,
				@JsonProperty("elements") List<SerialForm<? extends Proposition>> elements) {
			this.populationId = populationId;
			this.elements = elements;
		}

		@Override
		public LogicalDescriptor build(Workspace context) {
			checkArgument(context.contains(populationId, Population.class),
					"Workspace does not contain artifact '" + populationId + "' of type Population");
			Population populationEntity = context.get(populationId, Population.class).get();
			List<Proposition> elements = this.elements.stream().map(e -> e.build(context)).collect(toList());
			return create(populationEntity, elements);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return ImmutableSet.of(this.populationId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof DefaultLogicalDescriptorSerialForm)) {
				return false;
			}
			DefaultLogicalDescriptorSerialForm other = (DefaultLogicalDescriptorSerialForm) obj;
			return this.populationId.equals(other.populationId) && this.elements.equals(other.elements);
		}
	}

	@Deprecated
	public static class LogicalDescriptorBuilderDefaultImplementation implements SerialForm<LogicalDescriptor> {

		private final Identifier propositionalLogicIdentifier;

		private final ArrayList<Integer> elementIndices;

		@JsonCreator
		public LogicalDescriptorBuilderDefaultImplementation(
				@JsonProperty("propositionalLogicIdentifier") Identifier propositionalLogicIdentifier,
				@JsonProperty("elementIndexList") List<Integer> elementIndices) {
			this.propositionalLogicIdentifier = propositionalLogicIdentifier;
			this.elementIndices = Lists.newArrayList(elementIndices);
		}

		@Override
		public LogicalDescriptor build(Workspace context) {
			checkArgument(context.contains(propositionalLogicIdentifier, PropositionalContext.class),
					"Workspace does not contain artifact '" + propositionalLogicIdentifier
							+ "' of type PropositionalLogic");
			PropositionalContext propositionalLogic = (PropositionalContext) context.get(propositionalLogicIdentifier);
			List<Proposition> propositions = elementIndices.stream().map(i -> propositionalLogic.propositions().get(i))
					.collect(Collectors.toList());
			return create(propositionalLogic.population(), propositions);
		}

		public List<Integer> getElementIndexList() {
			return elementIndices;
		}

		public Identifier getPropositionalLogicIdentifier() {
			return propositionalLogicIdentifier;
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return ImmutableSet.of(getPropositionalLogicIdentifier());
		}

		// public void propositionalLogicIdentifier(String identifier) {
		// this.propositionalLogicIdentifier = identifier;
		// }

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof LogicalDescriptorBuilderDefaultImplementation)) {
				return false;
			}
			LogicalDescriptorBuilderDefaultImplementation otherDescriptor = (LogicalDescriptorBuilderDefaultImplementation) other;
			return (this.elementIndices.equals(otherDescriptor.elementIndices)
					&& this.propositionalLogicIdentifier.equals(otherDescriptor.propositionalLogicIdentifier));
		}

	}

}
