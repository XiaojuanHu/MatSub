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
package de.unibonn.realkd.patterns.association;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.measures.Measures;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternBuilder;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.QualityMeasureId;
import de.unibonn.realkd.patterns.logical.Lift;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * <p>
 * Utility class providing access to singleton association measurement
 * procedures and static factory method for association construction.
 * </p>
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.7.1
 *
 */
public class Associations {

	/**
	 * <p>
	 * Creates an association pattern from a given logical descriptor with a
	 * form of lift measurement and any number of additional measurements based
	 * on an input list of measurement procedures.
	 * </p>
	 * <p>
	 * The lift measurement is adapted in the following sense: In case lift
	 * measurement is positive, creates an association with lift measurement; in
	 * case lift measurement is positive, creates an association with negative
	 * lift measurement (which consequently will be positive).
	 * </p>
	 * 
	 * @param descriptor
	 *            the logical descriptor of the association pattern
	 * @param additionalProcedures
	 *            other measurement procedures that the results of which are
	 *            added to the pattern
	 * @return an association pattern with measurement for positive or negative
	 *         lift
	 */
	public static Association association(LogicalDescriptor descriptor,
			List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalProcedures) {
		List<Measurement> measurements = new ArrayList<>();

		Measurement liftMeasurement = Lift.LIFT.perform(descriptor);
		/*
		 * Turn lift measurement in negative lift measurement in case lift is
		 * negative
		 */
		if (liftMeasurement.value() < 0) {
			liftMeasurement = Measures.measurement(QualityMeasureId.NEGATIVE_LIFT, -1 * liftMeasurement.value(),
					liftMeasurement.auxiliaryMeasurements());
		}
		measurements.add(liftMeasurement);

		List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures = additionalProcedures
				.stream().filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());

		measurements.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));

		return new AssociationImplementation(descriptor, measurements);
	}

	/**
	 * <p>
	 * Creates an association pattern from a given logical descriptor with a
	 * form of lift measurement.
	 * </p>
	 * 
	 * @param descriptor
	 *            the logical descriptor of the association pattern
	 * @return an association pattern with measurement for positive or negative
	 *         lift
	 * 
	 * @see #association(LogicalDescriptor, List)
	 * 
	 */
	public static Association association(LogicalDescriptor descriptor) {
		return association(descriptor, ImmutableList.of());

	}

	private static class AssociationImplementation extends DefaultPattern<LogicalDescriptor> implements Association {

		private AssociationImplementation(LogicalDescriptor description, List<Measurement> measurements) {
			super(description.population(), description, measurements);
		}
		
		@Override
		public Association add(Measurement measurement) {
			if(this.hasMeasure(measurement.measure())) {
				return this;
			}
			
			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);
			
			return new AssociationImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements));
		}

		@Override
		public double getLift() {
			if (hasMeasure(Lift.LIFT)) {
				return value(Lift.LIFT);
			} else {
				return -1 * value(QualityMeasureId.NEGATIVE_LIFT);
			}
		}

		@Override
		public double getExpectedFrequency() {
			return value(QualityMeasureId.EXPECTED_FREQUENCY);
		}

		@Override
		public LogicalDescriptor descriptor() {
			return (LogicalDescriptor) super.descriptor();
		}

		@Override
		public SerialForm<Association> serialForm() {
			return new AssociationBuilderImplementation(descriptor().serialForm(),
					measurements().stream().toArray(i -> new Measurement[i]));
		}
	}

	@KdonTypeName("association")
	public static final class AssociationBuilderImplementation
			implements PatternBuilder<LogicalDescriptor, Association> {

		private final SerialForm<LogicalDescriptor> descriptor;

		@JsonProperty("measurements")
		private final Measurement[] measurements;

		@JsonCreator
		private AssociationBuilderImplementation(@JsonProperty("descriptor") SerialForm<LogicalDescriptor> descriptor,
				@JsonProperty("measurements") Measurement[] measurements) {
			this.descriptor = descriptor;
			this.measurements = measurements;
		}

		@Override
		public synchronized Association build(Workspace context) {
			LogicalDescriptor logicalDescriptor = descriptor.build(context);
			return new AssociationImplementation(logicalDescriptor, stream(measurements).collect(toList()));
		}

		@Override
		@JsonProperty("descriptor")
		public synchronized SerialForm<LogicalDescriptor> descriptor() {
			return descriptor;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AssociationBuilderImplementation)) {
				return false;
			}
			AssociationBuilderImplementation otherAssBuilder = (AssociationBuilderImplementation) other;
			return (this.descriptor().equals(otherAssBuilder.descriptor)
					&& Arrays.equals(this.measurements, otherAssBuilder.measurements));
		}

	}

}
