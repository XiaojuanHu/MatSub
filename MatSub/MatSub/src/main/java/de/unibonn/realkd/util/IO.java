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
package de.unibonn.realkd.util;

import static java.nio.file.Files.createDirectories;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class IO {

	private static final Logger LOGGER = Logger.getLogger(IO.class.getName());

	/**
	 * Attempts to create the specified directory and terminates program execution
	 * on {@link IOException}.
	 * 
	 * @param directory
	 *            the directory to be created
	 * 
	 */
	public static void createDirectoryOrTerminate(Path directory) {
		try {
			createDirectories(directory);
		} catch (IOException e) {
			LOGGER.severe("could not create directory " + directory + "; terminating");
			System.exit(1);
		}
	}

	private static final HashMap<String, FileHandler> HANDLER = new HashMap<>();

	/**
	 * Adds file handler to logger (failing silently) and buffers logger on success
	 * to allow later removal via {@link #removeFileLogger(String, String)}.
	 * 
	 * @param logger
	 * @param logfileName
	 */
	public static void addFileLogger(String logger, String logfileName) {
		try {
//			FileHandler logFileHandler = new FileHandler(logfileName, 1000000, 10);
			FileHandler logFileHandler = new FileHandler(logfileName, true);
			logFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger(logger).addHandler(logFileHandler);
			HANDLER.put(logger + logfileName, logFileHandler);
		} catch (SecurityException e1) {
			LOGGER.severe("No permission to log file: " + logfileName);
		} catch (IOException e1) {
			LOGGER.severe("IO exception when trying to access log file: " + logfileName);
		}
	}

	public static void removeFileLogger(String logger, String logfileName) {
		FileHandler fileHandler = HANDLER.get(logger + logfileName);
		fileHandler.close();
		Logger.getLogger(logger).removeHandler(fileHandler);
	}

	public static void switchLogfile(String logger, String oldFile, String newFile) {
		removeFileLogger(logger, oldFile);
		addFileLogger(logger, newFile);
	}

	public static void writeOut(Path file, String content) {
		if (Files.isDirectory(file)) {
			LOGGER.severe(file + " is directory");
			return;
		}
		if (!Files.exists(file)) {
			try {
				Files.createFile(file);
			} catch (IOException e) {
				LOGGER.severe(e.getMessage());
				return;
			}
		}
		Charset charset = Charset.forName("UTF-8");
		try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	/**
	 * @param table
	 * @param rowKeyHeader
	 * @param delimiter
	 * @return
	 */
	public static String csv(Table<?, ?, ?> table, String rowKeyHeader, String delimiter) {
		StringBuilder output = new StringBuilder();
		String header = rowKeyHeader + delimiter
				+ table.columnKeySet().stream().map(x -> x.toString()).collect(Collectors.joining(delimiter)) + "\n";
		output.append(header);
		output.append(table.rowMap().entrySet().stream()
				.map(entry -> ImmutableList.builder().add(entry.getKey()).addAll(entry.getValue().values()).build()
						.stream().map(x -> x.toString()).collect(Collectors.joining(delimiter)))
				.collect(Collectors.joining("\n")));
		String string = output.toString();
		return string;
	}
	
}
