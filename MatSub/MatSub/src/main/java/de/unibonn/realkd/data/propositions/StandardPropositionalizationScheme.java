/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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

import static de.unibonn.realkd.data.propositions.CategoricEqualityPropositionalizationRule.categoricEquality;
import static de.unibonn.realkd.data.propositions.KMeansPropositionalizationRule.kMeansPropRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("standardPropScheme")
@KdonDoc("Propositionalizes table by applying a set of propositionalization rules to each attribute separately.")
public class StandardPropositionalizationScheme implements PropositionalizationScheme {

	private static final CategoricEqualityPropositionalizationRule DEFAULT_DEFAULT_CATEGORIC_RULE = categoricEquality();

	private static final SmartDiscreteOrdinalAttributeToPropositionMapper DEFAULT_DEFAULT_ORDINAL_RULE = SmartDiscreteOrdinalAttributeToPropositionMapper.SMART_DISCRETE_ORDINAL;

	private static final KMeansPropositionalizationRule DEFAULT_DEFAULT_METRIC_RULE = kMeansPropRule();

	public static StandardPropositionalizationScheme standardPropositionalizationScheme() {
		return new StandardPropositionalizationScheme(null, null, null, null, null);
	}

	private static final Logger LOGGER = Logger.getLogger(StandardPropositionalizationScheme.class.getName());

	private final PropositionalizationRule defaultMetricRule;

	private final PropositionalizationRule defaultOrdinalRule;

	private final PropositionalizationRule defaultCategoricRule;

	@JsonProperty("additionalRules")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Additional rules that are applied after the default rules.")
	private final PropositionalizationRule[] additionalRules;

	@JsonProperty("specialRules")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Exclusive rules that are applied first to attribute and, when firing, prevent further rules from being applied.")
	private final PropositionalizationRule[] specialRules;

	private final List<PropositionalizationRule> rules;

	@JsonCreator
	public StandardPropositionalizationScheme(@JsonProperty("defaultMetricRule") PropositionalizationRule metricRule,
			@JsonProperty("defaultOrdinalRule") PropositionalizationRule ordinalrule,
			@JsonProperty("defaultCategoricRule") PropositionalizationRule categoricRule,
			@JsonProperty("additionalRules") PropositionalizationRule[] additionalRules,
			@JsonProperty("specialRules") PropositionalizationRule[] specialRules) {
		this.defaultMetricRule = (metricRule != null) ? metricRule : DEFAULT_DEFAULT_METRIC_RULE;
		this.defaultOrdinalRule = (ordinalrule != null) ? ordinalrule : DEFAULT_DEFAULT_ORDINAL_RULE;
		this.defaultCategoricRule = (categoricRule != null) ? categoricRule : DEFAULT_DEFAULT_CATEGORIC_RULE;
		this.additionalRules = (additionalRules != null) ? additionalRules : new PropositionalizationRule[0];
		this.specialRules = (specialRules != null) ? specialRules : new PropositionalizationRule[0];

		this.rules = new ArrayList<>();
		this.rules.addAll(Arrays.asList(this.defaultMetricRule, this.defaultCategoricRule, this.defaultOrdinalRule));
		this.rules.addAll(Arrays.asList(this.additionalRules));
	}

	@JsonProperty("defaultMetricRule")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("First rule to be applied (default finds four cut off points by k-means clustering for metric attributes).")
	public PropositionalizationRule defaultMetricRule() {
		return defaultMetricRule.equals(DEFAULT_DEFAULT_METRIC_RULE) ? null : defaultMetricRule;
	}

	@JsonProperty("defaultOrdinalRule")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Second rule to be applied (default creates count-based inequality conditions for ordinal attributes that are not metric).")
	public PropositionalizationRule defaultOrdinalRule() {
		return defaultOrdinalRule.equals(DEFAULT_DEFAULT_ORDINAL_RULE) ? null : defaultOrdinalRule;
	}

	@JsonProperty("defaultCategoricRule")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Third rule to be applied (default creates equality proposition for all values of categoric attribute.)")
	public PropositionalizationRule defaultCategoricRule() {
		return defaultCategoricRule.equals(DEFAULT_DEFAULT_CATEGORIC_RULE) ? null : defaultCategoricRule;
	}

	@Override
	public PropositionalContext apply(DataTable table) {
		LOGGER.fine("Compiling proposition list");
		List<AttributeBasedProposition<?>> propositions = new ArrayList<>();
		for (Attribute<?> attribute : table.attributes()) {
			boolean specialRuleApplied = false;
			for (PropositionalizationRule rule : this.specialRules) {
				List<AttributeBasedProposition<?>> props = rule.apply(table, attribute);
				propositions.addAll(props);
				if (!props.isEmpty()) {
					specialRuleApplied = true;
					break;
				}
			}
			if (specialRuleApplied)
				continue;

			for (PropositionalizationRule rule : this.rules) {
				propositions.addAll(rule.apply(table, attribute));
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

		return new DefaultTableBasedPropositionalLogic(table, propositions, Identifier.id("statements_about_" + table.identifier()),
				"Statements about " + table.caption(), "Propositional logic generated using mappers: " + rules);
	}

}
