/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.run;

import static de.unibonn.realkd.common.base.IntegerValues.intValue;
import static de.unibonn.realkd.util.IO.addFileLogger;
import static de.unibonn.realkd.util.IO.switchLogfile;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.JsonSerializable;
import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.IntegerValues.PositiveIntegerValue;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.patterns.NamedPatternCollection;
import de.unibonn.realkd.patterns.Pattern;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.7.0
 *
 */
@KdonTypeName("productWorkScheme")
@KdonDoc("Specifies a sequence of jobs resulting from applying a set of computations to each of a sequence of workspaces.")
public class ProductWorkScheme implements JsonSerializable, Identifiable {

	public static ProductWorkScheme simpleExperiment(Identifier id, WorkspaceSpecification[] workspaces,
			ComputationSpecification[] computations, Tracker[] trackers, int computationTimeLimit) {
		return new ProductWorkScheme(id, workspaces, computations, trackers, (PositiveIntegerValue) intValue(computationTimeLimit), null);
	}

	private static final Logger LOGGER = Logger.getLogger(ProductWorkScheme.class.getName());

	@JsonProperty("id")
	private final Identifier id;

	@JsonProperty("computationTimeLimit")
	@JsonInclude(Include.NON_EMPTY)
	private final PositiveIntegerValue computationTimeLimit;

	@JsonProperty("workspaces")
	@KdonDoc("The sequences of workspaces on which to run all the specified jobs.")
	private final WorkspaceSpecification[] inputs;

	@JsonProperty("computations")
	@JsonInclude(Include.NON_EMPTY)
	private final ComputationSpecification[] computations;

	@JsonProperty("trackers")
	@JsonInclude(Include.NON_EMPTY)
	private final Tracker[] trackers;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@JsonCreator
	private ProductWorkScheme(@JsonProperty("id") Identifier id,
			@JsonProperty("workspaces") WorkspaceSpecification[] inputs,
			@JsonProperty("computations") ComputationSpecification[] computations,
			@JsonProperty("trackers") Tracker[] trackers,
			@JsonProperty("computationTimeLimit") PositiveIntegerValue computationTimeLimit,
			@JsonProperty("dataPath") String dataPathName) {
		this.id = id;
		this.inputs = inputs;
		this.computations = computations != null ? computations : new ComputationSpecification[0];
		this.trackers = trackers != null ? trackers : new Tracker[0];
		this.computationTimeLimit = computationTimeLimit;
		if (dataPathName != null) {
			LOGGER.warning(
					"Providing data path via job file is deprecated; provide via command line or other global option");
		}
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	private boolean timeOut(long startMillis) {
		if (computationTimeLimit == null) {
			return false;
		}
		return (System.currentTimeMillis() - startMillis) / 1000 > computationTimeLimit.asInt();
	}

	public void run(ExecutionContext context) {
		context.exportToFile(this, "_jobBackup.json");

		String generalLogfileName = context.logPath().resolve("general.log").toString();
		addFileLogger("", generalLogfileName);

		for (WorkspaceSpecification input : inputs) {
			try {
				Workspace workspace = input.build(context);
				for (ComputationSpecification computationBuilder : computations) {
					String computationFilename = input.identifier() + "_" + computationBuilder.identifier();
					String computationLogfile = context.logPath().resolve(computationFilename + ".log").toString();
					switchLogfile("", generalLogfileName, computationLogfile);
					try {
						Computation<?> computation = computationBuilder.build(workspace);
						Future<?> futureResult = executor.submit(computation);
						long startTime = System.currentTimeMillis();
						while (!futureResult.isDone()) {
							if (timeOut(startTime) && computation.stoppable()) {
								LOGGER.info("Sending stop signal to computation");
								computation.requestStop();
								synchronized (this) {
									wait(1000);
								}
								if (!futureResult.isDone()) {
									LOGGER.info("Trying to cancel execution on thread level");
									futureResult.cancel(true);
								}
							}
							synchronized (this) {
								wait(1000);
							}
						}
						switchLogfile("", computationLogfile, generalLogfileName);
						Object result = futureResult.get();

						for (Tracker tracker : trackers)
							tracker.consume(input.identifier(), computationBuilder.identifier(), computation, result);

						if (result instanceof Collection) {
							Collection<?> collection = ((Collection<?>) result);
							List<? extends Pattern<?>> patterns = collection.stream().filter(e -> e instanceof Pattern)
									.map(e -> (Pattern<?>) e).collect(Collectors.toList());
							if (!patterns.isEmpty()) {
								workspace.add(new NamedPatternCollection(
										Identifier.id("$results_of_" + computationBuilder.identifier().toString()), "", "", patterns));
							}

						}
						context.exportToFile(result, computationFilename + ".json");

					} catch (ValidationException e) {
						LOGGER.warning(e.getMessage());
						LOGGER.warning(e.hint());
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				LOGGER.severe(e.getMessage() + "; skipping input");
				e.printStackTrace();
			}
		}
		executor.shutdownNow();

		for (Tracker tracker : trackers)
			tracker.writeResults(context.reportsPath());
	}

}
