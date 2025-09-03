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
package de.unibonn.realkd.patterns.emm;

import static de.unibonn.realkd.computations.core.Computations.computation;
import static de.unibonn.realkd.patterns.Frequency.FREQUENCY;
import static de.unibonn.realkd.patterns.emm.AngularDistanceOfSlopes.ANGULAR_DISTANCE_OF_SLOPES;
import static de.unibonn.realkd.patterns.emm.CumulativeJensenShannonDivergence.CJS;
import static de.unibonn.realkd.patterns.emm.HellingerDistance.HELLINGER_DISTANCE;
import static de.unibonn.realkd.patterns.emm.KolmogorovSmirnovStatistic.KOLMOGOROV_SMIRNOV_STATISTIC;
import static de.unibonn.realkd.patterns.emm.ManhattenMeanDistance.MANHATTAN_MEAN_DISTANCE;
import static de.unibonn.realkd.patterns.emm.NormalizedAbsoluteMeanShift.NORMALIZED_ABSOLUTE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedNegativeMeanShift.NORMALIZED_NEGATIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.NormalizedPositiveMeanShift.NORMALIZED_POSITIVE_MEAN_SHIFT;
import static de.unibonn.realkd.patterns.emm.PositiveProbabilityShift.POSITIVE_PROBABILITY_SHIFT;
import static de.unibonn.realkd.patterns.emm.TotalVariationDistance.TOTAL_VARIATION_DISTANCE;
//import static de.unibonn.realkd.patterns.emm.NormalizedMaxWithConstatntRef.NORMALIZED_MAX_CONSTANT_REF;
import static de.unibonn.realkd.patterns.subgroups.Subgroups.representativenessMeasurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.computations.core.CallableWithStopInterface;
import de.unibonn.realkd.computations.core.Computation;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.PatternBuilder;
import de.unibonn.realkd.patterns.PatternDescriptor;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;
import de.unibonn.realkd.patterns.models.Model;
import de.unibonn.realkd.patterns.models.ModelFactory;
import de.unibonn.realkd.patterns.subgroups.ControlledSubgroup;
import de.unibonn.realkd.patterns.subgroups.ReferenceDescriptor;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.patterns.subgroups.Subgroups;
import de.unibonn.realkd.patterns.subgroups.Subgroups.SubgroupSerialForm;
import de.unibonn.realkd.run.ComputationSpecification;

/**
 * <p>
 * Provides factory method for exceptional model patterns and measurement
 * procedures applicable to EMM pattern descriptors.
 * </p>
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.5.0
 *
 */
public class ExceptionalModelMining {

	public static ExceptionalModelPattern emmPattern(Subgroup<?> descriptor, ModelDeviationMeasure deviationMeasure,
			List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalMeasurementProcedures) {
		List<Measurement> measurements = new ArrayList<>();
		measurements.add(FREQUENCY.perform(descriptor));
		measurements.add(deviationMeasure.perform(descriptor));
		measurements.addAll(Subgroups.accuracyGainMeasurements(descriptor));
		// measurements.addAll(Subgroups.descriptiveModelMeasurements(descriptor));

		List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> procedures = additionalMeasurementProcedures
				.stream().filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());

