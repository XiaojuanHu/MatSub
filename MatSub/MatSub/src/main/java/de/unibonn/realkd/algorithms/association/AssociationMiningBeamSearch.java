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

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.beamsearch.BeamSearch;
import de.unibonn.realkd.algorithms.common.PatternConstraint;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.algorithms.derived.SimpleParameterAdapter;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Associations;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * Algorithm that performs association discovery using beam search.
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.0
 * 
 * @version 0.1.0.1
 * 
 */
public class AssociationMiningBeamSearch extends AbstractMiningAlgorithm {

	private final AssociationTargetFunctionParameter targetFunctionParameter;
	private final BeamSearch beamSearch;

	public AssociationMiningBeamSearch(Workspace workspace) {
		targetFunctionParameter = new AssociationTargetFunctionParameter();

		// create and configure beamsearch
		beamSearch = new BeamSearch(workspace, descr -> Associations.association(descr, ImmutableList.of()),
				pattern -> (LogicalDescriptor) pattern.descriptor());
		beamSearch.addPruningConstraint(PatternConstraint.POSITIVE_FREQUENCY);
		beamSearch.addPruningConstraint(
				PatternConstraint.DESCRIPTOR_DOES_NOT_CONTAIN_TWO_ELEMENTS_REFERRING_TO_SAME_META_ATTRIBUTE);

		new SimpleParameterAdapter<>(beamSearch.getTargetFunctionParameter(), targetFunctionParameter);

		registerParameter(beamSearch.getPropositionalLogicParameter());
		registerParameter(targetFunctionParameter);
		registerParameter(beamSearch.getNumberOfResultsParameter());
		registerParameter(beamSearch.getBeamWidthParameter());

	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String caption() {
		return this.getClass().getSimpleName();
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.ASSOCIATION_MINING;
	}

	@Override
	public String description() {
		return "Association Mining using beam search strategy.";
	}

	public Parameter<PatternOptimizationFunction> getTargetFunctionParameter() {
		return targetFunctionParameter;
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
