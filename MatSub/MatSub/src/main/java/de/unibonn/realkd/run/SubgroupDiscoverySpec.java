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
package de.unibonn.realkd.run;

import static de.unibonn.realkd.computations.core.Computations.computation;
import static de.unibonn.realkd.data.propositions.Propositions.isNotRelatedTo;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.branchbound.BestFirstBranchAndBound;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.BranchAndBoundSearchNode;
import de.unibonn.realkd.algorithms.branchbound.BranchAndBoundSearch.LcmSearchNode;
import de.unibonn.realkd.algorithms.branchbound.OptimisticEstimators.OptimisticEstimatorOption;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.conditional.DiscretelyConditionedBernoulliFactory;
import de.unibonn.realkd.patterns.subgroups.ReferenceDescriptor;

/**
 * @author Kailash Budhathoki
 *
 * @since 0.7.0
 * 
 * @version 0.7.0
 * 
 */
@KdonTypeName("subgroupDiscovery")
@KdonDoc("Computation specification for subgroup discovery task.")
public class SubgroupDiscoverySpec implements ComputationSpecification {

	@JsonProperty("id")
	@KdonDoc("id of the computation")
	private final Identifier id;

	@JsonProperty("targets")
	@KdonDoc("Target variables.")
	private final Identifier[] targets;

	@JsonProperty("controls")
	@KdonDoc("Control variables.")
	private final Identifier[] controls;

	@JsonProperty("attrFilter")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("Variables to ignore for subgroup discovery.")
	private final Identifier[] attrFilter;

	@JsonProperty("numRes")
	@KdonDoc("number of results to return")
	private final int numRes;

	@JsonProperty("apxFac")
	@KdonDoc("approximation factor for branch-and-bound search")
	private final int apxFactor;

	@JsonProperty("maxDepth")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("maximum depth for branch-and-bound search")
	private final int maxDepth;

	@JsonProperty("positiveCategory")
	@JsonInclude(Include.NON_EMPTY)
	@KdonDoc("positive category for a bernoulli model if the target is categorical")
	private final Object positiveCategory;

	@JsonProperty("deviationMeasure")
	@KdonDoc("objective function to measure the quality of a pattern")
	private final ModelDeviationMeasure deviationMeasure;

	@JsonProperty("optimisticEstimator")
	@KdonDoc("optimistic estimator of the deviation measure")
	private final OptimisticEstimatorOption optimisticEstimator;

	private SubgroupDiscoverySpec(@JsonProperty("id") Identifier id, @JsonProperty("targets") Identifier[] targets,
			@JsonProperty("controls") Identifier[] controls, @JsonProperty("attrFilter") Identifier[] attrFilter,
			@JsonProperty("numRes") int numRes, @JsonProperty("apxFac") int apxFactor,
			@JsonProperty("maxDepth") int maxDepth, @JsonProperty("positiveCategory") Object positiveCategory,
			@JsonProperty("optimisticEstimator") OptimisticEstimatorOption optimisticEstimator,
			@JsonProperty("deviationMeasure") ModelDeviationMeasure deviationMeasure) {
		this.id = id;
		this.targets = targets;
		this.controls = (controls == null) ? new Identifier[0] : controls;
		this.attrFilter = (attrFilter == null) ? new Identifier[0] : attrFilter;
		this.numRes = numRes;
		this.apxFactor = apxFactor;
		this.maxDepth = maxDepth;
		this.positiveCategory = positiveCategory;
		this.optimisticEstimator = optimisticEstimator;
		this.deviationMeasure = deviationMeasure;
	}

	@Override
	public Computation<?> build(Workspace context) throws ValidationException {
		DataTable table = context.datatables().iterator().next();
		PropositionalContext propContext = context.propositionalContexts().iterator().next();
		@SuppressWarnings("unchecked")
		List<Attribute<?>> targetAttributes = (List<Attribute<?>>) table.attributes(targets);
		// very ugly hack to make it work with existing setup
		targetAttributes.addAll(table.attributes(controls));

		DiscretelyConditionedBernoulliFactory modelingMethod = new DiscretelyConditionedBernoulliFactory(
				positiveCategory);
		Function<LogicalDescriptor, ReferenceDescriptor> descriptorToReferenceDescriptor;
		Function<ReferenceDescriptor, Model> selectorToReferenceModel;

		descriptorToReferenceDescriptor = d -> ReferenceDescriptor.complement(d);
		selectorToReferenceModel = r -> modelingMethod.getModel(table, targetAttributes, r.supportSet());

		Function<LogicalDescriptor, ExceptionalModelPattern> toEmmPatternMap = ExceptionalModelMining
				.extensionDescriptorToEmmPatternMap(table, targetAttributes, modelingMethod, selectorToReferenceModel,
						descriptorToReferenceDescriptor, deviationMeasure, ImmutableList.of());

		List<Attribute<?>> attributes = new ArrayList<>();
		attributes.addAll(targetAttributes);
		attributes.addAll(table.attributes(attrFilter));
		attributes.addAll(table.attributes(controls));
		
		Function<LcmSearchNode<ExceptionalModelPattern>, Collection<LcmSearchNode<ExceptionalModelPattern>>> expander = BranchAndBoundSearch
				.closedDescriptorsExpander(propContext, isNotRelatedTo(table, attributes), toEmmPatternMap);
		LcmSearchNode<ExceptionalModelPattern> lcmRootNode = BranchAndBoundSearch.lcmRootNode(propContext, x -> true,
				toEmmPatternMap);

		ToDoubleFunction<BranchAndBoundSearchNode<ExceptionalModelPattern>> optFunc = pattern -> {
			return pattern.content.value(pattern.content.getDeviationMeasure());
		};
		
		BestFirstBranchAndBound<ExceptionalModelPattern, LcmSearchNode<ExceptionalModelPattern>> search = new BestFirstBranchAndBound<ExceptionalModelPattern, BranchAndBoundSearch.LcmSearchNode<ExceptionalModelPattern>>(
				n -> toEmmPatternMap.apply(n.descriptor), expander, lcmRootNode, optFunc, optimisticEstimator.get(),
				numRes, apxFactor, Optional.of(maxDepth));

		return computation(() -> {
			Collection<ExceptionalModelPattern> res = search.call();
			return res.stream().map(p->p.greedySimplification()).collect(toList());
		});
	}

	@Override
	public Identifier identifier() {
		return id;
	}

}