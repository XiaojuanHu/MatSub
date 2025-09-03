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

package de.unibonn.realkd.algorithms.association;

import static de.unibonn.realkd.common.base.Identifier.id;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.common.FreePropositionalLogicParameter;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.algorithms.sampling.ConsaptBasedSamplingMiner;
import de.unibonn.realkd.algorithms.sampling.DistributionFactory;
import de.unibonn.realkd.algorithms.sampling.FrequencyDistributionFactory;
import de.unibonn.realkd.algorithms.sampling.RareDistributionTimesPowerOfFrequencyFactory;
import de.unibonn.realkd.algorithms.sampling.SamplingParameters;
import de.unibonn.realkd.algorithms.sampling.SinglePatternPostProcessor;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.Parameters;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * Algorithm with UI for randomized association discovery.
 * 
 * @author Pavel Tokmakov
 * 
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 *
 */
public class AssociationSampler extends AbstractMiningAlgorithm<Association> {

	private ConsaptBasedSamplingMiner<Association> samplingMiner = null;

	private final RareDistributionTimesPowerOfFrequencyFactory rareOption = new RareDistributionTimesPowerOfFrequencyFactory();

	private final DistributionFactory frequencyOption = new FrequencyDistributionFactory();

	private final List<DistributionFactory> distributionOptions = ImmutableList.of(frequencyOption, rareOption);

	private final Parameter<DistributionFactory> distributionParameter;

	private final RangeEnumerableParameter<SinglePatternPostProcessor> postProcessingParameter = SamplingParameters
			.postProcessingParameter();

	private final Parameter<Integer> numberOfResultsParameter = SamplingParameters.numberOfResultsParameter();

	private final Parameter<Integer> numberOfSeedsParameter;

	private final RangeEnumerableParameter<PropositionalContext> propositionalLogicParameter;

	private final Parameter<PatternOptimizationFunction> optimizationFunctionParameter;

	public AssociationSampler(Workspace workspace) {
		super();

		propositionalLogicParameter = new FreePropositionalLogicParameter(workspace);
		distributionParameter = Parameters.rangeEnumerableParameter(id("seed_dist"),"Seed distribution", "", DistributionFactory.class,
				() -> distributionOptions);
		optimizationFunctionParameter = new AssociationTargetFunctionParameter();
		numberOfSeedsParameter = SamplingParameters.numberOfSeedsParameter(numberOfResultsParameter);

		registerParameter(propositionalLogicParameter);
		registerParameter(distributionParameter);
		registerParameter(numberOfResultsParameter);
		registerParameter(numberOfSeedsParameter);
		registerParameter(postProcessingParameter);
		registerParameter(optimizationFunctionParameter);
	}

	@Override
	protected Collection<Association> concreteCall() throws ValidationException {
		samplingMiner = new ConsaptBasedSamplingMiner<>(descr -> Associations.association(descr, ImmutableList.of()),
				pattern -> (LogicalDescriptor) pattern.descriptor(), propositionalLogicParameter.current(),
				distributionParameter.current().getDistribution(propositionalLogicParameter.current()),
				optimizationFunctionParameter.current(), postProcessingParameter.current(),
				numberOfResultsParameter.current(), numberOfSeedsParameter.current());
		Collection<Association> result = samplingMiner.call();
		samplingMiner = null;
		return result;
	}

	@Override
	protected void onStopRequest() {
		if (samplingMiner != null) {
			samplingMiner.requestStop();
		}
	}

	public void setRareOption() {
		distributionParameter.set(rareOption);
	}

	public void setFrequencyOption() {
		distributionParameter.set(frequencyOption);
	}

	public RareDistributionTimesPowerOfFrequencyFactory rareOption() {
		return rareOption;
	}

	@Override
	public String toString() {
		return "AssociationSampler|" + distributionParameter.current() + "|" + numberOfResultsParameter.current();
	}

	@Override
	public String caption() {
		return "2-Step Association Sampling";
	}

	@Override
	public String description() {
		return "Direct Association Sampler";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.ASSOCIATION_MINING;
	}

	public void setNumberOfResults(int numberOfResults) {
		numberOfResultsParameter.set(numberOfResults);
	}

	public Parameter<PatternOptimizationFunction> getTargetFunctionParameter() {
		return optimizationFunctionParameter;
	}
}
