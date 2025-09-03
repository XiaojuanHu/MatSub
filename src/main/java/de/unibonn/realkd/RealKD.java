/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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

package de.unibonn.realkd;

import static de.unibonn.realkd.common.JsonSerialization.deserialization;
import static de.unibonn.realkd.run.ExecutionContext.oneShotExecutionContext;
import static java.nio.file.Files.newBufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import de.unibonn.realkd.lang.Intepreters;
import de.unibonn.realkd.run.ExecutionContext;
import de.unibonn.realkd.run.ProductWorkScheme;

/**
 * <p>
 * Main class of the realKD library, which is a general purpose pattern
 * discovery library for discovering real knowledge from real data for real
 * users.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.2
 * 
 */
public class RealKD {

	private static final Properties PROPERTIES = new Properties();
	static {
		InputStream stream = RealKD.class.getClassLoader().getResourceAsStream("realkd.properties");
		if (stream != null) {
			try {
				PROPERTIES.load(stream);
			} catch (IOException e) {
				System.out.println("corrupt jar---could not read realkd.properties");
				System.exit(1);
			}
		}
	}

	private static final String NAME = "realKD";

	private static final String COPYRIGHT = "(c) 2014-18 by the Contributors of the realKD project";

	private static final String VERSION = PROPERTIES.getProperty("version", "unknown version");

	private static final String DEFAULT_INPUT_SUBFOLDER = "data";

	private static final String DEFAULT_OUTPUT_SUBFOLDER = "output";

	public static String name() {
		return NAME;
	}

	public static String version() {
		return VERSION;
	}

	public static void main(String[] args) {
		System.out.println(NAME + " " + VERSION + " " + COPYRIGHT);
		if (args.length == 0) {
			Intepreters.createShell().run();
		} else if (args.length == 1) {
			if (args[0].endsWith(".rs")) {
				Intepreters.createBatchInterpreter().interpret(args[0]);
			} else if (args[0].endsWith(".json")) {
				ProductWorkScheme exp;
				try {
					Path here = FileSystems.getDefault().getPath(".");
					Path inputFile = here.resolve(args[0]);
					exp = deserialization(newBufferedReader(inputFile), ProductWorkScheme.class);
					ExecutionContext context = oneShotExecutionContext(
							here.resolve(DEFAULT_OUTPUT_SUBFOLDER).resolve(exp.identifier().toString()), here,
							here.resolve(DEFAULT_INPUT_SUBFOLDER));
					exp.run(context);
				} catch (IOException e) {
					System.out.println("Error intepreting " + args[0] + ": " + e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			System.out.println(
					"Expecting 0 arguments (interactive mode) or 1 argument (script or json filename for batch mode)");
		}
	}

}
