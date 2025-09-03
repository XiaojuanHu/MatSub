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

import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.statements.Statement;

/**
 * <p>
 * Tokens are the elemental syntactic unit of the shell language that are
 * produced by the tokenizer when scanning the input. Tokens jointly with the
 * parser can then transform themselves into a structures statement or
 * expression tree.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Token {

	private final String stringValue;

	public Token(String stringValue) {
		this.stringValue = stringValue;
	}

	public int bindingPower() {
		return 0;
	}

	public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser) throws ParseException {
		throw new ParseException(this + " expects no left operand");
	}

	public Expression<?> parseInNullPosition(Tokenizer tokens, Parser parser) throws ParseException {
		throw new ParseException(this + " needs left operand");
	}

	public Statement parseInStatementPosition(Tokenizer tokens, Parser parser) throws ParseException {
		throw new ParseException(this + " cannot be parsed as a statement");
	}

	/**
	 * @return whether token can be parsed in statement position
	 */
	public boolean statement() {
		return false;
	}

	@Override
	public String toString() {
		return stringValue;
	}

}