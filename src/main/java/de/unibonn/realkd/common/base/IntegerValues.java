/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
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
package de.unibonn.realkd.common.base;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.base.DoubleValues.DoubleValue;

/**
 * Types of literal integer values.
 * 
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class IntegerValues {

	@KdonTypeName("int")
	@KdonDoc("Arbitrary integer literals ...-2,-1,0,1,2,... ; corresponds to the <strong>int</strong> symbol in the JSON grammar specification.")
	public interface IntegerValue extends DoubleValue {

		int asInt();

	}

	@KdonTypeName("oddInt")
	@KdonDoc("Odd integer literal -3,-1,1,3,5... and so on.")
	public interface OddIntegerValue extends IntegerValue {
	}

	@KdonTypeName("evenInt")
	@KdonDoc("Even integer literal -2,0,2,4,6... and so on.")
	public interface EvenIntegerValue extends IntegerValue {
	}

	@KdonTypeName("posInt")
	@KdonDoc("Positive integer literal 1,2,... and so on.")
	public interface PositiveIntegerValue extends IntegerValue {
	}

	@KdonTypeName("evenPosInt")
	@KdonDoc("Even positive integer literal 2,4,6... and so on.")
	public interface EvenPositiveIntegerValue extends EvenIntegerValue, PositiveIntegerValue {
	}

	@KdonTypeName("oddPosInt")
	@KdonDoc("Odd positive integer literal 1,3,5... and so on.")
	public interface OddPositiveIntegerValue extends OddIntegerValue, PositiveIntegerValue {
	}

	public static IntegerValue intValue(int val) {
		if (val > 0) {
			if ((val & 1) == 0) {
				return new EvenPositiveIntegerCapture(val);
			} else {
				return new OddPositiveIntegerCapture(val);
			}
		}
		if ((val & 1) == 0) {
			return new EvenIntegerCapture(val);
		} else {
			return new OddIntegerCapture(val);
		}
	}

	public static PositiveIntegerValue posIntValue(int val) {
		if (val <= 0) {
			throw new IllegalArgumentException(val + " is not a positive value");
		}
		if ((val & 1) == 0) {
			return new EvenPositiveIntegerCapture(val);
		} else {
			return new OddPositiveIntegerCapture(val);
		}
	}

	public static EvenPositiveIntegerValue evenPosIntValue(int val) {
		if (val <= 0 || (val & 1) != 0) {
			throw new IllegalArgumentException(val + " is not a positive value");
		}
		return new EvenPositiveIntegerCapture(val);
	}
	
	private final static class DefaultIntegerValueSerializer extends JsonSerializer<IntegerValue> {

		@Override
		public void serialize(IntegerValue value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeNumber(value.asInt());
		}

	}

	@JsonSerialize(using = DefaultIntegerValueSerializer.class)
	private static class DefaultIntegerValue implements IntegerValue {

		private final int value;

		protected DefaultIntegerValue(int val) {
			this.value = val;
		}

		@Override
		public double asDouble() {
			return value;
		}

		@Override
		public String asString() {
			return String.valueOf(value);
		}

		@Override
		public int asInt() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof IntegerValue)) {
				return false;
			}
			return (this.value == ((IntegerValue) other).asInt());
		}

		@Override
		public int hashCode() {
			return value;
		}

	}

	final static class EvenPositiveIntegerCapture extends DefaultIntegerValue
			implements EvenPositiveIntegerValue {
		private EvenPositiveIntegerCapture(int val) {
			super(val);
		}
	}

	final static class OddPositiveIntegerCapture extends DefaultIntegerValue
			implements OddPositiveIntegerValue {
		private OddPositiveIntegerCapture(int val) {
			super(val);
		}
	}

	final static class OddIntegerCapture extends DefaultIntegerValue implements OddIntegerValue {
		private OddIntegerCapture(int val) {
			super(val);
		}
	}

	final static class EvenIntegerCapture extends DefaultIntegerValue implements EvenIntegerValue {
		private EvenIntegerCapture(int val) {
			super(val);
		}
	}

}
