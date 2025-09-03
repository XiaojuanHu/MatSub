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
package de.unibonn.realkd.lang.statements;

import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Workspace;

/**
 * Shells allow the execution of statements via inversion of control, i.e., a
 * concrete statement implements its behavior and manipulates the state of the
 * shell.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.2
 *
 */
public interface Interpreter {

	public void requestTermination();

	public void print(String msg);

	public void printJobs();

	public void runJob(MiningAlgorithm algorithm, Identifier entityId);
	
	public void set(String variable, String value);

	public Workspace workspace();

	public Character readChar(String msg, Character defaultOption, ImmutableSet<Character> otherOptions);

	public default boolean confirmWithPositiveDefault(String msg) {
		return readChar(msg, 'y', ImmutableSet.of('n')).equals('y');
	}
	
//	public List<Future<Collection<Pattern>>> getResults();

}
