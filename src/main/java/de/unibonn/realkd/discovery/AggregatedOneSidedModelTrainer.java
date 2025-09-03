package de.unibonn.realkd.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unibonn.realkd.common.optimization.RegressionModelFromPreferenceLearner;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Trainer aggregates all user feedback provided within one discovery round and
 * generates training examples at the end of the round.
 * </p>
 * <p>
 * Regarding patterns in result candidates, trainer only considers whether the
 * user did save a pattern to results or not, i.e., it treats deletions and seen
 * but ignored identically. Hence the name: one sided. This differs from
 * deletion-aware trainers. For deletions from result, however, both trainers
 * work identically.
 * </p>
 * <p>
 * In particular, at the end of the round
 * </p>
 * <ol>
 * <li>all patterns saved to results this round are favored absolutely (over the
 * ZERO-pattern).</li>
 * <li>all seen but not saved patterns from candidates are disfavored over all
 * patterns in the results at the end of the round. Note that these includes old
 * results that have saved previously to the current round. If no patterns are
 * in the results, then they are disfavored absolutely over the ZERO-pattern.</li>
 * <li>all patterns deleted from the results are disfavored over all patterns
 * added to the results in this round. If no pattern was added this round, then
 * all patterns deleted from results are disfavored over the ZERO-pattern
 * instead (effectively negating the absolute positive example generated at some
 * previous time when they where added).</li>
 * </ol>
 * <p>
 * All patterns count as ignored that are in the candidate list x above or y
 * below a pattern that was deleted or saved. Also always at least the z first
 * patterns in a new result list are considered seen. Default values are x=1,
 * y=0, z=1; other values can be specified in the constructor.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 * 
 */
public final class AggregatedOneSidedModelTrainer extends
		AbstractDiscoveryProcessBasedPreferenceModelTrainer {

	private List<Pattern<?>> deletedFromCandidatesThisRound = new ArrayList<>();

	private Set<Pattern<?>> seenButIgnored = new HashSet<>();

	private Set<Pattern<?>> oldResultSnapShot = new HashSet<>();

	private final int numInitiallySeen;

	private final int numSeenAbove;

	private final int numSeenBelow;

	public AggregatedOneSidedModelTrainer(
			RegressionModelFromPreferenceLearner<Pattern<?>> utilityModel,
			DiscoveryProcessState patternState, int numInititiallySeen,
			int numSeenAbove, int numSeenBelow) {
		super(utilityModel, patternState);
		this.numInitiallySeen = numInititiallySeen;
		this.numSeenAbove = numSeenAbove;
		this.numSeenBelow = numSeenBelow;
	}

	public AggregatedOneSidedModelTrainer(
			RegressionModelFromPreferenceLearner<Pattern<?>> modelLearner,
			DiscoveryProcessState sessionPatternState) {
		this(modelLearner, sessionPatternState, 1, 1, 0);
	}

	private void updateSeenButIgnored(int consideredIndex) {
		// if (consideredIndex <= maxIgnoredIndex) {
		// maxIgnoredIndex--;
		// } else {
		// maxIgnoredIndex = consideredIndex - 1;
		// }
		if (seenButIgnored.contains(getPatternState().candidatePatterns()
				.get(consideredIndex))) {
			seenButIgnored.remove(getPatternState().candidatePatterns().get(
					consideredIndex));
		}
		for (int i = 1; consideredIndex - i >= Math.max(consideredIndex
				- numSeenAbove, 0); i++) {
			seenButIgnored.add(getPatternState().candidatePatterns().get(
					consideredIndex - i));
		}
		for (int i = 1; consideredIndex + i <= Math.min(consideredIndex
				+ numSeenBelow,
				getPatternState().candidatePatterns().size() - 1); i++) {
			seenButIgnored.add(getPatternState().candidatePatterns().get(
					consideredIndex + i));
		}
	}

	public void aboutToDeleteFromCandidates(Pattern<?> deleted) {
		updateSeenButIgnored(getPatternState().candidatePatterns().indexOf(
				deleted));
		deletedFromCandidatesThisRound.add(deleted);
	}

	public void aboutToSave(Pattern<?> saved) {
		updateSeenButIgnored(getPatternState().candidatePatterns().indexOf(
				saved));
	}

	@Override
	public void markAsSeen(Pattern<?> p) {
		;
	}

	public void roundEnded() {
		Set<Pattern<?>> addedToResults = getAddedToResultThisRound();
		Set<Pattern<?>> deletedFromResults = getDeletedFromResultsThisRound();

		// all new result patterns are absolutely positive
		for (Pattern<?> newResult : addedToResults) {
			getLearner().tellPreference(newResult,
					getLearner().getModel().getFeatureSpace().getZeroElement());
		}

		tellCollectionWiseInferiority(getPatternState().resultPatterns(),
				deletedFromCandidatesThisRound);
		tellCollectionWiseInferiority(getPatternState().resultPatterns(),
				seenButIgnored);
		tellCollectionWiseInferiority(addedToResults, deletedFromResults);

		getLearner().doUpdate();
	}

	/**
	 * Tells all pairs (p1,p2) for p1 in referenceCollection and p2 in
	 * inferiorCollection if referenceCollection non-empty; all pairs (ZERO, p2)
	 * otherwise. Note the asymmetry: no pairs are told when inferiorCollection
	 * is empty
	 */
	private void tellCollectionWiseInferiority(
			Collection<Pattern<?>> referenceCollection,
			Collection<Pattern<?>> inferiorCollection) {

		for (Pattern<?> inferior : inferiorCollection) {
			if (referenceCollection.isEmpty()) {
				getLearner().tellPreference(
						getLearner().getModel().getFeatureSpace()
								.getZeroElement(), inferior);
			} else {
				for (Pattern<?> superior : referenceCollection) {
					getLearner().tellPreference(superior, inferior);
				}
			}
		}

	}

	private void updateResultSnapshot() {
		oldResultSnapShot.clear();
		oldResultSnapShot.addAll(getPatternState().resultPatterns());
	}

	private Set<Pattern<?>> getDeletedFromResultsThisRound() {
		Set<Pattern<?>> deletedFromResultsThisRound = new HashSet<>(
				oldResultSnapShot);
		deletedFromResultsThisRound.removeAll(getPatternState()
				.resultPatterns());
		return deletedFromResultsThisRound;
	}

	private Set<Pattern<?>> getAddedToResultThisRound() {
		Set<Pattern<?>> newResults = new HashSet<>(getPatternState()
				.resultPatterns());
		newResults.removeAll(oldResultSnapShot);
		return newResults;
	}

	@Override
	public void justBeganNewRound() {
		deletedFromCandidatesThisRound.clear();
		updateResultSnapshot();
		reinitializeSeenButIgnored();
	}

	private void reinitializeSeenButIgnored() {
		seenButIgnored.clear();
		for (int i = 0; i < Math.min(numInitiallySeen, getPatternState()
				.candidatePatterns().size()); i++) {
			seenButIgnored.add(getPatternState().candidatePatterns().get(i));
		}
	}

	@Override
	public void aboutToDeletePatternFromResults(Pattern<?> pattern) {
		;
	}

}
