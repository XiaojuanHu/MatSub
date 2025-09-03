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
package de.unibonn.realkd.data.sequences;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.data.sequences.SequenceEvents.newSequenceEvent;
import static de.unibonn.realkd.data.sequences.SequenceTransactions.newSequenceTransaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.propositions.PropositionalizationRule;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 *
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.2
 * 
 * @version 0.7.0
 *
 */
public class SequentialPropositionalContextFromTableFactory implements ParameterContainer {

	private static final Logger LOGGER = Logger.getLogger(PropositionalContextFromTableBuilder.class.getName());

	private SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeFactories;

	private String groupingAttributeName;
	private String distanceAttributeName;

	public SequentialPropositionalContextFromTableFactory() {
		this.attributeFactories = Parameters.subSetParameter(Identifier.identifier("Attribute_mappers"),
				"Attribute mappers",
				"Mappers that are used to create binary propositions for pattern mining from the data table",
				() -> PropositionalContextFromTableBuilder.ALL_MAPPERS,
				() -> PropositionalContextFromTableBuilder.DEFAULT_MAPPERS);

		this.distanceAttributeName = "id";
		this.distanceAttributeName = "date";
	}

	public List<Parameter<?>> getTopLevelParameters() {
		return ImmutableList.of(this.attributeFactories);
	}

	/**
	 * 
	 * @return the externally accessible parameter of the attribute mappers
	 */
	public SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeMapperParamater() {
		return attributeFactories;
	}

	/**
	 * 
	 * @return the current set of attribute to propositions mappers to be used for
	 *         creating propositional logics from table
	 */
	public Set<PropositionalizationRule> mappers() {
		return attributeFactories.current();
	}

	/**
	 * 
	 * @param mappers
	 *            the collection of mappers to be used for creating propositional
	 *            logics from table
	 */
	public void mappers(Set<PropositionalizationRule> mappers) {
		attributeFactories.set(mappers);
	}

	/**
	 * Sets the name of the attribute that is used for grouping sequences
	 * 
	 * @param groupingAttributeName
	 *            the name of the grouping attribute in the data table
	 */
	public void groupingAttributeName(String groupingAttributeName) {
		this.groupingAttributeName = groupingAttributeName;
	}

	/**
	 * Sets the name of the attribute that is used for annotating the distance
	 * between events in a sequences
	 * 
	 * @param distanceAttributeName
	 *            the name of the distance attribute in the data table
	 */
	public void distanceAttributeName(String distanceAttributeName) {
		this.distanceAttributeName = distanceAttributeName;
	}
	
	public SequentialPropositionalContext build(DataTable dataTable) {
		List<Proposition> propositions = compilePropositions(dataTable);
		List<SequenceTransaction> sequences = compileSequences(dataTable, propositions);
		
		propositions = recompilePropositions(propositions, sequences);
		sequences = recompileSequences(propositions, sequences);
		
		return DefaultSequentialPropositionalContext.sequentialPropositionalContext(
				"Sequences for statements about " + dataTable.caption(),
				"Sequential database for data table " + dataTable.caption(),
				sequences, propositions);
	}

	private List<Proposition> compilePropositions(DataTable dataTable) {
		LOGGER.fine("Compiling proposition list");
		
		List<Proposition> propositions = new ArrayList<>();
		
		for (Attribute<?> attribute : dataTable.attributes()) {
			if (this.groupingAttributeName.equals(attribute.caption())
					|| this.distanceAttributeName.equals(attribute.caption())) {
				continue;
			}
			for (PropositionalizationRule mapper : this.attributeFactories.current()) {
				propositions.addAll(mapper.apply(dataTable, attribute));
			}
		}

//		propositions.forEach(p -> {
//			propositions.subList(0, propositions.indexOf(p)).forEach(q -> {
//				if (p.implies(q)) {
//					LOGGER.fine("'" + p + "' implies already present proposition '" + q + "'");
//				}
//				if (q.implies(p)) {
//					LOGGER.fine("'" + p + "' is implied by already present proposition '" + q + "'");
//				}
//			});
//		});

		LOGGER.info("Done compiling proposition list (" + propositions.size() + " propositions added)");

		return propositions;
	}

