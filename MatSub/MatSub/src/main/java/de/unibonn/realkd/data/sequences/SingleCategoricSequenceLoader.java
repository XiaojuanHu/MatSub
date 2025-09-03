/*
 * GNU AFFERO GENERAL PUBLIC LICENSE
 * 
 * Version 3, 19 November 2007 
 *
 * Copyright (C) 2015-2019 University of Antwerp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.unibonn.realkd.data.sequences;

import static de.unibonn.realkd.data.sequences.DefaultTableBasedSingleSequencePropositionalContext.singleSequencePropositionalContext;
import static de.unibonn.realkd.data.sequences.SequenceEvents.newSequenceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.data.constraints.Constraints;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.DefaultTableBasedPropositionalLogic;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContextFromTableBuilder;
import de.unibonn.realkd.data.propositions.PropositionalizationRule;
import de.unibonn.realkd.data.propositions.Propositions;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 *
 *
 * @author Ali Doku
 * @author Sandy Moens
 * @since 0.2.0
 * @version 0.2.0
 */
public class SingleCategoricSequenceLoader {

	private static final Logger LOGGER = Logger.getLogger(PropositionalContextFromTableBuilder.class.getName());

	private SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeFactories;

	private Optional<Identifier> id = Optional.empty();

	private Optional<String> name = Optional.empty();

	public SingleCategoricSequenceLoader() {

		this.attributeFactories = Parameters.subSetParameter(Identifier.identifier("Attribute_mappers"),
				"Attribute mappers",
				"Mappers that are used to create binary propositions for pattern mining from the data table",
				() -> PropositionalContextFromTableBuilder.ALL_MAPPERS,
				() -> PropositionalContextFromTableBuilder.DEFAULT_MAPPERS);

	}

	public SingleCategoricSequenceLoader name(String name) {
		this.name = Optional.ofNullable(name);
		return this;
	}

	public SingleCategoricSequenceLoader id(String id) {
		this.id = Optional.ofNullable(Identifier.id(id));
		return this;
	}

	public List<Parameter<?>> getTopLevelParameters() {
		return ImmutableList.of(this.attributeFactories);
	}

	/**
	 * 
	 * @return the externally accessible parameter of the attribute mappers
	 */
	public SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeMapperParamater() {
		return this.attributeFactories;
	}

	/**
	 * 
	 * @return the current set of attribute to propositions mappers to be used
	 * for creating propositional logics from table
	 */
	public Set<PropositionalizationRule> mappers() {
		return this.attributeFactories.current();
	}

	/**
	 * 
	 * @param mappers the collection of mappers to be used for creating
	 * propositional logics from table
	 */
	public void mappers(Set<PropositionalizationRule> mappers) {
		this.attributeFactories.set(mappers);
	}

	public SingleSequencePropositionalContext build(DataTable dataTable) {
		DefaultTableBasedPropositionalLogic propositionalLogic = compilePropositions(dataTable);
		List<SequenceEvent<?>> events = compileEvents(dataTable, propositionalLogic);
		List<Proposition> propositions = propositionalLogic.propositions();

		propositions = recompilePropositions(propositions, events);
		events = recompileSequence(propositions, events);

		return singleSequencePropositionalContext(dataTable, events, propositions);
	}

	private List<SequenceEvent<?>> compileEvents(DataTable datatable,
			DefaultTableBasedPropositionalLogic propositionalLogic) {

		List<SequenceEvent<?>> events = new ArrayList<>(datatable.population().size());

		long beg = System.currentTimeMillis();

		List<? extends Attribute<?>> attributes = datatable.attributes().subList(1, datatable.attributes().size());

		for (int i = 0; i < datatable.population().size(); i++) {

			Double a = Double.valueOf(datatable.attribute(0).getValueOption(i).get().toString());

			for (Attribute<?> attribute : attributes) {
				Optional<?> valueOption = attribute.getValueOption(i);

				if (!valueOption.isPresent()) {
					continue;
				}

				events.add(SequenceEvents.newSequenceEvent(a,
						Propositions.proposition(datatable, attribute, Constraints.equalTo(valueOption.get()))));
			}
			if (i % 10000 == 0) {
				System.out.println(i + " " + (System.currentTimeMillis() - beg) + "ms");
			}
		}

		return events;
	}

