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

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.IntegerValues.IntegerValue;
import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.expressions.IntegerDifference;
import de.unibonn.realkd.lang.expressions.IntegerExpression;
import de.unibonn.realkd.lang.expressions.IntegerInversion;
import de.unibonn.realkd.lang.expressions.IntegerProduct;
import de.unibonn.realkd.lang.expressions.IntegerSum;
import de.unibonn.realkd.lang.expressions.ListLiteral;
import de.unibonn.realkd.lang.expressions.NamedExpression;
import de.unibonn.realkd.lang.expressions.NumberDifference;
import de.unibonn.realkd.lang.expressions.NumberInversion;
import de.unibonn.realkd.lang.expressions.NumberProduct;
import de.unibonn.realkd.lang.expressions.NumberSum;
import de.unibonn.realkd.lang.expressions.SetDifference;
import de.unibonn.realkd.lang.expressions.SetIntersection;
import de.unibonn.realkd.lang.expressions.SetLiteral;
import de.unibonn.realkd.lang.expressions.SetUnion;
import de.unibonn.realkd.lang.expressions.StringSum;
import de.unibonn.realkd.lang.types.NumericValue;
import de.unibonn.realkd.lang.types.StringValue;

/**
 * Contains collection of all special character symbols reserved in the script
 * language. Can be programmatically accessed via
 * {@link SpecialSymbols#SPECIAL_SYMBOLS}.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public final class SpecialSymbols {

	// public static final CharSymbol DOT = new CharSymbol('.');

	public static final Symbol EQUALS = new EqualsSymbol();
	
	public static final Symbol COMMA = new Symbol(",");

	public static final Symbol PLUS = new PlusSymbol();

	public static final Symbol MINUS = new MinusSymbol();

	public static final Symbol ASTERISK = new ProductSymbol();

	public static final Symbol STICK = new Stick();

	public static final Symbol AMPERSAND = new Ampersand();

	public static final Symbol OPEN_BRACKET = new OpenBracket();

	public static final Symbol CLOSING_BRACKET = new Symbol("]");

	public static final Symbol OPEN_PARENS = new OpenParens();

	public static final Symbol CLOSE_PARENS = new Symbol(")");

	public static final Symbol OPEN_CURLY = new OpenCurly();

	public static final Symbol CLOSING_CURLY = new Symbol("}");

	public static final Symbol OF = new Symbol("of");

	public static final Symbol WITH = new Symbol("with");

	/**
	 * Collection of all special character symbols reserved in the script
	 * language.
	 */
	public static final Set<Symbol> SPECIAL_SYMBOLS = ImmutableSet.of(EQUALS, COMMA, PLUS, MINUS, ASTERISK, AMPERSAND,
			STICK, OPEN_BRACKET, CLOSING_BRACKET, OPEN_PARENS, CLOSE_PARENS, OPEN_CURLY, CLOSING_CURLY, OF,
			WITH);
	
	private static class EqualsSymbol extends Symbol {

		public EqualsSymbol() {
			super("=");
		}
		
		public int bindingPower() {
			return 10;
		}

		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser) throws ParseException {
			if (left instanceof UnboundName) {
				Expression<?> expression=parser.parseExpression(input, 10);
				Expression<?> result=new NamedExpression<>(((UnboundName) left).name(),expression);
				parser.define(new NamedExpressionSymbol((NamedExpression<?>) result));
				return result;
			}
			throw new ParseException("left operand must be unbound name");
		}
		
	}

	private static class PlusSymbol extends Symbol {

		public PlusSymbol() {
			super("+");
		}

		@Override
		public int bindingPower() {
			return 50;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser)
				throws ParseException {
			Expression<?> right = parser.parseExpression(input, 50);
			if (IntegerValue.class.isAssignableFrom(left.resultType())
					&& IntegerValue.class.isAssignableFrom(right.resultType())) {
				return new IntegerSum((Expression<IntegerValue>) left, (Expression<IntegerValue>) right);
			} else if (NumericValue.class.isAssignableFrom(left.resultType())
					&& NumericValue.class.isAssignableFrom(right.resultType())) {
				return new NumberSum((Expression<NumericValue>) left, (Expression<NumericValue>) right);
			} else if (StringValue.class.isAssignableFrom(left.resultType())
					&& StringValue.class.isAssignableFrom(right.resultType())) {
				return new StringSum((Expression<StringValue>) left, (Expression<StringValue>) right);
			}
			throw new ParseException(this + ": imcompatible operand types " + left + " and " + right);
		}

	}

	private static class Ampersand extends Symbol {

		public Ampersand() {
			super("&");
		}

		@Override
		public int bindingPower() {
			return 60;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser)
				throws ParseException {
			Expression<?> right = parser.parseExpression(input, 50);
			if (Set.class.isAssignableFrom(left.resultType()) && Set.class.isAssignableFrom(right.resultType())) {
				return new SetIntersection((Expression<Set<?>>) left, (Expression<Set<?>>) right);
			}
			throw new ParseException(this + ": imcompatible operand types " + left + " and " + right);
		}

	}

	private static class Stick extends Symbol {

		public Stick() {
			super("|");
		}

		@Override
		public int bindingPower() {
			return 50;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser)
				throws ParseException {
			Expression<?> right = parser.parseExpression(input, 50);
			if (Set.class.isAssignableFrom(left.resultType()) && Set.class.isAssignableFrom(right.resultType())) {
				return new SetUnion((Expression<Set<?>>) left, (Expression<Set<?>>) right);
			}
			throw new ParseException(this + ": imcompatible operand types " + left + " and " + right);
		}

	}

	private static class MinusSymbol extends Symbol {

		public MinusSymbol() {
			super("-");
		}

		@Override
		public int bindingPower() {
			return 50;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<?> parseInNullPosition(Tokenizer input, Parser parser) throws ParseException {
			Expression<?> right = parser.parseExpression(input);
			if (IntegerValue.class.isAssignableFrom(right.resultType())) {
				return new IntegerInversion((Expression<IntegerValue>) right);
			} else if (NumericValue.class.isAssignableFrom(right.resultType())) {
				return new NumberInversion((Expression<NumericValue>) right);
			}
			throw new ParseException(this + " expects integer or number");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser)
				throws ParseException {
			Expression<?> right = parser.parseExpression(input, 50);
			if (IntegerValue.class.isAssignableFrom(left.resultType())
					&& IntegerValue.class.isAssignableFrom(right.resultType())) {
				return new IntegerDifference((Expression<IntegerValue>) left, (Expression<IntegerValue>) right);
			} else if (NumericValue.class.isAssignableFrom(left.resultType())
					&& NumericValue.class.isAssignableFrom(right.resultType())) {
				return new NumberDifference((Expression<NumericValue>) left, (Expression<NumericValue>) right);
			} else if (Set.class.isAssignableFrom(left.resultType())
					&& Set.class.isAssignableFrom(right.resultType())) {
				return new SetDifference((Expression<Set<?>>) left, (Expression<Set<?>>) right);
			}
			throw new ParseException(this + ": imcompatible operand types " + left + " and " + right);
		}

	}

	private static class ProductSymbol extends Symbol {

		public ProductSymbol() {
			super("*");
		}

		@Override
		public int bindingPower() {
			return 60;
		}

		@Override
		public Expression<?> parseWithLeftOperand(Expression<?> left, Tokenizer input, Parser parser)
				throws ParseException {
			Expression<?> right = parser.parseExpression(input, 60);
			if (IntegerValue.class.isAssignableFrom(left.resultType())
					&& IntegerValue.class.isAssignableFrom(right.resultType())) {
				return new IntegerProduct((IntegerExpression) left, (IntegerExpression) right);
			} else if (NumericValue.class.isAssignableFrom(left.resultType())
					&& NumericValue.class.isAssignableFrom(right.resultType())) {
				return new NumberProduct((Expression<NumericValue>) left, (Expression<NumericValue>) right);
			}
			throw new ParseException(this + ": imcopatible operand types " + left + " and " + right);
		}

	}

	private static class OpenParens extends Symbol {

		private OpenParens() {
			super("(");
		}

		public Expression<?> parseInNullPosition(Tokenizer input, Parser parser) throws ParseException {
			Expression<?> expression = parser.parseExpression(input);
			input.advance(CLOSE_PARENS);
			return expression;
		}

	}

	private static class OpenBracket extends Symbol {

		private OpenBracket() {
			super("[");
		}

		@Override
		public Expression<?> parseInNullPosition(Tokenizer input, Parser parser) throws ParseException {
			Symbol closingToken = CLOSING_BRACKET;
			List<Expression<?>> listElements = parser.parseExpressionList(input, closingToken);
			return new ListLiteral(listElements);
		}

	}

	private static class OpenCurly extends Symbol {

		private OpenCurly() {
			super("{");
		}

		@Override
		public Expression<?> parseInNullPosition(Tokenizer input, Parser parser) throws ParseException {
			Symbol closingToken = CLOSING_CURLY;
			List<Expression<?>> elements = parser.parseExpressionList(input, closingToken);
			return new SetLiteral(elements);
		}

	}

	private SpecialSymbols() {
		;
	}

}