	private Map<String, List<Integer>> getGroupToIndices(DataTable dataTable, Attribute<?> groupingAttribute) {
		Map<String, List<Integer>> groupToIndices = newHashMap();
		
		for (int i = 0; i < dataTable.population().size(); i++) {
			Optional<?> groupingValue = groupingAttribute.getValueOption(i);
			
			if (!groupingValue.isPresent()) {
				continue;
			}
			
			String value = groupingValue.get().toString();
			
			List<Integer> indices = groupToIndices.get(value);
			
			if(indices == null) {
				groupToIndices.put(value, indices = newLinkedList());
			}
			
			indices.add(i);
		}
		
		return groupToIndices;
	}

	private List<List<SequenceEvent<?>>> getSequences(DataTable dataTable, List<Proposition> propositions, Attribute<?> distanceAttribute,
			Collection<List<Integer>> groupToIndices) {
 		
		return groupToIndices.stream().map(indexList -> {
			List<SequenceEvent<?>> sequence = Lists.newArrayList();
			
			for(int i : indexList) {
				Optional<?> distValue = distanceAttribute.getValueOption(i);
				
				if(!distValue.isPresent()) {
					continue;
				};
				
				Comparable<?> object = (Comparable<?>) distValue.get();

				List<Proposition> eventList = IntStream.range(0,  propositions.size())
						.filter(j -> propositions.get(j).holdsFor(i))
						.mapToObj(j -> propositions.get(j))
						.collect(Collectors.toList());
				

				for(Proposition proposition: eventList) {
					sequence.add(newSequenceEvent(object, proposition));
				}
			}
			
			return sequence;
		}).collect(Collectors.toList());
	}

	private List<SequenceTransaction> compileSequences(DataTable dataTable,
			List<Proposition> propositions) {
		LOGGER.fine("Creating sequences");

		Attribute<?> groupingAttribute = dataTable
				.attribute(id(this.groupingAttributeName)).get();
		Attribute<?> distanceAttribute = dataTable
				.attribute(id(this.distanceAttributeName)).get();
		
		Map<String, List<Integer>> groupToIndices = getGroupToIndices(dataTable, groupingAttribute);

		List<List<SequenceEvent<?>>> sequences = getSequences(dataTable, propositions, distanceAttribute, groupToIndices.values());

		List<SequenceTransaction> sequenceTransactions = sequences.stream()
				.map(s -> newSequenceTransaction(s)).collect(Collectors.toList());
		
		LOGGER.info("Done creating sequences (" + sequences.size() + " sequences created)");

		return sequenceTransactions;
	}
	
	private String getName(Proposition proposition) {
		if(AttributeBasedProposition.class.isAssignableFrom(proposition.getClass())) {
			AttributeBasedProposition<?> aProposition = (AttributeBasedProposition<?>) proposition;
			return aProposition.attribute().caption() + "=" + aProposition.constraint().description();
		}
		return proposition.name();
	}

	private List<Proposition> recompilePropositions(List<Proposition> propositions, List<SequenceTransaction> sequences) {
		Map<Proposition, Set<Integer>> indexSets = Maps.newHashMap();
		
		for(Proposition proposition: propositions) {
			indexSets.put(proposition, Sets.newHashSet());
		}
		
		for(int i = 0; i < sequences.size(); i++) {
			int ii = i;
			
			sequences.get(i).events().forEach(e -> indexSets.get(e.proposition()).add(ii));
		}
		
		List<Proposition> newPropositions = Lists.newArrayList();
		
		int i = 0;
		for(Proposition proposition: propositions) {
			newPropositions.add(Propositions.proposition(i++, getName(proposition), IndexSets.copyOf(indexSets.get(proposition))));
		}
		
		return newPropositions;
	}

	private List<SequenceTransaction> recompileSequences(List<Proposition> propositions,
			List<SequenceTransaction> sequences) {
		Map<String, Proposition> mapping = Maps.newHashMap();
		
		for(Proposition proposition: propositions) {
			mapping.put(proposition.name(), proposition);
		}

		List<SequenceTransaction> newSequences = Lists.newArrayList();
		
		for(SequenceTransaction sequence: sequences) {
			List<SequenceEvent<?>> newEvents = Lists.newArrayList();
			
			for(SequenceEvent<?> sequenceEvent: sequence.events()) {
				newEvents.add(newSequenceEvent(sequenceEvent.value(), mapping.get(getName(sequenceEvent.proposition()))));	
			}
			
			newSequences.add(newSequenceTransaction(newEvents));
		}
		
		return newSequences;
	}

}
