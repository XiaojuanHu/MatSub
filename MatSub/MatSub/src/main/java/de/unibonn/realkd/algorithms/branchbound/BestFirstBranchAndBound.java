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

import static de.unibonn.realkd.util.Comparison.flippedCompareNanLast;
import static de.unibonn.realkd.util.Comparison.greaterThanOrSecondNaN;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.computations.dag.DagSearch;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.util.Comparison;

/**
 * <p>
 * Implements best-first branch-and-bound search for maximizing some target
 * function with a given optimistic estimator. Search starts in a provided root
 * search node and then successively refines nodes by a provided refinement
 * operator.
 * </p>
 * 
 * @param R
 *            the result type
 * 
 * @param N
 *            the search node type
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
public class BestFirstBranchAndBound<R extends Pattern<?>, N> extends AbstractMiningAlgorithm<R>
		implements DagSearch<Collection<? extends Pattern<?>>> {

	private static final Logger LOGGER = Logger.getLogger(BestFirstBranchAndBound.class.getName());

	private static class EvaluatedNode<N> implements Comparable<EvaluatedNode<?>> {

		public final N content;

		public final double potential;

		public final double value;

		public final int depth;

		public EvaluatedNode(N content, double potential, double value, int depth) {
			this.content = content;
			this.potential = potential;
			this.value = value;
			this.depth = depth;
		}

		@Override
		public int compareTo(EvaluatedNode<?> o) {
			/*
			 * smaller in preference order if this potential larger than other potential,
			 * a.k.a priority element in the queue will be the one with the best potential.
			 * This is used for the boundary queue. For the best result queue its different
			 */
			return Double.compare(o.potential, this.potential);
		}

	}

	private static <N> EvaluatedNode<N> evaluatedNode(N content, double potential, double value, int depth) {
		return new EvaluatedNode<N>(content, potential, value, depth);
	}

	private final Function<? super N, ? extends Collection<? extends N>> succ;

	private final ToDoubleFunction<? super N> f;

	private final ToDoubleFunction<? super N> oest;

	private final Function<? super N, ? extends R> toPattern;

	private final PriorityQueue<EvaluatedNode<N>> boundary;

	// alpha approximation of the best possible solution
	private final double alpha;

	// the size of the result queue
	private final int numberOfResults;

	private final Optional<Integer> depthLimit;

	private PriorityQueue<EvaluatedNode<N>> best;

	private int nodesCreated = 1;

	private int nodesDiscarded = 0;

	private int maxDepth = 0;

	private int maxBoundarySize = 1;

	// depth of the first encounter of the best solution
	private int solutionDepth = 0;

	// stores the best solution so far, to keep track of depth
	private EvaluatedNode<N> bestSolution;

	public BestFirstBranchAndBound(Function<? super N, ? extends R> toPattern,
			Function<? super N, ? extends Collection<? extends N>> succ, N root, ToDoubleFunction<? super N> f,
			ToDoubleFunction<? super N> oest, int numberOfResults, double alpha, Optional<Integer> depthLimit) {
		this.succ = succ;
		this.f = f;
		this.oest = oest;
		this.boundary = new PriorityQueue<>();
		this.toPattern = toPattern;
		this.numberOfResults = numberOfResults;
		this.alpha = alpha;
		this.depthLimit = depthLimit;

		// initialize search
		EvaluatedNode<N> evaluatedRoot = evaluatedNode(root, oest.applyAsDouble(root), f.applyAsDouble(root), 0);
		/*
		 * priority element in the queue will be the one with the smallest value
		 */
		// best = new PriorityQueue<>((n, m) -> Double.compare(n.value, m.value));
		best = new PriorityQueue<>((n, m) -> Comparison.compareNanSmallest(n.value, m.value));
		best.add(evaluatedRoot);
		bestSolution = evaluatedRoot;
		boundary.add(evaluatedRoot);

	}

	public BestFirstBranchAndBound(Function<? super N, ? extends R> toPattern,
			Function<? super N, ? extends Collection<? extends N>> succ, N root, ToDoubleFunction<? super N> f,
			ToDoubleFunction<? super N> oest) {
		this(toPattern, succ, root, f, oest, 1, 1, Optional.empty());
	}

	private void updateResults(EvaluatedNode<N> candidate) {
		// update tracked metrics; no effect on result
		if (greaterThanOrSecondNaN(candidate.value, bestSolution.value)) {
			bestSolution = candidate;
			solutionDepth = candidate.depth;
			LOGGER.info("Best solution updated: " + bestSolution.content + "; value " + candidate.value);
		}

		if (best.size() < numberOfResults) {
			best.add(candidate);
		} else if (greaterThanOrSecondNaN(candidate.value, best.peek().value) && (best.size() == numberOfResults)) {
			best.poll();
			best.add(candidate);
		}
	}

	private void updateBoundary(EvaluatedNode<N> candidate) {
		if (hasPotential(candidate)) {
			boundary.add(candidate);
		} else {
			nodesDiscarded++;
		}
	}

	private EvaluatedNode<N> evaluate(N n, int depth) {
		maxDepth = Math.max(maxDepth, depth);
		return evaluatedNode(n, oest.applyAsDouble(n), f.applyAsDouble(n), depth);
	}

	private boolean hasPotential(EvaluatedNode<N> candidate) {
		if (Comparison.greaterThanOrSecondNaN(candidate.potential, best.peek().value / alpha)) {
			// if (candidate.potential > best.peek().value / alpha) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Collection<R> concreteCall() {
		while (!stopRequested() && !boundary.isEmpty() && hasPotential(boundary.peek())) {
			EvaluatedNode<N> topPotentialNode = boundary.poll();
			Collection<? extends N> specializations = succ.apply(topPotentialNode.content);
			nodesCreated += specializations.size();
			if ((nodesCreated - specializations.size()) / 10000 < nodesCreated / 10000) {
				logStats();
			}

			List<EvaluatedNode<N>> evaluatedNodes = specializations.stream()
					.map(n -> evaluate(n, topPotentialNode.depth + 1)).collect(toList());
			evaluatedNodes.forEach(this::updateResults);

			if (!depthLimit.isPresent() || depthLimit.get() - 1 > topPotentialNode.depth) {
				evaluatedNodes.forEach(this::updateBoundary);
				if (boundary.size() > maxBoundarySize) {
					maxBoundarySize = boundary.size();
				}
			}
		}

		logStats();

		List<EvaluatedNode<N>> resultNodes = new ArrayList<>(best);
		sort(resultNodes, (n, m) -> flippedCompareNanLast(n.value, m.value));
		List<R> result = resultNodes.stream().map(n -> toPattern.apply(n.content)).collect(toList());
		return result;
	}

	private void logStats() {
		// LOGGER.info(nodesDiscarded + "/" + nodesCreated + "(" + (1.0 *
		// nodesDiscarded / nodesCreated) + ")"
		// + " nodes discarded/created (" + solutionDepth + "/" + maxDepth + "
		// best solution depth/max depth)");

		double kBest = best.peek().value;

		double howCloseToBestPossibleSolution;
		if (!boundary.isEmpty()) {
			howCloseToBestPossibleSolution = kBest / boundary.peek().potential;
		} else {
			howCloseToBestPossibleSolution = 1;
		}
		LOGGER.info(nodesDiscarded + "/" + nodesCreated + "(" + (1.0 * nodesDiscarded / nodesCreated) + ")"
				+ " nodes discarded/created (" + solutionDepth + "/" + maxDepth + " best solution depth/max depth)"
				+ "\n" + "kth-best found - potential left: " + best.peek().value + " - "
				+ (boundary.isEmpty() ? "Nil"
						: String.valueOf(boundary.peek().potential) + " (" + howCloseToBestPossibleSolution + ") " + " "
								+ "\n" + boundary.peek().content)
				+ "\n" + "Size of boundary queue: " + boundary.size());

		// LOGGER.info("kth-best found - potential left: " + best.peek().value +
		// " - "
		// + (boundary.isEmpty() ? "Nil"
		// : String.valueOf(boundary.peek().potential) + " (" +
		// howCloseToBestPossibleSolution + ") " + " " +
		// boundary.peek().content));
		// LOGGER.info("Size of boundary queue: " + boundary.size());
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
		return nodesDiscarded;
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

}
