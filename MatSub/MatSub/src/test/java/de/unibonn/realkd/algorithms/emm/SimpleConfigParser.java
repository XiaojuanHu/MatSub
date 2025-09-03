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
package de.unibonn.realkd.algorithms.emm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Parse a simple configuration file into the similarly named fields of classes.
 * 
 * Format:
 * 
 * <pre>
 * objects = <i>&lt;int:N&gt;</i>
 * fields = <i>&lt;int:M1&gt;</i>
 * <i>... M1 fields of object 1 ... </i>
 * <i> ...  ... </i>
 * fields = <i>&lt;int:MN&gt;</i>
 * <i>... M1 fields of object N ... </i>
 * </pre>
 * 
 * The fields can be of type int, int[], int[][], double, double[], double[][],
 * String. Note: Strings are encoded using base64 coding. Note: This class
 * relies on reflection.
 * 
 * @author Janis Kalofolias
 * @since 0.5.1
 * @version 0.5.1
 * 
 *          TODO: If kept, get the javadoc more verbose
 */
public class SimpleConfigParser {
	private static final Pattern rexAttr = Pattern.compile("(\\w+)\\s*=\\s*([^\\s]+)\\s+(.*)");
	private static final Pattern rexComment = Pattern.compile("^\\s*#.*");
	private static final Pattern rexArray = Pattern.compile("\\(([^)]*)\\)\\s*");

	public static String[][] tokenizeArray(String sbody) {
		List<String[]> arrayTokens = new ArrayList<String[]>();
		Matcher m = rexArray.matcher(sbody);
		while (m.find()) {
			String[] tokens = m.group(1).split("(\\s|,)+");
			if (tokens.length == 1 && tokens[0].isEmpty()) {
				tokens = new String[0];
			}
			arrayTokens.add(tokens);
		}
		return arrayTokens.toArray(new String[arrayTokens.size()][]);
	}

	public static int[][] readIntArray2D(String sbody) {
		String[][] arrayTokens = tokenizeArray(sbody);
		return Arrays.stream(arrayTokens).map(SimpleConfigParser::readIntArray).toArray(t -> new int[t][]);
	}

	public static int[] readIntArray(String[] tokens) {
		return Arrays.stream(tokens).mapToInt(Integer::parseInt).toArray();
	}

	public static int[] readIntArray(String sbody) {
		return readIntArray(tokenizeArray(sbody)[0]);
	}

	public static double[][] readDoubleArray2D(String sbody) {
		String[][] arrayTokens = tokenizeArray(sbody);
		return Arrays.stream(arrayTokens).map(SimpleConfigParser::readDoubleArray).toArray(t -> new double[t][]);
	}

	public static double[] readDoubleArray(String[] tokens) {
		return Arrays.stream(tokens).mapToDouble(Double::parseDouble).toArray();
	}

	public static double[] readDoubleArray(String sbody) {
		return readDoubleArray(tokenizeArray(sbody)[0]);
	}

	public static String[] getParts(BufferedReader r) throws IOException {
		String sline;
		do {
			sline = r.readLine();
		} while (rexComment.matcher(sline).matches());
		return sline.split("\\s*=\\s*");
	}

	public static int getNextIntAttribute(LineNumberReader r, String sattr) throws IOException {
		String[] sparts = getParts(r);
		if (!sparts[0].equals(sattr) || sparts.length != 2) {
			throw new RuntimeException("Attribute mismatch while parsing: " + sattr + " at line " + r.getLineNumber());
		}
		return Integer.parseInt(sparts[1]);
	}

	public static String[] getAttributeParts(LineNumberReader r) throws IOException {
		String sline;
		do {
			sline = r.readLine();
		} while (rexComment.matcher(sline).matches());
		Matcher m = rexAttr.matcher(sline);
		if (!m.matches()) {
			throw new RuntimeException("Attribute assignment parse error at line " + r.getLineNumber());
		}
		return IntStream.range(1, 4).mapToObj(m::group).toArray(String[]::new);
	}

	public static String decodeString(String sval, Decoder decoder) {
		byte[] bval = decoder.decode(sval.getBytes());
		String sres = new String(bval);
		int idxNull = sres.indexOf(0);
		if (idxNull != -1) {
			sres = sres.substring(0, idxNull);
		}
		return sres;
	}

	// static FunctionObject
	public static <C> C[] readConfig(String name, Class<C> Config, IntFunction<C[]> newConfigs, Supplier<C> newConfig)
			throws IOException, NoSuchFieldException, IllegalAccessException {
		URL url = BalancedCoveragePositiveMeanShiftOptimisticEstimator.class.getResource(name);
		LineNumberReader r = new LineNumberReader(new FileReader(url.getPath()));
		Decoder b64decoder = java.util.Base64.getDecoder();

		int numObj = getNextIntAttribute(r, "objects");
		C[] configs = newConfigs.apply(numObj);
		for (int ci = 0; ci < numObj; ++ci) {
			C config = newConfig.get();
			int numRows = getNextIntAttribute(r, "fields");
			for (int ri = 0; ri < numRows; ++ri) {
				try {
					String[] sparts = getAttributeParts(r);
					String sattr = sparts[0];
					String scls = sparts[1];
					String sbody = sparts[2];
					Field field = Config.getDeclaredField(sattr);
					switch (scls) {
					case "String":
						field.set(config, decodeString(sbody, b64decoder));
						break;
					case "int":
						field.setInt(config, Integer.parseInt(sbody));
						break;
					case "int[]":
						field.set(config, readIntArray(sbody));
						break;
					case "int[][]":
						field.set(config, readIntArray2D(sbody));
						break;
					case "double":
						field.setDouble(config, Double.parseDouble(sbody));
						break;
					case "double[]":
						field.set(config, readDoubleArray(sbody));
						break;
					case "double[][]":
						field.set(config, readDoubleArray2D(sbody));
						break;
					default:
						throw new RuntimeException(String.format("Could not parse value %s for object %d", sattr, ri));
					}
				} catch (RuntimeException | IllegalAccessException e) {
					System.err.format("Error at line %d\n", r.getLineNumber());
					throw (e);
				}
			}
			configs[ci] = config;
		}
		r.close();
		return configs;
	}
	
	
	class ParseException extends Exception {
		public ParseException() {
			super();
		}
		
		public ParseException(String message) {
			super(message);
		}
		
		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public ParseException(Throwable cause) {
			super(cause);
		}
	}
}
