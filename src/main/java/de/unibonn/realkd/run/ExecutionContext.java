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

import static de.unibonn.realkd.common.JsonSerialization.export;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Arrays.stream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.common.workspace.Workspaces;

/**
 * <p>
 * Encapsulates environment for the execution of realKD jobs; in particular
 * provides access to input files, workspaces, as well as functionality for
 * exporting and logging.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.7.0
 *
 */
public class ExecutionContext {

	private static final Charset OUTPUT_CHARSET = Charset.forName("UTF-8");

	private static final Logger LOGGER = Logger.getLogger(ExecutionContext.class.getName());

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");

	/**
	 * Execution context where all outputs are written to new sub-directories
	 * relative to some provided root output path.
	 * 
	 * @param outputRoot
	 *            root folder relative to which all output folders are created
	 * 
	 * @param inputPaths
	 *            all input paths available to execution context in order of
	 *            priority
	 * 
	 * @return execution context
	 * 
	 * @throws IOException
	 *             if one of the required output paths cannot be created
	 */
	public static ExecutionContext oneShotExecutionContext(Path outputRoot, Path... inputPaths) throws IOException {

		Path finalOutputRoot = outputRoot.resolve(DATE_FORMAT.format(new Date()));

		Path logPath = finalOutputRoot.resolve("logs");
		Path workspacePath = finalOutputRoot.resolve("workspaces");
		Path resultsPath = finalOutputRoot.resolve("results");
		Path reportsPath = finalOutputRoot.resolve("reports");

		createDirectories(finalOutputRoot);
		createDirectories(logPath);
		createDirectories(resultsPath);
		createDirectories(reportsPath);

		return new ExecutionContext(finalOutputRoot, inputPaths, workspacePath, logPath, resultsPath, reportsPath);
	}

	private final Path outputRoot;

	private final Path[] inputPaths;

	private final Path workspacesPath;

	private final Path logPath;

	private final Path resultsPath;

	private final Path reportsPath;

	private Workspace currentWorkspace;

	private ExecutionContext(Path outputRoot, Path[] inputPaths, Path workspacePath, Path logPath, Path resultsPath,
			Path reportsPath) {
		this.outputRoot = outputRoot;
		this.inputPaths = inputPaths;
		this.workspacesPath = workspacePath;
		this.logPath = logPath;
		this.resultsPath = resultsPath;
		this.reportsPath = reportsPath;
		try {
			Path defaultWorkspacePath = workspacePath.resolve("_default");
			createDirectories(defaultWorkspacePath);
			this.currentWorkspace = Workspaces.workspace(defaultWorkspacePath);
		} catch (IOException exp) {
			this.currentWorkspace = Workspaces.workspace();
		}
	}

	public Path outputRoot() {
		return outputRoot;
	}

	/**
	 * Provides specified input file from available input paths in order of
	 * priority.
	 * 
	 * @param fileName
	 *            name of the file to find
	 * 
	 * @return optional path to readable input file relative to first input path
	 *         that contains readable file of given name; empty if no input path
	 *         contains file
	 * 
	 */
	public Optional<Path> inputFile(String fileName) {
		return stream(inputPaths).map(p -> p.resolve(fileName)).filter(f -> isReadable(f)).findFirst();
	}

	public Path logPath() {
		return logPath;
	}

	public Path reportsPath() {
		return reportsPath;
	}

	/**
	 * Exports object to file.
	 * 
	 * @param object
	 *            the object to be exported
	 * 
	 * @param filename
	 *            the name of the file to which export is written
	 * 
	 */
	public void exportToFile(Object object, String filename) {
		Path resultPath = resultsPath.resolve(filename);
		try (BufferedWriter writer = newBufferedWriter(resultPath, OUTPUT_CHARSET)) {
			export(writer, object);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	/**
	 * Switches current workspace (creating if not yet existing).
	 * 
	 * @param id
	 *            identifier of workspace to switch to
	 * 
	 * @return current workspace after switching
	 * 
	 */
	public Workspace workspace(Identifier id) {
		Path workspacePath = workspacesPath.resolve(id.toString());
		try {
			createDirectories(workspacePath);
			currentWorkspace = Workspaces.workspace(workspacePath);
		} catch (IOException e) {
			LOGGER.warning(
					"could not create workspace directory; check system privileges; creating new in-memory workspace");
			currentWorkspace = Workspaces.workspace();
		}
		return currentWorkspace;
	}

	/**
	 * @return current workspace
	 */
	public Workspace workspace() {
		return currentWorkspace;
	}

}
