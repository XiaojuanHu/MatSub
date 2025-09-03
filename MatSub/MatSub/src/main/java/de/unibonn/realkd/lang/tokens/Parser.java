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

import static de.unibonn.realkd.lang.tokens.SpecialSymbols.SPECIAL_SYMBOLS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.ConstantProvider;
import de.unibonn.realkd.lang.FunctionProvider;
import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.statements.BuiltInStatementDefs;
import de.unibonn.realkd.lang.statements.Statement;
import de.unibonn.realkd.lang.statements.Statements;

/**
 * Based on a given list of terminal symbols, converts a list of primitive input
 * tokens into a parsed token tree.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Parser {

	public static final Token END = new Token("(END)");

	public static final Token END_OF_LINE = new Token("(EOL)");

	public static final Token START = new Token("(START)");

	public static final Pattern WHITESPACE = Pattern.compile("\\G[ \t\f\r]+");

	public static final Pattern COMMENT = Pattern.compile("\\G\\-\\-.*?\\z");
	
	public static final Pattern FLOAT = Pattern.compile("\\G-?[0-9]+\\.[0-9]*");

	public static final Pattern INTEGER = Pattern.compile("\\G-?[0-9]+");

	public static final Pattern STRING = Pattern.compile("\\G\\\".*?\\\"");

	public static final Pattern SYMBOL = Pattern.compile("\\G[\\*|\\+|\\-|\\(|\\)|\\[|\\]|\\{|\\}|\\,\\=]");

	public static final Pattern STICK = Pattern.compile("\\G\\|");
	
	public static final Pattern AMPERSAND = Pattern.compile("\\G\\&");
	
	public static final Pattern NAME = Pattern.compile("\\G[a-zA-Z][a-zA-Z0-9_]*");

	public static final Pattern EOL = Pattern.compile("\\G\n");

	public static final Pattern EOI = Pattern.compile("\\G\\z");

	private final HashMap<String, Symbol> symbols = new HashMap<>();

	private final ImmutableList<Tokenizer.PatternToTokenMatcher> patternToTokenMatchers;

	public Parser(Workspace workspace, Iterator<AlgorithmProvider> algorithmProviders,
			Iterator<FunctionProvider> functionProviders, Iterator<ConstantProvider> constantProvider) {
//		symbol(DOUBLE_STICK);
		SPECIAL_SYMBOLS.forEach(s -> symbol(s));
		BuiltInStatementDefs.ALL.forEach(def -> symbol(new StatementSymbol(def)));
		constantProvider.forEachRemaining(p -> {
			System.out.println("Loading constant symbols for " + p.name());
			p.get().forEach(f -> {
				symbol(new ConstantSymbol(f));
			});
		});
		functionProviders.forEachRemaining(p -> {
			System.out.println("Loading function symbols for " + p.name());
			p.get().forEach(f -> {
				symbol(new FunctionSymbol(f));
			});
		});
		algorithmProviders.forEachRemaining(p -> {
			System.out.println("Loading algorithm symbols for " + p.name());
			p.get().forEach(a -> {
				symbol(new AlgorithmSymbol(a));
			});
		});

		patternToTokenMatchers = ImmutableList.of(
				Tokenizer.matcher(STICK, s -> symbols.containsKey(s) ? symbols.get(s)
						: new NameToken(s)),
				Tokenizer.matcher(AMPERSAND, s -> symbols.containsKey(s) ? symbols.get(s)
						: new NameToken(s)),
				Tokenizer.matcher(SYMBOL, s -> symbols.containsKey(s) ? symbols.get(s)
						: new NameToken(s)),
				Tokenizer.matcher(STRING, s -> new StringToken(s.substring(1, s.length() - 1))),
				Tokenizer.matcher(FLOAT, s -> new NumberToken(Double.parseDouble(s))),
				Tokenizer.matcher(INTEGER, s -> new IntegerToken(Integer.parseInt(s))),
				Tokenizer.matcher(NAME, s -> symbols.containsKey(s) ? symbols.get(s)
						: (workspace.contains(Identifier.id(s)) ? new ExistingEntityNameToken(s, workspace.get(Identifier.id(s)).getClass())
								: new NameToken(s))),
				Tokenizer.matcher(EOL, s -> END_OF_LINE), Tokenizer.matcher(EOI, s -> END));
	}

	private void symbol(Symbol symbol) {
		symbols.put(symbol.stringForm(), symbol);
	}
	
	public void define(Symbol s) throws ParseException {
		if (symbols.containsKey(s.stringForm())) {
			throw new ParseException(s.stringForm()+" already defined.");
		}
		this.symbol(s);
	}

	public List<Statement> parse(Scanner input) throws ParseException {
		return parseStatements(new Tokenizer(input, patternToTokenMatchers, START));
	}

	public Statement parseStatement(Tokenizer input) throws ParseException {
		Token token = input.current();
		if (token == END) {
			return Statements.EMPTY;
		}
		if (token == END_OF_LINE) {
			input.advance();
			return Statements.EMPTY;
		}
		if (token.statement()) {
			input.advance(); // analog to expression
			return token.parseInStatementPosition(input, this);
		}
		Expression<?> expression = parseExpression(input);
		if (input.current() != END_OF_LINE && input.current() != END) {
			throw new ParseException("unexpected token: " + input.current());
		}
		return Statements.evaluationStatement(expression);
	}

	public List<Statement> parseStatements(Tokenizer input) throws ParseException {
		List<Statement> result = new ArrayList<>();
		input.advance(); // advance start token
		while (input.current() != END) {
			result.add(parseStatement(input));
		}
		return result;
	}

	public Expression<?> parseExpression(Tokenizer input) throws ParseException {
		return parseExpression(input, 0);
	}

	@SuppressWarnings("unchecked")
	public <T> Expression<T> parseExpression(Tokenizer input, Class<T> type) throws ParseException {
		Expression<?> expression = parseExpression(input, 0);
		if (type.isAssignableFrom(expression.resultType())) {
			return (Expression<T>) expression;
			// return type.cast(expression);
		}
		throw new ParseException("expected expression of type '" + type.getSimpleName() + "' but found '"
				+ expression.getClass().getSimpleName() + "'");
	}

	public Expression<?> parseExpression(Tokenizer input, int bindingPower) throws ParseException {
		Token start = input.current();
		input.advance(); // consume current token (tokens expect themselves to
							// be consumed when parsing control is passed over
							// to them)
		Expression<?> left = start.parseInNullPosition(input, this);
		while (input.current() != END && bindingPower < input.current().bindingPower()) {
			Token right = input.current();
			input.advance();
			left = right.parseWithLeftOperand(left, input, this);
		}

		return left;
	}

	public List<Expression<?>> parseExpressionList(Tokenizer input, Symbol closingToken) throws ParseException {
		List<Expression<?>> listElements = new ArrayList<>();
		while (input.current() != closingToken) {
			listElements.add(parseExpression(input));
			if (input.current() != SpecialSymbols.COMMA && input.current() != closingToken) {
				throw new ParseException("found " + input.current() + " but expected separator or "
						+ closingToken.stringForm() + " in expression list");
			}
			if (input.current() == SpecialSymbols.COMMA) {
				input.advance();
			}
		}
		input.advance(closingToken);
		return listElements;
	}

}
