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
package de.unibonn.realkd.lang.expressions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.workspace.Workspace;

@SuppressWarnings("rawtypes")
public class SetLiteral implements Expression<Set> {

	private final Set<Expression<?>> elements;

	public SetLiteral(List<Expression<?>> elements) {
		this.elements = ImmutableSet.copyOf(elements);
	}

//	public List<Expression<?>> elements() {
//		return elements;
//	}

	@Override
	public Set<?> evaluate(Workspace workspace) throws InterpretationException {
		Set<Object> result = new HashSet<>();
		for (Expression<?> exp : elements) {
			result.add(exp.evaluate(workspace));
		}
		return result;
	}

	@Override
	public Class<Set> resultType() {
		return Set.class;
	}

//	public <T> List<T> evaluateAs(DataWorkspace workspace, Class<T> clazz) throws InterpretationException {
//		List<T> result = new ArrayList<>();
//		for (Expression exp : elements) {
//			Object res = exp.evaluate(workspace);
//			try {
//				result.add(clazz.cast(res));
//			} catch (ClassCastException exc) {
//				throw new InterpretationException(res.toString() + " could not be cast to " + clazz);
//			}
//		}
//		return result;
//	}

}