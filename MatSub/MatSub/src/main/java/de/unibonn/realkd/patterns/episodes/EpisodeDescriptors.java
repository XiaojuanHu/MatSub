/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.episodes;

import static com.google.common.collect.Sets.newHashSet;
import static de.unibonn.realkd.common.base.Pair.pair;
import static de.unibonn.realkd.data.sequences.Window.window;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.Pair;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.sequences.SequenceEvent;
import de.unibonn.realkd.data.sequences.SingleSequencePropositionalContext;
import de.unibonn.realkd.data.sequences.Window;
import de.unibonn.realkd.patterns.graphs.Edge;
import de.unibonn.realkd.patterns.graphs.GraphDescriptor;
import de.unibonn.realkd.patterns.graphs.Node;

/**
 *
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class EpisodeDescriptors {

	
	public static EpisodeDescriptor create(SingleSequencePropositionalContext propositionalContext, double windowSize,
			GraphDescriptor graph) {
		return new DefaultEpisodeDescriptor(propositionalContext, windowSize, graph);
	}

	private static class DefaultEpisodeDescriptor implements EpisodeDescriptor {

		@KdonTypeName("episodeDescriptor")
		private static class EpisodeDescriptorSerialForm implements SerialForm<EpisodeDescriptor> {

			@JsonProperty("propositionalContextIdentifier")
			private Identifier propositionalContextIdentifier;

			@JsonProperty("windowSize")
			private double windowSize;

			@JsonProperty("windows")
			private List<List<Double>> windows;

			@JsonProperty("graph")
			private SerialForm<GraphDescriptor> graph;

			private EpisodeDescriptorSerialForm(
					@JsonProperty("propositionalContextIdentifier") Identifier propositionalContextIdentifier,
					@JsonProperty("windowSize") double windowSize, @JsonProperty("windows") List<List<Double>> windows,
					@JsonProperty("graph") SerialForm<GraphDescriptor> graph) {
				this.propositionalContextIdentifier = propositionalContextIdentifier;
				this.windowSize = windowSize;
				this.windows = windows;
				this.graph = graph;
			}

			@Override
			public EpisodeDescriptor build(Workspace workspace) {
				List<Window> windows = this.windows.stream().map(w -> Window.window(w.get(0), w.get(1)))
						.collect(Collectors.toList());

				return new DefaultEpisodeDescriptor(
						(SingleSequencePropositionalContext) workspace.get(this.propositionalContextIdentifier),
						this.windowSize, windows, this.graph.build(workspace));
			}

			@Override
			public Collection<Identifier> dependencyIds() {
				Set<Identifier> dependencyIds = newHashSet();
				dependencyIds.add(this.propositionalContextIdentifier);
				dependencyIds.addAll(this.graph.dependencyIds());
				return dependencyIds;
			}

		}

		private final SingleSequencePropositionalContext propositionalContext;

		private final double windowSize;

		private List<Window> windows;

		private final GraphDescriptor graph;

		private DefaultEpisodeDescriptor(SingleSequencePropositionalContext propositionalContext, double windowSize,
				GraphDescriptor graph) {
			this.propositionalContext = propositionalContext;
			this.windowSize = windowSize;
			this.windows = null;
			this.graph = graph;
		}

		private DefaultEpisodeDescriptor(SingleSequencePropositionalContext propositionalContext, double windowSize,
				List<Window> windows, GraphDescriptor graph) {
			this(propositionalContext, windowSize, graph);
			this.windows = windows;
		}

		@Override
		public double windowSize() {
			return this.windowSize;
		}

		@Override
		public List<Window> windows() {
			if (this.windows == null) {
				this.windows = EpisodeDescriptors.getOverlappingMinimalWindows(this.graph, this.windowSize,
						this.propositionalContext.events());
			}
			return this.windows;
		}

		@Override
		public SingleSequencePropositionalContext propositionalContext() {
			return this.propositionalContext;
		}

		@Override
		public GraphDescriptor graph() {
			return this.graph;
		}

		@Override
		public SerialForm<EpisodeDescriptor> serialForm() {
			List<List<Double>> windows = windows().stream().map(w -> ImmutableList.of(w.start(), w.end()))
					.collect(Collectors.toList());

			return new EpisodeDescriptorSerialForm(this.propositionalContext.identifier(), this.windowSize, windows,
					this.graph.serialForm());
		}

		@Override
		public int hashCode() {
			return hash(this.propositionalContext.identifier(), this.windowSize, this.graph);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof EpisodeDescriptor))
				return false;

			EpisodeDescriptor other = (EpisodeDescriptor) o;

			return this.propositionalContext.identifier().equals(other.propositionalContext().identifier())
					&& this.windowSize() == other.windowSize() && this.graph.equals(other.graph());
		}

	}

	/**
	 * <p>
	 * Computes overlapping minimal windows. A window is minimal if it does not
	 * contain any sub windows that also contains the full graph.
	 * </p>
	 * 
	 * @param graph graph for which minimal windows are to be computed
	 * @param windowSize the maximum size of a minimal window
	 * @param transaction sequence event
	 * @return list of overlapping minimal windows for the given graph
	 */
	static List<Window> getOverlappingMinimalWindows(GraphDescriptor graph, double windowSize,
			List<SequenceEvent<?>> transaction) {
		List<Proposition> propositions = getPropositions(graph);

		List<List<Integer>> ordering = getOrdering(graph);

		List<Window> windows = new ArrayList<>();

		if (graph.edges().size() > 0) {
			windows = EpisodeDescriptors.generalEpisode(transaction, windowSize, propositions, ordering);
		} else {
			windows = EpisodeDescriptors.parallelEpisode(transaction, windowSize, propositions);
		}

		return ImmutableList.copyOf(windows);
	}

	private static List<Proposition> getPropositions(GraphDescriptor graph) {
		return graph.nodes().stream().map(n -> n.proposition()).collect(Collectors.toList());
	}

	private static List<List<Integer>> getOrdering(GraphDescriptor graph) {
		List<List<Integer>> orderings = Lists.newArrayList();

		Map<Integer, Integer> nodeIdToIndex = Maps.newHashMap();

		int i = 0;
		for (Node node : graph.nodes()) {
			nodeIdToIndex.put(node.id(), i++);
		}

		graph.nodes().stream().forEach(n -> orderings.add(Lists.newArrayList()));

		for (Edge edge : graph.edges()) {
			orderings.get(nodeIdToIndex.get(edge.start())).add(nodeIdToIndex.get(edge.end()));
		}

		return orderings;
	}

	private static List<Window> generalEpisode(List<SequenceEvent<?>> transaction, double windowSize,
			List<Proposition> propositions, List<List<Integer>> ordering) {
		LinkedList<Window> windows = Lists.newLinkedList();

		OfInt[] iterators = propositions.stream().map(p -> p.supportSet().iterator()).collect(Collectors.toList())
				.toArray(new OfInt[] {});

		MutableInt[] indices = Arrays.stream(iterators).map(i -> new MutableInt(i.next())).collect(Collectors.toList())
				.toArray(new MutableInt[] {});

		if (!EpisodeDescriptors.uniqueUnorderedIndices(iterators, indices, ordering)
				|| !EpisodeDescriptors.satisfyOrdering(transaction, iterators, indices, ordering)
				|| !EpisodeDescriptors.uniqueIndices(iterators, indices)) {
			return windows;
		}

		while (true) {
			Window window = EpisodeDescriptors.getWindow(transaction, indices);

			if (window.size() <= windowSize && indices.length == Sets.newHashSet(indices).size()
					&& EpisodeDescriptors.validOrdering(indices, ordering)) {
				boolean add = true;
				if (!windows.isEmpty()) {
					Window lastWindow = windows.getLast();
					if (lastWindow.start() < window.start() && lastWindow.end() == window.end()) {
						windows.removeLast();
					} else if (lastWindow.start() == window.start() && lastWindow.end() <= window.end()) {
						add = false;
					}
				}
				if (add) {
					windows.addLast(window);
				}
			}

			if (!EpisodeDescriptors.incrementLowestIndex(transaction, iterators, indices, window, windowSize)
					|| !EpisodeDescriptors.uniqueUnorderedIndices(iterators, indices, ordering)
					|| !EpisodeDescriptors.satisfyOrdering(transaction, iterators, indices, ordering)
					|| !EpisodeDescriptors.uniqueIndices(iterators, indices)) {
				break;
			}
		}

		return windows;
	}

	private static List<Window> parallelEpisode(List<SequenceEvent<?>> transaction, double windowSize,
			List<Proposition> propositions) {
		if (propositions.size() == 0) {
			return ImmutableList.of();
		} else if (propositions.size() == 1) {
			return propositions.get(0).supportSet().stream().mapToObj(i -> {
				double value = (Double) transaction.get(i).value();
				return window(value, value);
			}).collect(Collectors.toList());
		}

		LinkedList<Window> windows = Lists.newLinkedList();

		OfInt[] iterators = propositions.stream().map(p -> p.supportSet().iterator()).collect(Collectors.toList())
				.toArray(new OfInt[] {});

		MutableInt[] indices = Arrays.stream(iterators).map(i -> new MutableInt(i.next())).collect(Collectors.toList())
				.toArray(new MutableInt[] {});

		if (!EpisodeDescriptors.uniqueIndices(iterators, indices)) {
			return windows;
		}

		while (true) {
			Window window = EpisodeDescriptors.getWindow(transaction, indices);

			if (window.size() <= windowSize && indices.length == Sets.newHashSet(indices).size()) {
				boolean add = true;
				if (!windows.isEmpty()) {
					Window lastWindow = windows.getLast();
					if (lastWindow.start() < window.start() && lastWindow.end() == window.end()) {
						windows.removeLast();
					} else if (lastWindow.start() == window.start() && lastWindow.end() <= window.end()) {
						add = false;
					}
				}
				if (add) {
					windows.addLast(window);
				}
			}

			if (!EpisodeDescriptors.incrementLowestIndex(transaction, iterators, indices, window, windowSize)
					|| !EpisodeDescriptors.uniqueIndices(iterators, indices)) {
				break;
			}

		}

		return windows;
	}

	private static Window getWindow(List<SequenceEvent<?>> transaction, MutableInt[] indices) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (MutableInt ix : indices) {
			double d = (Double) transaction.get(ix.intValue()).value();

			min = min < d ? min : d;
			max = max > d ? max : d;
		}

		return window(min, max);
	}

	private static boolean satisfyOrdering(List<SequenceEvent<?>> transaction, OfInt[] iterators, MutableInt[] indices,
			List<List<Integer>> ordering) {
		do {
			int i = 0;

			for (List<Integer> order : ordering) {
				MutableInt value = indices[i];

				if (value.intValue() == -1) {
					return false;
				}

				Double vValue = (Double) transaction.get(value.intValue()).value();

				for (Integer o : order) {
					OfInt iterator = iterators[o];
					MutableInt other = indices[o];

					if (other.intValue() == -1) {
						return false;
					}

					Double vOther = (Double) transaction.get(other.intValue()).value();

					while (vValue.compareTo(vOther) >= 0) {
						if (!iterator.hasNext()) {
							return false;
						}

						other.setValue(iterator.nextInt());

						vOther = (Double) transaction.get(other.intValue()).value();
					}
				}

				i++;
			}
		} while (!EpisodeDescriptors.orderSatisfied(indices, ordering));

		return true;
	}

	private static boolean orderSatisfied(MutableInt[] indices, List<List<Integer>> ordering) {
		int i = 0;

		for (List<Integer> order : ordering) {
			MutableInt value = indices[i];

			for (Integer o : order) {
				if (value.compareTo(indices[o]) >= 0) {
					return false;
				}
			}

			i++;
		}

		return true;
	}

	private static boolean validOrdering(MutableInt[] indices, List<List<Integer>> ordering) {
		int i = 0;
		for (List<Integer> order : ordering) {
			MutableInt ix = indices[i];

			for (Integer o : order) {
				if (ix.compareTo(indices[o]) >= 0) {
					return false;
				}
			}

			i++;
		}

		return true;
	}

	private static boolean uniqueUnorderedIndices(OfInt[] iterators, MutableInt[] indices,
			List<List<Integer>> ordering) {
		List<OfInt> unorderedIterators = Lists.newArrayList();
		List<MutableInt> unorderedIndices = Lists.newArrayList();

		Set<Integer> ordered = Sets.newHashSet();

		for (List<Integer> order : ordering) {
			ordered.addAll(order);
		}

		IntStream.range(0, iterators.length).forEach(i -> {
			if (!ordered.contains(i)) {
				unorderedIterators.add(iterators[i]);
				unorderedIndices.add(indices[i]);
			}
		});

		do {
			Iterator<OfInt> itIterator = unorderedIterators.iterator();
			Iterator<MutableInt> itIndices = unorderedIndices.iterator();

			// get a map of all iterators on the same index
			Map<Integer, List<Pair<OfInt, MutableInt>>> sameIndices = Maps.newHashMap();

			while (itIndices.hasNext()) {
				MutableInt index = itIndices.next();
				OfInt iterator = itIterator.next();

				List<Pair<OfInt, MutableInt>> list = sameIndices.get(index.intValue());
				if (list == null) {
					sameIndices.put(index.getValue(), list = Lists.newArrayList());
				}
				list.add(pair(iterator, index));
			}

			// increment iterators on the same index
			for (Entry<Integer, List<Pair<OfInt, MutableInt>>> entry : sameIndices.entrySet()) {
				List<Pair<OfInt, MutableInt>> value = entry.getValue();

				if (value.size() > 1) {
					if (value.subList(1, value.size()).stream().anyMatch(p -> !p._1().hasNext())) {
						return false;
					}
					// first one does not have to be incremented
					value.subList(1, value.size()).stream().forEach(p -> p._2().setValue(p._1().next()));
				}
			}
		} while (unorderedIndices.size() != Sets.newHashSet(unorderedIndices).size());

		return true;
	}

	private static boolean uniqueIndices(OfInt[] iterators, MutableInt[] indices) {
		while (indices.length != Sets.newHashSet(indices).size()) {
			// get a map of all iterators on the same index
			Map<Integer, List<Pair<OfInt, MutableInt>>> sameIndices = Maps.newHashMap();

			int i = 0;

			for (MutableInt index : indices) {

				List<Pair<OfInt, MutableInt>> list = sameIndices.get(index.intValue());
				if (list == null) {
					sameIndices.put(index.getValue(), list = Lists.newArrayList());
				}
				list.add(pair(iterators[i], index));

				i++;
			}

			// increment iterators on the same index
			for (Entry<Integer, List<Pair<OfInt, MutableInt>>> entry : sameIndices.entrySet()) {
				List<Pair<OfInt, MutableInt>> value = entry.getValue();

				if (value.size() > 1) {
					if (value.subList(1, value.size()).stream().anyMatch(p -> !p._1().hasNext())) {
						return false;
					}
					// first one does not have to be incremented
					value.subList(1, value.size()).stream().forEach(p -> p._2().setValue(p._1().next()));
				}
			}
		}

		return true;
	}

	private static boolean incrementLowestIndex(List<SequenceEvent<?>> transaction, OfInt[] iterators,
			MutableInt[] indices, Window currentWindow, double windowSize) {
		while (true) {
			MutableInt min = indices[0];
			int minIndex = 0;
			int i = 0;

			for (MutableInt curr : indices) {
				if (curr.compareTo(min) < 0) {
					min = curr;
					minIndex = i;
				}

				i++;
			}

			if (!iterators[minIndex].hasNext()) {
				return false;
			}

			min.setValue(iterators[minIndex].next());

			if (currentWindow.end() - transaction.get(min.intValue()).doubleValue() < windowSize) {
				return true;
			}
		}
	}

	// Suppress default constructor for non-instantiability
	private EpisodeDescriptors() {
		throw new AssertionError();
	}

}
