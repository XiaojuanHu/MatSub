/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-15 The Contributors of the realKD Project
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
package de.unibonn.realkd.common;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import de.unibonn.realkd.common.base.Identifier;

/**
 * <p>
 * Provides functionality for the serialization/deserialization of objects as
 * json string. Currently this encapsulates Jackson to achieve this. Previous
 * GSON implementation was removed because GSON behaved unpredictable with
 * abstract generic types.
 * <p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.7.0
 *
 */
public class JsonSerialization {

	static final Logger LOGGER = Logger.getLogger(JsonSerialization.class.getName());

	private static class IdentifierSerializer extends JsonSerializer<Identifier> {

		@Override
		public void serialize(Identifier identifier, JsonGenerator generator, SerializerProvider serializerProvider)
				throws IOException {
			generator.writeString(identifier.toString());
		}

	}

	private static class IdentifierDeserializer extends JsonDeserializer<Identifier> {

		@Override
		public Identifier deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			return Identifier.identifier(p.readValueAs(String.class));
		}

	}

	private static final SimpleModule REALKD_MODULE = new SimpleModule();
	static {
		// use toString serializer instead?!
		REALKD_MODULE.addSerializer(Identifier.class, new IdentifierSerializer());
		REALKD_MODULE.addDeserializer(Identifier.class, new IdentifierDeserializer());
	}

	private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper().configure(Feature.AUTO_CLOSE_TARGET,
			false);
	static {
		DEFAULT_OBJECT_MAPPER.enableDefaultTypingAsProperty(DefaultTyping.OBJECT_AND_NON_CONCRETE, "type");
		DEFAULT_OBJECT_MAPPER.registerModule(new GuavaModule());
		DEFAULT_OBJECT_MAPPER.registerModule(REALKD_MODULE);
		DEFAULT_OBJECT_MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
	}

	public static Optional<String> optionalPrettySerialString(JsonSerializable obj) {
		try {
			return Optional.of(serialString(obj));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public static String serialString(Object obj) throws IOException {
		return serialStringWithFeatures(obj, SerializationFeature.INDENT_OUTPUT);
	}

	public static String compactSerialString(Object obj) throws IOException {
		return serialStringWithFeatures(obj);
	}

	public static String serialStringWithFeatures(Object obj, SerializationFeature... features) throws IOException {
		StringWriter outWriter = new StringWriter();
		serializeWithFeatures(outWriter, obj, features);
		return outWriter.toString();
	}

	public static void serialize(Writer outputWriter, Object obj) throws IOException {
		serializeWithFeatures(outputWriter, obj, SerializationFeature.INDENT_OUTPUT);
	}

	public static void serializeCompact(Writer outputWriter, Object obj) throws IOException {
		serializeWithFeatures(outputWriter, obj);
	}

	// public static void serializeAll(Writer outputWriter,
	// Collection<JsonSerializable> coll) throws IOException {
	// for (JsonSerializable e : coll) {
	// serialize(outputWriter, e);
	// }
	// }

	public static void export(Writer outputWriter, Object obj) throws IOException {
		if (obj instanceof JsonSerializable) {
			serialize(outputWriter, obj);
		} else if (obj instanceof HasExportableForm) {
			serialize(outputWriter, ((HasExportableForm) obj).exportableForm());
		} else if (obj instanceof Collection<?>) {
			outputWriter.write('[');
			Iterator<?> elements = ((Collection<?>) obj).iterator();
			while (elements.hasNext()) {
				export(outputWriter, elements.next());
				if (elements.hasNext())
					outputWriter.write(",");
			}
			outputWriter.write(']');
		} else {
			LOGGER.warning(obj + " may not have exportable form");
			serialize(outputWriter, obj);
		}
	}

	/**
	 * Generic entry point for serializing an arbitrary object to some arbitrary
	 * writer; exposing optional Jackson features.
	 * 
	 * @param outputWriter the writer to write serial representation to
	 * @param obj the object to be serialized
	 * @param features Jackson serialization features to be used for serialization
	 * 
	 * @throws IOException
	 *             object cannot be serialized by encapsulated object mapper
	 * 
	 */
	public static void serializeWithFeatures(Writer outputWriter, Object obj, SerializationFeature... features)
			throws IOException {
		ObjectWriter writer = DEFAULT_OBJECT_MAPPER.writer();
		for (SerializationFeature feature : features) {
			writer = writer.with(feature);
		}
		if (!(obj instanceof JsonSerializable)) {
			LOGGER.warning(obj + " is not instance of JsonSerializable");
		}
		writer.writeValue(outputWriter, obj);
	}
	
	public static <T> T deserialization(String input, Class<T> type) throws IOException {
		return DEFAULT_OBJECT_MAPPER.readValue(input, type);
	}

	public static <T> T deserialization(Reader input, Class<T> type) throws IOException {
		return DEFAULT_OBJECT_MAPPER.readValue(input, type);
	}
	
	public static <T> T deserialization(JsonParser parser, JavaType type) throws IOException {
		return DEFAULT_OBJECT_MAPPER.readValue(parser, type);
	}


	@Deprecated
	public static <T> String toJson(T object) {
		try {
			return DEFAULT_OBJECT_MAPPER.writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public static <T> String toPrettyJson(T object) {
		try {
			return DEFAULT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public static <T> T fromJson(String json, Class<T> type) {
		try {
			return DEFAULT_OBJECT_MAPPER.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
