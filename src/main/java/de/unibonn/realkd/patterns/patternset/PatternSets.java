/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.patternset;

import static com.google.common.base.Preconditions.checkArgument;
import static de.unibonn.realkd.common.IndexSets.union;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.common.IndexSets;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.PatternBuilder;

/**
 * <p>
 * Utility class providing factory method for constructing pattern set
 * descriptors.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2.1
 *
 */
public class PatternSets {

	public static PatternSetDescriptor createPatternSetDescriptor(Population dataArtifact, Set<Pattern<?>> patterns) {
		return new PatternSetDescriptorImplementation(dataArtifact, patterns);
	}

	public static PatternSet createPatternSet(Population dataArtifact, Set<Pattern<?>> patterns) {
		PatternSetDescriptor descriptor = createPatternSetDescriptor(dataArtifact, patterns);
		return new PatternSetImplementation(dataArtifact, descriptor);
	}

	private static class PatternSetDescriptorImplementation implements PatternSetDescriptor {

		private final Set<Pattern<?>> patterns;

		private final IndexSet supportSet;

		private final Population population;

		private PatternSetDescriptorImplementation(Population dataArtifact, Set<Pattern<?>> patterns) {
			this.population = dataArtifact;
			this.patterns = patterns;
			this.supportSet = calculateSupport();
		}

		@Override
		public IndexSet supportSet() {
			return supportSet;
		}

		/**
		 * Support is union of all pattern supports or all ids in case of
		 * aggregating no pattern or at least one is not describing a
		 * sub-population.
		 * 
		 */
		private IndexSet calculateSupport() {
			if (patterns.isEmpty()) {
				return population.objectIds();
			}
			
			IndexSet result = IndexSets.empty();
			for (Pattern<?> pattern : patterns) {
				if (pattern.descriptor() instanceof LocalPatternDescriptor) {
					result = union(result, ((LocalPatternDescriptor) pattern.descriptor()).supportSet());
				} else {
					return population.objectIds();
				}
			}
			return result;
		}

		@Override
		public Population population() {
			return population;
		}

		@Override
		public Set<Pattern<?>> getPatterns() {
			return patterns;
		}

		@Override
		public SerialForm<PatternSetDescriptor> serialForm() {
			List<SerialForm<? extends Pattern<?>>> elementBuilders = patterns.stream().map(p -> p.serialForm())
					.collect(Collectors.toList());
			return new PatternSetDescriptorBuilderImplementation(population().identifier(), elementBuilders);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof PatternSetDescriptorImplementation)) {
				return false;
			}
			PatternSetDescriptorImplementation otherDescriptor = (PatternSetDescriptorImplementation) other;
			return (this.population.equals(otherDescriptor.population)
					&& this.patterns.equals(otherDescriptor.patterns));
		}

	}

	private static class PatternSetDescriptorBuilderImplementation implements SerialForm<PatternSetDescriptor> {

		private final Identifier populationId;

		private final ArrayList<SerialForm<? extends Pattern<?>>> elementBuilders;

		@JsonCreator
		private PatternSetDescriptorBuilderImplementation(@JsonProperty("populationId") Identifier populationIdentifier,
				@JsonProperty("elementBuilders") List<SerialForm<? extends Pattern<?>>> elementBuilders) {
			this.populationId = populationIdentifier;
			this.elementBuilders = new ArrayList<>(elementBuilders);
		}

		@JsonProperty("populationId")
		public Identifier populationId() {
			return populationId;
		}

		@JsonProperty("elementBuilders")
		public List<SerialForm<? extends Pattern<?>>> elementBuilders() {
			return elementBuilders;
		}

		@Override
		public PatternSetDescriptor build(Workspace dataWorkspace) {
			checkArgument(dataWorkspace.contains(populationId), "Workspace does not contain '" + populationId + "'");
			Population artifact = (Population) dataWorkspace.get(populationId);
			Set<Pattern<?>> elements = elementBuilders.stream().map(b -> b.build(dataWorkspace))
					.collect(Collectors.toSet());
			return createPatternSetDescriptor(artifact, elements);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof PatternSetDescriptorBuilderImplementation)) {
				return false;
			}
			PatternSetDescriptorBuilderImplementation otherDescriptor = (PatternSetDescriptorBuilderImplementation) other;
			return (this.populationId.equals(otherDescriptor.populationId)
					&& this.elementBuilders.equals(otherDescriptor.elementBuilders));
		}

	}

	private static class PatternSetImplementation extends DefaultPattern<PatternSetDescriptor> implements PatternSet {

		private PatternSetImplementation(Population dataArtifact, PatternSetDescriptor descriptor) {
			super(dataArtifact, descriptor, ImmutableList.of());
		}

		public PatternSetDescriptor descriptor() {
			return (PatternSetDescriptor) super.descriptor();
		}

		@Override
		protected String getAdditionsForStringRepresentation() {
			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append(super.getAdditionsForStringRepresentation());
			resultBuilder.append("elements: [\n");
			for (Pattern<?> pattern : descriptor().getPatterns()) {
				resultBuilder.append(pattern.toString());
			}
			resultBuilder.append("]\n");
			return resultBuilder.toString();
		}

		@Override
		public SerialForm<PatternSet> serialForm() {
			return new PatternSetBuilderImplementation(population().identifier(), descriptor().serialForm());
		}

	}

	private static class PatternSetBuilderImplementation implements PatternBuilder<PatternSetDescriptor, PatternSet> {

		private SerialForm<PatternSetDescriptor> descriptor;

		private Identifier populationId;

		@JsonCreator
		public PatternSetBuilderImplementation(@JsonProperty("population") Identifier populationId,
				@JsonProperty("descriptor") SerialForm<PatternSetDescriptor> descriptorBuilder) {
			this.populationId = populationId;
			this.descriptor = descriptorBuilder;
		}

		@JsonProperty("population")
		public Identifier population() {
			return populationId;
		}

		@Override
		public PatternSet build(Workspace workspace) {
			return new PatternSetImplementation((Population) workspace.get(populationId),
					descriptor.build(workspace));
		}

		@Override
		@JsonProperty("descriptor")
		public SerialForm<PatternSetDescriptor> descriptor() {
			return descriptor;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof PatternSetBuilderImplementation)) {
				return false;
			}
			PatternSetBuilderImplementation otherPatternSet = (PatternSetBuilderImplementation) other;
			return (this.populationId.equals(otherPatternSet.populationId)
					&& this.descriptor.equals(otherPatternSet.descriptor));
		}
	}

}
