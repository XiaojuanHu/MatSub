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
package de.unibonn.realkd.algorithms.pmm;

import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
import de.unibonn.realkd.algorithms.emm.ModelClassParameter;
import de.unibonn.realkd.algorithms.emm.TargetAttributePropositionFilter;
import de.unibonn.realkd.algorithms.sampling.ConsaptBasedSamplingMiner;
import de.unibonn.realkd.algorithms.sampling.DistributionFactory;
import de.unibonn.realkd.algorithms.sampling.FrequencyDistributionFactory;
import de.unibonn.realkd.algorithms.sampling.SamplingParameters;
import de.unibonn.realkd.algorithms.sampling.SinglePatternPostProcessor;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.parameter.SubCollectionParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.pmm.PureModelMining;
import de.unibonn.realkd.patterns.pmm.PureModelSubgroup;
import de.unibonn.realkd.patterns.subgroups.ReferenceDescriptor;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.patterns.subgroups.Subgroups;

/**
 * Wraps a {@link ConsaptBasedSamplingMiner} for randomly generating pure model
 * subgroups.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class RandomizedLocalPureSubgroupSearch extends AbstractMiningAlgorithm<PureModelSubgroup> {

	private final Parameter<DataTable> datatable;

	private final Parameter<List<Attribute<?>>> targets;

	private final ModelClassParameter model;

	private final RangeEnumerableParameter<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>> purityMeasure;

	private final Parameter<PropositionalContext> propLogic;

	private final RangeEnumerableParameter<SinglePatternPostProcessor> postProcessing;

	private final Parameter<Integer> numberOfResults;

	private final Parameter<Integer> numberOfSeeds;

	private final RangeEnumerableParameter<PatternOptimizationFunction> targetFunction;

	private final SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> descriptorAttributeFilter;

	private final DefaultRangeEnumerableParameter<DistributionFactory> distributionFactoryParameter;

	private ConsaptBasedSamplingMiner<PureModelSubgroup> sampler = null;

	public RandomizedLocalPureSubgroupSearch(Workspace workspace) {
		this.datatable = MiningParameters.dataTableParameter(workspace);
		this.targets = EMMParameters.getEMMTargetAttributesParameter(datatable);
		this.model = new ModelClassParameter(targets);
		this.purityMeasure = PmmParameters.purityMeasureParameter(model, targets);

		this.propLogic = MiningParameters.matchingPropositionalLogicParameter(workspace, datatable);
		this.descriptorAttributeFilter = EMMParameters.getEMMDescriptorAttributesParameter(datatable, targets);
		Predicate<Proposition> targetFilter = new TargetAttributePropositionFilter(datatable, targets);
		Predicate<Proposition> additionalPropFilter = prop -> !((prop instanceof AttributeBasedProposition)
				&& descriptorAttributeFilter.current().contains(((AttributeBasedProposition<?>) prop).attribute()));

		Predicate<Proposition> propositionFilter = additionalPropFilter.and(targetFilter);

		DistributionFactory frequencyOption = new FrequencyDistributionFactory(propositionFilter);

		List<DistributionFactory> distributionOptions = ImmutableList.of(frequencyOption);

		this.distributionFactoryParameter = rangeEnumerableParameter(Identifier.id("seed_dist"), "Seed distribution",
				"The probability distribution on the pattern space that is used to generate random seeds for PMM pattern search",
				DistributionFactory.class, () -> distributionOptions, targets, propLogic, descriptorAttributeFilter);

		this.postProcessing = SamplingParameters.postProcessingParameter();
		this.targetFunction = PmmParameters.targetFunctionParameter();
		this.numberOfResults = SamplingParameters.numberOfResultsParameter();
		this.numberOfSeeds = SamplingParameters.numberOfSeedsParameter(numberOfResults);

		this.registerParameter(datatable);
		this.registerParameter(targets);
		this.registerParameter(model);
		this.registerParameter(purityMeasure);
		this.registerParameter(propLogic);
		this.registerParameter(descriptorAttributeFilter);
		this.registerParameter(distributionFactoryParameter);
		this.registerParameter(postProcessing);
		this.registerParameter(targetFunction);
		this.registerParameter(numberOfResults);
		this.registerParameter(numberOfSeeds);
	}

	@Override
	public String caption() {
		return "Randomized Local Pure Subgroup Search";
	}

	@Override
	public String description() {
		return "Performs local optimization of random descriptor seeds.";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.PURE_SUBGROUP_DISCOVERY;
	}

	@Override
	protected Collection<PureModelSubgroup> concreteCall() throws ValidationException {
		final ModelFactory<?> modelFactory = model.current().get();
		final DataTable table = datatable.current();
		final List<Attribute<?>> targetAttr = targets.current();
		final Model globalModel = modelFactory.getModel(table, targetAttr);
		MeasurementProcedure<? extends Measure, ? super Subgroup<?>> purityMeasurementProcedure = purityMeasure
				.current();

		Function<LogicalDescriptor, PureModelSubgroup> toPattern = d -> {
			Model localModel = modelFactory.getModel(table, targetAttr, d.supportSet());
			return PureModelMining
					.pureSubgroup(
							Subgroups.subgroup(d, ReferenceDescriptor.global(table.population()), table, targetAttr,
									modelFactory, globalModel, localModel),
							purityMeasure.current(), ImmutableList.of());
		};

		BiFunction<LogicalDescriptor, PureModelSubgroup, PureModelSubgroup> toPatternWithPrevious = (d, p) -> {
			if (p instanceof PureModelSubgroup
					&& ((Subgroup<?>) p.descriptor()).extensionDescriptor().supportSet().equals(d.supportSet())) {
				return PureModelMining.pureSubgroup(
						Subgroups.subgroup(d, ReferenceDescriptor.global(table.population()), table, targetAttr,
								modelFactory, globalModel, ((Subgroup<?>) p.descriptor()).localModel()),
						purityMeasurementProcedure, ImmutableList.of());
			}
			Model localModel = modelFactory.getModel(table, targetAttr, d.supportSet());
			return PureModelMining
					.pureSubgroup(
							Subgroups.subgroup(d, ReferenceDescriptor.global(table.population()), table, targetAttr,
									modelFactory, globalModel, localModel),
							purityMeasurementProcedure, ImmutableList.of());
		};

		sampler = new ConsaptBasedSamplingMiner<PureModelSubgroup>(toPattern, toPatternWithPrevious,
				p -> ((PureModelSubgroup) p).descriptor().extensionDescriptor(), propLogic.current(),
				distributionFactoryParameter.current().getDistribution(propLogic.current()), targetFunction.current(),
				postProcessing.current(), numberOfResults.current(), numberOfSeeds.current());

		Collection<PureModelSubgroup> result = sampler.call();
		sampler = null;

		return result;
	}

	@Override
	protected void onStopRequest() {
		if (sampler != null) {
			sampler.requestStop();
		}
	}

	public Parameter<DataTable> datatable() {
		return datatable;
	}

	public ModelClassParameter model() {
		return model;
	}

	public Parameter<List<Attribute<?>>> targets() {
		return targets;
	}

	public SubCollectionParameter<Attribute<?>, Set<Attribute<?>>> descriptorAttributeFilter() {
		return descriptorAttributeFilter;
	}

	public RangeEnumerableParameter<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>> purityMeasure() {
		return purityMeasure;
	}

	public Parameter<Integer> numberOfResults() {
		return numberOfResults;
	}

	public Parameter<Integer> numberOfSeeds() {
		return numberOfSeeds;
	}

	public RangeEnumerableParameter<PatternOptimizationFunction> targetFunction() {
		return targetFunction;
	}

}
