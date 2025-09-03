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

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.statements.Statement;
import de.unibonn.realkd.lang.statements.StatementDefinition;

/**
 * Symbol for generic statement definition. Parses a number of arguments of
 * appropriate type as given in the definition.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public final class StatementSymbol extends Symbol {

	private final StatementDefinition definition;

	public StatementSymbol(StatementDefinition def) {
		super(def.name());
		this.definition = def;
	}

	@Override
	public Statement parseInStatementPosition(Tokenizer tokens, Parser parser) throws ParseException {
		List<Expression<?>> args = newArrayList();
		for (Class<?> argType : definition.argumentTypes()) {
			args.add(parser.parseExpression(tokens, argType));
		}
		if (tokens.current() != Parser.END && tokens.current()!=Parser.END_OF_LINE) {
			throw new ParseException(this + ": unexpected input '"+tokens.current()+"'");
		}
		if (tokens.current()==Parser.END_OF_LINE) {
			tokens.advance(); //consume EOL as last symbol of statement
		}
		return definition.toStatement(args);
	}

	@Override
	public boolean statement() {
		return true;
	}

}
