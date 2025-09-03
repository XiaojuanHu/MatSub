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

import java.io.BufferedReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.common.workspace.Workspaces;

/**
 * @author mboley
 *
 */
public class BatchInterpreter extends AbstractInterpreter {

	BatchInterpreter(Iterator<AlgorithmProvider> algorithmProviders, Iterator<FunctionProvider> functionProviders,
			Iterator<ConstantProvider> constantProvider) {
		super(Workspaces.workspace(), algorithmProviders, functionProviders, constantProvider);
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("expect script filename as single argument");
			return;
		}
		BatchInterpreter interpreter = Intepreters.createBatchInterpreter();
		interpreter.interpret(args[0]);
	}

	public void interpret(String filename) {
		Path path = FileSystems.getDefault().getPath(filename);
		try {
			BufferedReader reader = Files.newBufferedReader(path);
			String line = null;
			while ((line = reader.readLine()) != null && !terminationRequested) {
				System.out.println(">" + line);
				Scanner input = new Scanner(line);
				interpret(input);
			}
		} catch (Exception e) {
			System.err.println("error: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			executor.shutdown();
		}
	}

	@Override
	public Character readChar(String msg, Character defaultOption, ImmutableSet<Character> otherOptions) {
		return defaultOption;
	}

}
