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
package de.unibonn.realkd.data.propositions;

import static de.unibonn.realkd.data.constraints.Constraints.divisibleBy;
import static de.unibonn.realkd.data.constraints.Constraints.inClosedInterval;
import static de.unibonn.realkd.data.constraints.Constraints.namedConstraint;
import static de.unibonn.realkd.data.constraints.Constraints.notDivisibleBy;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.realkd.data.constraints.Constraint;
import de.unibonn.realkd.data.constraints.Constraints;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * Collection of legacy attribute to proposition mappers, all of which are
 * contained in {@link PropositionalContextFromTableBuilder#ALL_MAPPERS} and are
 * thus valid options for proposition constructions. However, this collection is
 * subject to be removed and consolidated in future versions.
 * 
 * @author Mario Boley
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.0
 * 
 * @version 0.3.0
 *
 */
public enum LegacyAttributesToPropositionsMapper implements PropositionalizationRule {

	CATEGORIC_EQUALiTY {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof CategoricAttribute && !(attribute instanceof OrdinalAttribute)) {
				for (Object category : ((CategoricAttribute<?>) attribute).categories()) {
					result.add(
							Propositions.proposition(table, (Attribute<?>) attribute, Constraints.equalTo(category)));
				}
			}
			return result;
		}
	},

	CATEGORIC_INEQUALiTY {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof CategoricAttribute && !(attribute instanceof OrdinalAttribute)) {
				for (Object category : ((CategoricAttribute<?>) attribute).categories()) {
					result.add(
							Propositions.proposition(table, (Attribute<?>) attribute, Constraints.notEqualTo(category)));
				}
			}
			return result;
		}
	},
	
	ABOVE_MEDIAN {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof OrdinalAttribute && !(attribute instanceof MetricAttribute)) {
				OrdinalAttribute<T> ordinalAtt = (OrdinalAttribute<T>) attribute;
				Constraint<T> constraint = Constraints.greaterThan(ordinalAtt.median(), ordinalAtt.valueComparator());
				result.add(Propositions.proposition(table, attribute, constraint));
			}
			return result;
		}
	},

	VERY_LOW_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute defaultMetricAttribute = (MetricAttribute) attribute;
				double upperValue = defaultMetricAttribute.mean() - 1.5 * defaultMetricAttribute.standardDeviation();
				Constraint<Double> lessThan = Constraints.lessThan(upperValue);
				String name = "very low";
				String description = name + " [inf," + (float) upperValue + ")";
				result.add(Propositions.proposition(table, defaultMetricAttribute,
						Constraints.namedConstraint(lessThan, "=" + name, description)));
			}
			return result;
		}
	},

	NOT_VERY_LOW_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute defaultMetricAttribute = (MetricAttribute) attribute;
				double lowerValue = defaultMetricAttribute.mean() - 1.5 * defaultMetricAttribute.standardDeviation();
				Constraint<Double> largerThan = Constraints.greaterOrEquals(lowerValue);
				String name = "not very low";
				String description = name + " [" + (float) lowerValue + ", inf]";
				result.add(Propositions.proposition(table, defaultMetricAttribute,
						Constraints.namedConstraint(largerThan, "=" + name, description)));
			}
			return result;
		}
	},

	LOW_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute defaultMetricAttribute = (MetricAttribute) attribute;
				double upperValue = defaultMetricAttribute.mean() - 0.5 * defaultMetricAttribute.standardDeviation();
				Constraint<Double> lessThan = Constraints.lessThan(upperValue);
				String name = "low";
				String description = name + " [inf," + (float) upperValue + ")";
				result.add(Propositions.proposition(table, defaultMetricAttribute,
						Constraints.namedConstraint(lessThan, "=" + name, description)));
			}
			return result;
		}
	},

	NORMAL_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute metricAttribute = (MetricAttribute) attribute;
				double lowerBorder = metricAttribute.mean() - 0.5 * metricAttribute.standardDeviation();
				double upperBorder = metricAttribute.mean() + 0.5 * metricAttribute.standardDeviation();

				Constraint<Double> closedIntervalConstraint = inClosedInterval(lowerBorder, upperBorder);
				result.add(Propositions.proposition(table, metricAttribute, Constraints.namedConstraint(
						closedIntervalConstraint, "=normal", "normal [" + lowerBorder + "," + upperBorder + "]")));
			}
			return result;
		}
	},

	HIGH_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute metricAttribute = (MetricAttribute) attribute;
				double lowerValue = metricAttribute.mean() + 0.5 * metricAttribute.standardDeviation();
				Constraint<Double> greaterThan = Constraints.greaterThan(lowerValue);
				String name = "high";
				String description = name + " (" + (float) lowerValue + ",inf]";
				result.add(Propositions.proposition(table, metricAttribute,
						Constraints.namedConstraint(greaterThan, "=" + name, description)));
			}
			return result;
		}
	},

	VERY_HIGH_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute metricAttribute = (MetricAttribute) attribute;
				double lowerValue = metricAttribute.mean() + 1.5 * metricAttribute.standardDeviation();
				Constraint<Double> greaterThan = Constraints.greaterThan(lowerValue);
				String name = "very high";
				String description = name + " (" + (float) lowerValue + ",inf]";
				result.add(Propositions.proposition(table, metricAttribute,
						Constraints.namedConstraint(greaterThan, "=" + name, description)));
			}
			return result;
		}
	},

	NOT_VERY_HIGH_BY_STDEV {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute metricAttribute = (MetricAttribute) attribute;
				double upperValue = metricAttribute.mean() + 1.5 * metricAttribute.standardDeviation();
				Constraint<Double> lessThan = Constraints.lessOrEquals(upperValue);
				String name = "not very high";
				String description = name + " [-inf," + (float) upperValue + "]";
				result.add(Propositions.proposition(table, metricAttribute,
						Constraints.namedConstraint(lessThan, "=not very high", description)));
			}
			return result;
		}
	},

	/**
	 * For metric attributes, the empirical range of which include 0.0, this adds
	 * propositions for values being positive, negative, and zero, respectively
	 * (where positive and negative are only added if there are indeed empirical
	 * positive or negative values).
	 */
	POSITIVE_AND_NEGATIVE {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof MetricAttribute) {
				MetricAttribute metricAttribute = (MetricAttribute) attribute;
				Double min = metricAttribute.min();
				Double max = metricAttribute.max();
				if (min <= 0 && max >= 0) {
					if (min < 0) {
						result.add(Propositions.proposition(table, metricAttribute,
								namedConstraint(Constraints.lessThan(0), "=negative", "negative [-inf,0)")));
					}
					if (max > 0) {
						result.add(Propositions.proposition(table, metricAttribute,
								namedConstraint(Constraints.greaterThan(0), "=positive", "positive (0,inf]")));
					}
					result.add(Propositions.proposition(table, metricAttribute,
							namedConstraint(Constraints.equalTo(0.0), "=0.0", "zero [0,0]")));
				}
			}
			return result;
		}
	},

	EVEN_ODD {

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (Integer.class.isAssignableFrom(attribute.type())) {
				/*
				 * should be safe after check for type
				 */
				result.add(Propositions.proposition(table, (Attribute<Integer>) attribute, divisibleBy(2)));
				result.add(Propositions.proposition(table, (Attribute<Integer>) attribute, notDivisibleBy(2)));
			}
			return result;
		}

	};

	// ORDINAL_ALL_CUTOFFS {
	//
	// private List<Double> allCutOffValues(MetricAttribute attribute) {
	// List<Double> result = new ArrayList<>();
	// for (int i = 1; i < attribute.nonMissingValuesInOrder().size(); i++) {
	// result.add((attribute.nonMissingValuesInOrder().get(i - 1) +
	// attribute.nonMissingValuesInOrder().get(i))
	// / 2.0);
	// }
	// return result;
	// }
	//
	// @Override
	// public <T> void constructPropositions(Attribute<T> attribute,
	// List<AttributeBasedProposition<?>> result) {
	// if (!(attribute instanceof MetricAttribute)) {
	// return;
	// }
	//
	// MetricAttribute ordinal = (MetricAttribute) attribute;
	//
	// List<Double> cutOffValues =
	// ordinal.distinctNonMissingValuesInOrder().size() <= 20
	// ? allCutOffValues(ordinal) :
	// Lists.kMeansCutPoints(ordinal.nonMissingValuesInOrder(), 21, 25);
	//
	// cutOffValues.forEach(c -> {
	// result.add(new DefaultAttributeBasedProposition<Double>(ordinal,
	// namedConstraint(greaterOrEquals(c), ">=" + c, attribute.name() + " >= " +
	// c), result.size()));
	// result.add(new DefaultAttributeBasedProposition<>(ordinal,
	// namedConstraint(lessOrEquals(c), "=<" + c, attribute.name() + " =< " +
	// c), result.size()));
	// });
	// }
	//
	// };

	@Override
	public abstract <T> List<AttributeBasedProposition<?>> apply(DataTable table,
			Attribute<T> attribute);

}