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
package de.unibonn.realkd.data.table.attributegroups;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public interface DistributionGroup extends AttributeGroup {

	@Override
	public List<MetricAttribute> elements();

	/**
	 * 
	 * @return whether the order of the the list returned by
	 *         {@link #elements()} reflects some underlying order
	 */
	@JsonProperty("ordered")
	public boolean ordered();

	/**
	 * 
	 * @param i
	 *            the object index between 0 and the number of population
	 *            members
	 * @return list of values of all contained attributes (substituting 0.0 for
	 *         missing values)
	 */
	public default List<Double> values(int i) {
		Stream<Double> valueOrZero = elements().stream().map(a -> a.getValueOption(i).orElse(0.0));
		return valueOrZero.collect(Collectors.toList());
	}
	

}
