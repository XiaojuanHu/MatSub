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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class EpisodeRules {

	public static EpisodeRule create(EpisodeRuleDescriptor descriptor,
			List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalProcedures,
			List<Measurement> measurements) {

		List<Measurement> measures = new ArrayList<>();
		measures.addAll(measurements);
		List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures = additionalProcedures
				.stream().filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());

		measures.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));

		return new EpisodeRuleImplementation(descriptor, measures, ImmutableList.copyOf(procedures));
	}

	private static class EpisodeRuleImplementation extends DefaultPattern<EpisodeRuleDescriptor>
			implements EpisodeRule {

		private final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> proceduresBackup;

		private EpisodeRuleImplementation(EpisodeRuleDescriptor description, List<Measurement> measurements,
				List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures) {
			super(description.propositionalContext().population(), description, measurements);
			this.proceduresBackup = ImmutableList.copyOf(procedures);
		}

		@Override
		public EpisodeRule add(Measurement measurement) {
			if (this.hasMeasure(measurement.measure())) {
				return this;
			}

			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);

			return new EpisodeRuleImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements),
					ImmutableList.of());
		}

		@Override
		public EpisodeRuleDescriptor descriptor() {
			return super.descriptor();
		}

		@Override
		public SerialForm<EpisodeRule> serialForm() {
			return new EpisodeRuleSerialForm(descriptor().serialForm(), this.proceduresBackup, this.measurements());
		}

	}

	@KdonTypeName("episodeRule")
	private static class EpisodeRuleSerialForm implements SerialForm<EpisodeRule> {

		@JsonProperty("descriptor")
		private final SerialForm<EpisodeRuleDescriptor> descriptor;

		@JsonProperty("additionalMeasurementProcedures")
		private final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures;

		@JsonProperty("measurements")
		private final List<Measurement> measurements;

		@JsonCreator
		public EpisodeRuleSerialForm(@JsonProperty("descriptor") SerialForm<EpisodeRuleDescriptor> descriptorBuilder,
				@JsonProperty("additionalMeasurementProcedures") List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures,
				@JsonProperty("measurements") List<Measurement> measurements) {
			this.procedures = new ArrayList<>(procedures);
			this.descriptor = descriptorBuilder;
			this.measurements = measurements;
		}

		@Override
		public synchronized EpisodeRule build(Workspace workspace) {
			EpisodeRuleDescriptor ruleDescriptor = this.descriptor.build(workspace);
			return EpisodeRules.create(ruleDescriptor, this.procedures, this.measurements);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return this.descriptor.dependencyIds();
		}

	}

	// Suppress default constructor for non-instantiability
	private EpisodeRules() {
		throw new AssertionError();
	}

}
