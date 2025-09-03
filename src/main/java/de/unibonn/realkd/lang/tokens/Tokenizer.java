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
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Tokenizer {

	public static class PatternToTokenMatcher {

		private Pattern pattern;

		private Function<String, Token> action;

		public PatternToTokenMatcher(Pattern pattern, Function<String, Token> action) {
			this.pattern = pattern;
			this.action = action;
		}

		public Optional<String> setIfMatched(Scanner scanner) {
			String string = scanner.findWithinHorizon(pattern, 0);
//			scanner.match().
			return Optional.ofNullable(string);
		}

	}

	public static PatternToTokenMatcher matcher(Pattern pattern, Function<String, Token> stringToToken) {
		return new PatternToTokenMatcher(pattern, stringToToken);
	}

	private final List<PatternToTokenMatcher> matchers;

	private final Scanner scanner;

	private Token current;

	public Tokenizer(Scanner input, List<PatternToTokenMatcher> matchers, Token startToken) {
		this.scanner = input;
		this.matchers = matchers;
		this.current = startToken;
	}

	public Token current() {
		return current;
	}

	public Token advance(Token expected) throws ParseException {
		if (!current.equals(expected)) {
			throw new ParseException("expected " + expected + " but found " + current);
		}
		return advance();
	}

	public Token advance() throws ParseException {
		boolean matched = false;
		scanner.findWithinHorizon(Parser.WHITESPACE, 0);
		scanner.findWithinHorizon(Parser.COMMENT, 0);
		for (Tokenizer.PatternToTokenMatcher matcher : matchers) {
			Optional<String> match = matcher.setIfMatched(scanner);
			if (match.isPresent()) {
				current = matcher.action.apply(match.get());
				matched = true;
				break;
			}
		}
		if (!matched) {
			throw new ParseException("unexpected input: " + scanner.next());
		}
		return current;
	}

}