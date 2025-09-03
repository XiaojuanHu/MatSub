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
package de.unibonn.realkd.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class Reflection {

	private Reflection() {
		;
	}

	public static <T> Optional<T> value(Object obj, String accessPath, Class<T> clazz) {
		Optional<Object> value = value(obj, accessPath);
		return value.filter(o -> clazz.isAssignableFrom(o.getClass())).map(o -> clazz.cast(o));
	}

	public static Optional<Object> value(Object obj, String accessPath) {
		final String[] split = accessPath.split("\\.");
		return value(obj, ImmutableList.copyOf(split));
	}

	/**
	 * Indirect member value of some given object recursively referenced through a
	 * list of field / method names.
	 * 
	 */
	public static Optional<Object> value(Object obj, List<String> accessPath) {
		if (obj == null) {
			return Optional.empty();
		}

		if (accessPath.isEmpty()) {
			return Optional.ofNullable(obj);
		}

		try {
			Field field = obj.getClass().getDeclaredField(accessPath.get(0));
			field.setAccessible(true);
			return value(field.get(obj), accessPath.subList(1, accessPath.size()));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			;
		}

		try {
			Method method = obj.getClass().getDeclaredMethod(accessPath.get(0));
			method.setAccessible(true);
			return value(method.invoke(obj), accessPath.subList(1, accessPath.size()));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			;
		}

		try {
			Method method = obj.getClass().getMethod(accessPath.get(0));
			return value(method.invoke(obj), accessPath.subList(1, accessPath.size()));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			;
		}

		return Optional.empty();
	}

	/**
	 * <p>
	 * Dynamically retrieves method on given object by name and signature.
	 * </p>
	 * 
	 * @param obj
	 *            Object on which method is to be found
	 * @param name
	 *            Simple name of method
	 * @param returnType
	 *            Type T such that method return type can be assigned to T
	 * @param parameterTypes
	 *            Array of types such that returned method parameters are of same
	 *            number and each of them can be assigned from given parameterTypes
	 * 
	 * @return declared method satisfying the above requirements or empty in case no
	 *         such method present; accessibility set to true
	 */
	public static Optional<Method> method(Object obj, String name, Class<?> returnType, Class<?>... parameterTypes) {
		Method[] declaredMethods = obj.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			method.setAccessible(true);
			if (!method.getName().equals(name)) {
				continue;
			}
			if (!returnType.isAssignableFrom(method.getReturnType())
					|| method.getParameterCount() != parameterTypes.length) {
				continue;
			}
			if (IntStream.range(0, parameterTypes.length)
					.anyMatch(i -> !method.getParameterTypes()[i].isAssignableFrom(parameterTypes[i]))) {
				continue;
			}
			return Optional.of(method);
		}
		return Optional.empty();
	}

	public static List<Method> methods(Object obj, String name, Class<?> returnType, int numParams) {
		Method[] declaredMethods = obj.getClass().getDeclaredMethods();
		final List<Method> res = java.util.Arrays
				.stream(declaredMethods).peek(m -> m.setAccessible(true)).filter(m -> m.getName().equals(name)
						&& returnType.isAssignableFrom(m.getReturnType()) && m.getParameterCount() == numParams)
				.collect(Collectors.toList());
		return res;
	}

	/**
	 * Uniform access to type of member; field type in case of field, return type in
	 * case of method.
	 * 
	 * @param member
	 * 
	 * @return type of member
	 * 
	 */
	public static Class<?> memberType(Member member) {
		return (member instanceof Field) ? ((Field) member).getType() : ((Method) member).getReturnType();
	}

	/**
	 * Returns optional annotation of specified type from member object if such an
	 * annotation is present; empty otherwise.
	 * 
	 * @param member
	 *            field or method with potential annotation
	 * @param annotationClass
	 *            class of desired annotation
	 * @return optional with annotation of empty
	 * 
	 */
	public static <T extends Annotation> Optional<T> annotation(Member member, Class<T> annotationClass) {
		T annotation = (member instanceof Field) ? ((Field) member).getAnnotation(annotationClass)
				: ((Method) member).getAnnotation(annotationClass);
		return Optional.ofNullable(annotation);
	}

	public static Type genericType(Member member) {
		return (member instanceof Field) ? ((Field) member).getGenericType() : ((Method) member).getGenericReturnType();
	}

	/**
	 * <p>
	 * Resolves upper bounds for type arguments of generic type.
	 * </p>
	 * <p>
	 * Currently implemented with naive resolution. Consider using
	 * https://github.com/jhalterman/typetools in the future.
	 * </p>
	 * 
	 */
	public static Type[] upperBoundsOfTypeArguments(Type type) {
		if (!(type instanceof ParameterizedType)) {
			return new Type[] {};
		}
		Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
		return java.util.Arrays.stream(arguments)
				.map(a -> (a instanceof WildcardType && ((WildcardType) a).getUpperBounds().length > 0)
						? ((WildcardType) a).getUpperBounds()[0]
						: a)
				.toArray(n -> new Type[n]);
	}

	public static Multimap<Class<?>, Class<?>> subtypeStructure(Set<Class<?>> classes) {
		Multimap<Class<?>, Class<?>> res = ArrayListMultimap.create();
		for (Class<?> clazz : classes) {
			TypeToken.of(clazz).getTypes().stream().map(t -> t.getRawType())
					.filter(c -> !clazz.equals(c) && classes.contains(c)).forEach(c -> res.put(c, clazz));
		}
		return res;
	}

}
