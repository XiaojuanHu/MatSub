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

package de.unibonn.realkd.data.constraints;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.data.table.attribute.OrdinalAttribute;

/**
 * Provides static factory methods for the construction of constraints.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 *
 */
public final class Constraints {

	private Constraints() {
		; // not to be instantiated
	}

	/**
	 * 
	 * @param value
	 *            the value for which equality is checked by the constraint
	 * 
	 * @return constraint that holds for all values equal to given value
	 */
	public static <T> Constraint<T> equalTo(T value) {
		return new EqualsConstraint<T>(value);
	}

	public static <T> Constraint<T> notEqualTo(T value) {
		return new NotEqualsConstraint<T>(value);
	}
	
	public static <T> Constraint<T> greaterThan(T value, Comparator<T> comparator) {
		return greaterThan(value, comparator, "");
	}

	public static <T> Constraint<T> greaterThan(T value, Comparator<T> comparator, String thresholdLabel) {
		return new GreaterThanConstraint<T>(value, comparator, thresholdLabel);
	}

	public static <T> Constraint<T> lessThan(T value, Comparator<T> comparator) {
		return lessThan(value, comparator, "");
	}

	public static <T> Constraint<T> lessThan(T value, Comparator<T> comparator, String thresholdLabel) {
		return new LessThanConstraint<T>(value, comparator, thresholdLabel);
	}

	public static Constraint<Double> greaterThan(double value) {
		return Constraints.greaterThan(value, Comparator.naturalOrder());
	}

	public static Constraint<Double> greaterThan(double value, String thresholdLabel) {
		return Constraints.greaterThan(value, Comparator.naturalOrder(), thresholdLabel);
	}

	public static Constraint<Double> greaterOrEquals(double threshold) {
		return greaterOrEquals(threshold, Comparator.naturalOrder());
	}
	
	public static Constraint<Double> greaterOrEquals(double value, String name) {
		return greaterOrEquals(value, Comparator.naturalOrder(), name);
	}

	public static <T> Constraint<T> greaterOrEquals(T threshold, Comparator<T> order) {
		return greaterOrEquals(threshold, order, "");
	}
	
	public static <T> Constraint<T> greaterOrEquals(T threshold, Comparator<T> order, String label) {
		return new GreaterOrEqualsConstraints<T>(threshold, order, label);
	}

	public static Constraint<Double> lessThan(double value) {
		return Constraints.lessThan(value, Comparator.naturalOrder());
	}

	public static Constraint<Double> lessThan(double value, String thresholdLabel) {
		return Constraints.lessThan(value, Comparator.naturalOrder(), thresholdLabel);
	}

	public static Constraint<Double> lessOrEquals(double threshold) {
		return lessOrEquals(threshold, Comparator.naturalOrder());
	}
	
	public static <T> Constraint<T> lessOrEquals(T threshold, Comparator<T> order) {
		return new LessOrEqualsConstraint<T>(threshold, order, "");
	}
		
	public static Constraint<Double> lessOrEquals(double value, String thresholdLabel) {
		return Constraints.lessOrEquals(value, Comparator.naturalOrder(), thresholdLabel);
	}
	
	public static <T> Constraint<T> lessOrEquals(T value, Comparator<T> order, String label) {
		return new LessOrEqualsConstraint<T>(value, order, label);
	}

	public static Constraint<Double> inClosedInterval(double lowerThreshold, double upperThreshold) {
		Constraint<Double> lowerBorder = greaterOrEquals(lowerThreshold);
		Constraint<Double> upperBorder = lessOrEquals(upperThreshold);
		return and(lowerBorder, upperBorder);
	}

	public static <T> Constraint<T> and(Constraint<T> firstEntailedConstraint, Constraint<T> secondEntailedConstraint) {
		return new AndConstraint<T>(firstEntailedConstraint, secondEntailedConstraint);
	}

	public static <T> Constraint<T> or(Constraint<T> firstEntailedConstraint, Constraint<T> secondEntailedConstraint) {
		return new OrConstraint<T>(firstEntailedConstraint, secondEntailedConstraint);
	}

	/**
	 * Returns constraint equivalent to another constraint, adding specific
	 * suffix-notation-name and description.
	 * 
	 * @author Mario Boley
	 * 
	 * @since 0.1.0
	 * 
	 * @version 0.3.0
	 * 
	 */
	public static <T> Constraint<T> namedConstraint(Constraint<T> entailedConstraint, String suffixName,
			String description) {
		return new NamedConstraint<T>(entailedConstraint, suffixName, description);
	}

	@KdonTypeName("namedConstraint")
	public static class NamedConstraint<T> implements DerivedConstraint<T> {

