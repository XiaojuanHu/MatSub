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
package de.unibonn.realkd.patterns.util;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.data.Population;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.PatternDescriptor;

/**
 * Utility class for patterns created to keep the core interfaces clean.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.1.2.1
 *
 */
public class PatternUtil {

//	/**
//	 * <p>
//	 * Constructs new pattern object based on the descriptor and measurements of
//	 * another origin pattern and additional measurements. When origin pattern
//	 * already contains a "top-level", i.e., non-auxiliary, measurement for the
//	 * same measure as the additional measurement, then this measurement will be
//	 * replaced in the created pattern object. Otherwise the measurement
//	 * collection will simply be augmented.
//	 * </p>
//	 * <p>
//	 * Regarding the class of the created patterns, it is guaranteed that if the
//	 * origin pattern is an Association, the result type will again be an
//	 * Association. Similarly, for ExceptionalModelPatterns. In particular, the
//	 * new pattern will always use the same descriptor object as the origin
//	 * pattern.
//	 * </p>
//	 * 
//	 * @param pattern
//	 * @param additionalMeasurements
//	 * @return
//	 */
//	public static Pattern createPatternWithAdditionalMeasurements(
//			Pattern pattern, QualityMeasurement... additionalMeasurements) {
//
//		List<QualityMeasurement> newMeasurements = new ArrayList<>(
//				pattern.getMeasurements());
//
//		for (QualityMeasurement additionalMeasurement : additionalMeasurements) {
//			Optional<QualityMeasurement> oldMeasurementOfSameMeasure = newMeasurements
//					.stream()
//					.filter(m -> m.getMeasureId().equals(
//							additionalMeasurement.getMeasureId())).findFirst();
//
//			if (oldMeasurementOfSameMeasure.isPresent()) {
//				newMeasurements.set(newMeasurements
//						.indexOf(oldMeasurementOfSameMeasure.get()),
//						additionalMeasurement);
//			} else {
//				newMeasurements.add(additionalMeasurement);
//			}
//		}
//
//		if (pattern instanceof Association) {
//			return Association.createAssociation(((Association) pattern).getDescriptor(),
//					newMeasurements);
//		}
//		if (pattern instanceof ExceptionalModelPattern) {
//			return ExceptionalModelPattern.create(
//					((ExceptionalModelPattern) pattern).getDescriptor(),
//					newMeasurements,
//					((ExceptionalModelPattern) pattern).getDeviationMeasure());
//		}
//		/*
//		 * Include again when outlier provides standardized ways of construction
//		 */
//		// if (pattern instanceof Outlier) {
//		// return new Outlier(((Outlier) pattern).getDatatable(),
//		// ((Outlier) pattern).getDescriptor().getSupportSet(),
//		// ((Outlier) pattern).getDescriptor()
//		// .getReferencedAttributes(), newMeasurements);
//		// }
//		return new DefaultPattern(pattern.getDataArtifact(),
//				pattern.getDescriptor(), newMeasurements);
//	}

	/**
	 * Abstract 0-element that can be used for providing absolute positive and
	 * negative examples within the preference learning frameworks for patterns.
	 * This is done by providing: positivePattern is preferred over ZERO_PATTERN
	 * (or the other way around for negative examples).
	 * 
	 * Inner products with this pattern must be 0 in all implementations of
	 * InnerProductSpace. Does not support any methods other than toString and
	 * equals.
	 * 
	 * This is not to be confused with the empty pattern!
	 */
	public static final Pattern<?> ZERO_PATTERN = new Pattern<PatternDescriptor>() {

		@Override
		public Pattern<?> clone() {
			throw new UnsupportedOperationException();
		}
		
		@Override 
		public Pattern<PatternDescriptor> add(Measurement measurement) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "ZERO";
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}

		@Override
		public List<Measure> measures() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double value(Measure measure) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasMeasure(Measure measure) {
			return false;
		}

		@Override
		public Population population() {
			throw new UnsupportedOperationException();
		}

		@Override
		public PatternDescriptor descriptor() {
			return new PatternDescriptor() {

				@Override
				public SerialForm<? extends PatternDescriptor> serialForm() {
					throw new UnsupportedOperationException();
				}

			};
		}

		@Override
		public List<Measurement> measurements() {
			return ImmutableList.of();
		}

		@Override
		public SerialForm<? extends Pattern<PatternDescriptor>> serialForm() {
			return null;
		}

		@Override
		public boolean hasMeasurement(Class<? extends Measure> measure) {
			return false;
		}

		@Override
		public Optional<Measurement> measurement(Class<? extends Measure> measureClass) {
			return Optional.empty();
		}

	};

}
