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
package de.unibonn.realkd.patterns.graphs;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.hash;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.Proposition;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class GraphDescriptors {

	public static GraphDescriptor create(List<Node> nodes, List<Edge> edges) {
		return new DefaultGraphDescriptor(getType(nodes, edges), nodes, edges);
	}

	static GraphType getType(List<Node> nodes, List<Edge> edges) {
		if (nodes.size() == 1) {
			return GraphType.GENERAL;
		}
		if (edges.size() == 0) {
			return GraphType.PARALLEL;
		}
		if (edges.size() < nodes.size() - 1) {
			return GraphType.GENERAL;
		}
		if (edges.stream().map(e -> e.start()).collect(Collectors.toSet()).size() == edges.size()
				&& edges.stream().map(e -> e.end()).collect(Collectors.toSet()).size() == edges.size()) {
			return GraphType.SERIAL;
		}
		return GraphType.GENERAL;

	}

	private static class DefaultGraphDescriptor implements GraphDescriptor {

		@KdonTypeName("graphDescriptor")
		private static class GraphDescriptorSerialForm implements SerialForm<GraphDescriptor> {

			@JsonProperty("nodes")
			private List<SerialForm<? extends Node>> nodes;

			@JsonProperty("edges")
			private List<List<Integer>> edges;

			private GraphDescriptorSerialForm(@JsonProperty("nodes") List<SerialForm<? extends Node>> nodes,
					@JsonProperty("edges") List<List<Integer>> edges) {
				this.nodes = nodes;
				this.edges = edges;
			}

			@Override
			public GraphDescriptor build(Workspace workspace) {
				List<Node> nodes = this.nodes.stream().map(n -> n.build(workspace)).collect(Collectors.toList());
				List<Edge> edges = this.edges.stream().map(l -> Edges.create(l.get(0), l.get(1)))
						.collect(Collectors.toList());

				return create(nodes, edges);
			}

			@Override
			public Collection<Identifier> dependencyIds() {
				Set<Identifier> dependencyIds = newHashSet();
				this.nodes.stream().forEach(n -> dependencyIds.addAll(n.dependencyIds()));
				return dependencyIds;
			}

		}

		private final GraphType graphType;

		private final List<Node> nodes;

		private final List<Edge> edges;

		private DefaultGraphDescriptor(GraphType graphType, List<Node> nodes, List<Edge> edges) {
			this.graphType = graphType;
			this.nodes = ImmutableList.copyOf(nodes);
			this.edges = ImmutableList.copyOf(edges);
		}

		@Override
		public GraphType graphType() {
			return this.graphType;
		}

		@Override
		public List<Node> nodes() {
			return this.nodes;
		}

		@Override
		public List<Edge> edges() {
			return this.edges;
		}

		private Node node(int id) {
			if (this.nodes.isEmpty())
				return null;

			for (Node n : this.nodes) {
				if (n.id() == id)
					return n;
			}
			return null;
		}

		@Override
		public GraphDescriptor specialization(Proposition proposition) {
			// make a first check to find the greatest id.
			int greatestId = 0;
			for (Node n : this.nodes()) {
				greatestId = n.id() > greatestId ? n.id() : greatestId;
			}

			int checkId = -1;
			boolean[] ids = new boolean[greatestId + 1];
			for (Node n : this.nodes()) {
				ids[n.id()] = true;
			}
			for (int i = 0; i < ids.length; i++) {
				if (!ids[i]) {
					checkId = i;
					break;
				}
			}

			List<Node> nodes = Lists.newArrayList(nodes());

			if (checkId == -1) {
				nodes.add(Nodes.create(this.nodes().size(), proposition));
			} else {
				nodes.add(Nodes.create(checkId, proposition));
			}

			return create(nodes, this.edges);
		}

		@Override
		public GraphDescriptor specialization(Edge edge) {
			if (this.edges.contains(edge) || node(edge.start()) == null || node(edge.end()) == null) {
				return this;
			}

			List<Edge> edges = newArrayList(this.edges);
			edges.add(edge);

			return GraphHelper.hsutransitive(this.nodes, edges);
		}

		@Override
		public GraphDescriptor generalization(int id) {
			Node node = node(id);

			List<Node> nodes = newArrayList(this.nodes);
			nodes.remove(node);

			List<Edge> edges = this.edges.stream().filter(e -> e.start() != id && e.end() != id)
					.collect(Collectors.toList());

			return create(nodes, edges);
		}

		@Override
		public GraphDescriptor generalization(Edge edge) {
			List<Edge> edges = Lists.newArrayList(edges());
			edges.remove(edge);

			return create(this.nodes, edges);
		}

		@Override
		public SerialForm<GraphDescriptor> serialForm() {
			List<SerialForm<? extends Node>> nodes = this.nodes.stream().map(n -> n.serialForm())
					.collect(Collectors.toList());

			List<List<Integer>> edges = this.edges.stream().map(e -> ImmutableList.of(e.start(), e.end()))
					.collect(Collectors.toList());

			return new GraphDescriptorSerialForm(nodes, edges);
		}

		@Override
		public int hashCode() {
			return hash(this.graphType, this.nodes, this.edges);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof GraphDescriptor))
				return false;

			GraphDescriptor other = (GraphDescriptor) o;

			return this.graphType().equals(other.graphType()) && this.nodes().equals(other.nodes())
					&& this.edges().equals(other.edges());
		}

	}

	// Suppress default constructor for non-instantiability
	private GraphDescriptors() {
		throw new AssertionError();
	}

}
