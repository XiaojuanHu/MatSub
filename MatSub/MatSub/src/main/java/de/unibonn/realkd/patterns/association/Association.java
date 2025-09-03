/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.patterns.association;

import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

/**
 * <p>
 * Pattern that captures above (or below) expectation frequency of the
 * conjunction of a set of propositions.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.1.2.1
 * 
 */
public interface Association extends Pattern<LogicalDescriptor> {

	public double getLift();

	public double getExpectedFrequency();

	@Override
	public LogicalDescriptor descriptor();

	@Override
	public SerialForm<Association> serialForm();

	/*
	 * In the future we want to just guarantee that at least one association
	 * measurement from a specific white list is included (and provide a
	 * reference to those for a given object)
	 */
	// public static final Set<QualityMeasureId> ASSOCIATION_MEASURES =
	// ImmutableSet
	// .of(QualityMeasureId.LIFT, QualityMeasureId.NEGATIVE_LIFT,
	// QualityMeasureId.ABSOLUTE_LIFT);

	// /**
	// * WARNING: currently this assumes that the measurements contain either a
	// * measurement for LIFT or NEGATIVE_LIFT and an auxiliary measurment of
	// * PRODUCT_OF_INDIVIDUAL_FREQUENCIES. Will be generalized in the future.
	// *
	// */
	// public static Association create(LogicalDescriptor description,
	// List<QualityMeasurement> measurements) {
	// return new Association(description, measurements);
	// }

	// public static AssociationBuilder getNewBuilder(
	// LogicalDescriptorBuilder descriptorBuilder) {
	// return new AssociationBuilderImplementation(descriptorBuilder,
	// new ArrayList<>());
	// }

}
