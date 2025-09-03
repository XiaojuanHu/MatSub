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
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class Episodes {

	public static Episode create(EpisodeDescriptor descriptor,
			List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalProcedures,
			List<Measurement> measurements) {

		List<Measurement> measures = new ArrayList<>();
		measures.addAll(measurements);
		List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures = additionalProcedures
				.stream().filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());

		measures.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));

		return new EpisodeImplementation(descriptor, measures, ImmutableList.copyOf(procedures));
	}

	private static class EpisodeImplementation extends DefaultPattern<EpisodeDescriptor> implements Episode {

		private final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> proceduresBackup;

		private EpisodeImplementation(EpisodeDescriptor description, List<Measurement> measurements,
				List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures) {
			super(description.propositionalContext().population(), description, measurements);
			this.proceduresBackup = ImmutableList.copyOf(procedures);
		}

		@Override
		public Episode add(Measurement measurement) {
			if (this.hasMeasure(measurement.measure())) {
				return this;
			}

			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);

			return new EpisodeImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements),
					ImmutableList.of());
		}

		@Override
		public EpisodeDescriptor descriptor() {
			return super.descriptor();
		}

		@Override
		public SerialForm<Episode> serialForm() {
			return new EpisodeSerialForm(descriptor().serialForm(), this.proceduresBackup, this.measurements());
		}

	}

	@KdonTypeName("episode")
	private static class EpisodeSerialForm implements SerialForm<Episode> {

		@JsonProperty("descriptor")
		private final SerialForm<EpisodeDescriptor> descriptor;

		@JsonProperty("additionalMeasurementProcedures")
		private final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures;

		@JsonProperty("measurements")
		private final List<Measurement> measurements;

		@JsonCreator
		public EpisodeSerialForm(@JsonProperty("descriptor") SerialForm<EpisodeDescriptor> descriptorBuilder,
				@JsonProperty("additionalMeasurementProcedures") List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures,
				@JsonProperty("measurements") List<Measurement> measurements) {
			this.procedures = new ArrayList<>(procedures);
			this.descriptor = descriptorBuilder;
			this.measurements = measurements;
		}

		@Override
		public synchronized Episode build(Workspace workspace) {
			EpisodeDescriptor descriptor = this.descriptor.build(workspace);
			return Episodes.create(descriptor, this.procedures, this.measurements);
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return this.descriptor.dependencyIds();
		}

	}

	// Suppress default constructor for non-instantiability
	private Episodes() {
		throw new AssertionError();
	}

}
