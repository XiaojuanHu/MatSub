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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternBuilder;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 * Utilities for creating sequences.
 * 
 * @author Sandy Moens
 * 
 * @since 0.3.0
 * 
 * @version 0.6.0
 * 
 */
public class Sequences {

	/**
	 * Creates a new sequence with the specified descriptor and additional
	 * measurements procedures.
	 * 
	 * @param descriptor
	 *            the descriptor or the sequence pattern
	 * 
	 * @param additionalProcedures
	 *            additional measurement procedures that are applied to the
	 *            sequence
	 * 
	 * @return a new sequence
	 */
	public static Sequence create(SequenceDescriptor descriptor,
			List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> additionalProcedures) {
		List<Measurement> measurements = new ArrayList<>();

		List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> procedures = additionalProcedures.stream()
				.filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());

		measurements.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));

		return new SequenceImplementation(descriptor, measurements, ImmutableList.copyOf(procedures));
	}

	private static class SequenceImplementation extends DefaultPattern<SequenceDescriptor> implements Sequence {

		private List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> proceduresBackup;

		public SequenceImplementation(SequenceDescriptor description, List<Measurement> measurements,
				List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> procedures) {
			super(null, description, measurements);
			this.proceduresBackup = ImmutableList.copyOf(procedures);

		}
		
		@Override
		public Sequence add(Measurement measurement) {
			if(this.hasMeasure(measurement.measure())) {
				return this;
			}
			
			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);
			
			return new SequenceImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements),
					ImmutableList.of());
		}

		public SequenceDescriptor descriptor() {
			return (SequenceDescriptor) super.descriptor();
		}

		@Override
		public SerialForm<Sequence> serialForm() {
			return new SequenceBuilderImplementation(descriptor().serialForm(), proceduresBackup);
		}

	}

	private static class SequenceBuilderImplementation implements PatternBuilder<SequenceDescriptor, Sequence> {

		@JsonProperty("descriptor")
		private final SerialForm<SequenceDescriptor> descriptor;

		@JsonProperty("additionalMeasurementProcedures")
		private final List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> procedures;

		@JsonCreator
		public SequenceBuilderImplementation(
				@JsonProperty("descriptor") SerialForm<SequenceDescriptor> descriptorBuilder,
				@JsonProperty("additionalMeasurementProcedures") List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> procedures) {
			this.procedures = newArrayList(procedures);
			this.descriptor = descriptorBuilder;
		}

		@Override
		public synchronized Sequence build(Workspace workspace) {
			SequenceDescriptor seqDescriptor = descriptor.build(workspace);
			return Sequences.create(seqDescriptor, procedures);
		}

		@Override
		public synchronized SerialForm<SequenceDescriptor> descriptor() {
			return descriptor;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SequenceBuilderImplementation)) {
				return false;
			}
			SequenceBuilderImplementation otherAssBuilder = (SequenceBuilderImplementation) other;
			return (this.descriptor().equals(descriptor)
					&& this.procedures.equals(otherAssBuilder.procedures));
		}

	}

}
