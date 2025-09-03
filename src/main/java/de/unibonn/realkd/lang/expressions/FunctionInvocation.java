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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.types.FunctionDefinition;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class FunctionInvocation<T> implements Expression<T> {

	private final FunctionDefinition<T> function;

	private final Expression<?> mainArgumentExpression;

	private final List<Expression<?>> otherMandatoryArgExpressions;

	private final List<Optional<Expression<?>>> otherOptionalArgExpressions;

	public FunctionInvocation(FunctionDefinition<T> function, Expression<?> mainArgumentExpression,
			List<Expression<?>> otherMandatoryArgExpressions,
			List<Optional<Expression<?>>> otherOptionalArgExpressions) {
		this.function = function;
		this.mainArgumentExpression = mainArgumentExpression;
		this.otherMandatoryArgExpressions = otherMandatoryArgExpressions;
		this.otherOptionalArgExpressions = otherOptionalArgExpressions;
	}

	@Override
	public T evaluate(Workspace workspace) throws InterpretationException {
		Function<? super Expression<?>, ?> mapper = e -> {
			try {
				return e.evaluate(workspace);
			} catch (InterpretationException excep) {
				throw new RuntimeException(excep.getMessage());
			}
		};
		try {
			List<Object> mandatoryArguments = otherMandatoryArgExpressions.stream().map(mapper)
					.collect(Collectors.toList());
			List<Optional<Object>> optionalArguments = otherOptionalArgExpressions.stream()
					.map((Function<Optional<Expression<?>>, Optional<Object>>) o -> o.map(mapper))
					.collect(Collectors.toList());
			return function.evaluate(mainArgumentExpression.evaluate(workspace), mandatoryArguments, optionalArguments);
		} catch (RuntimeException e) {
			throw new InterpretationException(e.getMessage());
		}
	}

	@Override
	public Class<?> resultType() {
		return function.resultType();
	}

}
