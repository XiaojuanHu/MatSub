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

import static com.google.common.collect.Sets.newTreeSet;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import de.unibonn.realkd.data.constraints.Constraints;
import de.unibonn.realkd.data.constraints.DateConstraints;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.DefaultDateAttribute;

/**
 * @author Sandy Moens
 * 
 * @since
 * 
 * @version 0.6.0
 *
 */
public enum DateAttributeToPropositionsMapper implements PropositionalizationRule {
	
	MONTH {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				Collection<Integer> values = uniqueValues((DefaultDateAttribute) attribute, Calendar.MONTH);
				
				for (int value: values) {
					result.add(Propositions.proposition(table, (DefaultDateAttribute) attribute, DateConstraints.isMonth(value)));
				}
			}
			return result;
		}
	},
	
	DAY_OF_WEEK {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				Collection<Integer> values = uniqueValues((DefaultDateAttribute) attribute, Calendar.DAY_OF_WEEK);
				
				for (int value: values) {
					result.add(Propositions.proposition(table, (DefaultDateAttribute) attribute, DateConstraints.isDayOfWeek(value)));
				}
			}
			return result;
		}
	},
	
	DAY_OF_MONTH {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				Collection<Integer> values = uniqueValues((DefaultDateAttribute) attribute, Calendar.DAY_OF_MONTH);
				
				for (int value: values) {
					result.add(Propositions.proposition(table, (DefaultDateAttribute) attribute, DateConstraints.isDayOfMonth(value)));
				}
			}
			return result;
		}
	},
	
	DAY_OF_YEAR {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				Collection<Integer> values = uniqueValues((DefaultDateAttribute) attribute, Calendar.DAY_OF_YEAR);
				
				for (int value: values) {
					result.add(Propositions.proposition(table, (DefaultDateAttribute) attribute, DateConstraints.isDayOfYear(value)));
				}
			}
			return result;
		}
	},
	
	IS_WEEKDAY {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				DefaultDateAttribute dateAttribute = (DefaultDateAttribute) attribute;

				result.add(Propositions.proposition(table, dateAttribute, DateConstraints.isWeekday(true)));
				result.add(Propositions.proposition(table, dateAttribute, DateConstraints.isWeekday(false)));
			}
			return result;
		}
	},
	
	YEAR_MONTH_DATE_HOUR {
		@Override
		public <T> List<AttributeBasedProposition<?>> apply(DataTable table, Attribute<T> attribute) {
			List<AttributeBasedProposition<?>> result = new ArrayList<>();
			if (attribute instanceof DefaultDateAttribute) {
				for (Date date : newTreeSet(((DefaultDateAttribute) attribute).nonMissingValues())) {
					result.add(Propositions.proposition(table, (Attribute<?>) attribute, Constraints.equalTo(date)));
				}
			}
			return result;
		}
	};

	@Override
	public abstract <T> List<AttributeBasedProposition<?>> apply(DataTable table,
			Attribute<T> attribute);
	
	private static Collection<Integer> uniqueValues(DefaultDateAttribute attribute, int calendarField) {
		TreeSet<Date> dates = newTreeSet(((DefaultDateAttribute) attribute).nonMissingValues());
		
		return dates.stream().map(d -> {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d);
			if(Calendar.DAY_OF_WEEK == calendarField) {
				return calendar.get(calendarField);
			}
			return calendar.get(calendarField);
		}).collect(toSet());
	}
}
