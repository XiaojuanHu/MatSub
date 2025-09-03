package de.unibonn.realkd.discovery;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.common.optimization.RegressionModelFromPreferenceLearner;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Trainer generates and communicates training examples directly after each user
 * action (and not just after the current round is closed). This has the
 * advantage of allowing model training in the background while user is still
 * occupied with inspecting further mining results. On the other hand, when
 * generating a training example, the complete user feedback for the current
 * result ranking is only observed partially. Hence, incomplete or noisy
 * examples might be generated.
 * </p>
 * <p>
 * Trainer treats deleted patterns is disfavored over seen but ignored patterns.
 * In particular, trainer generates one absolute positive (negative) example per
 * saved (deleted) pattern. Additionally, if a pattern x was saved (deleted)
 * from the candidate list, also relative examples are generated between the x
 * and all patterns that
 * </p>
 * <ol>
 * <li>
 * are above of x in the candidate list</li>
 * <li>all patterns that have previously been saved (in case x was deleted) or
 * deleted (in case x was saved).</li>
 * </ol>
 * <p>
 * Finally, if a pattern is deleted from the result board, then it is declared
 * to be disfavored relatively to all patterns that have been saved from the
 * candidates in this round.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1.1
 * 
 */
public final class EagerDeletionAwareModelTrainer extends
		AbstractDiscoveryProcessBasedPreferenceModelTrainer {

	private List<Pattern<?>> deletedFromCandidatesThisRound = new ArrayList<>();

	private List<Pattern<?>> savedThisRound = new ArrayList<>();

	public EagerDeletionAwareModelTrainer(
			RegressionModelFromPreferenceLearner<Pattern<?>> utilityModel,
			DiscoveryProcessState patternState) {
		super(utilityModel, patternState);

	}

	public void aboutToDeleteFromCandidates(Pattern<?> deleted) {
		this.getLearner().tellPreference(
				getLearner().getModel().getFeatureSpace().getZeroElement(),
				deleted);

		int patternIndex = getPatternState().candidatePatterns().indexOf(
				deleted);

		for (int i = 0; i < patternIndex; i++) {
			this.getLearner().tellPreference(
					getPatternState().candidatePatterns().get(i), deleted);
		}
		for (Pattern<?> saved : savedThisRound) {
			this.getLearner().tellPreference(saved, deleted);
		}
		deletedFromCandidatesThisRound.add(deleted);
	}

	public void aboutToSave(Pattern<?> saved) {
		this.getLearner().tellPreference(saved,
				getLearner().getModel().getFeatureSpace().getZeroElement());

		int patternIndex = getPatternState().candidatePatterns().indexOf(
				saved);

		for (int i = 0; i < patternIndex; i++) {
			this.getLearner().tellPreference(saved,
					getPatternState().candidatePatterns().get(i));
		}
		for (Pattern<?> deleted : deletedFromCandidatesThisRound) {
			this.getLearner().tellPreference(saved, deleted);
		}
		savedThisRound.add(saved);
	}

	@Override
	public void markAsSeen(Pattern<?> p) {
		// not used atm
		;
	}

	public void roundEnded() {
		savedThisRound.clear();
		deletedFromCandidatesThisRound.clear();
		getLearner().doUpdate();
	}

	@Override
	public void justBeganNewRound() {
		;
	}

	@Override
	public void aboutToDeletePatternFromResults(Pattern<?> pattern) {
		if (savedThisRound.isEmpty()) {
			this.getLearner().tellPreference(
					getLearner().getModel().getFeatureSpace().getZeroElement(),
					pattern);
		}

		for (Pattern<?> saved : savedThisRound) {
			if (!saved.equals(pattern)) {
				this.getLearner().tellPreference(saved, pattern);
			}
		}
	}

}
