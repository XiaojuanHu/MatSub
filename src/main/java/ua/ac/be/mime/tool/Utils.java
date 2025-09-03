package ua.ac.be.mime.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Utility functions
 * 
 * @author Sandy Moens
 */
public class Utils {

	/**
	 * Computes the factorial of a positive integer
	 * 
	 * @param num
	 *            number of which the factorial is to be computed
	 * @return the factorial
	 */
	public static double factorial(double num) {
		if (num <= 1) {
			return 1;
		} else {
			return num * factorial(num - 1);
		}
	}

	/**
	 * Computes the sum of a positive integer
	 * 
	 * @param num
	 *            number of which the sum is to be computed
	 * @return the sum
	 */
	public static int sum(int num) {
		if (num <= 1) {
			return 1;
		} else {
			return num + sum(num - 1);
		}
	}

	/**
	 * Computes the combination of bottom items out of top choices
	 * 
	 * @param top
	 *            the number of items from which can be chosen
	 * @param bottom
	 *            the number of items that need to be chosen
	 * @return the number of possible combinations with given top and bottom
	 *         value
	 */
	public static double combination(double top, double bottom) {
		if (bottom > top)
			return 0;

		double r = (bottom > top - bottom ? bottom : top - bottom) + 1;
		if (bottom == top)
			return 1;

		double d = 2;
		for (double m = r + 1; m <= top; m++, d++) {
			r *= m;
			r /= d;
		}
		return r;
	}

	public static double combinationWithRepetition(double top, double bottom) {
		return combination(top + bottom - 1, bottom);
	}

	public static int countNumberOfLines(String fileName) {
		int size = 0;
		LineNumberReader lnr;
		try {
			lnr = new LineNumberReader(new FileReader(fileName));
			while (lnr.readLine() != null) {
			}
			size = lnr.getLineNumber();
			lnr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	public static int logIndexSearch(double[] values, double toSearch) {
		double left = 0;
		double right = values.length;
		double split;

		if (right % 2 == 0) {
			while (right - left > 3) {
				split = (left + (right - left) / 2.0);
				if (values[(int) split] >= toSearch) {
					right = split;
				} else {
					left = split;
				}
			}
			for (int i = 0; i < 3; i++) {
				if (values[(int) left + i] >= toSearch) {
					right = left + i;
					break;
				}
			}
		} else {
			while (right - left > 1) {
				split = (left + (right - left) / 2.0);
				if (values[(int) split] >= toSearch) {
					right = split;
				} else {
					left = split;
				}
			}
		}
		return (int) (right);
	}

	public static double pow(double base, int exp) {
		double r = 1;
		for (int ix = 0; ix < exp; ix++) {
			r *= base;
		}
		return r;
	}

	public static long pow(int base, int exp) {
		long r = 1;
		for (int ix = 0; ix < exp; ix++) {
			r *= base;
		}
		return r;
	}

	public static double log(double x, double base) {
		return Math.log(x) / Math.log(base);
	}

	public static boolean checkFileExists(String filePath) {
		return new File(filePath).exists();
	}
}
