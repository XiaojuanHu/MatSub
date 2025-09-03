/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.algorithms.beamsearch;

import static java.lang.Math.min;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Implements beam search for greedily maximizing some target function. Search
 * starts with provided root search nodes and then successively refines the
 * nodes in the beam with a redundant refinement operator. An optimistic
 * estimator can prune elements of the beam.
 * </p>
 * 
 * @param R the result type
 * 
 * @param N the search node type
 *
 *
 * @author Panagiotis Mandros
 *
 */
public class NewBeamSearch<R extends Pattern<?>, N> extends AbstractMiningAlgorithm<R>
		implements DagSearch<Collection<? extends Pattern<?>>> {

	private static final Logger LOGGER = Logger.getLogger(NewBeamSearch.class.getName());

	private static class BeamNode<N> {
		public final N content;
		public final int depth;

		public double potential;
		public double value;
		public final Set<Function<? super N, ? extends N>> active;

		public BeamNode(N content, Set<Function<? super N, ? extends N>> active, int depth) {
			this.active = active;
			this.content = content;
			this.depth = depth;
		}

		// constructor for single root element
		public BeamNode(N content, Optional<Function<? super N, ? extends N>> opOperatorUsed,
				Set<Function<? super N, ? extends N>> active, double potential, double value, int depth) {
			this(content, active, depth);
			this.potential = potential;
			this.value = value;
		}

		// constructor for when there are multiple root elements
		public BeamNode(N content, Function<? super N, ? extends N> opOperatorUsed,
				Set<Function<? super N, ? extends N>> active, double potential, double value, int depth) {
			this(content, active, depth);
			this.potential = potential;
			this.value = value;
		}

		public String toString() {
			return content.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof BeamNode)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			BeamNode<N> that = (BeamNode<N>) o;
			return this.content.equals(that.content);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.content);
		}
	}

	// factory for refinements
	// removes the operator used for the refinement
	private static <N> BeamNode<N> beamNode(N content, Function<? super N, ? extends N> opOperatorUsed,
			Set<Function<? super N, ? extends N>> parentActive, int depth) {
		Set<Function<? super N, ? extends N>> childActive = new LinkedHashSet<>(parentActive);
		childActive.remove(opOperatorUsed);
		return new BeamNode<N>(content, childActive, depth);
	}

	// factory for single root node
	private static <N> BeamNode<N> beamNode(N content, Set<Function<? super N, ? extends N>> active, double potential,
			double value, int depth) {
		return new BeamNode<N>(content, Optional.empty(), active, potential, value, depth);
	}

	// factory for multiple root nodes
	// removes the operator used for the refinement
	private static <N> BeamNode<N> beamNode(N content, Function<? super N, ? extends N> opOperatorUsed,
			Set<Function<? super N, ? extends N>> parentActive, double potential, double value, int depth) {
		Set<Function<? super N, ? extends N>> childActive = new LinkedHashSet<>(parentActive);
		childActive.remove(opOperatorUsed);
		return new BeamNode<N>(content, opOperatorUsed, childActive, potential, value, depth);
	}

	private final ToDoubleFunction<? super N> f;

	private final List<ToDoubleFunction<? super N>> oests;

	private final Function<? super N, ? extends R> toPattern;

	private final Set<BeamNode<N>> boundary;

	// the size of the result queue
	private final int numberOfResults;

	private final int beamWidth;

	private final Optional<Integer> depthLimit;

	private final RefinementPropagation refProp;

	private PriorityQueue<BeamNode<N>> best;

	private int nodesCreated = 1;

	private int nodesDiscardedPotential = 0;

	private int nodesDiscardedPotentials[];

	private int maxDepth = 0;

	private int maxBoundarySize = 1;

	// depth of the first encounter of the best solution
	private int solutionDepth = 0;

	// stores the best solution so far, to keep track of depth
	private BeamNode<N> bestSolution;

	public NewBeamSearch(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active,
			N root, ToDoubleFunction<? super N> f, List<ToDoubleFunction<? super N>> oests, int numberOfResults,
			int beamWidth, Optional<Integer> depthLimit, RefinementPropagation refProp) {
		this.f = f;
		this.oests = oests;
		this.boundary = new LinkedHashSet<>();
		this.best = new PriorityQueue<>((n, m) -> Double.compare(n.value, m.value));
		this.toPattern = toPattern;
		this.numberOfResults = numberOfResults;
		this.beamWidth = beamWidth;
		this.refProp = refProp;
		this.depthLimit = depthLimit;
		this.nodesDiscardedPotentials = new int[oests.size()];

		// initialize search
		BeamNode<N> rootNode = beamNode(root, active, Double.MAX_VALUE, f.applyAsDouble(root), 0);
		updateResults(rootNode);
		boundary.add(rootNode);
		LOGGER.info("\n" + "k for top-k used: " + numberOfResults + "\n" + "beam width used: " + beamWidth + "\n"
				+ "number of opt. estimators: " + oests.size() + "\n" + "refinement propagatiom strategy: "
				+ refProp.toString());
	}

	// constructor for beam search with all singletons as root nodes
	// assumes that active operators and roots follow the same order (i.e., the
	// first root node and first operator refer to the same singleton)
	public NewBeamSearch(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active,
			List<N> roots, ToDoubleFunction<? super N> f, List<ToDoubleFunction<? super N>> oests, int numberOfResults,
			int beamWidth, Optional<Integer> depthLimit, RefinementPropagation refProp) {
		this.f = f;
		this.oests = oests;
		this.boundary = new LinkedHashSet<>();
		this.best = new PriorityQueue<>((n, m) -> Double.compare(n.value, m.value));
		this.toPattern = toPattern;
		this.numberOfResults = numberOfResults;
		this.beamWidth = beamWidth;
		this.refProp = refProp;
		this.depthLimit = depthLimit;
		this.nodesDiscardedPotentials = new int[oests.size()];

		// for every root node
		// (iteration over operators is more convenient)
		int i = 0;
		Iterator<Function<? super N, ? extends N>> opIterator = active.iterator();
		while (opIterator.hasNext()) {
			Function<? super N, ? extends N> next = opIterator.next();
			BeamNode<N> rootNode = beamNode(roots.get(i), next, active, Double.MAX_VALUE, f.applyAsDouble(roots.get(i)),
					1);

			if (refProp.remove()) {
				opIterator.remove();
			}

			boundary.add(rootNode);
			updateResults(rootNode);
			i++;
		}
		maxDepth = 1;

		LOGGER.info("\n" + "k for top-k used: " + numberOfResults + "\n" + "beam width used: " + beamWidth + "\n"
				+ "number of opt. estimators: " + oests.size() + "\n" + "refinement propagatiom strategy: "
				+ refProp.toString());
	}

	public NewBeamSearch(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active,
			N root, ToDoubleFunction<? super N> f, ToDoubleFunction<? super N> oest, int numberOfResults, int beamWidth,
			Optional<Integer> depthLimit, RefinementPropagation refProp) {
		this(toPattern, active, root, f, ImmutableList.of(oest), numberOfResults, beamWidth, depthLimit, refProp);
	}

	public NewBeamSearch(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active,
			N root, ToDoubleFunction<? super N> f, ToDoubleFunction<? super N> oest) {
		this(toPattern, active, root, f, oest, 1, 1, Optional.empty(), RefinementPropagation.ALL);
	}

	private void updateResults(BeamNode<N> candidate) {
		if (best.isEmpty()) {
			bestSolution = candidate;
		}

		boolean updated = false;
		/*
		 * if the queue has space insert candidate
		 */

		if (best.size() < numberOfResults && !best.contains(candidate)) {
			best.add(candidate);
			updated = true;
		}
		/*
		 * if the queue has no space, and the candidate has bigger value than the least
		 * best current solution, poll the queue, and insert the candidate
		 */
		else if ((candidate.value > best.peek().value) && (best.size() == numberOfResults)
				&& !best.contains(candidate)) {
			best.poll();
			best.add(candidate);
			updated = true;
		}

		// check if tracked metrics have to be updated (non crucial for result)
		if (updated == true) {
			if (candidate.value > bestSolution.value) {
				bestSolution = candidate;
				solutionDepth = candidate.depth;
				LOGGER.info("Best solution updated: " + bestSolution.content);
			}
		}
	}

	@Override
	protected Collection<R> concreteCall() {
		while (!stopRequested() && !boundary.isEmpty()) {
			Set<BeamNode<N>> refinements = new LinkedHashSet<>();
			for (BeamNode<N> nodeToExpand : boundary) {
				// active operators
				Set<Function<? super N, ? extends N>> active = nodeToExpand.active;

				// iterator of active
				Iterator<Function<? super N, ? extends N>> iter = active.iterator();

				Set<BeamNode<N>> newRefinements = new LinkedHashSet<>();
				while (iter.hasNext()) {
					Function<? super N, ? extends N> ref = iter.next();
					newRefinements.add(beamNode(ref.apply(nodeToExpand.content), ref, active, nodeToExpand.depth + 1));

					if (refProp.remove()) {
						iter.remove();
					}
				}

				nodesCreated += newRefinements.size();

				// updateResults
				newRefinements.forEach(n -> n.value = f.applyAsDouble(n.content));
				newRefinements.forEach(this::updateResults);
				refinements.addAll(newRefinements);
				filterBasedOnPotential(refinements);
			}

			// log
			if ((nodesCreated - refinements.size()) / 10000 < nodesCreated / 10000) {
				logStats();
			}

			boundary.clear();
			if (!refinements.isEmpty()) {
				maxDepth++;
			}

			// prune based on rules
			// filterBasedOnAdditionalPruningRules(refinements);

			// prune and update boundary
			if (!depthLimit.isPresent() || depthLimit.get() > maxDepth) {
				// prune

				List<BeamNode<N>> refinementsToList = new ArrayList<>(refinements);
				// sort and get only the BEAMWIDTH best elements
				refinementsToList.sort((n, m) -> Double.compare(m.value, n.value));
				int sublistBoundary = (beamWidth > refinementsToList.size() ? refinementsToList.size() : beamWidth);
				refinementsToList = refinementsToList.subList(0, sublistBoundary);

				// update boundary
				for (BeamNode<N> node : refinementsToList) {
					boundary.add(node);
				}

				trackBoundarySize();
			}
		}

		// logStats();
		List<BeamNode<N>> resultNodes = new ArrayList<>(best);
		sort(resultNodes, (n, m) -> Double.compare(m.value, n.value));
		List<R> result = resultNodes.stream().map(n -> toPattern.apply(n.content)).collect(toList());
		return result;
	}

	// private void filterBasedOnAdditionalPruningRules(List<BeamNode<N>>
	// unevaluatedNodes) {
	// for (int i = unevaluatedNodes.size() - 1; i >= 0; i--) {
	// BeamNode<N> node = unevaluatedNodes.get(i);
	// for (Predicate<N> rule : additionalPruningRules) {
	// if (rule.test(node.content)) {
	// unevaluatedNodes.remove(i);
	// nodesDiscardedPruning++;
	// // only one rule has to apply
	// break;
	// }
	// }
	// }
	// }

	private void trackBoundarySize() {
		if (boundary.size() > maxBoundarySize) {
			maxBoundarySize = boundary.size();
		}
	}

	private void filterBasedOnPotential(Set<BeamNode<N>> newNodes) {

		Iterator<BeamNode<N>> iter = newNodes.iterator();

		while (iter.hasNext()) {
			BeamNode<N> node = iter.next();
			boolean hasPotential = hasPotential(node);
			if (!hasPotential) {
				iter.remove();
			}
		}

	}

	private boolean hasPotential(BeamNode<N> candidate) {
		candidate.potential = Double.MAX_VALUE;
		int index = 0;
		for (ToDoubleFunction<? super N> oest : oests) {
			candidate.potential = min(candidate.potential, oest.applyAsDouble(candidate.content));
			if (candidate.potential <= best.peek().value) {
				nodesDiscardedPotentials[index]++;
				nodesDiscardedPotential++;
				return false;
			}
			index++;
		}
		return true;
	}

	@Override
	public String caption() {
		return "";
	}

	@Override
	public String description() {
		return "";
	}

	@Override
	public AlgorithmCategory getCategory() {
		return AlgorithmCategory.OTHER;
	}

	@Override
	public int nodesCreated() {
		return nodesCreated;
	}

	@Override
	public int nodesDiscarded() {
		return nodesDiscardedPotential;
	}

	@Override
	public int boundarySize() {
		return boundary.size();
	}

	@Override
	public int maxAttainedBoundarySize() {
		return maxBoundarySize;
	}

	@Override
	public int maxAttainedDepth() {
		return maxDepth;
	}

	@Override
	public int bestDepth() {
		return solutionDepth;
	}

	private void logStats() {
		LOGGER.info("Nodes created: " + nodesCreated + "\n" + "Nodes discarded due to potential: "
				+ nodesDiscardedPotential + "\n" + "best solution depth/max depth: " + solutionDepth + "/" + maxDepth);
	}

	// decides whether to non-redundantly propagate refinement operators
	public static enum RefinementPropagation {

		ALL {
			public boolean remove() {
				return false;
			}
		},
		NON_REDUNDANT {
			public boolean remove() {
				return true;
			}
		};

		public abstract boolean remove();
	}

}
