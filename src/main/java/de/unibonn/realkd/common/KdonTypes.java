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
package de.unibonn.realkd.common;

import static com.google.common.collect.ObjectArrays.concat;
import static de.unibonn.realkd.common.KdonField.fieldParameter;
import static de.unibonn.realkd.util.Reflection.annotation;
import static de.unibonn.realkd.util.Reflection.memberType;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;

import de.unibonn.realkd.common.KdonField.KdonFieldParameter;
import de.unibonn.realkd.util.Reflection;

/**
 * <p>
 * Loads and holds all available realKD types of object notation (KDON) values.
 * </p>
 * 
 * <p>
 * Types are loaded via the Java {@link ServiceLoader} mechanism: all registered
 * services of type {@link KdonJavaTypeProvider} are queried for Java classes
 * that correspond to KDON types.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class KdonTypes {

	public static final String SPECIFIC_JAVA_ARRAY_TYPE_NAME = "harray";

	public static final String JAVA_ARRAY_TYPE_NAME = "array";

	private static final Logger LOGGER = Logger.getLogger(KdonTypes.class.getName());

	private static final Set<JsonInclude.Include> OPTIONAL_JSON_INCLUDE_VALUES = ImmutableSet
			.of(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_ABSENT);

	private final static Map<String, KdonType> JAVATYPENAME_TO_KDONTYPE = new HashMap<>();
	private final static Map<String, KdonType> KDONTYPENAME_TO_KDONTYPE = new HashMap<>();
	static {
		LOGGER.info("Loading KdonTypes");

		// KDONTYPENAME_TO_KDONTYPE.put("array", new KdonType("array",
		// Object[].class.getName(), new String[] { "array" },
		// new String[] { "harray" }, "Standard JSON <strong>array</strong> type.", new
		// KdonField[] {}));
		// KDONTYPENAME_TO_KDONTYPE.put("harray",
		// new KdonType("harray", Object[].class.getName(), new String[] { "array" },
		// new String[] {},
		// "Homogenious array where all elements are of some specific type T.", new
		// KdonField[] {},
		// new KdonTypeParameter("T", "Type that all elements of the array have in
		// common.")));

		ServiceLoader<KdonJavaTypeProvider> loader = ServiceLoader.load(KdonJavaTypeProvider.class);

		Set<Class<?>> javaTypes = stream(spliteratorUnknownSize(loader.iterator(), Spliterator.ORDERED), false)
				.flatMap(p -> p.get().stream()).collect(toSet());

		Multimap<Class<?>, Class<?>> structure = Reflection.subtypeStructure(javaTypes);
		javaTypes.stream().map(c -> kdonType(c, structure.get(c), javaTypes)).forEach(t -> {
			KDONTYPENAME_TO_KDONTYPE.put(t.name, t);
			JAVATYPENAME_TO_KDONTYPE.put(t.javaTypeName, t);
		});
		LOGGER.info("Done loading KdonTypes");
	}

	private static Class<?> typeOrComponentTypeIfArray(Class<?> type) {
		return type.isArray() ? type.getComponentType() : type;
	}

	private static String kdonTypeName(Class<?> javaClass) {
		if (javaClass.isArray()) {
			return javaClass.getComponentType().equals(Object.class) ? JAVA_ARRAY_TYPE_NAME
					: SPECIFIC_JAVA_ARRAY_TYPE_NAME;
		}
		KdonTypeName nameAnnotation = javaClass.getAnnotation(KdonTypeName.class);
		if (nameAnnotation == null) {
			return javaClass.getName();
		}
		return nameAnnotation.value();
	}

	private static boolean isOptional(Member member) {
		Optional<JsonInclude> include = Reflection.annotation(member, JsonInclude.class);
		return include.map(i -> OPTIONAL_JSON_INCLUDE_VALUES.contains(i.value())).orElse(false);
	}

	private static KdonFieldParameter[] fieldTypeParameters(Member member) {
		Type type = Reflection.genericType(member);
		TypeToken<?> token = TypeToken.of(type);

		if (token.isArray()) {
			TypeToken<?> componentType = token.getComponentType();
			return new KdonFieldParameter[] {
					fieldParameter(kdonTypeName(componentType.getRawType()), isExternal(componentType.getRawType())) };
		}

		Type[] arguments = Reflection.upperBoundsOfTypeArguments(type);
		return stream(arguments).map(a -> fieldParameter(kdonTypeName(TypeToken.of(a).getRawType()),
				isExternal(TypeToken.of(a).getRawType()))).toArray(n -> new KdonFieldParameter[n]);
	}

	private static Optional<KdonField> kdonField(Member field) {
		Optional<JsonProperty> jsonProperty = annotation(field, JsonProperty.class);
		if (!jsonProperty.isPresent()) {
			return Optional.empty();
		}

		String name = jsonProperty.get().value();
		Optional<KdonDoc> doc = annotation(field, KdonDoc.class);
		String description = doc.map(d -> d.value()).orElse("");
		Class<?> memberType = memberType(field);
		Class<?> type = typeOrComponentTypeIfArray(memberType);
		String fieldType = kdonTypeName(memberType);

		KdonFieldParameter[] fieldTypeParameters = fieldTypeParameters(field);

		return Optional.of(
				new KdonField(name, description, fieldType, isExternal(type), isOptional(field), fieldTypeParameters));
	}

	private static boolean isExternal(Class<?> type) {
		return !type.isAnnotationPresent(KdonTypeName.class);
	}

	private static KdonType kdonType(Class<?> clazz, Collection<Class<?>> knownSubtypes,
			Set<Class<?>> possibleSupertypes) {
		String name = kdonTypeName(clazz);
		KdonDoc docAnnotation = clazz.getAnnotation(KdonDoc.class);
		String description = (docAnnotation != null ? docAnnotation.value() : "");
		String[] types = TypeToken.of(clazz).getTypes().stream().filter(t -> !t.getRawType().equals(clazz))
				.filter(t -> possibleSupertypes.contains(t.getRawType())).map(t -> kdonTypeName(t.getRawType()))
				.toArray(n -> new String[n]);
		String[] subtypes = knownSubtypes.stream().map(c -> kdonTypeName(c)).toArray(n -> new String[n]);

		Field[] declaredFields = clazz.getDeclaredFields();
		Method[] declaredMethods = clazz.getDeclaredMethods();

		KdonField[] fieldSpecsFromFields = stream(declaredFields).map(KdonTypes::kdonField).filter(d -> d.isPresent())
				.map(d -> d.get()).toArray(n -> new KdonField[n]);
		KdonField[] fieldSpecsFromMethods = stream(declaredMethods).map(KdonTypes::kdonField).filter(d -> d.isPresent())
				.map(d -> d.get()).toArray(n -> new KdonField[n]);

		KdonField[] fields = concat(fieldSpecsFromMethods, fieldSpecsFromFields, KdonField.class);
		sort(fields, (f1, f2) -> f1.name.compareTo(f2.name));

		KdonTypeParameter[] typeParameters = Arrays.stream(clazz.getTypeParameters())
				.filter(p -> p.getAnnotation(KdonDoc.class) != null)
				.map(p -> new KdonTypeParameter(p.getName(), p.getAnnotation(KdonDoc.class).value()))
				.toArray(n -> new KdonTypeParameter[n]);

		return new KdonType(name, clazz.getName(), types, subtypes, description, fields, typeParameters);
	}

	public static KdonType[] kdonTypes() {
		return KDONTYPENAME_TO_KDONTYPE.values().stream().toArray(n -> new KdonType[n]);
	}

	public static Optional<KdonType> kdonTypeByName(String kdonTypeName) {
		return Optional.ofNullable(KDONTYPENAME_TO_KDONTYPE.get(kdonTypeName));
	}

	public static Optional<KdonType> kdonTypeByJavaTypeName(String javaTypeName) {
		return Optional.ofNullable(JAVATYPENAME_TO_KDONTYPE.get(javaTypeName));
	}

	public static void main(String[] args) throws IOException {
		JsonSerialization.serialize(new OutputStreamWriter(System.out), kdonTypes());
	}

	private KdonTypes() {
		;
	}

}
