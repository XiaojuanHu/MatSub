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
package de.unibonn.realkd.algorithms.emm;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.emm.ModelDeviationMeasure;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.subgroups.Subgroups;

/**
 * <p>
 * Uses the current value of a set of emm parameter (table, target attributes,
 * etc.) to map a logical descriptor to an emm pattern.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 * 
 */
public class ParameterBoundLogicalDescriptorToEmmPatternMap
		implements Function<LogicalDescriptor, ExceptionalModelPattern> {

	private final Parameter<List<Attribute<?>>> targetAttributesParameter;

	private final ModelClassParameter emmModelClassParameter;

	private final Parameter<ModelDeviationMeasure> distanceFunctionParameter;

	private final Parameter<DataTable> dataTableParameter;

	public ParameterBoundLogicalDescriptorToEmmPatternMap(Parameter<DataTable> dataTableParameter,
			Parameter<List<Attribute<?>>> targetAttributesParameter, ModelClassParameter emmModelClassParameter,
			Parameter<ModelDeviationMeasure> distanceFunctionParameter) {

		this.targetAttributesParameter = targetAttributesParameter;
		this.emmModelClassParameter = emmModelClassParameter;
		this.distanceFunctionParameter = distanceFunctionParameter;
		this.dataTableParameter = dataTableParameter;
	}

	@Override
	public ExceptionalModelPattern apply(LogicalDescriptor description) {

		return ExceptionalModelMining.emmPattern(
				Subgroups.subgroup(description, dataTableParameter.current(),
						targetAttributesParameter.current(), emmModelClassParameter.current().get()),
				distanceFunctionParameter.current(), ImmutableList.of());

	}
}
