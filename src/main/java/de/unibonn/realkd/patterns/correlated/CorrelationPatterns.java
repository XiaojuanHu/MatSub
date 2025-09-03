/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.correlated;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.DefaultPattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.models.table.ContingencyTables;

/**
 * @author Panagiotis Mandros
 *
 */
public class CorrelationPatterns {

	public static class AttributeSetRelationImplementation implements AttributeSetRelation {

		private final DataTable table;

		private final Set<Attribute<?>> attributeSet;

		private final List<Attribute<?>> allReferenced;

		private final ContingencyTable contingencyTable;

		private AttributeSetRelationImplementation(DataTable table, Set<Attribute<?>> attributeSet,
				ContingencyTable contingencyTable) {
			this.table = table;
			this.attributeSet = attributeSet;
			this.allReferenced = ImmutableList.copyOf(attributeSet);
			this.contingencyTable = contingencyTable;
		}

		@Override
		public List<Attribute<?>> getReferencedAttributes() {
			return allReferenced;
		}

		@Override
		public DataTable table() {
			return table;
		}

		@Override
		public Set<Attribute<?>> attributeSet() {
			return attributeSet;
		}

		@Override
		public ContingencyTable nWayContingencyTable() {
			return contingencyTable;
		}

		// TODO
		@Override
		public SerialForm<AttributeSetRelation> serialForm() {
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof AttributeSetRelation)) {
				return false;
			}

			AttributeSetRelation that = (AttributeSetRelation) o;
			HashSet<Attribute<?>> thisOne = new HashSet<>(this.attributeSet());
			HashSet<Attribute<?>> thatOne = new HashSet<>(that.attributeSet());
			return thisOne.equals(thatOne);
		}

		@Override
		public int hashCode() {
			return Objects.hash(new HashSet<>(this.attributeSet()));
		}

		@Override
		public String toString() {
			return "(" + attributeSet.toString() + ")";
		}

	}

	public static AttributeSetRelation attributeSetRelation(DataTable table, Set<Attribute<?>> attributeSet) {
		List<Attribute<?>> attributes = new ArrayList<>();
		attributes.addAll(attributeSet);
		ContingencyTable contingencyTable = ContingencyTables.contingencyTable(table, attributes);
		return new AttributeSetRelationImplementation(table, attributeSet, contingencyTable);
	}

	private static class CorrelationPatternImplementation extends DefaultPattern<AttributeSetRelation>
			implements CorrelationPattern {

		private final CorrelationMeasure measure;

		/**
		 * 
		 * @param population
		 * @param descriptor
		 * @param measurements non-empty list of measurements computed for this
		 *                     correlation descriptor, the first of which has to be of a
		 *                     {@link CorrelationMeasure}
		 */
		public CorrelationPatternImplementation(Population population, AttributeSetRelation descriptor,
				List<Measurement> measurements) {
			super(population, descriptor, measurements);
			measure = (CorrelationMeasure) measurements.get(0).measure();
		}

		@Override
		public AttributeSetRelation descriptor() {
			return (AttributeSetRelation) super.descriptor();
		}

		// TODO
		@Override
		public SerialForm<CorrelationPattern> serialForm() {
			return null;
		}

		@Override
		public Measure correlationMeasure() {
			return measure;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CorrelationPattern)) {
				return false;
			}
			CorrelationPattern that = (CorrelationPattern) o;
			return this.descriptor().equals(that.descriptor());
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.descriptor());
		}

	}

	public static CorrelationPattern correlationPattern(AttributeSetRelation descriptor, CorrelationMeasure measure,
			Measurement... additionalMeasurements) {
		List<Measurement> measurements = new ArrayList<>();
		measurements.add(measure.perform(descriptor));
		stream(additionalMeasurements).forEach(m -> measurements.add(m));
		return new CorrelationPatternImplementation(descriptor.table().population(), descriptor, measurements);
	}

	public static CorrelationPattern correlationPattern(AttributeSetRelation descriptor) {
		return correlationPattern(descriptor, ReliableNormalizedTotalCorrelation.RELIABLE_NORMALIZED_TOTAL_CORRELATION);
	}

	public static CorrelationPattern correlationPattern(AttributeSetRelation descriptor,
			Measurement dependencyMeasurement, Measurement... additionalMeasurements) {
		List<Measurement> measurements = new ArrayList<>();
		measurements.add(dependencyMeasurement);
		stream(additionalMeasurements).forEach(m -> measurements.add(m));
		return new CorrelationPatternImplementation(descriptor.table().population(), descriptor, measurements);
	}

}
