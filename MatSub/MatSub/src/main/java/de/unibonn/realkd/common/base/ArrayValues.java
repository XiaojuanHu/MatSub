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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

import de.unibonn.realkd.common.KdonDoc;
import de.unibonn.realkd.common.KdonTypeName;
import de.unibonn.realkd.common.KdonTypes;

/**
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class ArrayValues {

	@KdonTypeName(KdonTypes.JAVA_ARRAY_TYPE_NAME)
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	@JsonDeserialize(using = GenericArrayDeserializer.class)
	@JsonSerialize(using = GenericArraySerializer.class)
	public static interface Array {

		List<? extends Object> elements();

	}

	@KdonTypeName(KdonTypes.SPECIFIC_JAVA_ARRAY_TYPE_NAME)
	@KdonDoc("Homogenious array where all elements are of some specific type T.")
	public static interface GenericArray<@KdonDoc("A type that all elements of the array have in common.") T>
			extends Array {

		List<T> elements();

	}

	@SafeVarargs
	public static <T> GenericArray<T> array(T... elements) {
		return new DefaultGenericArray<T>(elements);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> GenericArray<T> arrayFromList(List<T> elements) {
		return new DefaultGenericArray<T>((T[]) elements.toArray());
	}
	
	private static class DefaultGenericArray<T> implements GenericArray<T> {

		private final T[] elements;

		@SafeVarargs
		public DefaultGenericArray(T... elements) {
			this.elements = elements;
		}

		@Override
		public List<T> elements() {
			return Arrays.asList(elements);
		}

		@Override
		public String toString() {
			return stream(elements).map(e -> e.toString()).collect(joining(",", "[", "]"));
		}
		
		@Override
		public boolean equals(Object other) {
			if (this==other) {
				return true;
			}
			if (!(other instanceof DefaultGenericArray)) {
				return false;
			}
			return Arrays.equals(elements, ((DefaultGenericArray<?>) other).elements);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(elements);
		}

	}

	private static class GenericArraySerializer extends JsonSerializer<DefaultGenericArray<?>> {

		@Override
		public void serialize(DefaultGenericArray<?> value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeStartArray();

			for (Object e : value.elements) {
				serializers.defaultSerializeValue(e, gen);
			}

			gen.writeEndArray();
		}

	}

	private static class GenericArrayDeserializer extends JsonDeserializer<DefaultGenericArray<?>>
			implements ContextualDeserializer {

		private final JavaType elementType;

		@SuppressWarnings("unused") // available for Jackson
		public GenericArrayDeserializer() {
			elementType = TypeFactory.unknownType();
		}

		public GenericArrayDeserializer(JavaType elementType) {
			this.elementType = elementType;
		}

		@Override
		public DefaultGenericArray<?> deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			JsonDeserializer<?> contentDeserializer;
			if (GenericArray.class.isAssignableFrom(elementType.getRawClass())) {
				contentDeserializer = new GenericArrayDeserializer(elementType.containedTypeOrUnknown(0));
			} else {
				contentDeserializer = ctxt.findRootValueDeserializer(elementType);
			}

			if (!p.isExpectedStartArrayToken()) {
				throw new JsonParseException(p, "expected start array symbol");
			}
			p.nextToken();

			ObjectBuffer buffer = ctxt.leaseObjectBuffer();
			Object[] chunk = buffer.resetAndStart();
			int i = 0;

			while (!p.currentToken().equals(JsonToken.END_ARRAY)) {
				Object el = contentDeserializer.deserialize(p, ctxt);

				p.nextToken();
				if (i >= chunk.length) {
					chunk = buffer.appendCompletedChunk(chunk);
					i = 0;
				}
				chunk[i++] = el;

			}

			Object[] result = buffer.completeAndClearBuffer(chunk, i, elementType.getRawClass());
			ctxt.returnObjectBuffer(buffer);

			return new DefaultGenericArray<>(result);
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {
			if (property == null) {
				return this;
			}
			JavaType type = property.getType();
			JavaType typeOrUnknown = type.containedTypeOrUnknown(0);
			return new GenericArrayDeserializer(typeOrUnknown);
		}

	}

}
