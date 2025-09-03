/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.sequences;

import java.util.Date;

import de.unibonn.realkd.data.propositions.Proposition;

/**
 * Concrete implementations of sequence events based on dates and ordinal values. 
 * 
 * @author Sandy Moens
 * 
 * @since 0.5.2
 * 
 * @version 0.7.0
 *
 */
public class SequenceEvents {

	/**
	 * Constructs a new sequence event with the given value as time/distance indicator of the event
	 * and the proposition as value that takes place at that specific time/distance. The time/distance
	 * indicator should be comparable because this will be used to construct a sequence from.
	 * 
	 * @param value
	 * 			the time/distance indicator of the event
	 * @param proposition
	 * 			the values that takes place
	 * 
	 * @return a new sequence event  
	 */
	public static <T extends Comparable<?>> SequenceEvent<?> newSequenceEvent(T value, Proposition proposition) {
		if(value instanceof Date) {
			return new DateSequenceEvent((Date)value, proposition);
		} else if(value instanceof Double) {
			return new OrdinalSequenceEvent((Double)value, proposition);
		}
		return null;
	}

	private static class DateSequenceEvent implements SequenceEvent<Date> {
		
		private Date value;
		private Proposition proposition;
		
		public DateSequenceEvent(Date value, Proposition propositions) {
			this.value = value;
			this.proposition = propositions;
		}
		
		@Override
		public int compareTo(SequenceEvent<?> o) {
			if(o.value() instanceof Date) {
				return this.value.compareTo((Date)o.value());
			}
			return 0;
		}
	
		@Override
		public Date value() {
			return this.value;
		}
		
		public double doubleValue() {
			return this.value.getTime();
		}
	
		@Override
		public Proposition proposition() {
			return this.proposition;
		}
		
		@Override
		public String toString() {
			return this.value + ": " + this.proposition.name();
		}
		
	}

	private static class OrdinalSequenceEvent implements SequenceEvent<Double> {
		
		private Double value;
		private Proposition proposition;
		
		public OrdinalSequenceEvent(Double value, Proposition proposition) {
			this.value = value;
			this.proposition = proposition;
		}
		
		@Override
		public int compareTo(SequenceEvent<?> o) {
			if(o.value() instanceof Double) {
				return this.value.compareTo((Double)o.value());
			}
			return 0;
		}
		
		@Override
		public Double value() {
			return this.value;
		}
		
		public double doubleValue() {
			return this.value;
		}
		
		@Override
		public Proposition proposition() {
			return this.proposition;
		}
		
		@Override
		public String toString() {
			return this.value + ": " + this.proposition.name();
		}
		
	}
	
	// Suppress default constructor for non-instantiability
	private SequenceEvents() {
		throw new AssertionError();
	}

}
