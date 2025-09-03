package ua.ac.be.statistics;

/**
 * Computes the mean value from a list of values
 * 
 * @author Sandy Moens
 */

public class Mean {

	/**
	 * Computes the mean of a list of values
	 * 
	 * Mean(x) = SUM( x_i )/ |x|
	 * 
	 * @param values
	 *          the list of values from which the mean is computed
	 * @return the mean value
	 */
	public static double evaluate(double[] values) {
		return evaluate(values, 0, values.length);
	}

	/**
	 * Computes the mean of a sublist of values (including start and excluding
	 * end)
	 * 
	 * Mean(x) = SUM( x_i )/ |x|
	 * 
	 * @param values
	 *          the complete list of values from which a mean is computed
	 * @param start
	 *          the start index of the sublist
	 * @param end
	 *          the end index of the sublist
	 * @return the mean value
	 */
	public static double evaluate(double[] values, int start, int end) {
		double mean = 0.0;
		for (int i = start; i < end; i++) {
			mean += values[i];
		}
		return mean / (end - start);
	}

	public static float evaluate(float[] values, int start, int end) {
		float mean = 0;
		for (int i = start; i < end; i++) {
			mean += values[i];
		}
		return mean / (end - start);
	}

	/**
	 * Computes the combined mean
	 * 
	 * Var(v, e) = 1/|v| * (SUM( (v_i - e_i)^2) )
	 * 
	 * @param values
	 *          the values that are evaluated
	 * @param expectedValues
	 *          the expected/theoretical values
	 * @return the total variance
	 */
	public static double evaluate(double[] values, double[] expectedValues) {
		return evaluate(values, expectedValues, 0, values.length);
	}

	/**
	 * Evaluates a sublist of values to a given sublist of expected values and
	 * computes the total variance of each pair
	 * 
	 * Var(v, e) = 1/|v| * (SUM( (v_i - e_i)^2) )
	 * 
	 * @param values
	 *          the complete list of values from which a sublist is evaluated
	 * @param expectedValues
	 *          the complete list of expected/theoretical values from which a
	 *          sublist is evaluated
	 * @param start
	 *          the start index of the sublist
	 * @param end
	 *          the end index of the sublist
	 * 
	 * @return the total variance
	 */
	public static double evaluate(double[] values, double[] expectedValues,
			int start, int end) {
		double sum = 0.0;

		for (int i = start; i < end; i++) {
			sum += Math.pow(values[i] - expectedValues[i], 2);
		}
		return sum / (end - start);
	}
}
