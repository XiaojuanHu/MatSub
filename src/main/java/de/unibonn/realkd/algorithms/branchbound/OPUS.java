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
package de.unibonn.realkd.algorithms.branchbound;

import static java.lang.Math.min;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.common.StackBackedQueue;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Implements OPUS branch-and-bound search for maximizing some target function
 * with a given optimistic estimator. Search starts in a provided root search
 * node and then successively refines nodes by a provided refinement operator.
 * </p>
 * 
 * @param R the result type
 * 
 * @param N the search node type
 * 
 * @author Mario Boley
 * 
 * @author Panagiotis Mandros
 * 
 * @since 0.4.0
 * 
 * @version 0.4.1
 *
 */
public class OPUS<R extends Pattern<?>, N> extends AbstractMiningAlgorithm<R>
		implements DagSearch<Collection<? extends Pattern<?>>> {

	private static final Logger LOGGER = Logger.getLogger(OPUS.class.getName());

	private static class OpusNode<N> {
		public final Function<? super N, ? extends N> opOperatorUsed;
		public final N content;
		public final int depth;

		public double potential;
		public double value;
		public Set<Function<? super N, ? extends N>> active;

		public OpusNode(N content, Function<? super N, ? extends N> opOperatorUsed, int depth) {
			this.opOperatorUsed = opOperatorUsed;
			this.content = content;
			this.depth = depth;
		}

		public OpusNode(N content, Optional<Function<? super N, ? extends N>> opOperatorUsed, double potential,
				double value, int depth) {
			this(content, opOperatorUsed.orElse(null), depth);
			this.potential = potential;
			this.value = value;
		}

		public String toString() {
			return content.toString();
		}
	}

	private static <N> OpusNode<N> setActive(OpusNode<N> evalNode, Set<Function<? super N, ? extends N>> active) {
		evalNode.active = new LinkedHashSet<>(active);
		return evalNode;
	}

	private static <N> OpusNode<N> opusNode(N content, Function<? super N, ? extends N> opOperatorUsed, int depth) {
		return new OpusNode<N>(content, opOperatorUsed, depth);
	}

	private static <N> OpusNode<N> opusNode(N content, Optional<Function<? super N, ? extends N>> opOperatorUsed,
			double potential, double value, int depth) {
		return new OpusNode<N>(content, opOperatorUsed, potential, value, depth);
	}

	private final OperatorOrder distributionOrder;

	private final TraverseOrder traverseOrder;

	private final ToDoubleFunction<? super N> f;

	private final List<ToDoubleFunction<? super N>> oests;

	private final Predicate<N>[] additionalPruningRules;

	private final Function<? super N, ? extends R> toPattern;

	// the structure that contains the candidates
	private final Queue<OpusNode<N>> traverseStructure;

	// alpha approximation of the best possible solution
	private final double alpha;

	private final int numberOfResults;

	private final Optional<Integer> depthLimit;

	private PriorityQueue<OpusNode<N>> best;

	private int nodesCreated = 1;

	private int nodesDiscardedPotential = 0;

	private int nodesDiscardedPotentials[];

	private int nodesDiscardedPruning = 0;

	private int maxDepth = 0;

	private int maxBoundarySize = 1;

	// depth of the first encounter of the best solution
	private int solutionDepth = 0;

	// stores the best solution so far, to keep track of depth
	private OpusNode<N> bestSolution;

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public OPUS(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active, N root,
			ToDoubleFunction<? super N> f, List<ToDoubleFunction<? super N>> oests, int numberOfResults, double alpha,
			Optional<Integer> depthLimit, OperatorOrder distributionOrder, TraverseOrder traverseOrder,
			Predicate<N>... additionalPruningRules) {
		this.f = f;
		this.oests = oests;
		this.distributionOrder = distributionOrder;
		this.traverseOrder = traverseOrder;
		this.traverseStructure = (Queue<OpusNode<N>>) traverseOrder.initializeOPUSDataStructure();
		this.best = new PriorityQueue<>((n, m) -> Double.compare(n.value, m.value));
		this.toPattern = toPattern;
		this.numberOfResults = numberOfResults;
		this.alpha = alpha;
		this.depthLimit = depthLimit;
		this.additionalPruningRules = additionalPruningRules;
		this.nodesDiscardedPotentials = new int[oests.size()];

		// initialize search
		OpusNode<N> rootNode = opusNode(root, Optional.empty(), Double.MAX_VALUE, f.applyAsDouble(root), 0);
		/*
		 * priority element in the queue will be the one with the smallest value
		 */
		best.add(rootNode);
		bestSolution = rootNode;
		traverseStructure.add(setActive(rootNode, active));
		LOGGER.info("\n" + "alpha used: " + alpha + "\n" + "k for top-k used: " + numberOfResults + "\n"
				+ "traverse order used: " + traverseOrder + "\n" + "number of opt. estimators: " + oests.size());
	}

	// constructor for all singletons as root nodes
	// if operator order is important, then active should be passed as a
	// linkedhashset
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public OPUS(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active, List<N> roots,
			ToDoubleFunction<? super N> f, List<ToDoubleFunction<? super N>> oests, int numberOfResults, double alpha,
			Optional<Integer> depthLimit, OperatorOrder distributionOrder, TraverseOrder traverseOrder,
			Predicate<N>... additionalPruningRules) {
		this.f = f;
		this.oests = oests;
		this.distributionOrder = distributionOrder;
		this.traverseOrder = traverseOrder;
		this.traverseStructure = (Queue<OpusNode<N>>) traverseOrder.initializeOPUSDataStructure();
		this.best = new PriorityQueue<>((n, m) -> Double.compare(n.value, m.value));
		this.toPattern = toPattern;
		this.numberOfResults = numberOfResults;
		this.alpha = alpha;
		this.depthLimit = depthLimit;
		this.additionalPruningRules = additionalPruningRules;
		this.nodesDiscardedPotentials = new int[oests.size()];
		int i = 0;

		// for every root node
		// (iteration over operators is more convenient)
		// evaluate the nodes, distribute non-redundantly the operators, and add to the
		// boundary
		Iterator<Function<? super N, ? extends N>> opIterator = active.iterator();
		while (opIterator.hasNext()) {
			Function<? super N, ? extends N> next = opIterator.next();
			OpusNode<N> rootNode = opusNode(roots.get(i), Optional.of(next), Double.MAX_VALUE,
					f.applyAsDouble(roots.get(i)), 1);
			opIterator.remove();
			traverseStructure.add(setActive(rootNode, active));
			updateResults(rootNode);
			i++;
		}
		LOGGER.info("\n" + "alpha used: " + alpha + "\n" + "k for top-k used: " + numberOfResults + "\n"
				+ "traverse order used: " + traverseOrder + "\n" + "number of opt. estimators: " + oests.size());
	}

	@SafeVarargs
	public OPUS(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active, N root,
			ToDoubleFunction<? super N> f, ToDoubleFunction<? super N> oest, int numberOfResults, double alpha,
			Optional<Integer> depthLimit, OperatorOrder distributionOrder, TraverseOrder traverseOrder,
			Predicate<N>... additionalPruningRules) {
		this(toPattern, active, root, f, ImmutableList.of(oest), numberOfResults, alpha, depthLimit, distributionOrder,
				traverseOrder, additionalPruningRules);
	}

	public OPUS(Function<? super N, ? extends R> toPattern, Set<Function<? super N, ? extends N>> active, N root,
			ToDoubleFunction<? super N> f, ToDoubleFunction<? super N> oest) {
		this(toPattern, active, root, f, oest, 1, 1, Optional.empty(), OperatorOrder.OPUS_PAPER,
				TraverseOrder.BESTFSPOTENTIAL);
	}

	private void updateResults(OpusNode<N> candidate) {
		if (best.isEmpty()) {
			bestSolution = candidate;
		}

		boolean updated = false;
		/*
		 * if the queue has space insert candidate
		 */
		if (best.size() < numberOfResults) {
			best.add(candidate);
			updated = true;
		}
		/*
		 * if the queue has no space, and the candidate has bigger value than the least
		 * best current solution, poll the queue, and insert the candidate
		 */
		else if ((candidate.value > best.peek().value) && (best.size() == numberOfResults)) {
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
		while (!stopRequested() && !traverseStructure.isEmpty()) {

			OpusNode<N> nextNode = traverseStructure.poll();

			if (!hasTopPotential(nextNode)) {
				if (traverseOrder.earlyAbortBasedOnPotentialPossible()) {
					break;
				}
				continue;
			}

			// list here and never change once it is passed to node
			Set<Function<? super N, ? extends N>> remainingOperators = new LinkedHashSet<>(nextNode.active);

			// create the unevaluated nodes
			List<OpusNode<N>> refinements = remainingOperators.stream()
					.map(n -> opusNode(n.apply(nextNode.content), n, nextNode.depth + 1)).collect(toList());
			nodesCreated += refinements.size();

			// log
			if ((nodesCreated - remainingOperators.size()) / 10000 < nodesCreated / 10000) {
				logStats();
			}
			// track of max depth
			if (!refinements.isEmpty()) {
				maxDepth = Math.max(maxDepth, nextNode.depth + 1);
			}

			// prune based on rules
			filterBasedOnAdditionalPruningRules(remainingOperators, refinements);

			refinements.forEach(n -> n.value = f.applyAsDouble(n.content));

			// updateResults
			refinements.forEach(this::updateResults);

			// prune and update boundary
			if (!depthLimit.isPresent() || depthLimit.get() - 1 > nextNode.depth) {
				filterBasedOnPotential(remainingOperators, refinements);
				// assign active operators to children non-redundantly
				distributionOrder.apply(refinements);
				for (OpusNode<N> tempNode : refinements) {
					remainingOperators.remove(tempNode.opOperatorUsed);
					traverseStructure.add(setActive(tempNode, remainingOperators));
				}
				trackBoundarySize();
			}
		}

		logStats();
		LOGGER.info("Nodes discarded on potentials: " + Arrays.toString(nodesDiscardedPotentials));
		List<OpusNode<N>> resultNodes = new ArrayList<>(best);
		sort(resultNodes, (n, m) -> Double.compare(m.value, n.value));
		List<R> result = resultNodes.stream().map(n -> toPattern.apply(n.content)).collect(toList());
		return result;
	}

	private void filterBasedOnAdditionalPruningRules(Set<Function<? super N, ? extends N>> remainingOperators,
			List<OpusNode<N>> unevaluatedNodes) {
		for (int i = unevaluatedNodes.size() - 1; i >= 0; i--) {
			OpusNode<N> node = unevaluatedNodes.get(i);
			for (Predicate<N> rule : additionalPruningRules) {
				if (rule.test(node.content)) {
					remainingOperators.remove(node.opOperatorUsed);
					unevaluatedNodes.remove(i);
					nodesDiscardedPruning++;
					// only one rule has to apply
					break;
				}
			}
		}
	}

	private void filterBasedOnPotential(Set<Function<? super N, ? extends N>> remainingOperators,
			List<OpusNode<N>> newNodes) {
		for (int i = newNodes.size() - 1; i >= 0; i--) {
			OpusNode<N> node = newNodes.get(i);
			boolean hasPotential = hasRefinementPotential(node);
			if (!hasPotential) {
				remainingOperators.remove(node.opOperatorUsed);
				newNodes.remove(i);
			}
		}
	}

	private boolean hasRefinementPotential(OpusNode<N> candidate) {
		candidate.potential = Double.MAX_VALUE;
		int index = 0;
		for (ToDoubleFunction<? super N> oest : oests) {
			candidate.potential = min(candidate.potential, oest.applyAsDouble(candidate.content));
			if (candidate.potential <= best.peek().value / alpha) {
				nodesDiscardedPotentials[index]++;
				nodesDiscardedPotential++;
				return false;
			}
			index++;
		}
		return true;
	}

	private boolean hasTopPotential(OpusNode<N> candidate) {
		if (candidate.potential <= best.peek().value / alpha) {
			nodesDiscardedPotential++;
			return false;
		}
		return true;
	}

	public static enum OperatorOrder {

		OPUS_PAPER {
			public void apply(List<? extends OpusNode<?>> newNodes) {
				sort(newNodes, (n, m) -> Double.compare(n.potential, m.potential));
			}
		},

		/**
		 * Sticks with given order.
		 */
		TRIVIAL {
			public void apply(List<? extends OpusNode<?>> newNodes) {
				;
			}
		};

		public abstract void apply(List<? extends OpusNode<?>> newNodes);

	}

	public static enum TraverseOrder {

		BESTFSPOTENTIAL(true) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new PriorityQueue<>(
						(Comparator<OpusNode<?>>) (n, m) -> Double.compare(m.potential, n.potential));
				return queue;
			}
		},

		BESTFSVALUE(false) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new PriorityQueue<>(
						(Comparator<OpusNode<?>>) (n, m) -> Double.compare(m.value, n.value));
				return queue;
			}
		},

		BREADTHFSPOTENTIAL(false) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new PriorityQueue<>((Comparator<OpusNode<?>>) (n, m) -> {
					if (n.depth == m.depth) {
						return Double.compare(m.potential, n.potential);
					} else {
						return Integer.compare(n.depth, m.depth);
					}
				});
				return queue;
			}
		},

		BREADTHFSVALUE(false) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new PriorityQueue<>((Comparator<OpusNode<?>>) (n, m) -> {
					if (n.depth == m.depth) {
						return Double.compare(m.value, n.value);
					} else {
						return Integer.compare(n.depth, m.depth);
					}
				});
				return queue;
			}
		},

		DFS(false) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new StackBackedQueue<>();
				return queue;
			}
		},

		FIFO(false) {
			@Override
			public Queue<?> initializeOPUSDataStructure() {
				Queue<?> queue = new LinkedList<>();
				return queue;
			}
		};

		private final boolean earlyAbortPossible;

		TraverseOrder(boolean earlyAbordPossible) {
			this.earlyAbortPossible = earlyAbordPossible;
		}

		public boolean earlyAbortBasedOnPotentialPossible() {
			return earlyAbortPossible;
		}

		public abstract Queue<?> initializeOPUSDataStructure();

	}

	private void trackBoundarySize() {
		if (traverseStructure.size() > maxBoundarySize) {
			maxBoundarySize = traverseStructure.size();
		}
	}

	private void logStats() {
		double kBest = best.peek().value;

		double howCloseToBestPossibleSolution;
		if (!traverseStructure.isEmpty()) {
			howCloseToBestPossibleSolution = kBest / traverseStructure.peek().potential;
		} else {
			howCloseToBestPossibleSolution = 1;
		}

		LOGGER.info("Nodes created: " + nodesCreated + "\n" + "Nodes discarded due to potential: "
				+ nodesDiscardedPotential + "\n" + "Nodes discarded due to pruning rules: " + nodesDiscardedPruning
				+ "\n" + "best solution depth/max depth: " + solutionDepth + "/" + maxDepth + "\n"
				+ "kth-best found - top potential: " + best.peek().value + " - "
				+ (traverseStructure.isEmpty() ? "Nil"
						: String.valueOf(traverseStructure.peek().potential) + " (" + howCloseToBestPossibleSolution
								+ ") " + " " + "\n" + traverseStructure.peek().content)
				+ "\n" + "Size of boundary queue: " + traverseStructure.size());
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
		return traverseStructure.size();
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

	public int nodesDiscardedPruningRules() {
		return nodesDiscardedPruning;
	}

}
