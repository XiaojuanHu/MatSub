package de.unibonn.realkd.algorithms.emm.dssd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.unibonn.realkd.algorithms.common.PatternCollectionProcessor;
import de.unibonn.realkd.algorithms.common.PatternOptimizationFunction;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Abstract parent class for subgroup set selector implementations.
 * </p>
 *
 * <p>
 * Subgroup set selectors are used in
 * {@link de.unibonn.realkd.algorithms.emm.dssd.DiverseSubgroupSetDiscovery
 * DSSD} <i>during</i> beam search to select diverse beams and ensure better
 * exploration of the subgroup space, and <i>after</i> the search to select a
 * diverse final result set from a larger intermediate collection of subgroups.
 * </p>
 *
 * <p>
 * Typically, subgroup set selection is guided by a {@link #setQualityMeasure
 * subgroup quality measure}. For example, most often, the best candidate
 * according to the measure of choice is the first subgroup to be included in
 * the selected set. Then, a heuristic proceeds with greedily selecting
 * high-quality subgroups that are sufficiently <i>different</i> from already
 * selected ones, according to a certain notion of <i>difference</i>.
 * </p>
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.DescriptionBasedSubgroupSetSelector
 * @see de.unibonn.realkd.algorithms.emm.dssd.CoverBasedSubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public abstract class SubgroupSetSelector implements PatternCollectionProcessor {
	public static final SubgroupSetSelector QUALITY = new QualityBasedSubgroupSetSelector();
	public static final SubgroupSetSelector DESCRIPTION = new DescriptionBasedSubgroupSetSelector();
	public static final SubgroupSetSelector COVER_SEQUENTIAL = new SequentialCoverBasedSubgroupSetSelector();
	public static final SubgroupSetSelector COVER_ADDITIVE = new AdditiveCoverBasedSubgroupSetSelector();

	protected PatternOptimizationFunction qualityMeasure;
	protected Comparator<Pattern<?>> qualityComparator;
	protected int k;

	@Override
	/**
	 * <p>
	 * Returns a diverse subset of the input collection of patterns. If there
	 * are less than {@link k} candidate patterns, simply returns the sorted
	 * input.
	 * </p>
	 *
	 * <p>
	 * The input collection is not modified; the returned collection is a new
	 * object.
	 * </p>
	 *
	 * @param patterns
	 *            candidate patterns
	 * @return Diverse set of subgroups of size at most {@link k}
	 */
	public final Collection<Pattern<?>> process(final Collection<Pattern<?>> patterns) {
		final List<Pattern<?>> candidates = new ArrayList<>(patterns);
		Collections.sort(candidates, qualityComparator);

		if (candidates.size() > k)
			return selectDiverseSet(candidates, k);
		else
			return candidates;
	}

	/**
	 * <p>
	 * Implementation of a particular subgroup set selection heuristic.
	 * </p>
	 *
	 * <p>
	 * The following preconditions are satisfied:
	 * </p>
	 * <ul>
	 * <li>Number of candidates is strictly greater than k</li>
	 * <li>Candidates are ordered by quality measure values descending</li>
	 * <li>The candidate list is a new object and can be destructively modified
	 * </li>
	 * </ul>
	 *
	 * @param candidates
	 *            Sorted list of candidate subgroups of size &gt; k
	 * @param k
	 *            Number of subgroups to select
	 * @return Selected subgroups, the order is heuristic-dependent
	 */
	protected abstract List<Pattern<?>> selectDiverseSet(final List<Pattern<?>> candidates, final int k);

	/**
	 * @param qualityMeasure
	 *            pattern quality measure that guides set selection
	 */
	public void setQualityMeasure(final PatternOptimizationFunction qualityMeasure) {
		this.qualityMeasure = qualityMeasure;
		// Sorts pattern in the quality-descending order, note `value(o2) -
		// value(o1)`
		this.qualityComparator = (p1, p2) -> Double.compare(qualityMeasure.apply(p2), qualityMeasure.apply(p1));
	}

	/**
	 * @param k
	 *            Maximal number of subgroups to select
	 */
	public void setK(final int k) {
		this.k = k;
	}

	protected final double quality(final Pattern<?> pattern) {
		return qualityMeasure.apply(pattern);
	}
}
