package de.unibonn.realkd.algorithms.emm.dssd;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * <p>
 * Description-based subgroup set selection.
 * </p>
 *
 * It aims at selecting subgroups, whose descriptions are different, namely it
 * discards each subgroup that has the same quality as an already selected
 * subgroup and whose description is only different in one condition from that
 * subgroup. For example, {@code A && C, quality = 0.1} will be discarded, if
 * {@code A && B, quality = 0.1} is already selected.
 *
 * @see de.unibonn.realkd.algorithms.emm.dssd.SubgroupSetSelector
 *
 * @author Vladimir Dzyuba, KU Leuven
 */
public class DescriptionBasedSubgroupSetSelector extends SubgroupSetSelector {
	protected static final double EPSILON = 5.96e-8;

	@Override
	/**
	 * Discards subgroups that have only one different condition (proposition) in the description
	 * when compared to any already selected subgroup of <b>equal</b> quality.
	 *
	 * Can return less than {@code k} subgroups, if the candidate list is not diverse enough.
	 *
	 * @see de.unibonn.realkd.algorithms.emm.dssd.SubgroupSetSelector#selectDiverseSet
	 */
	protected List<Pattern<?>> selectDiverseSet(final List<Pattern<?>> candidates,
			final int k) {
		final List<Pattern<?>> results = new ArrayList<>(k);
		results.add(candidates.remove(0)); // Always add the top-ranked pattern

		while (results.size() < k && !candidates.isEmpty()) {
			final Pattern<?> candidate = candidates.remove(0);

			boolean redundantToAlreadySelectedPatterns = false;
			for (Pattern<?> alreadySelected : results) {
				if (isRedundant(candidate, alreadySelected)) {
					redundantToAlreadySelectedPatterns = true;
					break;
				}
			}

			if (!redundantToAlreadySelectedPatterns)
				results.add(candidate);
		}

		return results;
	}

	private boolean isRedundant(final Pattern<?> candidate,
			final Pattern<?> alreadySelected) {
		return isEqualQuality(candidate, alreadySelected)
				&& hasSimilarDescription(candidate, alreadySelected);
	}

	private boolean isEqualQuality(final Pattern<?> candidate,
			final Pattern<?> alreadySelected) {
		return abs(quality(alreadySelected) - quality(candidate)) < EPSILON;
	}

	private boolean hasSimilarDescription(final Pattern<?> candidate,
			final Pattern<?> alreadySelected) {
		final Collection<Proposition> candidateDesc = description(candidate);
		final Collection<Proposition> beamMemberDesc = description(alreadySelected);

		// TODO: Assumes candidates are unique
		if (candidateDesc.size() == 1 && beamMemberDesc.size() == 1) {
			return false;
		}

		// TODO: it should be possible to make use of the canonical element
		// order
		int difference = 0;
		for (Proposition proposition : candidateDesc) {
			if (!beamMemberDesc.contains(proposition)) {
				difference++;
			}

			if (difference > 1) {
				return false;
			}
		}

		return true;
	}

	private Collection<Proposition> description(final Pattern<?> uncastCandidate) {
		return ((Subgroup<?>) uncastCandidate.descriptor())
				.extensionDescriptor().elements();
	}

	@Override
	public String toString() {
		return "Description-based selector";
	}
}
