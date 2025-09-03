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
package de.unibonn.realkd.lang.tokens;

import static de.unibonn.realkd.lang.tokens.SpecialSymbols.OF;
import static de.unibonn.realkd.lang.tokens.SpecialSymbols.WITH;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.expressions.FunctionInvocation;
import de.unibonn.realkd.lang.types.FunctionDefinition;

/**
 * Symbol that creates a function invocation when parsed in null position.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 * 
 */
public class FunctionSymbol extends Symbol {

	private final FunctionDefinition<?> function;

	private final Map<String, Class<?>> mandatoryArgumentNames;

	private final Map<String, Class<?>> optionalArgumentNames;

	public FunctionSymbol(FunctionDefinition<?> function) {
		super(function.name());
		this.function = function;
		mandatoryArgumentNames = new HashMap<>();
		loadParams(mandatoryArgumentNames, function.mandatoryArgumentNames(), function.otherMandatoryArgumentTypes());
		optionalArgumentNames = new HashMap<>();
		loadParams(optionalArgumentNames, function.optionalArguementNames(), function.otherOptionalArgumentTypes());
	}

	private void loadParams(Map<String, Class<?>> map, List<String> names, List<Class<?>> types) {
		for (int i = 0; i < names.size(); i++) {
			map.put(names.get(i), types.get(i));
		}
	}

	private boolean isOptionalArgumentName(Token token) {
		return (token instanceof NameToken && optionalArgumentNames.keySet().contains(((NameToken) token).name()));
	}

	private boolean isMandatoryArgumentName(Token token) {
		return (token instanceof NameToken && mandatoryArgumentNames.keySet().contains(((NameToken) token).name()));
	}

	@Override
	public Expression<?> parseInNullPosition(Tokenizer input, Parser parser) throws ParseException {
		if (input.current() == OF) {
			input.advance(OF);
		}
		Expression<?> mainArgExpression = parser.parseExpression(input, function.mainArgumentType());
		if (input.current() == WITH) {
			input.advance(WITH);
		}
		Map<String, Expression<?>> mandatoryTable = new HashMap<>();
		Map<String, Optional<Expression<?>>> optionalTable = new HashMap<>();
		int numberOfRequiredArgs = mandatoryArgumentNames.size();
		boolean endOfInvocationText = false;
		while (!endOfInvocationText) {
			Token current = input.current();
			if (isMandatoryArgumentName(current)) {
				String argumentName = ((NameToken) current).name();
				if (mandatoryTable.containsKey(argumentName)) {
					throw new ParseException("specified " + argumentName + " more than once");
				}
				input.advance();
				input.advance(SpecialSymbols.EQUALS);
				mandatoryTable.put(argumentName,
						parser.parseExpression(input, mandatoryArgumentNames.get(argumentName)));
				numberOfRequiredArgs--;
			} else if (isOptionalArgumentName(current)) {
				String argumentName = ((NameToken) current).name();
				if (optionalTable.containsKey(argumentName)) {
					throw new ParseException("specified " + argumentName + " more than once");
				}
				input.advance();
				input.advance(SpecialSymbols.EQUALS);
				Expression<?> expression = parser.parseExpression(input, optionalArgumentNames.get(argumentName));
				optionalTable.put(argumentName, Optional.of(expression));
			} else {
				endOfInvocationText = true;
			}
		}
		if (numberOfRequiredArgs > 0) {
			List<String> unspecified = mandatoryArgumentNames.keySet().stream()
					.filter(n -> !mandatoryTable.containsKey(n)).collect(Collectors.toList());
			throw new ParseException(this + ": not all mandatory argument specified; missing: " + unspecified);
		}
		for (String optArg : optionalArgumentNames.keySet()) {
			if (!optionalTable.containsKey(optArg)) {
				optionalTable.put(optArg, Optional.empty());
			}
		}

		return new FunctionInvocation<>(function, mainArgExpression,
				function.mandatoryArgumentNames().stream().map(n -> mandatoryTable.get(n)).collect(Collectors.toList()),
				function.optionalArguementNames().stream().map(n -> optionalTable.get(n)).collect(Collectors.toList()));
	}

}
