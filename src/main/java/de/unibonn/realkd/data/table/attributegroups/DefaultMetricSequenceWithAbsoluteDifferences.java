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

package de.unibonn.realkd.data.table.attributegroups;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

public class DefaultMetricSequenceWithAbsoluteDifferences extends DefaultAttributeGroup
		implements OrderedAttributeSequence<Double> {

	private static final Logger LOGGER = Logger.getLogger(DefaultMetricSequenceWithAbsoluteDifferences.class.getName());

	private List<Attribute<Double>> consecutiveChangeAttributes;

	@JsonProperty("sequenceElementNames")
	private List<String> sequenceElementNames;

	@JsonCreator
	public DefaultMetricSequenceWithAbsoluteDifferences(@JsonProperty("name") String name,
			@JsonProperty("elements") List<MetricAttribute> members,
			@JsonProperty("sequenceElementNames") List<String> sequenceElementNames) {
		super(name, members);
		this.sequenceElementNames = sequenceElementNames;
		this.consecutiveChangeAttributes = new ArrayList<Attribute<Double>>(members.size() - 1);
		for (int i = 0; i < members.size() - 1; i++) {
			MetricAttribute changeAttribute = createChangeAttribute(i, i + 1);
			LOGGER.fine("Buffering change attribute '" + changeAttribute.caption() + "'");
			this.consecutiveChangeAttributes.add(changeAttribute);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Attribute<Double>> elements() {
		return (List<Attribute<Double>>) super.elements();
	}

	private MetricAttribute createChangeAttribute(int fromIndex, int toIndex) {
		List<Double> values = new ArrayList<Double>(elements().get(fromIndex).maxIndex() + 1);
		for (int j = 0; j <= elements().get(0).maxIndex(); j++) {
			if (elements().get(fromIndex).valueMissing(j) || elements().get(toIndex).valueMissing(j)) {
				values.add(null);
			} else {
				values.add(elements().get(toIndex).value(j) - elements().get(fromIndex).value(j));
			}
		}
		MetricAttribute changeAttribute = Attributes.metricDoubleAttribute(
				name() + " plus/minus " + sequenceElementNames.get(fromIndex) + "-" + sequenceElementNames.get(toIndex),
				"The difference of " + name() + " between " + sequenceElementNames.get(fromIndex) + " and "
						+ sequenceElementNames.get(toIndex),
				values);
		return changeAttribute;
	}

	@Override
	public Attribute<Double> getChangeAttribute(int fromIndex, int toIndex) {
		if (toIndex == fromIndex + 1) {
			return consecutiveChangeAttributes.get(fromIndex);
		}
		return createChangeAttribute(fromIndex, toIndex);
	}

	@Override
	public String getNameInSequence(int index) {
		return sequenceElementNames.get(index);
	}

}
