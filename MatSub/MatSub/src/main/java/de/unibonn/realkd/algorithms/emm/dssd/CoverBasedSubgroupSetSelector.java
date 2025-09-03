package de.unibonn.realkd.algorithms.emm.dssd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Cover-based subgroup set selection.
 * </p>
 *
 * It aims at selecting subgroups that cover different parts of the data. It
 * greedily selects subgroups that maximise the <i>coverage score</i>, a
 * multiplicative combination of the subgroup quality and the <i>coverage
 * bonus</i> based on the data records it covers, which decreases if a record is
 * covered by previously selected subgroups. There exist alternative strategies
 * to compute this bonus.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.SequentialCoverBasedSubgroupSetSelector
 * @see de.unibonn.realkd.algorithms.emm.dssd.AdditiveCoverBasedSubgroupSetSelector
 * @see de.unibonn.realkd.algorithms.emm.dssd.MultiplicativeCoverBasedSubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public abstract class CoverBasedSubgroupSetSelector extends SubgroupSetSelector {
	protected int[] coverCounts;

	@Override
	/**
	 * Greedily selects subgroups that maximise the (dynamic) coverage score.
	 *
	 * @see de.unibonn.realkd.algorithms.emm.dssd.SubgroupSetSelector#selectDiverseSet
	 */
	protected final List<Pattern<?>> selectDiverseSet(
			final List<Pattern<?>> candidates, final int k) {
		final List<Pattern<?>> results = new ArrayList<>(k);
		final Pattern<?> topRanked = candidates.remove(0);
		results.add(topRanked); // Always add the top-ranked pattern

		coverCounts = new int[topRanked.population().size()];
		incrementCountsWith(topRanked);

		while (results.size() < k) {
			final Pattern<?> next = selectNext(candidates);
			candidates.remove(next);

			incrementCountsWith(next);
			results.add(next);
		}

		return results;
	}

	private void incrementCountsWith(final Pattern<?> addedPattern) {
		checkArgument(
				addedPattern.descriptor() instanceof LocalPatternDescriptor,
				"Pattern descriptor must describe sub population of data.");
		for (int i : ((LocalPatternDescriptor) addedPattern.descriptor())
				.supportSet()) {
			coverCounts[i]++;
		}
	}

	private Pattern<?> selectNext(final List<Pattern<?>> candidates) {
		Pattern<?> next = null;
		double maxScore = -Double.MAX_VALUE;
		for (Pattern<?> cand : candidates) {
			checkArgument(
					cand.descriptor() instanceof LocalPatternDescriptor,
					"Descriptor must describe sub population of data.");
			final double quality = quality(cand);
			if (quality < maxScore)
				continue;

			final IndexSet cover = ((LocalPatternDescriptor) cand
					.descriptor()).supportSet();
			final int coverage = cover.size();

			// TODO: very convoluted method of maintaining a bound
			final double coverageBonus = coverageBonus(coverCounts, cover,
					coverage, maxScore * coverage / quality);
			final double score = quality * coverageBonus;
			if (score > maxScore) {
				next = cand;
				maxScore = score;
			}
		}

		return next;
	}

	private double coverageBonus(final int[] coverCounts,
			final IndexSet candidateCover, final int candidateSize,
			final double currentMax) {
		double score = 0.0;
		int residualCoverage = candidateSize;

		for (int i : candidateCover) {
			final int coverCount = coverCounts[i];
			score += (coverCount == 0) ? 1
					: bonusForAlreadyCoveredRecord(coverCount);

			final double scoreUpperBound = score + --residualCoverage;
			if (scoreUpperBound < currentMax)
				return Double.NEGATIVE_INFINITY;
		}

		return (score / candidateSize);
	}

	/**
	 * @param coverCount
	 *            Number of already selected subgroups that cover a data record
	 * @return Coverage bonus that a candidate subgroup receives for covering
	 *         this data record
	 */
	protected abstract double bonusForAlreadyCoveredRecord(final int coverCount);
}
