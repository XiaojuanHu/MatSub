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

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Sandy Moens
 *
 * @since 0.6.0
 *
 * @version 0.6.0
 */
public final class DateFieldEqualsConstraint implements Constraint<Date> {

	private static String[] strDays = new String[] { "sunday", "monday", "tuesday", "wednesday", "thursday",
	        "friday", "saturday" };
	
	
	private static String[] strMonth = new String[] { "January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November", "December" };

	@JsonProperty("value")
	private int value;
	
	@JsonProperty("calendarField")
	private int calendarField;

	@JsonCreator
	DateFieldEqualsConstraint(@JsonProperty("value") int value, @JsonProperty("calendarField") int calendarField) {
		this.value = value;
		this.calendarField = calendarField;
	}

	@Override
	public boolean holds(Date queryValue) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(queryValue);
		return instance.get(calendarField) == this.value;
	}

	@Override
	public String suffixNotationName() {
		if(Calendar.MONTH ==  calendarField) {
			return strMonth[value];
		} else if(Calendar.DAY_OF_WEEK == calendarField) {
			return strDays[value - 1];
		} else if(Calendar.DAY_OF_MONTH == calendarField) {
			return "day_of_month=" + value;
		} else if(Calendar.DAY_OF_YEAR == calendarField) {
			return "day_of_year=" + value;
		}
		return "=" + value;
	}

	@Override
	public String description() {
		return String.valueOf(value);
	}

	public int comparisonValue() {
		return value;
	}

	@Override
	public boolean implies(Constraint<Date> anotherConstraint) {
		return false;
//		return anotherConstraint.holds(value);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof DateFieldEqualsConstraint)) {
			return false;
		}
		DateFieldEqualsConstraint constraint = (DateFieldEqualsConstraint) other;
		return value == constraint.value && calendarField == constraint.calendarField;
	}
	
	private static final int EQUALS_HASH_OFFSET = 13;

	@Override
	public int hashCode() {
		return Objects.hashCode(value) + EQUALS_HASH_OFFSET;
	}
	
	@Override
	public String toString() {
		return suffixNotationName();
	}

}