		measurements.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));
		if (descriptor instanceof ControlledSubgroup<?, ?>) {
			representativenessMeasurement((ControlledSubgroup<?, ?>) descriptor).ifPresent(m -> measurements.add(m));
		}

		return new ExceptionalModelPatternImplementation(descriptor, measurements, deviationMeasure.getMeasure());
	}

	private static final Collection<? extends ModelDeviationMeasure> MODEL_DEVIATION_MEASURES = ImmutableList.of(
			TOTAL_VARIATION_DISTANCE, HELLINGER_DISTANCE, POSITIVE_PROBABILITY_SHIFT, MANHATTAN_MEAN_DISTANCE,
			NORMALIZED_POSITIVE_MEAN_SHIFT, NORMALIZED_NEGATIVE_MEAN_SHIFT, NORMALIZED_ABSOLUTE_MEAN_SHIFT,
			ANGULAR_DISTANCE_OF_SLOPES, CJS, KOLMOGOROV_SMIRNOV_STATISTIC);

	/**
	 * <p>
	 * Provides collection of all known instances of {@link ModelDeviationMeasure}.
	 * </p>
	 * 
	 * @since 0.4.0
	 * 
	 * @version 0.6.0
	 * 
	 * @return all known model deviation measures
	 */
	public static Collection<? extends ModelDeviationMeasure> modelDeviationMeasures() {
		return MODEL_DEVIATION_MEASURES;
	}

	private static class ExceptionalModelPatternImplementation extends DefaultPattern<Subgroup<?>>
			implements ExceptionalModelPattern {

		private final Subgroup<?> descriptor;

		private final ModelDeviationMeasure deviationMeasure;

		private ExceptionalModelPatternImplementation(Subgroup<?> descriptor, List<Measurement> measurements,
				ModelDeviationMeasure deviationMeasure) {
			super(descriptor.extensionDescriptor().population(), descriptor, measurements);
			this.descriptor = descriptor;
			this.deviationMeasure = deviationMeasure;
		}
		
		@Override
		public ExceptionalModelPattern add(Measurement measurement) {
			if(this.hasMeasure(measurement.measure())) {
				return this;
			}
			
			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);
			
			return new ExceptionalModelPatternImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements),
					this.deviationMeasure);
		}

		@Override
		public Subgroup<?> descriptor() {
			return this.descriptor;
		}

		@Override
		public ModelDeviationMeasure getDeviationMeasure() {
			return deviationMeasure;
		}

		@Override
		public SerialForm<ExceptionalModelPattern> serialForm() {
			return new ExceptionalModelPatternSerialForm(descriptor.serialForm(), this.deviationMeasure,
					measurements().stream().toArray(i -> new Measurement[i]));
		}

		@Override
		protected String typeString() {
			return "ExceptionalPattern";
		}

	}

	@KdonTypeName("exceptionalSubgroupPattern")
	public static class ExceptionalModelPatternSerialForm
			implements PatternBuilder<Subgroup<?>, ExceptionalModelPattern> {

		private final SerialForm<? extends Subgroup<?>> descriptor;

		@JsonProperty("measurements")
		private final Measurement[] measurements;

		@JsonProperty("deviationMeasure")
		private final ModelDeviationMeasure deviationMeasure;

		@JsonCreator
		private ExceptionalModelPatternSerialForm(
				@JsonProperty("descriptor") SerialForm<? extends Subgroup<?>> descriptorBuilder,
				@JsonProperty("deviationMeasure") ModelDeviationMeasure deviationMeasure,
				@JsonProperty("measurements") Measurement[] measurements) {
			this.descriptor = descriptorBuilder;
			this.deviationMeasure = deviationMeasure;
			this.measurements = measurements;
		}

		@Override
		public synchronized ExceptionalModelPattern build(Workspace workspace) {
			return new ExceptionalModelPatternImplementation(descriptor.build(workspace),
					Arrays.stream(measurements).collect(Collectors.toList()), deviationMeasure);
		}

		@Override
		@JsonProperty("descriptor")
		public synchronized SerialForm<? extends Subgroup<?>> descriptor() {
			return descriptor;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ExceptionalModelPatternSerialForm)) {
				return false;
			}
			ExceptionalModelPatternSerialForm otherBuilder = (ExceptionalModelPatternSerialForm) other;
			return (this.descriptor.equals(otherBuilder.descriptor)
					&& this.deviationMeasure.equals(otherBuilder.deviationMeasure)
					&& Arrays.equals(this.measurements, otherBuilder.measurements));
		}

	}

	/**
	 * Creates a function that maps extension descriptors to exceptional subgroup
	 * patterns; using a fixed sets of target attributes, a reference model, and a
	 * model factory for fitting the local model.
	 * 
	 * @param table
	 *            the data table containing the target attributes
	 * @param targets
	 *            target attributes
	 * @param modelFactory
	 *            factory for fitting the local model
	 * @param referenceModel
	 *            a constant reference model
	 * @param deviationMeasure
	 *            the procedure for measuring model distance
	 * @param additionalProcedures
	 *            other measurement procedures
	 * @return map from extension descriptor to exceptional subgroup
	 */
	public static Function<LogicalDescriptor, ExceptionalModelPattern> extensionDescriptorToEmmPatternMap(
			DataTable table, List<? extends Attribute<?>> targets, ModelFactory<?> modelFactory,
			Function<ReferenceDescriptor, Model> selectorToReferenceModel,
			Function<LogicalDescriptor, ReferenceDescriptor> descriptorToReferenceDescriptor,
			ModelDeviationMeasure deviationMeasure,
			List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalProcedures) {
		return d -> {
			ReferenceDescriptor referenceDescriptor = descriptorToReferenceDescriptor.apply(d);
			Model localModel = modelFactory.getModel(table, targets, d.supportSet());
			Model referenceModel = selectorToReferenceModel.apply(referenceDescriptor);
			Subgroup<?> subgroup = Subgroups.subgroup(d, referenceDescriptor, table, targets, modelFactory,
					referenceModel, localModel);
			return emmPattern(subgroup, deviationMeasure, additionalProcedures);
		};
	}

	// here need a map that computes both models

	/**
	 * Creates a function mapping extension descriptors to exceptional subgroup
	 * patterns; using a fixed sets of target attributes, a reference model, and a
	 * model factory for fitting the local model.
	 * 
	 * @param table
	 *            the data table containing the target attributes
	 * @param targetAttr
	 *            target attributes
	 * @param modelFactory
	 *            factory for fitting the local model
	 * @param referenceModel
	 *            a constant reference model
	 * @param distanceMeasurementProc
	 *            the procedure for measuring model distance
	 * @param additionalProcedures
	 *            other measurement procedures
	 * @return map from extension descriptor to exceptional subgroup
	 */
	public static <T extends Model, C extends Model> Function<LogicalDescriptor, ExceptionalModelPattern> extensionDescriptorToControlledEmmPatternMap(
			final DataTable table, final List<Attribute<?>> targetAttr, final ModelFactory<? extends T> modelFactory,
			final T referenceModel, final ModelDeviationMeasure distanceMeasurementProc,
			final List<MeasurementProcedure<? extends Measure, ? super PatternDescriptor>> additionalProcedures,
			final List<Attribute<?>> controlAttributes, final C referenceControlModel,
			final ModelFactory<C> controlModelFactory) {
		return d -> {
			T localModel = modelFactory.getModel(table, targetAttr, d.supportSet());
			C localControlModel = controlModelFactory.getModel(table, controlAttributes, d.supportSet());
			ControlledSubgroup<T, C> subgroup = Subgroups.controlledSubgroup(d, table, targetAttr, modelFactory,
					referenceModel, localModel, controlAttributes, controlModelFactory, referenceControlModel,
					localControlModel);
			return emmPattern(subgroup, distanceMeasurementProc, additionalProcedures);
		};
	}

	public static <T extends Model, C extends Model> Function<LogicalDescriptor, ExceptionalModelPattern> extensionDescriptorToControlledEmmPatternMap(
			final DataTable table, final List<Attribute<? extends Object>> targetAttr,
			final ModelFactory<? extends T> modelFactory, final ModelDeviationMeasure distanceMeasurementProc,
			final List<Attribute<? extends Object>> controlAttributes, final ModelFactory<C> controlModelFactory) {
		T referenceModel = modelFactory.getModel(table, targetAttr);
		C referenceControlModel = controlModelFactory.getModel(table, controlAttributes);
		return d -> {
			T localModel = modelFactory.getModel(table, targetAttr, d.supportSet());
			C localControlModel = controlModelFactory.getModel(table, controlAttributes, d.supportSet());
			ControlledSubgroup<T, C> subgroup = Subgroups.controlledSubgroup(d, table, targetAttr, modelFactory,
					referenceModel, localModel, controlAttributes, controlModelFactory, referenceControlModel,
					localControlModel);
			return emmPattern(subgroup, distanceMeasurementProc, ImmutableList.of());
		};
	}

	private static final Function<Pattern<?>, LogicalDescriptor> EXCEPTIONAL_SUBGROUP_TO_EXT_DESCR_MAP = pattern -> ((Subgroup<?>) pattern
			.descriptor()).extensionDescriptor();

	public static Function<Pattern<?>, LogicalDescriptor> exceptionalSubgroupToExtensionDescriptor() {
		return EXCEPTIONAL_SUBGROUP_TO_EXT_DESCR_MAP;
	}

	@KdonTypeName("exceptionalSubgroupComputation")
	public static class ExceptionalSubgroupComputationSpec implements ComputationSpecification {

		@JsonProperty("id")
		private final Identifier id;

		@JsonProperty("subgroup")
		private final SubgroupSerialForm<?> subgroupSpecification;

		@JsonProperty("measure")
		private final ModelDeviationMeasure deviationMeasure;

		@JsonCreator
		public ExceptionalSubgroupComputationSpec(@JsonProperty("id") Identifier id,
				@JsonProperty("subgroup") SubgroupSerialForm<?> subgroup,
				@JsonProperty("measure") ModelDeviationMeasure deviationMeasure) {
			this.id = id;
			this.subgroupSpecification = subgroup;
			this.deviationMeasure = deviationMeasure;
		}

		@Override
		public Computation<?> build(Workspace context) throws ValidationException {
			CallableWithStopInterface<ExceptionalModelPattern> callable = () -> emmPattern(
					subgroupSpecification.build(context), deviationMeasure, ImmutableList.of());
			return computation(callable);
		}

		@Override
		public Identifier identifier() {
			return id;
		}

	}

}
