package ua.ac.be.statistics;

/**
 * Computes the variance from a list of values
 * 
 * @author Sandy Moens
 */

public class Variance {

	/**
	 * Computes the variance of a list of values
	 * 
	 * Var(v) = 1/|v| * (SUM( (v_i - Mean(v))^2) )
	 * 
	 * @param values
	 *          the list of values from which the variance is computed
	 * @return the variance
	 */
	public static double evaluate(double[] values) {
		return evaluate(values, 0, values.length);
	}

	/**
	 * Computes the variance of a sublist of values (including start and excluding
	 * end)
	 * 
	 * Var(v) = 1/|v| * (SUM( (v_i - Mean(v))^2) )
	 * 
	 * @param values
	 *          the complete list of values from which a variance is computed
	 * @param start
	 *          the start index of the sublist
	 * @param end
	 *          the end index of the sublist
	 * @return the variance
	 */
	public static double evaluate(double[] values, int start, int end) {
		double sum = 0.0;
		double mean = Mean.evaluate(values, start, end);

		for (int i = start; i < end; i++) {
			sum += Math.pow(values[i] - mean, 2);
		}
		return sum / (end - start);
	}
	
	public static float evaluate(float[] values, int start, int end) {
		float sum = 0;
		float mean = Mean.evaluate(values, start, end);

		for (int i = start; i < end; i++) {
			sum += Math.pow(values[i] - mean, 2);
		}
		return sum / (end - start);
	}

	/**
	 * Evaluates a list of values to a given list of expected values and computes
	 * the total variance of each pair
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
