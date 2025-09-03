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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.lang.statements.Interpreter;
import de.unibonn.realkd.lang.statements.Statement;
import de.unibonn.realkd.lang.tokens.Parser;
import de.unibonn.realkd.patterns.NamedPatternCollection;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.2
 *
 */
public abstract class AbstractInterpreter implements Interpreter {

	private static Logger LOGGER = Logger.getLogger(AbstractInterpreter.class.getName());

	private static abstract class InterpreterVariable {

		private final String name;

		public InterpreterVariable(String name) {
			this.name = name;
		}

		public abstract void set(String value);

	}

	private static class LogFileVar extends InterpreterVariable {

		public LogFileVar() {
			super("logfile");
		}

		@Override
		public void set(String value) {
			try {
				FileHandler logFileHandler = new FileHandler(value, 1000000, 10);
				logFileHandler.setFormatter(new SimpleFormatter());
				Logger.getLogger("").addHandler(logFileHandler);
			} catch (SecurityException e1) {
				LOGGER.severe("No permission to log file: " + value);
			} catch (IOException e1) {
				LOGGER.severe("IO exception when trying to access log file: " + value);
			}
		}

	}

	protected final Map<String, InterpreterVariable> variables = new HashMap<>();

	protected final Parser parser;
	protected final List<MiningAlgorithm> jobs;
	protected boolean terminationRequested = false;
	protected final ExecutorService executor;
	protected final Workspace workspace;
	private final InterpreterVariable logfile;

	public AbstractInterpreter(Workspace workspace, Iterator<AlgorithmProvider> algorithmProviders,
			Iterator<FunctionProvider> functionProviders, Iterator<ConstantProvider> constantProviders) {
		this.logfile=new LogFileVar();
		this.variables.put(logfile.name, logfile);
		this.workspace = workspace;
		this.parser = new Parser(workspace, algorithmProviders, functionProviders, constantProviders);
		this.jobs = new ArrayList<>();
		this.executor = Executors.newFixedThreadPool(4);
	}

	public void interpret(Scanner scanner) throws Exception {
		List<Statement> parse;
//		try {
			parse = parser.parse(scanner);
			for (Statement statement : parse) {
				statement.execute(this);
			}
//		} catch (ParseException e) {
//			System.err.println("Parsing error: " + e.getMessage());
//		} catch (InterpretationException e) {
//			System.err.println("Intepretation error: " + e.getMessage());
//		} catch (Exception e) {
//			System.err.println("realKD error: " + e.getMessage());
//		}
	}

	@Override
	public void print(String msg) {
		System.out.println(msg);
	}

	private boolean existsUnfinishedJob() {
		return jobs.stream().filter(j -> j.running()).findFirst().isPresent();
	}

	public void set(String name, String value) {
		InterpreterVariable variable = variables.get(name);
		if (variable == null) {
			LOGGER.warning(String.format("Unknown variable %s; ignoring 'set'.", name));
			return;
		}
		variable.set(value);
	}

	public void requestTermination() {
		terminationRequested = true;
		executor.shutdown();
		if (existsUnfinishedJob()) {
			jobs.forEach(j -> {
				if (j.running() && j instanceof StoppableMiningAlgorithm) {
					print("requesting '" + j + "' to stop");
					((StoppableMiningAlgorithm) j).requestStop();
				}
			});
			try {
				wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (existsUnfinishedJob()) {
			if (confirmWithPositiveDefault("There is at least one unfinished job. Wait before termination?")) {
				while (existsUnfinishedJob()) {
					;
				}
			}
		}
		executor.shutdownNow();
		if (!executor.isTerminated()) {
			LOGGER.warning(() -> "Executor did not terminate.");
		}
	}

	public Workspace workspace() {
		return workspace;
	}

	@Override
	public void runJob(MiningAlgorithm algorithm, Identifier resultId) {
		jobs.add(algorithm);
		Future<Identifiable> result = executor.submit(
				() -> new NamedPatternCollection(resultId, "Results of " + algorithm.caption(), "", algorithm.call()));
		workspace.addFuture(resultId, result);
	}

	@Override
	public void printJobs() {
		System.out.println("List of jobs");
		System.out.println("------------");
		if (jobs.isEmpty()) {
			System.out.println("(empty)");
		}
		for (int i = 0; i < jobs.size(); i++) {
			System.out.println(i + ": " + jobs.get(i) + (jobs.get(i).running() ? " (running)" : " (done)"));
		}
	}

}