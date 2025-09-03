package de.unibonn.realkd.discovery;

import de.unibonn.realkd.patterns.Pattern;

/** 
 * Interface for all classes that need to receive updates from DiscoveryProcess, e.g., ModelTrainers.
 * 
 * @author mboley
 *
 */
public interface DiscoveryProcessObserver {
	
	public void justBeganNewRound();
	
	public void roundEnded();
	
	public void markAsSeen(Pattern<?> p);
	
	public void aboutToSave(Pattern<?> p);
	
	public void aboutToDeleteFromCandidates(Pattern<?> p);
	
	public void aboutToDeletePatternFromResults(Pattern<?> p);
	
}
