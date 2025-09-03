package ua.ac.be.mime.tool;

/**
 * Finds the k-subsets of a set that has n elements. Returns just the indices
 * not the real elements.
 * 
 * Uses some code from combinatoricslib package.
 * 
 * @author M. Emin Aksehirli
 * 
 */
public class Combinator {

	private final int n;
	private final int k;
	private final int[] currentCombination;
	private static long[][] combination_Cache;
	private static final int maxCacheSize = 29;

	/**
	 * Calculates value of C(n,k) where C means combination. This value also
	 * gives the number of k-subsets of set with n elements. Calculated value is
	 * 
	 * n! / (k! * (n-k)!)
	 * 
	 * @param n
	 *            Size of the set to choose from.
	 * @param k
	 *            Cardinality of subsets.
	 * @return Number of k-subsets of a set with n elements.
	 */
	public static long calculate(int n, int k) {
		if (n - k < k) {
			k = n - k;
		}

		if (n > 61) {
			// There will be overflows on computation starting from C(62,28)
			return calculate_safe(n, k);
		}

		return calculate_unsafe(n, k);
	}

	private static long calculate_unsafe(int n, int k) {
		int start = n - k;
		long result = start + 1;
		for (int i = 2; i <= k; ++i) {
			result = (result * (start + i)) / i;
		}
		return result;
	}

	private static long calculate_safe(int n, int k) {
		int start = n - k;
		long result = start + 1;
		for (int i = 2; i <= k; ++i) {
			result = (result * (start + i)) / i;
			if (result < 0) {
				throw new ArithmeticException("Overflow for C(" + n + "," + k
						+ ")!");
			}
		}
		return result;
	}

	/**
	 * Same as {@link Combinator.calculate} but returns values from a look-up
	 * table. Table is generated at first call of the function.
	 * 
	 * WARNING: This is not a clean implementation: May cause instability!
	 * 
	 * @param n
	 *            Size of set to choose from.
	 * @param k
	 *            Element count of subsets
	 * @return Number of k-subsets of a set with n elements.
	 */
	public static long calculateWithCache(int n, int k) {
		if (combination_Cache == null) {
			long[][] new_CombCache = new long[maxCacheSize][];
			for (int top = 2; top <= maxCacheSize; ++top) {
				new_CombCache[top - 1] = new long[top];
				int bottomMax = (top + 1) / 2;
				for (int bottom = 1; bottom <= bottomMax; ++bottom) {
					long numerator = 1;
					long denumerator = 1;
					for (int i = 0; i < bottom; ++i) {
						numerator = numerator * (top - i);
						denumerator = denumerator * (i + 1);
					}
					new_CombCache[top - 1][bottom - 1] = numerator
							/ denumerator;
					new_CombCache[top - 1][top - bottom - 1] = new_CombCache[top - 1][bottom - 1];
				}
			}
			combination_Cache = new_CombCache;
		}
		try {
			return combination_Cache[n - 1][k - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			return calculate_safe(n, k);
		}
	}

	public Combinator(int n, int k) {
		this.n = n;
		this.k = k;
		this.currentCombination = new int[k];

		for (int i = 0; i < k; ++i) {
			this.currentCombination[i] = i;
		}
	}

	/**
	 * Calculates the next combination.
	 */
	public void next() {
		int endIndex = this.k - 1;

		incrementBranch(endIndex);
	}

	public boolean hasNext() {
		if (this.currentCombination[0] < this.n - this.k)
			return true;
		return false;
	}

	public int[] currentCombination() {
		return this.currentCombination;
	}

	/**
	 * Skips a branch and continues with the next sibling.
	 * 
	 * @param branchRoot
	 *            Level of node to skip. Index of first level is 0.
	 */
	public void skip(int branchRoot) {
		incrementBranch(branchRoot);
	}

	private void incrementBranch(int notCorrectedEndIndex) {
		int endIndex = notCorrectedEndIndex;

		while (this.currentCombination[endIndex] == this.n - this.k + endIndex) {
			endIndex--;
			if (endIndex == 0)
				break;
		}

		++this.currentCombination[endIndex];
		for (int i = endIndex + 1; i < this.k; ++i) {
			this.currentCombination[i] = this.currentCombination[i - 1] + 1;
		}
	}
}
