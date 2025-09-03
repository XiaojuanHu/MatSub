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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import de.unibonn.realkd.algorithms.AlgorithmProvider;
import de.unibonn.realkd.algorithms.MiningAlgorithmFactory;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;
import de.unibonn.realkd.lang.expressions.Constant;
import de.unibonn.realkd.lang.statements.BuiltInStatementDefs;
import de.unibonn.realkd.lang.types.FunctionDefinition;
import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class Intepreters {

	/**
	 * 
	 */
	private static final FileNameCompleter FILENAME_COMPLETER = new FileNameCompleter();
	private static final ServiceLoader<ConstantProvider> constantLoader = ServiceLoader.load(ConstantProvider.class);
	private static final ServiceLoader<FunctionProvider> functionLoader = ServiceLoader.load(FunctionProvider.class);
	private static final ServiceLoader<AlgorithmProvider> algorithmLoader = ServiceLoader.load(AlgorithmProvider.class);

	private static List<Constant<?>> constants() {
		ImmutableList<ConstantProvider> loaders = ImmutableList.copyOf(Iterators.concat(constantLoader.iterator()));
		return loaders.stream().flatMap(cProv -> cProv.get().stream()).collect(Collectors.toList());
	}

	private static List<MiningAlgorithmFactory> algorithms() {
		ImmutableList<AlgorithmProvider> loaders = ImmutableList.copyOf(Iterators.concat(algorithmLoader.iterator()));
		return loaders.stream().flatMap(cProv -> cProv.get().stream()).collect(Collectors.toList());
	}

	private static List<FunctionDefinition<?>> functions() {
		ImmutableList<FunctionProvider> loaders = ImmutableList.copyOf(Iterators.concat(functionLoader.iterator()));
		return loaders.stream().flatMap(cProv -> cProv.get().stream()).collect(Collectors.toList());
	}

	private static class EntityCompleter implements Completer {

		private final Workspace workspace;

		public EntityCompleter(Workspace workspace) {
			this.workspace = workspace;
		}

		@Override
		public int complete(String buffer, int cursor, List<CharSequence> candidates) {
			StringsCompleter completer = new StringsCompleter(
					workspace.ids().stream().map(i -> i.toString()).collect(toList()));
			return completer.complete(buffer, cursor, candidates);
		}

	}

	private static ArgumentCompleter.ArgumentDelimiter ARGUMENT_DELIMETER = new ArgumentCompleter.AbstractArgumentDelimiter() {

		@Override
		public boolean isDelimiterChar(CharSequence buffer, int pos) {
			if (buffer == null) {
				// System.out.println("WTF");
				return false;
			} else if (buffer.length() <= pos) {
				// System.out.println("OMG");
				return false;
			}
			return Character.isWhitespace(buffer.charAt(pos)) || buffer.charAt(pos) == '=';
		}

	};

	private static ArgumentCompleter functionCompleter(FunctionDefinition<?> function, Completer baseCompleter) {
		List<Completer> completers = new ArrayList<>();
		completers.add(NullCompleter.INSTANCE);
		completers.add(new StringsCompleter(function.name()));
		completers.add(new StringsCompleter("of"));
		completers.add(baseCompleter);
		completers.add(new StringsCompleter("with"));
		// for (int i = 0; i < function.mandatoryArgumentNames().size() +
		// function.optionalArguementNames().size(); i++) {
		completers.add(new AggregateCompleter(new StringsCompleter(function.mandatoryArgumentNames()),
				new StringsCompleter(function.optionalArguementNames()), baseCompleter));
		// completers.add(baseCompleter);
		// }
		ArgumentCompleter result = new ArgumentCompleter(ARGUMENT_DELIMETER, completers);
		result.setStrict(false);
		return result;
	}

	public static Shell createShell() {
		Workspace workspace = Workspaces.workspace(FileSystems.getDefault().getPath("."));
		try {
			ConsoleReader reader = new ConsoleReader();
			reader.setPrompt(">");
			Completer baseCompleter = new AggregateCompleter(
					new StringsCompleter(constants().stream().map(c -> c.name()).collect(toList())),
					new StringsCompleter(algorithms().stream().map(c -> c.id()).collect(toList())),
					new EntityCompleter(workspace), FILENAME_COMPLETER, new NullCompleter());
			Completer aggregateFunctionCompleter = new AggregateCompleter(
					functions().stream().map(f -> functionCompleter(f, baseCompleter)).collect(toList()));

			List<String> names = BuiltInStatementDefs.ALL.stream().map(def -> def.name()).collect(toList());
			Completer statementCompleter = new StringsCompleter(names);
			// Completer statementCompleter = new ArgumentCompleter(new
			// StringsCompleter(names),functionCompleter(BuiltInFunctions.STATEMENTS,
			// baseCompleter));

			/*
			 * nested Argument completers seem to cause null pointer exceptions
			 */

			AggregateCompleter aggregateCompleter = new AggregateCompleter(statementCompleter,
					aggregateFunctionCompleter, baseCompleter);
			/*
			 * completers seem to be checked on a fire-first basis. so we want to register
			 * only aggregate completer in the end
			 */
			// functions().stream().map(f -> functionCompleter(f,
			// baseCompleter)).forEach(c -> {
			// aggregateCompleter.getCompleters().add(c);
			// });

			reader.addCompleter(aggregateCompleter);

			return new Shell(workspace, reader, algorithmLoader.iterator(), functionLoader.iterator(),
					constantLoader.iterator());
		} catch (IOException e) {
			System.err.println("IO error on instantiating JLine console: " + e.getMessage());
		}
		throw new RuntimeException("Could not instantiate shell");
	}

	public static BatchInterpreter createBatchInterpreter() {
		return new BatchInterpreter(algorithmLoader.iterator(), functionLoader.iterator(), constantLoader.iterator());
	}

}
