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
package de.unibonn.realkd.patterns.rules;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

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
 * Provides factory method for the construction of association rules.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * 
 * @since 0.1.2
 * 
 * @version 0.7.1
 *
 */
public class AssociationRules {

	public static AssociationRule create(RuleDescriptor descriptor, List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> additionalProcedures) {

		List<Measurement> measurements = new ArrayList<>();
		
		List<MeasurementProcedure<? extends Measure,? super PatternDescriptor>> procedures = 
				additionalProcedures.stream().filter(p -> p.isApplicable(descriptor)).collect(Collectors.toList());
		
		measurements.addAll(procedures.stream().map(p -> p.perform(descriptor)).collect(Collectors.toList()));
		
		return new AssociationRuleImplementation(descriptor, measurements);
	}

	private static class AssociationRuleImplementation extends DefaultPattern<RuleDescriptor> implements AssociationRule {

		private AssociationRuleImplementation(RuleDescriptor description, List<Measurement> measurements) {
			super(description.population(), description, measurements);
		}
		
		@Override
		public AssociationRule add(Measurement measurement) {
			if(this.hasMeasure(measurement.measure())) {
				return this;
			}
			
			List<Measurement> newMeasurements = Lists.newArrayList(measurements());
			newMeasurements.add(measurement);
			
			return new AssociationRuleImplementation(this.descriptor(), ImmutableList.copyOf(newMeasurements));
		}

		@Override
		public RuleDescriptor descriptor() {
			return (RuleDescriptor) super.descriptor();
		}

		@Override
		public SerialForm<AssociationRule> serialForm() {
			return new AssociationRuleBuilderImplementation(descriptor().serialForm(),
					measurements().stream().toArray(i -> new Measurement[i]));
		}

	}
	
	private static class AssociationRuleBuilderImplementation implements PatternBuilder<RuleDescriptor,AssociationRule> {

		@JsonProperty("descriptor")
		private final SerialForm<RuleDescriptor> descriptor;

		@JsonProperty("measurements")
		private final Measurement[] measurements;
		
		@JsonCreator
		public AssociationRuleBuilderImplementation(
				@JsonProperty("descriptor") SerialForm<RuleDescriptor> descriptor,
				@JsonProperty("measurements") Measurement[] measurements) {
			this.descriptor = descriptor;
			this.measurements = measurements;
		}

		@Override
		public synchronized AssociationRule build(Workspace workspace) {
			RuleDescriptor ruleDescriptor = descriptor.build(workspace);
			return new AssociationRuleImplementation(ruleDescriptor, stream(measurements).collect(toList()));
		}

		@Override
		public synchronized SerialForm<RuleDescriptor> descriptor() {
			return descriptor;
		}

		// @Override
		// public List<MeasurementProcedure> getMeasurementProcedures() {
		// return ImmutableList.copyOf(procedures);
		// }

	}

}
