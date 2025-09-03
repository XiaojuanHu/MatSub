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
package de.unibonn.realkd.patterns.pmm;

import static de.unibonn.realkd.patterns.Frequency.FREQUENCY;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.MeasurementProcedure;
import de.unibonn.realkd.patterns.PatternBuilder;
import de.unibonn.realkd.patterns.emm.ExceptionalModelMining;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * <p>
 * Provides static factory methods for the construction of simple model mining
 * subgroups as well as measurement procedures for pure model mining.
 * </p>
 * <p>
 * Pure model subgroups capture a subset of a population for which a selection
 * of target attributes can be represented "purely" by a certain model class.
 * The meaning of "purely" can vary between low prediction or fitting error, or
 * simply low target attribute variance.
 * </p>
 * <p>
 * In contrast to exceptional model mining, pure model mining is agnostic to the
 * global data distribution. However, depending on the specific configuration,
 * pure model mining is highly non-agnostic to the local data distribution,
 * which it explicitly tests for good fits of distributions that may have a
 * strong bias.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 * 
 * @see ExceptionalModelMining
 *
 */
public class PureModelMining {

	/**
	 * Creates a new pure model subgroup pattern based on a provided descriptor,
	 * a purity measurement procedure, and a list of optional measurement
	 * procedures. In addition to the specified procedures the resulting
	 * subgroup pattern will also contain a frequency measurement.
	 * 
	 * @param descriptor
	 *            the descriptor of the new subgroup
	 * @param purityMeasurementProcedure
	 *            the procedure to be used to measure model purity of the
	 *            subgroup
	 * @param optionalMeasurementProcedures
	 *            more optional procedures to be applied for the new subgroup
	 * @return new pure model subgroup pattern
	 */
	public static PureModelSubgroup pureSubgroup(final Subgroup<?> descriptor,
			final MeasurementProcedure<? extends Measure, ? super Subgroup<?>> purityMeasurementProcedure,
			final List<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>> optionalMeasurementProcedures) {

		List<MeasurementProcedure<? extends Measure, ? super Subgroup<?>>> list = Lists.newArrayList();
		list.add(FREQUENCY);
		list.add(purityMeasurementProcedure);
		list.addAll(optionalMeasurementProcedures);

		List<Measurement> measurements = list.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList());

		List<Measurement> allMeasurements = new ArrayList<>(measurements);
		// allMeasurements.addAll(Subgroups.descriptiveModelMeasurements(descriptor));

		return new PureModelSubgroupImplementation(descriptor, allMeasurements,
				purityMeasurementProcedure.getMeasure());
	}

	private PureModelMining() {
		;
	}

	@KdonTypeName("pureSubgroupPattern")
	public static class PureModelPatternSerialForm
			implements PatternBuilder<Subgroup<?>, PureModelSubgroup> {

		private final SerialForm<? extends Subgroup<?>> descriptor;

		@JsonProperty("purityMeasure")
		private final Measure purityMeasure;

		@JsonProperty("measurements")
		private final Measurement[] measurements;

		@JsonCreator
		private PureModelPatternSerialForm(
				@JsonProperty("descriptor") SerialForm<? extends Subgroup<?>> descriptor,
				@JsonProperty("purityMeasure") Measure purityMeasure,
				@JsonProperty("measurements") Measurement[] measurements) {
			this.descriptor = descriptor;
			this.purityMeasure = purityMeasure;
			this.measurements = measurements;
		}

		@JsonProperty("descriptor")
		@Override
		public SerialForm<? extends Subgroup<?>> descriptor() {
			return descriptor;
		}

		@Override
		public PureModelSubgroup build(Workspace context) {
			Subgroup<?> subgroup = descriptor.build(context);
			return new PureModelSubgroupImplementation(subgroup, Arrays.stream(measurements).collect(toList()),
					purityMeasure);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof PureModelPatternSerialForm)) {
				return false;
			}
			PureModelPatternSerialForm otherBuilder = (PureModelPatternSerialForm) other;
			return (Objects.equals(this.descriptor, otherBuilder.descriptor)
					&& Objects.equals(this.purityMeasure, otherBuilder.purityMeasure)
					&& Arrays.equals(this.measurements, otherBuilder.measurements));
		}

	}

	private static class PureModelSubgroupImplementation extends DefaultPattern<Subgroup<?>>
			implements PureModelSubgroup {

		private final Measure purityMeasure;

		public PureModelSubgroupImplementation(Subgroup<?> descriptor, List<Measurement> measurements,
				Measure purityMeasure) {
			super(descriptor.population(), descriptor, measurements);
			this.purityMeasure = purityMeasure;
		}

		@Override
		public Subgroup<?> descriptor() {
			return (Subgroup<?>) super.descriptor();
		}

		@Override
		public Measure purityGainMeasure() {
			return purityMeasure;
		}

		@Override
		public SerialForm<PureModelSubgroup> serialForm() {
			return new PureModelPatternSerialForm(descriptor().serialForm(), purityMeasure,
					measurements().stream().toArray(i -> new Measurement[i]));
		}

	}

}
