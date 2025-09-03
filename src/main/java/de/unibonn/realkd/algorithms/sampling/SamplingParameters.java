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
package de.unibonn.realkd.algorithms.sampling;

import static de.unibonn.realkd.common.base.Identifier.id;
import static de.unibonn.realkd.common.parameter.Parameters.dependentIntegerParameter;
import static de.unibonn.realkd.common.parameter.Parameters.integerParameter;
import static de.unibonn.realkd.common.parameter.Parameters.rangeEnumerableParameter;
import static de.unibonn.realkd.common.parameter.Parameters.doubleParameter;
import static de.unibonn.realkd.common.parameter.Parameters.stringParameter;

import java.util.Arrays;

import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.RangeEnumerableParameter;

/**
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 *
 */
public class SamplingParameters {

	private SamplingParameters() {
		;
	}

	public static Parameter<Integer> numberOfResultsParameter() {
		return integerParameter(id("num_res"), "Number of results", "Number of patterns in the results list.", 50,
				n -> n > 0, "Specify positive integer.");
	}

	public static Parameter<Integer> numberOfSeedsParameter(Parameter<Integer> numberOfResultsParam) {
		return dependentIntegerParameter(id("num_seeds"),"Number of seeds",
				"Number of times a seed is generated and subsequently heuristically optimized.",
				n -> n >= numberOfResultsParam.current(), "Specify integer not less than number of results.",
				() -> numberOfResultsParam.current() * 20, numberOfResultsParam);
	}

	public static RangeEnumerableParameter<SinglePatternPostProcessor> postProcessingParameter() {
		return rangeEnumerableParameter(id("post_proc"),"Post processing",
				"Local optimization algorithm applied to random seed after sampling.", SinglePatternPostProcessor.class,
				() -> Arrays.asList(SinglePatternPostProcessor.values()));
	}

    public static Parameter<Double> qualityFunctionParameter () {
	return doubleParameter(id("cutoff_in_util"), "border",
				  "cutoff parameter in NormalizedMaxWithConstatntRef", 2, n -> n > 0, "Specify number greater than 0");
    }

    public static Parameter<String> qualityFunctionParameters () {
	return stringParameter(id("qual_func_params"), "params", "quality function parameters", " ", n -> n.length()>0,
			       "Specify a string of numbers separated by spaces");
    }

    public static Parameter<String> hardCutoffParameters () {
        return stringParameter(id("hard_cutoffs"), "hard_cutoff_type", "hard cutoff types: above, below, or within", " ", n -> n.length()>0,
                               "Specify a string of keywords separated by spaces");
    }

    public static Parameter<Integer> numberOfThreadsParameter () {
        return integerParameter(id("num_threads"), "number of threads",
                                  "number of threads in Consapt", 1, n -> n > 0, "Specify positive integer");
    }

}
