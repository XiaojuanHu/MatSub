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
public final class DateIsWeekdayConstraint implements Constraint<Date> {

	@JsonProperty("isWeekday")
	private boolean isWeekday;

	@JsonCreator
	DateIsWeekdayConstraint(@JsonProperty("isWeekday") boolean isWeekday) {
		this.isWeekday = isWeekday;
	}

	@Override
	public boolean holds(Date queryValue) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(queryValue);
		
		int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK);
		
		boolean isWeekday = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
		
		return this.isWeekday ? isWeekday : !isWeekday;
	}

	@Override
	public String suffixNotationName() {
		return this.isWeekday ? "isWeekday" : "isWeekendday";
	}

	@Override
	public String description() {
		return this.isWeekday ? "isWeekday" : "isWeekendday";
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
		if (!(other instanceof DateIsWeekdayConstraint)) {
			return false;
		}
		DateIsWeekdayConstraint constraint = (DateIsWeekdayConstraint) other;
		return this.isWeekday == constraint.isWeekday;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(suffixNotationName());
	}
	
	@Override
	public String toString() {
		return suffixNotationName();
	}

}
