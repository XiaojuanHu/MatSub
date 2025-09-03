/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
package de.unibonn.realkd.algorithms.emm.dssd;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.beamsearch.BeamSearch;
import de.unibonn.realkd.algorithms.beamsearch.BeamWidthParameter;
import de.unibonn.realkd.algorithms.common.FreePropositionalLogicParameter;
import de.unibonn.realkd.algorithms.common.MinimumFrequencyThresholdParameter;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.algorithms.common.NumberOfResultsParameter;
import de.unibonn.realkd.algorithms.common.PatternConstraint;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.algorithms.common.PropositionFilter;
import de.unibonn.realkd.algorithms.derived.DerivedAlgorithms;
import de.unibonn.realkd.algorithms.derived.SimpleParameterAdapter;
import de.unibonn.realkd.algorithms.emm.EMMParameters;
import de.unibonn.realkd.algorithms.emm.ModelClassParameter;
import de.unibonn.realkd.algorithms.emm.ParameterBoundLogicalDescriptorToEmmPatternMap;
import de.unibonn.realkd.algorithms.emm.TargetAttributePropositionFilter;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.DefaultParameter;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.util.Predicates;

/**
 * Implementation of "M. van Leeuwen, A. Knobbe: Diverse subgroup set discovery,
 * <i>Data Mining and Knowledge Discovery, September 2012, Volume 25, Issue 2,
 * pp 208-242</i>".
 * 
 * @author Vladimir Dzyuba, KU Leuven
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 * 
 */
public class DiverseSubgroupSetDiscovery extends AbstractMiningAlgorithm<Pattern<?>> {

	private static final String NAME = "Diverse subgroup set discovery";
	private static final String DESCRIPTION = "Discovers a set of subgroups that are interesting individually and mutually not redundant.";

	private static final String BEAM_SELECTOR_NAME = "Beam selection heuristic";
	private static final String BEAM_SELECTOR_DESCRIPTION = "Applied to select a diverse set of candidate patterns at each level of beam search";
	private static final SubgroupSetSelector BEAM_SELECTOR_DEFAULT = SubgroupSetSelector.DESCRIPTION;

	private static final String POST_SELECTOR_NAME = "Post-selection heuristic";
	private static final String POST_SELECTOR_DESCRIPTION = "Applied to select a diverse subset of the output of beam search";
	private static final SubgroupSetSelector POST_SELECTOR_DEFAULT = SubgroupSetSelector.DESCRIPTION;

	private final BeamSearch beamSearch;
	private final Parameter<DataTable> datatableParameter;
	private final FreePropositionalLogicParameter propositionalLogicParameter;
	private final Parameter<List<Attribute<?>>> targetAttributesParameter;
	private final ModelClassParameter modelClassParameter;
	private final Parameter<ModelDeviationMeasure> modelDistanceFunctionParameter;
	private final MinimumFrequencyThresholdParameter minimumFrequencyThreshold;
	private final SubgroupSetSelectorParameter beamSelectorParameter;
	private final SubgroupSetSelectorParameter postSelectorParameter;
	private final NumberOfResultsParameter numberOfResultsParameter;
	private final NumberOfIntermediateResultsParameter numberOfIntermediateResultsParameter;
	private final MaxDepthParameter maxDepthParameter;
	private final Parameter<PatternOptimizationFunction> targetFunctionParameter;
	private final Parameter<Set<Attribute<?>>> descriptorAttributesParameter;

	DiverseSubgroupSetDiscovery(Workspace workspace) {
		super();

		this.datatableParameter = MiningParameters.dataTableParameter(workspace);
		this.propositionalLogicParameter = new FreePropositionalLogicParameter(workspace);
		this.targetAttributesParameter = EMMParameters.getEMMTargetAttributesParameter(datatableParameter);
		this.modelClassParameter = new ModelClassParameter(targetAttributesParameter);
		this.modelDistanceFunctionParameter = EMMParameters.distanceFunctionParameter(modelClassParameter);
		this.descriptorAttributesParameter = EMMParameters.getEMMDescriptorAttributesParameter(datatableParameter,
				targetAttributesParameter);
		this.targetFunctionParameter = new DefaultParameter<>(id("obj_func"), "Target function parameter",
				"Pattern optimization function that is used in beam search", PatternOptimizationFunction.class, null,
				null, Predicates.notNull(), "");
		this.minimumFrequencyThreshold = new MinimumFrequencyThresholdParameter();
		this.beamSelectorParameter = new SubgroupSetSelectorParameter(id("beam_selection"), BEAM_SELECTOR_NAME,
				BEAM_SELECTOR_DESCRIPTION, "beam selection");
		this.beamSelectorParameter.set(BEAM_SELECTOR_DEFAULT);
		this.postSelectorParameter = new SubgroupSetSelectorParameter(id("post_selection"), POST_SELECTOR_NAME,
				POST_SELECTOR_DESCRIPTION, "post-selection");
		this.postSelectorParameter.set(POST_SELECTOR_DEFAULT);
		this.numberOfResultsParameter = new NumberOfResultsParameter();
		this.numberOfIntermediateResultsParameter = new NumberOfIntermediateResultsParameter(
				this.numberOfResultsParameter);
		this.maxDepthParameter = new MaxDepthParameter();

		this.beamSearch = new BeamSearch(workspace,
				new ParameterBoundLogicalDescriptorToEmmPatternMap(datatableParameter, targetAttributesParameter,
						modelClassParameter, modelDistanceFunctionParameter),
				pattern -> ((Subgroup<?>) pattern.descriptor()).extensionDescriptor());

		Predicate<Proposition> propFilter = prop -> !((prop instanceof AttributeBasedProposition)
				&& descriptorAttributesParameter.current().contains(((AttributeBasedProposition<?>) prop).attribute()));
		Predicate<Proposition> targetFilter = new TargetAttributePropositionFilter(datatableParameter,
				targetAttributesParameter);
		beamSearch.setPropositionFilter(PropositionFilter.fromPredicate(propFilter.and(targetFilter)));

		new SimpleParameterAdapter<>(beamSearch.getTargetFunctionParameter(), this.targetFunctionParameter);

		registerParameter(this.targetAttributesParameter);
		registerParameter(this.modelClassParameter);
		registerParameter(this.modelDistanceFunctionParameter);
		registerParameter(this.descriptorAttributesParameter);
		registerParameter(this.targetFunctionParameter);
		registerParameter(this.numberOfResultsParameter);
		registerParameter(beamSearch.getBeamWidthParameter());
		registerParameter(this.maxDepthParameter);
		registerParameter(this.numberOfIntermediateResultsParameter);
		registerParameter(this.minimumFrequencyThreshold);
		registerParameter(this.beamSelectorParameter);
		registerParameter(this.postSelectorParameter);
	}

