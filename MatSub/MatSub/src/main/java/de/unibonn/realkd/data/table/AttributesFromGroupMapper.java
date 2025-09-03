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
package de.unibonn.realkd.data.table;

import static de.unibonn.realkd.data.table.attribute.Attributes.categoricalAttribute;
import static de.unibonn.realkd.data.table.attribute.Attributes.orderedCategoricAttribute;
import static de.unibonn.realkd.util.Lists.listOrEmpty;
import static java.util.stream.IntStream.rangeClosed;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.Attributes;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.FiniteOrder;
import de.unibonn.realkd.data.table.attribute.OrderedCategoricAttribute;
import de.unibonn.realkd.data.table.attributegroups.AttributeGroup;
import de.unibonn.realkd.data.table.attributegroups.DistributionGroup;
import de.unibonn.realkd.data.table.attributegroups.OrderedAttributeSequence;
import de.unibonn.realkd.util.Lists;
import de.unibonn.realkd.util.Search;

public enum AttributesFromGroupMapper implements Function<AttributeGroup, List<Attribute<?>>> {

	CONSECUTIVE_CHANGE_ATTRIBUTES {
		@Override
		public List<Attribute<?>> apply(AttributeGroup attributeGroup) {

			return (attributeGroup instanceof OrderedAttributeSequence)
					? changeAttributes((OrderedAttributeSequence<?>) attributeGroup) : ImmutableList.of();
		}

		private List<Attribute<?>> changeAttributes(OrderedAttributeSequence<?> sequenceGroup) {
			return IntStream.range(0, sequenceGroup.elements().size() - 1)
					.mapToObj(i -> sequenceGroup.getChangeAttribute(i, i + 1)).collect(Collectors.toList());
		}
	},

	DISTRIBUTION_MODE {
		@Override
		public List<Attribute<?>> apply(AttributeGroup attributeGroup) {
			if (attributeGroup instanceof DistributionGroup) {
				DistributionGroup distributionGroup = (DistributionGroup) attributeGroup;
				IntFunction<String> indexToMaxName = i -> distributionGroup.elements().stream()
						.filter(a -> !a.valueMissing(i)).reduce((a, b) -> b.value(i) > a.value(i) ? b : a)
						.map(a -> a.caption()).orElse(null);
				List<String> maximizers = rangeClosed(0, distributionGroup.elements().get(0).maxIndex())
						.mapToObj(indexToMaxName).collect(Collectors.toList());
				String name = distributionGroup.name() + " mode";
				String description = "The mode of " + distributionGroup.name();
				List<String> categories = distributionGroup.elements().stream().map(a -> a.caption())
						.collect(Collectors.toList());
				// Comparator comparator = Comparator.comparingInt(new
				// CategoryIndexExtractor(categories));
				Comparator<String> comparator = new FiniteOrder(categories);
				Attribute<String> modeAttribute = distributionGroup.ordered()
						? orderedCategoricAttribute(name, description, maximizers, comparator, String.class)
						: categoricalAttribute(name, description, maximizers);
				return ImmutableList.of(modeAttribute);
			}
			return ImmutableList.of();
		}
	},

	DISTRIBUTION_SHAPE {

		@Override
		public List<Attribute<?>> apply(AttributeGroup group) {
			return listOrEmpty(group instanceof DistributionGroup && ((DistributionGroup) group).ordered(),
					() -> shapeAttribute((DistributionGroup) group));
		}

		private String valuesToShape(List<Double> values) {
			return Search.localMaxIndices(Lists.valueSequence(values)).size() == 1 ? "Unimodal" : "Multimodal";
		}

		private CategoricAttribute<String> shapeAttribute(DistributionGroup group) {
			List<String> shapes = IntStream.rangeClosed(0, group.elements().get(0).maxIndex())
					.mapToObj(i -> valuesToShape(group.values(i))).collect(Collectors.toList());
			String name = group.name() + " shape";
			String description = "The shape of " + group.name();
			return Attributes.categoricalAttribute(name, description, shapes);
		}

	},

	DISTRIBUTION_MEDIAN {

		private int meanIndex(List<Double> potentialValues) {
			double cumProb = 0.0;
			double normConstant = sum(potentialValues);
			for (int i = 0; i < potentialValues.size(); i++) {
				cumProb += potentialValues.get(i) / normConstant;
				if (cumProb >= 0.5) {
					return i;
				}
			}
			throw new IllegalArgumentException("normalized potentials do not add up to 1");
		}

		private double sum(List<Double> values) {
			return values.stream().mapToDouble(v -> v).sum();
		}

		@Override
		public List<Attribute<?>> apply(AttributeGroup attributeGroup) {
			return listOrEmpty(
					attributeGroup instanceof DistributionGroup && ((DistributionGroup) attributeGroup).ordered(),
					() -> meanAttribute((DistributionGroup) attributeGroup));
		}

		private OrderedCategoricAttribute<String> meanAttribute(DistributionGroup group) {
			String name = group.name() + " median";
			String description = "The median of " + group.name();
			List<String> categories = group.elements().stream().map(a -> a.caption()).collect(Collectors.toList());
			List<String> means = IntStream.rangeClosed(0, group.elements().get(0).maxIndex())
					.map(i -> meanIndex(group.values(i))).mapToObj(j -> group.elements().get(j).caption())
					.collect(Collectors.toList());
			Comparator<String> comparator = new FiniteOrder(categories);
			OrderedCategoricAttribute<String> meanAttribute = orderedCategoricAttribute(name, description, means,
					comparator, String.class);
			return meanAttribute;
		}

	};

	public abstract List<Attribute<?>> apply(AttributeGroup attributeGroup);

}