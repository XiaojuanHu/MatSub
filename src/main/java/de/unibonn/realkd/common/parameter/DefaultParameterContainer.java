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
package de.unibonn.realkd.common.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Provides default mutable implementation of interface ParameterContainer that
 * can be wrapped by classes that entail parameters.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.1.1
 * 
 */
public class DefaultParameterContainer implements ParameterContainer {

	private final LinkedHashMap<String, Parameter<?>> parameterMap = new LinkedHashMap<>();

	public DefaultParameterContainer() {
		;
	}

	@Override
	public final List<Parameter<?>> getTopLevelParameters() {
		List<Parameter<?>> result = new ArrayList<>();

		for (String key : parameterMap.keySet()) {
			result.add(parameterMap.get(key));
		}

		return result;
	}

	public final void addParameter(Parameter<?> parameter) {
		if (parameterMap.containsKey(parameter.getName())) {
			throw new IllegalArgumentException(
					"Parameter with name is already present");
		}
		parameterMap.put(parameter.getName(), parameter);
	}

	public final void addAllParameters(Collection<Parameter<?>> parameters) {
		for (Parameter<?> param : parameters) {
			this.addParameter(param);
		}
	}

}
