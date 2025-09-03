package ua.ac.be.statistics;

/**
 * Computes the standard deviation from a list of values
 * 
 * @author Sandy Moens
 */

public class StandardDeviation {

	/**
	 * Computes the standard deviation of a list of values
	 * 
	 * Var(v) = 1/|v| * (SUM( (v_i - Mean(v))^2) )
	 * 
	 * @param values
	 *          the list of values from which the standard deviation is computed
	 * @return the variance
	 */
	public static double evaluate(double[] values) {
		return Math.sqrt(Variance.evaluate(values));
	}

	/**
	 * Computes the standard deviation of a sublist of values (including start and
	 * excluding end)
	 * 
	 * Var(v) = 1/|v| * (SUM( (v_i - Mean(v))^2) )
	 * 
	 * @param values
	 *          the complete list of values from which a standard deviation is
	 *          computed
	 * @param start
	 *          the start index of the sublist
	 * @param end
	 *          the end index of the sublist
	 * @return the standard deviation
	 */
	public static double evaluate(double[] values, int start, int end) {
		return Math.sqrt(Variance.evaluate(values, start, end));
	}

	public static float evaluate(float[] values, int start, int end) {
		return (float) Math.sqrt(((Float) Variance.evaluate(values, start, end))
				.doubleValue());
	}

	/**
	 * Evaluates a list of values to a given list of expected values and computes
	 * the total standard deviation of each pair
	 * 
	 * Var(v, e) = 1/|v| * (SUM( (v_i - e_i)^2) )
	 * 
	 * @param values
	 *          the values that are evaluated
	 * @param expectedValues
	 *          the expected/theoretical values
	 * @return the total standard deviation
	 */
	public static double evaluate(double[] values, double[] expectedValues) {
		return Math.sqrt(Variance.evaluate(values, expectedValues));
	}

	/**
	 * Evaluates a sublist of values to a given sublist of expected values and
	 * computes the total standard deviation of each pair
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
	 * @return the total standard deviation
	 */
	public static double evaluate(double[] values, double[] expectedValues,
			int start, int end) {
		return Math.sqrt(Variance.evaluate(values, expectedValues, start, end));
	}
}
