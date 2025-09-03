package de.unibonn.realkd.discovery;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.Pattern;

/**
 * Contains the currently maintained patterns in a DiscoveryProcess. This
 * includes candidate patterns, result patterns, and deleted patterns. State is
 * altered by DiscoveryProcess.
 * 
 * @see DiscoveryProcess
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public class DiscoveryProcessState implements Entity, HasSerialForm<DiscoveryProcessState> {

	public static DiscoveryProcessState discoveryProcessState(Identifier id, String name, String description) {
		return new DiscoveryProcessState(id, name, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
				0);
	}

	private static class DiscoveryProcessStateSerialForm implements IdentifiableSerialForm<DiscoveryProcessState> {

		@JsonProperty("identifier")
		private final Identifier identifier;

		@JsonProperty("name")
		private final String name;

		@JsonProperty("description")
		private final String description;

		@JsonProperty("results")
		private final List<IdentifiableSerialForm<? extends Discovery>> results;

		@JsonProperty("candidates")
		private final List<IdentifiableSerialForm<? extends Discovery>> candidates;

		@JsonProperty("discarded")
		private final List<IdentifiableSerialForm<? extends Discovery>> discarded;

		@JsonProperty("discoveryCount")
		private final int discoveryCount;

		@JsonCreator
		public DiscoveryProcessStateSerialForm(@JsonProperty("identifier") Identifier id, @JsonProperty("name") String name,
				@JsonProperty("description") String description,
				@JsonProperty("results") List<IdentifiableSerialForm<? extends Discovery>> results,
				@JsonProperty("candidates") List<IdentifiableSerialForm<? extends Discovery>> candidates,
				@JsonProperty("discarded") List<IdentifiableSerialForm<? extends Discovery>> discarded,
				@JsonProperty("discoveryCount") int discoveryCount) {
			this.identifier = id;
			this.name = name;
			this.description = description;
			this.results = results;
			this.candidates = candidates;
			this.discarded = discarded;
			this.discoveryCount = discoveryCount;
		}

		@Override
		public DiscoveryProcessState build(Workspace workspace) {
			Function<IdentifiableSerialForm<? extends Discovery>, Discovery> sfToDisc = sf -> sf.build(workspace);
			List<Discovery> resultDisoveries = results.stream().map(sfToDisc).collect(toList());
			List<Discovery> candidateDisoveries = candidates.stream().map(sfToDisc).collect(toList());
			List<Discovery> discardedDisoveries = discarded.stream().map(sfToDisc).collect(toList());
			return new DiscoveryProcessState(identifier, name, description, resultDisoveries, candidateDisoveries,
					discardedDisoveries, discoveryCount);
		}

		@Override
		public Identifier identifier() {
			return identifier;
		}

		@Override
		public List<Identifier> dependencyIds() {
			Set<Identifier> deps = new HashSet<>();
			results.forEach(d -> deps.addAll(d.dependencyIds()));
			candidates.forEach(d -> deps.addAll(d.dependencyIds()));
			discarded.forEach(d -> deps.addAll(d.dependencyIds()));
			return ImmutableList.copyOf(deps);
		}

	}

	private final List<Discovery> results;
	private final List<Discovery> candidates;
	private final List<Discovery> discarded;

	private static class DiscoveryCounter {
		private int number;

		public synchronized int next() {
			return number++;
		}

		public DiscoveryCounter(int initialNumber) {
			this.number = initialNumber;
		}
	}

	private final HashMap<Integer, Discovery> idToDiscovery;
	private final HashMap<Pattern<?>, Discovery> patternToDiscovery;
	private final DiscoveryCounter discoveryCounter;

	private final Identifier id;
	private final String name;
	private final String description;

	private DiscoveryProcessState(Identifier id, String name, String description, List<Discovery> results,
			List<Discovery> candidates, List<Discovery> discarded, int discoveryCounterOffset) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.results = results;
		this.candidates = candidates;
		this.discarded = discarded;
		this.discoveryCounter = new DiscoveryCounter(discoveryCounterOffset);

		this.idToDiscovery = new HashMap<>();
		this.patternToDiscovery = new HashMap<>();
		addToMaps(results);
		addToMaps(candidates);
		addToMaps(discarded);
	}

	private void addToMaps(Collection<Discovery> discoveries) {
		for (Discovery discovery : discoveries) {
			idToDiscovery.put(discovery.number(), discovery);
			patternToDiscovery.put(discovery.content(), discovery);
		}
	}

	/*
	 * only to be called by DiscoveryProcess
	 */
	void candidates(Collection<Pattern<?>> newCandidatePatterns) {
		resetDiscoveryList(candidates, newCandidatePatterns);
	}

	void results(Collection<Pattern<?>> newResultPatterns) {
		resetDiscoveryList(results, newResultPatterns);
	}

	private void resetDiscoveryList(List<Discovery> listToReset, Collection<Pattern<?>> newCandidatePatterns) {
		for (int id : listToReset.stream().mapToInt(c -> c.number()).toArray()) {
			patternToDiscovery.remove(idToDiscovery.get(id).content());
			idToDiscovery.remove(id);
		}
		listToReset.clear();
		for (Pattern<?> pattern : newCandidatePatterns) {
			int number = discoveryCounter.next();
			Discovery discovery = new Discovery(Identifier.id(identifier() + "$d" + number), "Discovery " + number, "", number,
					pattern);
			listToReset.add(discovery);
			idToDiscovery.put(discovery.number(), discovery);
			patternToDiscovery.put(pattern, discovery);
		}
	}

	/*
	 * only to be called by DiscoveryProcess
	 */
	synchronized void addResult(Pattern<?> pattern) {
		results.add(patternToDiscovery.get(pattern));
	}

	/*
	 * only to be called by DiscoveryProcess
	 */
	synchronized void removeResultPattern(Pattern<?> pattern) {
		results.remove(patternToDiscovery.get(pattern));
	}

	/*
	 * only to be called by DiscoveryProcess
	 */
	synchronized void removeCandidate(Pattern<?> pattern) {
		candidates.remove(patternToDiscovery.get(pattern));
	}

	/*
	 * only to be called by DiscoveryProcess
	 */
	synchronized void addToDiscarded(Pattern<?> pattern) {
		discarded.add(patternToDiscovery.get(pattern));
	}

	public synchronized List<Pattern<?>> candidatePatterns() {
		return candidates.stream().map(c -> c.content()).collect(toList());
	}

	/**
	 * Returns a new list of patterns in the result set in chronological order
	 * they got inserted.
	 * 
	 */
	public synchronized List<Pattern<?>> resultPatterns() {
		return results.stream().map(c -> c.content()).collect(toList());
	}

	public synchronized List<Pattern<?>> discardedPatterns() {
		return discarded.stream().map(c -> c.content()).collect(toList());
	}

	public synchronized boolean isInResults(Pattern<?> p) {
		return results.contains(patternToDiscovery.get(p));
	}

	public synchronized boolean isInDiscarded(Pattern<?> p) {
		return discarded.contains(patternToDiscovery.get(p));
	}

	public synchronized boolean isInCandidates(Pattern<?> p) {
		return candidates.contains(patternToDiscovery.get(p));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Candidates=");
		sb.append(candidates.size());
		sb.append(" Discarded=");
		sb.append(discarded.size());
		sb.append(" ResultPatterns=");
		sb.append(results.size());
		return sb.toString();
	}

	public List<Discovery> results() {
		return results;
	}

	public List<Discovery> candidates() {
		return candidates;
	}

	public List<Discovery> discarded() {
		return discarded;
	}

	public Discovery discovery(int number) {
		return idToDiscovery.get(number);
	}

	public Set<Integer> discoveryIds() {
		return idToDiscovery.keySet();
	}

	public Map<Integer, Discovery> discoveryMap() {
		return idToDiscovery;
	}

	@Override
	public Identifier identifier() {
		return id;
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public SerialForm<? extends DiscoveryProcessState> serialForm() {
		return new DiscoveryProcessStateSerialForm(id, name, description,
				results.stream().map(d -> d.serialForm()).collect(toList()),
				candidates.stream().map(d -> d.serialForm()).collect(toList()),
				discarded.stream().map(d -> d.serialForm()).collect(toList()), discoveryCounter.number);
	}

	@Override
	public List<Identifiable> dependencies() {
		Set<Identifiable> deps = new HashSet<>();
		results.forEach(d -> deps.addAll(d.dependencies()));
		candidates.forEach(d -> deps.addAll(d.dependencies()));
		discarded.forEach(d -> deps.addAll(d.dependencies()));
		return ImmutableList.copyOf(deps);
	}

}