	private DefaultTableBasedPropositionalLogic compilePropositions(DataTable dataTable) {
		LOGGER.fine("Compiling proposition list");
		List<AttributeBasedProposition<?>> propositions = new ArrayList<>();

		for (Attribute<?> attribute : dataTable.attributes()) {
			if (attribute.equals(dataTable.attribute(0)))
				continue;
			for (PropositionalizationRule propFactory : this.attributeFactories.current()) {
				propositions.addAll(propFactory.apply(dataTable, attribute));
			}
		}

		// propositions.forEach(p -> {
		// propositions.subList(0, propositions.indexOf(p)).forEach(q -> {
		// if (p.implies(q)) {
		// LOGGER.fine("'" + p + "' implies already present proposition '" + q +
		// "'");
		// }
		// if (q.implies(p)) {
		// LOGGER.fine("'" + p + "' is implied by already present proposition '"
		// + q +
		// "'");
		// }
		// });
		// });
		//
		// LOGGER.info("Done compiling proposition list (" + propositions.size()
		// +
		// "propositions added)");

		return new DefaultTableBasedPropositionalLogic(dataTable, propositions,
				this.id.orElse(Identifier.id("statements_about_" + dataTable.identifier())),
				this.name.orElse("Statements about " + dataTable.caption()),
				"Propositional logic generated using mappers: " + mappers());
	}

	private String getName(Proposition proposition) {
		if (AttributeBasedProposition.class.isAssignableFrom(proposition.getClass())) {
			AttributeBasedProposition<?> aProposition = (AttributeBasedProposition<?>) proposition;
			return aProposition.attribute().caption() + "=" + aProposition.constraint().description();
		}
		return proposition.name();
	}

	private List<Proposition> recompilePropositions(List<Proposition> propositions) {
		Map<Proposition, Set<Integer>> indexSets = Maps.newHashMap();

		for (Proposition proposition : propositions) {
			indexSets.put(proposition, Sets.newHashSet(0));
		}

		List<Proposition> newPropositions = Lists.newArrayList();

		int i = 0;
		for (Proposition proposition : propositions) {
			newPropositions.add(
					Propositions.proposition(i++, getName(proposition), IndexSets.copyOf(indexSets.get(proposition))));
		}

		return newPropositions;
	}

	private List<Proposition> recompilePropositions(List<Proposition> propositions, List<SequenceEvent<?>> events) {
		Map<Proposition, List<Integer>> indexSets = Maps.newHashMap();

		for (Proposition proposition : propositions) {
			indexSets.put(proposition, Lists.newLinkedList());
		}

		int i = 0;
		for (SequenceEvent<?> event : events) {
			indexSets.get(event.proposition()).add(i);
			i++;
		}

		List<Proposition> newPropositions = Lists.newArrayList();

		i = 0;
		for (Proposition proposition : propositions) {
			newPropositions.add(
					Propositions.proposition(i++, getName(proposition), IndexSets.copyOf(indexSets.get(proposition))));
		}

		return newPropositions;
	}

	private List<SequenceEvent<?>> recompileSequence(List<Proposition> propositions, List<SequenceEvent<?>> events) {
		Map<String, Proposition> mapping = Maps.newHashMap();

		for (Proposition proposition : propositions) {
			mapping.put(proposition.name(), proposition);
		}

		List<SequenceEvent<?>> newEvents = Lists.newArrayList();

		for (SequenceEvent<?> sequenceEvent : events) {
			newEvents.add(newSequenceEvent(sequenceEvent.value(), mapping.get(getName(sequenceEvent.proposition()))));
		}

		return newEvents;
	}

}
