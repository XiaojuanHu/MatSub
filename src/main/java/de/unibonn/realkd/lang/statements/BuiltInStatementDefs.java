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

import static de.unibonn.realkd.lang.statements.Statements.setStatement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.HasExportableForm;
import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.expressions.InterpretationException;
import de.unibonn.realkd.lang.types.StringValue;

/**
 * Contains definitions of statements that the parser turns into symbols that
 * can be parsed from the shell.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class BuiltInStatementDefs {

	private static final Logger LOGGER = Logger.getLogger(BuiltInStatementDefs.class.getName());

	public static final StatementDefinition EXPORT = new ExportDefinition();

	public static final StatementDefinition ADD = new AddDefinition();

	public static final StatementDefinition EXIT = new ExitDefinition();

	public static final StatementDefinition JOBS = new JobsDefinition();

	public static final StatementDefinition RUN = new RunDefinition();

	public static final StatementDefinition SET = new SetInterpreterVarDefinition();

	public static final List<StatementDefinition> ALL = ImmutableList.of(SET, ADD, EXPORT, EXIT, JOBS, RUN);

	private static final class SetInterpreterVarDefinition implements StatementDefinition {

		@Override
		public String name() {
			return "set";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ImmutableList.of(StringValue.class, StringValue.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return setStatement((Expression<StringValue>) args.get(0), (Expression<StringValue>) args.get(1));
		}

	}

	private static final class ExportDefinition implements StatementDefinition {

		@Override
		public String name() {
			return "export";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ImmutableList.of(Iterable.class, StringValue.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return new ExportStatement((Expression<Iterable<String>>) args.get(0),
					(Expression<StringValue>) args.get(1));
		}

	}

	private static final class ExportStatement implements Statement {

		private final Expression<Iterable<String>> contentExpr;

		private final Expression<StringValue> filename;

		public ExportStatement(Expression<Iterable<String>> content, Expression<StringValue> filename) {
			this.contentExpr = content;
			this.filename = filename;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			Iterable<String> content = contentExpr.evaluate(shell.workspace());
			Path path = FileSystems.getDefault().getPath(filename.evaluate(shell.workspace()).asString());
			try {
				if (content instanceof HasExportableForm) {
					String json = JsonSerialization.toPrettyJson(((HasExportableForm) content).exportableForm());
					Charset charset = Charset.forName("UTF-8");
					try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
						writer.write(json);
						writer.flush();
						writer.close();
					} catch (IOException e) {
						LOGGER.severe(e.getMessage());
					}
				} else {
					Files.write(path, content);
				}
			} catch (IOException e) {
				throw new InterpretationException(e.getMessage());
			}
		}

	}

	private static final class JobsDefinition implements StatementDefinition {

		@Override
		public String name() {
			return "jobs";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ImmutableList.of();
		}

		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return Statements.JOBS;
		}

	}

	private static final class RunDefinition implements StatementDefinition {

		private static final ImmutableList<Class<?>> ARG_TYPES = ImmutableList.of(MiningAlgorithm.class,
				StringValue.class);

		@Override
		public String name() {
			return "run";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ARG_TYPES;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return Statements.runStatement((Expression<MiningAlgorithm>) args.get(0),
					(Expression<StringValue>) args.get(1));
		}

	}

	private static final class ExitDefinition implements StatementDefinition {

		@Override
		public String name() {
			return "exit";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ImmutableList.of();
		}

		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return Statements.EXIT;
		}

	}

	private static final class AddDefinition implements StatementDefinition {

		private static final ImmutableList<Class<?>> ARG_TYPES = ImmutableList.of(Entity.class);

		@SuppressWarnings("unchecked")
		@Override
		public Statement toStatement(List<Expression<?>> args) {
			return Statements.addStatement((Expression<Entity>) args.get(0));
		}

		@Override
		public String name() {
			return "add";
		}

		@Override
		public List<Class<?>> argumentTypes() {
			return ARG_TYPES;
		}
	};

}
