package de.unibonn.realkd.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Models the interaction of a user with the output of pattern mining algorithms
 * in an incremental round-based fashion (together with
 * {@link DiscoveryProcessState}) and allows clients to observe this
 * interaction.
 * </p>
 * <p>
 * At all times patterns under consideration are partitioned into three sets:
 * </p>
 * 1. result patterns---containing patterns that the user currently considers as
 * the results of her analysis; these patterns are persistent through round
 * transitions.
 * </p>
 * <p>
 * 2. discarded patterns---containing patterns that have been explicitly marked
 * by the user to not be shown again; also this list is persistent through round
 * transitions.
 * </p>
 * <p>
 * 3. candidate patterns---containing new patterns that are currently presented
 * to the user to potentially augment her result set; a new round starts with a
 * new set of candidates.
 * </p>
 * <p>
 * The class provides three public methods to move patterns between these three
 * sets (addCandidateToResults, deletePatternFromCandidates,
 * deletePatternFromResults) and one method to start a new round with a new set
 * of candidate patterns ({@link #nextRound(List)}). Additionally, through
 * {@link #endRound()}, there is the possibility to enter a state where the
 * current round has ended (i.e., the move operations between the sets has no
 * effect), but no new round with new candidates has started yet.
 * </p>
 * <p>
 * Observers (implementing {@link DiscoveryProcessObserver}) can register to
 * receive notifications about all these operations happening.
 * </p>
 * 
 * @see DiscoveryProcessState
 * 
 * @author Mario Boley
 * @author Björn Jacobs
 * @author Bo Kang
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public class DiscoveryProcess {

	private static final Logger LOGGER = Logger.getLogger(DiscoveryProcess.class.getName());

	private interface OpenClosedState {

		public void nextRound(List<Pattern<?>> nextRoundPatterns);

		public void endRound();

		public boolean isClosed();

		public void deleteFromResults(Pattern<?> p);

		public void saveCandidate(Pattern<?> p);

		public void deleteFromCandidates(Pattern<?> p);
	}

	/**
	 * Represents the open state of a discovery process.
	 */
	private class OpenState implements OpenClosedState {
		@Override
		public void nextRound(List<Pattern<?>> nextRoundPatterns) {
			endRound();
			LOGGER.fine("Taking over cache content into candidates");
			processState.candidates(nextRoundPatterns);
			observers.forEach(o -> o.justBeganNewRound());
			openClosedState = openState;
		}

		@Override
		public void endRound() {
			openClosedState = closedState;
			notifyObserversRoundEnded();
		}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public void deleteFromResults(Pattern<?> p) {
			LOGGER.fine("Notifying observers about deletion from result of '" + p + "'");
			observers.forEach(o -> o.aboutToDeleteFromCandidates(p));
			processState.removeResultPattern(p);
			processState.addToDiscarded(p);
		}

		@Override
		public void saveCandidate(Pattern<?> p) {
			observers.forEach(o -> o.aboutToSave(p));
			processState.removeCandidate(p);
			processState.addResult(p);
		}

		@Override
		public void deleteFromCandidates(Pattern<?> p) {
			observers.forEach(o -> o.aboutToDeleteFromCandidates(p));
			processState.removeCandidate(p);
			processState.addToDiscarded(p);
		}

	}

	/**
	 * Represents the end state of a discovery process.
	 */
	private class ClosedState implements OpenClosedState {
		@Override
		public void nextRound(List<Pattern<?>> nextRoundPatterns) {
			openClosedState = openState;
			processState.candidates(nextRoundPatterns);
			observers.forEach(o -> o.justBeganNewRound());
		}

		@Override
		public void endRound() {
			;
			LOGGER.warning("endRound() called when round was already ended!");
		}

		@Override
		public boolean isClosed() {
			return true;
		}

		@Override
		public void deleteFromResults(Pattern<?> p) {
			;
		}

		@Override
		public void saveCandidate(Pattern<?> p) {
			;
		}

		@Override
		public void deleteFromCandidates(Pattern<?> p) {
			;
		}

	}

	private OpenClosedState openClosedState;

	private OpenState openState;

	private ClosedState closedState;

	private final List<DiscoveryProcessObserver> observers;

	private final DiscoveryProcessState processState;

	private void notifyObserversRoundEnded() {
		LOGGER.fine("Notify observers that round about to end");
		for (DiscoveryProcessObserver observer : observers) {
			observer.roundEnded();
		}
	}

	public DiscoveryProcess(DiscoveryProcessState initialState) {
		this.closedState = new ClosedState();
		this.openState = new OpenState();
		this.openClosedState = openState;
		this.observers = new ArrayList<>();
		this.processState = initialState;
	}

	public void addObserver(DiscoveryProcessObserver observer) {
		this.observers.add(observer);
	}

	public void markAsSeen(Pattern<?> p) {
		for (DiscoveryProcessObserver observer : observers) {
			observer.markAsSeen(p);
		}
	}

	/**
	 * Delete a pattern from the candidate set and add it to the discarded set.
	 * Has no effect if process closed for inputs. Observers are notified about
	 * this action.
	 * 
	 * @param p
	 *            pattern to be deleted from the candidates.
	 */
	public void deleteFromCandidates(Pattern<?> p) {
		openClosedState.deleteFromCandidates(p);
	}

	/**
	 * Delete a pattern from the result set and add it to the discarded set. Has
	 * no effect if process closed for inputs. Observers are notified about this
	 * action.
	 * 
	 * @param p
	 *            pattern to be deleted from the results.
	 */
	public void deleteFromResults(Pattern<?> p) {
		openClosedState.deleteFromResults(p);
	}

	/**
	 * Delete a pattern from the candidate set and add it to the result set. Has
	 * no effect if process closed for inputs. Observers are notified about this
	 * action.
	 * 
	 * @param p
	 *            pattern to be added to the results.
	 */
	public void saveCandidate(Pattern<?> p) {
		openClosedState.saveCandidate(p);
	}

	public DiscoveryProcessState state() {
		return this.processState;
	}

	/**
	 * Starts a new discovery round with the patterns provided as candidates.
	 * Opens process for inputs if it was closed before.
	 * 
	 * @param nextRoundPatterns
	 *            patterns to be used in the new round
	 */
	public void nextRound(List<Pattern<?>> nextRoundPatterns) {
		openClosedState.nextRound(nextRoundPatterns);
	}

	public void results(Collection<Pattern<?>> results) {
		processState.results(results);
	}

	/**
	 * Ends a discovery round, i.e., closes the discovery process for actions
	 * that move patterns between sets. Observers are notified about this action
	 * (unless round was already ended before---in this case a warning is
	 * logged).
	 * 
	 */
	public void endRound() {
		openClosedState.endRound();
	}

	/**
	 * @return false if actions for moving patterns between sets will currently
	 *         have an effect and true if not, i.e., the process state is
	 *         currently closed
	 * 
	 */
	public boolean isClosed() {
		return openClosedState.isClosed();
	}

	@Override
	public String toString() {
		return "Observers: " + this.observers + " State: " + this.processState;
	}

}
