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
import static java.util.Objects.hash;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.sequences.SingleSequencePropositionalContext;
import de.unibonn.realkd.data.sequences.Window;
import de.unibonn.realkd.patterns.graphs.GraphDescriptor;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class EpisodeRuleDescriptors {

	public static EpisodeRuleDescriptor create(SingleSequencePropositionalContext propositionalContext,
			double windowSize, GraphDescriptor antecedent, GraphDescriptor consequent) {
		return new DefaultEpisodeRuleDescriptor(propositionalContext, windowSize, antecedent, consequent);
	}

	private static class DefaultEpisodeRuleDescriptor implements EpisodeRuleDescriptor {

		@KdonTypeName("episodeRuleDescriptor")
		private static class EpisodeRuleDescriptorSerialForm implements SerialForm<EpisodeRuleDescriptor> {

			@JsonProperty("propositionalContextIdentifier")
			private Identifier propositionalContextIdentifier;

			@JsonProperty("windowSize")
			private double windowSize;

			@JsonProperty("antecedentWindows")
			private List<List<Double>> antecedentWindows;

			@JsonProperty("consequentWindows")
			private List<List<Double>> consequentWindows;

			@JsonProperty("antecedent")
			private SerialForm<GraphDescriptor> antecedent;

			@JsonProperty("consequent")
			private SerialForm<GraphDescriptor> consequent;

			private EpisodeRuleDescriptorSerialForm(
					@JsonProperty("propositionalContextIdentifier") Identifier propositionalContextIdentifier,
					@JsonProperty("windowSize") double windowSize,
					@JsonProperty("antecedentWindows") List<List<Double>> antecedentWindows,
					@JsonProperty("consequentWindows") List<List<Double>> consequentWindows,
					@JsonProperty("antecedent") SerialForm<GraphDescriptor> antecedent,
					@JsonProperty("consequent") SerialForm<GraphDescriptor> consequent) {
				this.propositionalContextIdentifier = propositionalContextIdentifier;
				this.windowSize = windowSize;
				this.antecedentWindows = antecedentWindows;
				this.consequentWindows = consequentWindows;
				this.antecedent = antecedent;
				this.consequent = consequent;
			}

			@Override
			public EpisodeRuleDescriptor build(Workspace workspace) {
				List<Window> antecedentWindows = this.antecedentWindows.stream()
						.map(w -> Window.window(w.get(0), w.get(1))).collect(Collectors.toList());
				List<Window> consequentWindows = this.consequentWindows.stream()
						.map(w -> Window.window(w.get(0), w.get(1))).collect(Collectors.toList());

				return new DefaultEpisodeRuleDescriptor(
						(SingleSequencePropositionalContext) workspace.get(this.propositionalContextIdentifier),
						this.windowSize, antecedentWindows, consequentWindows, this.antecedent.build(workspace),
						this.consequent.build(workspace));
			}

			@Override
			public Collection<Identifier> dependencyIds() {
				Set<Identifier> dependencyIds = newHashSet();
				dependencyIds.add(this.propositionalContextIdentifier);
				dependencyIds.addAll(this.antecedent.dependencyIds());
				dependencyIds.addAll(this.consequent.dependencyIds());
				return dependencyIds;
			}

		}

		private final SingleSequencePropositionalContext propositionalContext;

		private final double windowSize;

		private List<Window> antecedentWindows;

		private List<Window> consequentWindows;

		public final GraphDescriptor antecedent;

		public final GraphDescriptor consequent;

		private DefaultEpisodeRuleDescriptor(SingleSequencePropositionalContext propositionalContext, double windowSize,
				GraphDescriptor antecedent, GraphDescriptor consequent) {
			this.propositionalContext = propositionalContext;
			this.windowSize = windowSize;
			this.antecedentWindows = null;
			this.consequentWindows = null;
			this.antecedent = antecedent;
			this.consequent = consequent;
		}

		private DefaultEpisodeRuleDescriptor(SingleSequencePropositionalContext propositionalContext, double windowSize,
				List<Window> antecedentWindows, List<Window> consequentWindows, GraphDescriptor antecedent,
				GraphDescriptor consequent) {
			this(propositionalContext, windowSize, antecedent, consequent);
			this.antecedentWindows = antecedentWindows;
			this.consequentWindows = consequentWindows;
		}

		@Override
		public double windowSize() {
			return this.windowSize;
		}

		@Override
		public List<Window> windows() {
			return consequentWindows();
		}

		@Override
		public SingleSequencePropositionalContext propositionalContext() {
			return this.propositionalContext;
		}

		@Override
		public List<Window> antecedentWindows() {
			if (this.antecedentWindows == null) {
				this.antecedentWindows = EpisodeDescriptors.getOverlappingMinimalWindows(this.antecedent,
						this.windowSize, this.propositionalContext.events());
			}
			return this.antecedentWindows;
		}

		@Override
		public List<Window> consequentWindows() {
			if (this.consequentWindows == null) {
				this.consequentWindows = EpisodeDescriptors.getOverlappingMinimalWindows(this.consequent,
						this.windowSize, this.propositionalContext.events());
			}
			return this.consequentWindows;
		}

		@Override
		public GraphDescriptor antecedent() {
			return this.antecedent;
		}

		@Override
		public GraphDescriptor consequent() {
			return this.consequent;
		}

		@Override
		public SerialForm<EpisodeRuleDescriptor> serialForm() {
			List<List<Double>> antecedentWindows = antecedentWindows().stream()
					.map(w -> ImmutableList.of(w.start(), w.end())).collect(Collectors.toList());
			List<List<Double>> consequentWindows = consequentWindows().stream()
					.map(w -> ImmutableList.of(w.start(), w.end())).collect(Collectors.toList());

			return new EpisodeRuleDescriptorSerialForm(this.propositionalContext.identifier(), this.windowSize,
					antecedentWindows, consequentWindows, this.antecedent.serialForm(), this.consequent.serialForm());
		}

		@Override
		public int hashCode() {
			return hash(this.propositionalContext.identifier(), this.windowSize, this.antecedent, this.consequent);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof EpisodeRuleDescriptor))
				return false;

			EpisodeRuleDescriptor other = (EpisodeRuleDescriptor) o;

			return this.propositionalContext.identifier().equals(other.propositionalContext().identifier())
					&& this.windowSize() == other.windowSize() && this.antecedent.equals(other.antecedent())
					&& this.consequent.equals(other.consequent());
		}

	}

	// Suppress default constructor for non-instantiability
	private EpisodeRuleDescriptors() {
		throw new AssertionError();
	}

}