		@JsonProperty("entailedConstraint")
		private final Constraint<T> entailedConstraint;
		@JsonProperty("name")
		private final String name;
		@JsonProperty("description")
		private final String description;

		@JsonCreator
		private NamedConstraint(@JsonProperty("entailedConstraint") Constraint<T> entailedConstraint,
				@JsonProperty("name") String name, @JsonProperty("description") String description) {
			this.entailedConstraint = entailedConstraint;
			this.name = name;
			this.description = description;
		}

		@Override
		public boolean holds(T value) {
			return entailedConstraint.holds(value);
		}

		@Override
		public String description() {
			return description;
		}

		@Override
		public String suffixNotationName() {
			return name;
		}

		@Override
		public boolean implies(Constraint<T> anotherConstraint) {
			return entailedConstraint.implies(anotherConstraint);
		}

		@Override
		public boolean impliedBy(Constraint<T> anotherConstraint) {
			return anotherConstraint.implies(entailedConstraint);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof NamedConstraint)) {
				return false;
			}
			NamedConstraint<?> that = (NamedConstraint<?>) other;
			return (this.entailedConstraint.equals(that.entailedConstraint) && this.name.equals(that.name)
					&& this.description.equals(that.description));
		}

		public int hashCode() {
			return Objects.hash(entailedConstraint, name, description);
		}

	}

	// public static class LessOrEqualsSerialForm<T> implements
	// SerialForm<Constraint<T>> {
	//
	// private LessOrEqualsSerialForm() {
	// }
	//
	// @Override
	// public Constraint<T> build(Workspace workspace) {
	// return null;
	// }
	//
	// }

	public static Constraint<Integer> divisibleBy(int divisor) {
		return new DivisibleConstraint(divisor);
	}

	public static Constraint<Integer> notDivisibleBy(int divisor) {
		return new NotDivisibleConstraint(divisor);
	}

	/**
	 * @param ordinal
	 * @param threshold
	 * @return
	 */
	public static <T> Constraint<T> lowerQuantileBoundNegation(OrdinalAttribute<T> ordinal, T threshold) {
		final String name = topCutName(
				(double) ordinal.inverseOrderNumber(threshold) / ordinal.numberOfNonMissingValues());
		return namedConstraint(lessThan(threshold, ordinal.valueComparator()), " not in " + name,
				"not in " + name + " [-inf, " + threshold + ")");
	}

	/**
	 * 
	 * 
	 * @param ordinal
	 * @param threshold
	 * @return
	 */
	public static <T> Constraint<T> lowerQuantileBound(OrdinalAttribute<T> ordinal, T threshold) {
		final String name = topCutName(
				(double) ordinal.inverseOrderNumber(threshold) / ordinal.numberOfNonMissingValues());
//		return greaterOrEquals(threshold, ordinal.valueComparator(), name);
		return namedConstraint(greaterOrEquals(threshold, ordinal.valueComparator()),
				" in " + name, "in " + name + " [" + threshold + ", inf]");
	}

	/**
	 * @param ordinal
	 * @param threshold
	 * @return
	 */
	public static <T> Constraint<T> upperQuantileBound(OrdinalAttribute<T> ordinal, T threshold) {
		final String name = bottomCutName((double) ordinal.orderNumber(threshold) / ordinal.numberOfNonMissingValues());
		return namedConstraint(lessOrEquals(threshold, ordinal.valueComparator()), " in " + name,
				"in " + name + " [-inf, " + threshold + "]");
	}

	/**
	 * @param ordinal
	 * @param threshold
	 * @return
	 */
	public static <T> Constraint<T> upperQuantileBoundNegation(OrdinalAttribute<T> ordinal, T threshold) {
		final String name = bottomCutName((double) ordinal.orderNumber(threshold) / ordinal.numberOfNonMissingValues());
		return namedConstraint(greaterThan(threshold, ordinal.valueComparator()), " not in " + name,
				"not in " + name + " (" + threshold + ", inf]");
	}

	private final static NumberFormat QUANTILE_BASED_CONSTRAINT_NUMBER_FORMAT = NumberFormat.getPercentInstance();
	{
		QUANTILE_BASED_CONSTRAINT_NUMBER_FORMAT.setMaximumFractionDigits(1);
	}

	private static String topCutName(double cutPoint) {
		return "top " + QUANTILE_BASED_CONSTRAINT_NUMBER_FORMAT.format(cutPoint);
	}

	private static String bottomCutName(double cutPoint) {
		return "bottom " + QUANTILE_BASED_CONSTRAINT_NUMBER_FORMAT.format(cutPoint);
	}


}
