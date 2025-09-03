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
package de.unibonn.realkd.lang;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.statements.Interpreter;
import jline.console.ConsoleReader;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public final class Shell extends AbstractInterpreter implements Interpreter {

	Shell(Workspace workspace, ConsoleReader reader, Iterator<AlgorithmProvider> algorithmProviders,
			Iterator<FunctionProvider> functionProviders, Iterator<ConstantProvider> constantProvider) {
		super(workspace, algorithmProviders, functionProviders, constantProvider);
		this.reader = reader;
	}

	private final ConsoleReader reader;

	@Override
	public Character readChar(String msg, Character defaultOption, ImmutableSet<Character> otherOptions) {
		Character input = null;
		while (!defaultOption.equals(input) && !otherOptions.contains(input)) {
			print(msg + " (" + defaultOption + " or " + otherOptions + ")");
			try {
				input = new Character((char) reader.readCharacter());
			} catch (IOException e) {
				return defaultOption;
			}
		}
		return new Character((char) input);
	}

	public void run() {
		do {
			try {
				String input = reader.readLine();
				Scanner scanner = new Scanner(input);
				interpret(scanner);
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		} while (!terminationRequested);
	}

}