	/**
	 * Creates an instance of DSSD algorithm, which requires further configuration
	 * of the target function parameter.
	 * 
	 * This factory method is intended primarily for dashboard developers.
	 * 
	 * @see #getTargetFunctionParameter()
	 * @param workspace
	 *            Data workspace
	 * @return Unconfigured DSSD instance
	 */
	public static DiverseSubgroupSetDiscovery createCoreDiverseSubgroupSetDiscovery(Workspace workspace) {
		return new DiverseSubgroupSetDiscovery(workspace);
	}

	/**
	 * Creates a fully usable instance of DSSD algorithm, which enables using a wide
	 * range of target functions
	 * 
	 * This factory method is intended primarily for end users.
	 * 
	 * @param workspace
	 *            Data workspace
	 * @return Ready-to-use DSSD instance
	 */
	public static MiningAlgorithm createStandardDiverseSubgroupSetDiscovery(Workspace workspace) {
		DiverseSubgroupSetDiscovery dssd = new DiverseSubgroupSetDiscovery(workspace);
		SimpleParameterAdapter<PatternOptimizationFunction> optimizationFunctionAdapter = new SimpleParameterAdapter<>(
				dssd.getTargetFunctionParameter(),
				EMMParameters.emmTargetFunctionParameter(dssd.getModelClassParameter()));

		return DerivedAlgorithms.getAlgorithmWithWrappedParameters(dssd, ImmutableList.of(optimizationFunctionAdapter),
				DerivedAlgorithms.EXPOSE, dssd.caption());
	}

	@Override
	protected Collection<Pattern<?>> concreteCall() throws ValidationException {
		beamSearch.clearPruningConstraints();
		beamSearch.addPruningConstraint(new PatternConstraint.MinimumMeasureValeConstraint(Frequency.FREQUENCY,
				minimumFrequencyThreshold.current()));
		final int maxDepth = this.maxDepthParameter.current();
		if (maxDepth > 0) {
			beamSearch.addPruningConstraint(new MaxDescriptionLengthConstraint(maxDepth));
		}

		beamSearch.setNumberOfResults(this.numberOfIntermediateResultsParameter.current());

		final SubgroupSetSelector beamSelector = this.beamSelectorParameter.current();
		beamSelector.setK(beamSearch.getBeamWidth());
		beamSelector.setQualityMeasure(this.targetFunctionParameter.current());
		beamSearch.setNodeForNextLevelSelector(beamSelector);

		final SubgroupSetSelector postSelector = this.postSelectorParameter.current();
		postSelector.setK(this.numberOfResultsParameter.current());
		postSelector.setQualityMeasure(this.targetFunctionParameter.current());
		beamSearch.setPostProcessor(postSelector);

		return beamSearch.call();
	}

	@Override
	public String caption() {
		return NAME;
	}

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.EXCEPTIONAL_SUBGROUP_DISCOVERY;
	}

	public Parameter<DataTable> getDatatableParameter() {
		return datatableParameter;
	}

	public FreePropositionalLogicParameter getPropositionalLogicParameter() {
		return propositionalLogicParameter;
	}

	public Parameter<List<Attribute<?>>> getTargetAttributesParameter() {
		return targetAttributesParameter;
	}

	public ModelClassParameter getModelClassParameter() {
		return modelClassParameter;
	}

	public Parameter<ModelDeviationMeasure> getModelDistanceFunctionParameter() {
		return modelDistanceFunctionParameter;
	}

	public MinimumFrequencyThresholdParameter getMinimumFrequencyThreshold() {
		return minimumFrequencyThreshold;
	}

	public SubgroupSetSelectorParameter getBeamSelectorParameter() {
		return beamSelectorParameter;
	}

	public BeamWidthParameter getBeamWidthParameter() {
		return beamSearch.getBeamWidthParameter();
	}

	public SubgroupSetSelectorParameter getPostSelectorParameter() {
		return postSelectorParameter;
	}

	public NumberOfResultsParameter getNumberOfResultsParameter() {
		return numberOfResultsParameter;
	}

	public NumberOfIntermediateResultsParameter getNumberOfIntermediateResultsParameter() {
		return numberOfIntermediateResultsParameter;
	}

	public MaxDepthParameter getMaxDepthParameter() {
		return maxDepthParameter;
	}

	public Parameter<PatternOptimizationFunction> getTargetFunctionParameter() {
		return targetFunctionParameter;
	}

	public Parameter<Set<Attribute<?>>> getDescriptorAttributesParameter() {
		return descriptorAttributesParameter;
	}

	@Override
	public String toString() {
		return NAME;
	}
}
