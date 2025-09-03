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

import de.unibonn.realkd.algorithms.MiningAlgorithm;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.lang.expressions.Expression;
import de.unibonn.realkd.lang.expressions.InterpretationException;
import de.unibonn.realkd.lang.types.StringValue;

/**
 * Contains factory methods for creating statements that can be executed with a
 * shell.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.5.2
 *
 */
public final class Statements {

	public static final Statement EMPTY = new EmptyStatement();

	public static final Statement EXIT = new ExitStatement();

	public static final Statement JOBS = new JobsStatement();

	public static Statement addStatement(Expression<Entity> entityExp) {
		return new AddStatement(entityExp);
	}

	public static Statement runStatement(Expression<MiningAlgorithm> algExp, Expression<StringValue> resultId) {
		return new RunStatement(algExp, resultId);
	}

	public static Statement evaluationStatement(Expression<?> expression) {
		return new EvaluationStatement(expression);
	}

	public static Statement setStatement(Expression<StringValue> variable, Expression<StringValue> value) {
		return new SetStatement(variable, value);
	}

	private static class JobsStatement implements Statement {

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			shell.printJobs();
		}

	}

	private static class SetStatement implements Statement {

		private final Expression<StringValue> variable;

		private final Expression<StringValue> value;

		private SetStatement(Expression<StringValue> variable, Expression<StringValue> value) {
			this.variable = variable;
			this.value = value;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			shell.set(variable.evaluate(shell.workspace()).asString(), value.evaluate(shell.workspace()).asString());
		}

	}

	private static class AddStatement implements Statement {

		private final Expression<?> entityExp;

		private AddStatement(Expression<?> entityExp) {
			this.entityExp = entityExp;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			Entity entity = (Entity) entityExp.evaluate(shell.workspace());
			shell.workspace().add(entity);
			shell.print("Added entity " + entity + " to workspace");
		}

	}

	private static class RunStatement implements Statement {

		private final Expression<MiningAlgorithm> alg;

		private final Expression<StringValue> resultId;

		public RunStatement(Expression<MiningAlgorithm> alg, Expression<StringValue> resultId) {
			this.alg = alg;
			this.resultId = resultId;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			shell.runJob(alg.evaluate(shell.workspace()), Identifier.id(resultId.evaluate(shell.workspace()).asString()));
		}

	}

	private static class EmptyStatement implements Statement {

		private EmptyStatement() {
			;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			;
		}

	}

	private static class ExitStatement implements Statement {

		private ExitStatement() {
			;
		}

		@Override
		public void execute(Interpreter shell) {
			shell.requestTermination();
		}

	}

	/**
	 * Evaluates an expression and prints it string representation to the shell.
	 * 
	 * @author Mario Boley
	 * 
	 * @since 0.3.0
	 * 
	 * @version 0.3.0
	 *
	 */
	private static class EvaluationStatement implements Statement {

		private final Expression<?> expression;

		private EvaluationStatement(Expression<?> expression) {
			this.expression = expression;
		}

		@Override
		public void execute(Interpreter shell) throws InterpretationException {
			shell.print(expression.evaluate(shell.workspace()).toString());
		}

	}

	private Statements() {
		;
	}

}
