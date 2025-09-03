/*
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
 */

package de.unibonn.realkd.algorithms.emm;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.beamsearch.BeamSearch;
import de.unibonn.realkd.algorithms.common.MiningParameters;
import de.unibonn.realkd.algorithms.common.PatternConstraint;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.algorithms.common.PropositionFilter;
import de.unibonn.realkd.algorithms.derived.SimpleParameterAdapter;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;

public class ExceptionalModelBeamSearch extends AbstractMiningAlgorithm<Pattern<?>> {

	private final Parameter<DataTable> datatableParameter;

	private final Parameter<List<Attribute<?>>> targets;

	private final Parameter<Set<Attribute<? extends Object>>> descriptorAttributeFilterParameter;

	private final Parameter<PatternOptimizationFunction> targetFunctionParameter;

	private final ModelClassParameter emmModelClassParameter;

	private final RangeEnumerableParameter<ModelDeviationMeasure> modelDistanceFunctionParameter;

	private final BeamSearch beamSearch;

	private final Parameter<PropositionalContext> propLogicParameter;

	public ExceptionalModelBeamSearch(Workspace workspace) {
		datatableParameter = MiningParameters.dataTableParameter(workspace);
		propLogicParameter = MiningParameters.matchingPropositionalLogicParameter(workspace, datatableParameter);
		targets = EMMParameters.getEMMTargetAttributesParameter(datatableParameter);
		descriptorAttributeFilterParameter = EMMParameters.getEMMDescriptorAttributesParameter(datatableParameter,
				targets);
		emmModelClassParameter = new ModelClassParameter(targets);
		modelDistanceFunctionParameter = EMMParameters.distanceFunctionParameter(emmModelClassParameter);
		targetFunctionParameter = EMMParameters.emmTargetFunctionParameter(emmModelClassParameter);

		// create wrapped beam search instance and configure it with all options
		// that can be set statically without waiting for any user input coming
		// in from through our parameters
		beamSearch = new BeamSearch(workspace,
				new ParameterBoundLogicalDescriptorToEmmPatternMap(datatableParameter, targets, emmModelClassParameter,
						modelDistanceFunctionParameter),
				ExceptionalModelMining.exceptionalSubgroupToExtensionDescriptor());
		Predicate<Proposition> targetFilter = new TargetAttributePropositionFilter(datatableParameter, targets);

		Predicate<Proposition> additionalAttributeFilter = prop -> !((prop instanceof AttributeBasedProposition)
				&& descriptorAttributeFilterParameter.current()
						.contains(((AttributeBasedProposition<?>) prop).attribute()));
		beamSearch.setPropositionFilter(PropositionFilter.fromPredicate(additionalAttributeFilter.and(targetFilter)));

		beamSearch.addPruningConstraint(PatternConstraint.POSITIVE_FREQUENCY);

		new SimpleParameterAdapter<PropositionalContext>(beamSearch.getPropositionalLogicParameter(),
				propLogicParameter);

		new SimpleParameterAdapter<>(beamSearch.getTargetFunctionParameter(), targetFunctionParameter);

		registerParameter(datatableParameter);
		registerParameter(targets);
		registerParameter(emmModelClassParameter);
		registerParameter(modelDistanceFunctionParameter);
		registerParameter(propLogicParameter);
		registerParameter(descriptorAttributeFilterParameter);
		registerParameter(targetFunctionParameter);
		registerParameter(beamSearch.getNumberOfResultsParameter());
		registerParameter(beamSearch.getBeamWidthParameter());
	}

	@Override
	public String toString() {
		return "EMMBeamSearch";
	}

	@Override
	public String caption() {
		return "Greedy Exceptional Subgroup Discovery";
	}

	@Override
	public String description() {
		return "Level-wise beam search algorithm for discovering outstanding subgroups.";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.EXCEPTIONAL_SUBGROUP_DISCOVERY;
	}

	public Parameter<DataTable> getDataTableParameter() {
		return datatableParameter;
	}

	public Parameter<List<Attribute<?>>> getTargetAttributesParameter() {
		return targets;
	}

	public Parameter<PatternOptimizationFunction> getTargetFunctionParameter() {
		return targetFunctionParameter;
	}

	public ModelClassParameter getModelClassParameter() {
		return this.emmModelClassParameter;
	}

	public RangeEnumerableParameter<ModelDeviationMeasure> getModelDistanceFunctionParameter() {
		return this.modelDistanceFunctionParameter;
	}

	@Override
	protected Collection<Pattern<?>> concreteCall() throws ValidationException {
		return beamSearch.call();
	}

	@Override
	protected void onStopRequest() {
		beamSearch.requestStop();
	}

}
