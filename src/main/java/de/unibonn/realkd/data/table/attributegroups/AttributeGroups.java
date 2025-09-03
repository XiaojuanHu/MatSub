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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;

/**
 * Provides static factory method for the construction of attribute groups.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class AttributeGroups {

	private AttributeGroups() {
		; // not to be instantiated
	}

	/**
	 * 
	 * @param name
	 *            the name of the resulting group
	 * @param elements
	 *            the elements of the resulting group
	 * @return attribute group the elements of which jointly represent a
	 *         distribution for each member of an underlying population
	 */
	public static DistributionGroup distributionGroup(String name, List<MetricAttribute> elements) {
		return new DefaultDistributionGroup(name, elements, false);
	}

	/**
	 * Constructs a distribution group where the order of the contained
	 * attributes corresponds to the underlying order of the distribution
	 * domain.
	 * 
	 * @param name
	 *            the name of the resulting group
	 * @param elements
	 *            the elements of the resulting group
	 * @return attribute group the elements of which jointly represent a
	 *         distribution for each member of an underlying population
	 * 
	 * @see #distributionGroup(String, List)
	 * 
	 */
	public static DistributionGroup orderedDistributionGroup(String name, List<MetricAttribute> elements) {
		return new DefaultDistributionGroup(name, elements, true);
	}

	private static class DefaultFunctionalGroup extends DefaultAttributeGroup implements FunctionalGroup {

		public DefaultFunctionalGroup(@JsonProperty("name") String name,
				@JsonProperty("elements") List<? extends Attribute<?>> members) {
			super(name, members);
		}
		
	}

	private static class DefaultDistributionGroup extends DefaultAttributeGroup
			implements DistributionGroup, FunctionalGroup {

		private final boolean ordered;

		@JsonCreator
		public DefaultDistributionGroup(@JsonProperty("name") String name,
				@JsonProperty("elements") List<MetricAttribute> elements, @JsonProperty("ordered") boolean ordered) {
			super(name, elements);
			this.ordered = ordered;
		}

		@Override
		public List<MetricAttribute> elements() {
			@SuppressWarnings("unchecked") // safe since constructor guards type
			List<MetricAttribute> elements = (List<MetricAttribute>) super.elements();
			return elements;
		}

		@Override
		public boolean ordered() {
			return ordered;
		}

	}

	public static FunctionalGroup functionalGroup(String name, List<? extends Attribute<?>> members) {
		return new DefaultFunctionalGroup(name, members);
	}

}
