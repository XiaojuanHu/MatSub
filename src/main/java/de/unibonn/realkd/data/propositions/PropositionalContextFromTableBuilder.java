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

import static com.google.common.collect.Sets.union;
import static de.unibonn.realkd.common.base.Identifier.identifier;
import static de.unibonn.realkd.common.parameter.Parameters.subSetParameter;
import static java.util.EnumSet.allOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * <p>
 * Creates a table-based propositional logic by applying a set of attribute to
 * proposition mappers to each attribute present in the input datatable.
 * </p>
 * <p>
 * The set of mappers to be applied can be any subset of the collection given by
 * {@link #ALL_MAPPERS} and per default it is set to be the set given by
 * {@link #DEFAULT_MAPPERS}. The set to be used can be accessed through the
 * accessors {@link #mappers()}, {@link #mappers(Set)}, as well as through the
 * external (user) parameter provided through
 * {@link #attributeMapperParameter()}.
 * </p>
 * <p>
 * NOTE: the access through an external parameter is the reason why the set of
 * available mappers is currently upper-bounded by {@link #ALL_MAPPERS}. This is
 * subject to change in future version to allow open extensibility.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.6.0
 *
 */
public class PropositionalContextFromTableBuilder implements PropositionalizationScheme, ParameterContainer {

	private static final Logger LOGGER = Logger.getLogger(PropositionalContextFromTableBuilder.class.getName());

	public static final Set<PropositionalizationRule> ALL_MAPPERS = union(
			union(union(
					union(union(allOf(LegacyAttributesToPropositionsMapper.class),
							allOf(ClusteringAttributeToNamedPropositionsMapper.class)),
					allOf(SymmetricRegularQuantileBasedAttributeToPropositionsMapper.class)),
					allOf(DateAttributeToPropositionsMapper.class)),
			allOf(SmartDiscreteOrdinalAttributeToPropositionMapper.class)),
			allOf(ClusteringAttributeToPropositionsMapper.class));

	public static final Set<PropositionalizationRule> DEFAULT_MAPPERS = ImmutableSet.of(
			LegacyAttributesToPropositionsMapper.CATEGORIC_EQUALiTY,
			SmartDiscreteOrdinalAttributeToPropositionMapper.SMART_DISCRETE_ORDINAL,
			ClusteringAttributeToNamedPropositionsMapper.IRREGULAR_4_CUTOFFS_CLUSTERING,
			LegacyAttributesToPropositionsMapper.POSITIVE_AND_NEGATIVE,
			DateAttributeToPropositionsMapper.YEAR_MONTH_DATE_HOUR);

	private SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeFactories;

	private Optional<Identifier> id = Optional.empty();

	private Optional<String> name = Optional.empty();

	public DefaultTableBasedPropositionalLogic apply(DataTable dataTable) {
		LOGGER.fine("Compiling proposition list");
		List<AttributeBasedProposition<?>> propositions = new ArrayList<>();
		for (Attribute<?> attribute : dataTable.attributes()) {
			for (PropositionalizationRule propFactory : this.attributeFactories.current()) {
				propositions.addAll(propFactory.apply(dataTable, attribute));
			}
		}

		propositions.forEach(p -> {
			propositions.subList(0, propositions.indexOf(p)).forEach(q -> {
				if (p.implies(q)) {
					LOGGER.fine("'" + p + "' implies already present proposition '" + q + "'");
				}
				if (q.implies(p)) {
					LOGGER.fine("'" + p + "' is implied by already present proposition '" + q + "'");
				}
			});
		});

		LOGGER.info("Done compiling proposition list (" + propositions.size() + " propositions added)");

		return new DefaultTableBasedPropositionalLogic(dataTable, propositions,
				id.orElse(Identifier.id("statements_about_" + dataTable.identifier())),
				name.orElse("Statements about " + dataTable.caption()),
				"Propositional logic generated using mappers: " + mappers());
	}

	public PropositionalContextFromTableBuilder() {
		this.attributeFactories = subSetParameter(identifier("Attribute_mappers"), "Attribute mappers",
				"Mappers that are used to create binary propositions for pattern mining from the data table",
				() -> ALL_MAPPERS, () -> DEFAULT_MAPPERS);
	}

	public PropositionalContextFromTableBuilder name(String name) {
		this.name = Optional.ofNullable(name);
		return this;
	}

	public PropositionalContextFromTableBuilder id(Identifier id) {
		this.id = Optional.ofNullable(id);
		return this;
	}

	public List<Parameter<?>> getTopLevelParameters() {
		return ImmutableList.of(this.attributeFactories);
	}

	/**
	 * 
	 * @return the externally accessible parameter of the attribute mappers
	 */
	public SubCollectionParameter<PropositionalizationRule, Set<PropositionalizationRule>> attributeMapperParameter() {
		return attributeFactories;
	}

	/**
	 * 
	 * @return the current set of attribute to propositions mappers to be used
	 *         for creating propositional logics from table
	 */
	public Set<PropositionalizationRule> mappers() {
		return attributeFactories.current();
	}

	/**
	 * 
	 * @param mappers
	 *            the collection of mappers to be used for creating
	 *            propositional logics from table
	 */
	public void mappers(Set<PropositionalizationRule> mappers) {
		attributeFactories.set(mappers);
	}

}
