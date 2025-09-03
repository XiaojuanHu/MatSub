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
package de.unibonn.realkd.common.parameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.parameter.DefaultRangeEnumerableParameter.RangeComputer;
import de.unibonn.realkd.common.parameter.DefaultSubCollectionParameter.JsonStringToCollectionParser;
import de.unibonn.realkd.common.parameter.DefaultSubCollectionParameter.ValidateIsSubcollection;

/**
 * Provides static factory methods for constructing different default parameter
 * objects.
 * 
 * @author Mario Boley
 * 
 * @since 0.2.1
 * 
 * @version 0.6.0
 * 
 */
public class Parameters {

	public static Parameter<String> stringParameter(Identifier id, String name, String description, String initValue,
			Predicate<? super String> validator, String helperText) {
		return new DefaultParameter<>(id, name, description, String.class, initValue, input -> input, validator,
				helperText);
	}

	public static Parameter<Double> doubleParameter(Identifier id, String name, String description, double initValue,
			Predicate<? super Double> validator, String helperText) {
		return new DefaultParameter<>(id, name, description, Double.class, initValue, input -> Double.valueOf(input),
				validator, helperText);
	}

	public static Parameter<Double> doubleParameter(Identifier id, String name, String description, double initValue,
			Predicate<? super Double> validator, String helperText, Supplier<Boolean> hidden,
			Parameter<?>... dependsOn) {
		return new DefaultParameter<>(id, name, description, Double.class, input -> Double.valueOf(input), validator,
				helperText, () -> initValue, hidden, dependsOn);
	}
	
	public static Parameter<Double> doubleParameter(Identifier id, String name, String description, Supplier<Double> initializer ,
			Predicate<? super Double> validator, String helperText, Supplier<Boolean> hidden,
			Parameter<?>... dependsOn) {
		return new DefaultParameter<>(id, name, description, Double.class, input -> Double.valueOf(input), validator,
				helperText, initializer, hidden, dependsOn);
	}

	public static Parameter<Integer> integerParameter(Identifier id, String name, String description, int initValue,
			Predicate<? super Integer> validator, String helperText) {
		return new DefaultParameter<>(id, name, description, Integer.class, initValue, input -> Integer.valueOf(input),
				validator, helperText);
	}

	public static Parameter<Integer> dependentIntegerParameter(Identifier id, String name, String description,
			Predicate<? super Integer> validator, String helperText, Supplier<Integer> initializer,
			Parameter<?>... dependsOnParams) {
		return new DefaultParameter<>(id, name, description, Integer.class, input -> Integer.valueOf(input), validator,
				helperText, initializer, dependsOnParams);
	}

	/**
	 * @param name
	 *            the name of resulting parameter
	 * @param description
	 *            the description of resulting parameter
	 * @param type
	 *            the guaranteed runtime type of values of resulting parameter
	 * @param parameter
	 *            another parameter that resulting parameter depends on
	 * @param transformers
	 *            list of transformers to determine current range or resulting
	 *            parameter based on current state of other parameter
	 * @return a range enumerable parameter of type RangeEnumerableParameter <T> the
	 *         current valid range of which depends on another given parameters
	 * 
	 * @param <T>
	 *            the value type of the resulting parameter
	 * 
	 * @param <P>
	 *            the type of the other parameter that the resulting parameter
	 *            depends on
	 * 
	 */
	public static <P extends Parameter<?>, T> RangeEnumerableParameter<T> dependentRangeEnumerableParameter(
			Identifier id, String name, String description, Class<? super T> type, final P parameter,
			List<? extends Function<? super P, ? extends List<? extends T>>> transformers) {
		return Parameters.rangeEnumerableParameter(id, name, description, type, new RangeComputer<T>() {
			@Override
			public List<T> get() {
				Stream<T> applicableProcedures = transformers.stream().flatMap(p -> p.apply(parameter).stream());
				return applicableProcedures.collect(Collectors.toList());
			}
		}, parameter);
	}

	public static <P1 extends Parameter<?>, P2 extends Parameter<?>, T> RangeEnumerableParameter<T> dependentRangeEnumerableParameter(
			Identifier id, String name, String description, Class<? super T> type, final P1 parameter1,
			final P2 parameter2, List<BiFunction<P1, P2, List<T>>> transformers) {
		return Parameters.rangeEnumerableParameter(id, name, description, type, new RangeComputer<T>() {
			@Override
			public List<T> get() {
				Stream<T> applicableProcedures = transformers.stream()
						.flatMap(p -> p.apply(parameter1, parameter2).stream());
				return applicableProcedures.collect(Collectors.toList());
			}
		}, parameter1, parameter2);
	}

	public static <T> DefaultRangeEnumerableParameter<T> rangeEnumerableParameter(Identifier id, String name,
			String description, Class<?> type, Supplier<? extends List<? extends T>> rangeComputer,
			Parameter<?>... dependenParams) {
		return new DefaultRangeEnumerableParameter<T>(id, name, description, type, rangeComputer, dependenParams);
	}

	public static <T> DefaultRangeEnumerableParameter<T> rangeEnumerableParameter(Identifier id, String name,
			String description, Class<?> type, Supplier<? extends List<? extends T>> rangeComputer,
			Supplier<Boolean> hidden, Parameter<?>... dependenParams) {
		return new DefaultRangeEnumerableParameter<T>(id, name, description, type, rangeComputer, hidden,
				dependenParams);
	}

	/**
	 * Creates a range enumerable parameter based on another wrapped range
	 * enumerable parameter with options filtered through a white-list of strings.
	 * 
	 * @param original
	 *            the wrapped parameter
	 * @param validOptionList
	 *            the white-list of valid options (as string values)
	 * @return a new parameter with range at a given moment equal to range of
	 *         wrapped parameter filtered by white-list
	 */
	public static <T> RangeEnumerableParameter<T> filteredRangeEnumerableParameter(RangeEnumerableParameter<T> original,
			List<String> validOptionList) {
		RangeComputer<T> filteredRangeComputer = () -> original.getRange().stream()
				.filter(v -> validOptionList.contains(v.toString())).collect(Collectors.toList());
		return rangeEnumerableParameter(original.id(), original.getName(), original.getDescription(),
				original.getType(), filteredRangeComputer, original.getDependsOnParameters().toArray(new Parameter[0]));
	}

	/**
	 * @return range enumerable parameter with options "true" and "false" (default
	 *         "true")
	 */
	public static RangeEnumerableParameter<Boolean> booleanParameter(Identifier id, String name, String description) {
		return rangeEnumerableParameter(id, name, description, Boolean.class,
				() -> ImmutableList.of(Boolean.TRUE, Boolean.FALSE));
	}

	/**
	 * Creates a parameter with valid values being subsets of some dynamic range
	 * collection. Value is initialized by some arbitrary initializer.
	 * 
	 */
	public static <G> SubCollectionParameter<G, Set<G>> subSetParameter(Identifier id, String name, String description,
			Supplier<Set<G>> rangeComputer, Supplier<Set<G>> initializer, Parameter<?>... dependsOnParameters) {
		JsonStringToCollectionParser<G, Set<G>> toImmutableSetParser = new JsonStringToCollectionParser<G, Set<G>>(
				() -> rangeComputer.get(), l -> ImmutableSet.copyOf(l));
		ValidateIsSubcollection<G, Set<G>> validator = new ValidateIsSubcollection<G, Set<G>>(rangeComputer);
		return new DefaultSubCollectionParameter<G, Set<G>>(id, name, description, Set.class, rangeComputer,
				toImmutableSetParser, validator, initializer, dependsOnParameters);
	}

	/**
	 * Creates a parameter with valid values being subsets of some dynamic range
	 * collection. Value is initialized to the empty set.
	 * 
	 */
	public static <G> SubCollectionParameter<G, Set<G>> subSetParameter(Identifier id, String name, String description,
			Supplier<Set<G>> rangeComputer, Parameter<?>... dependsOnParameters) {
		return subSetParameter(id, name, description, rangeComputer, () -> new HashSet<>(), dependsOnParameters);
	}

	public static <G> SubCollectionParameter<G, List<G>> subListParameter(Identifier id, String name, String description,
			Supplier<List<G>> rangeComputer, Predicate<List<G>> validator, Supplier<List<G>> initializer,
			Parameter<?>... dependsOnParameters) {
		return new DefaultSubCollectionParameter<G, List<G>>(id, name, description, List.class, rangeComputer,
				new JsonStringToCollectionParser<G, List<G>>(() -> rangeComputer.get(), l -> ImmutableList.copyOf(l)),
				validator, initializer, dependsOnParameters);
	}

	public static <G> SubCollectionParameter<G, List<G>> subListParameter(Identifier id, String name, String description,
			Supplier<List<G>> rangeComputer, Predicate<List<G>> validator, Parameter<?>... dependsOnParameters) {
		return new DefaultSubCollectionParameter<G, List<G>>(id, name, description, List.class, rangeComputer,
				new JsonStringToCollectionParser<G, List<G>>(() -> rangeComputer.get(), l -> ImmutableList.copyOf(l)),
				validator, () -> new ArrayList<>(), dependsOnParameters);
	}

	public static <G> SubCollectionParameter<G, List<G>> subListParameter(Identifier id, String name, String description,
			Supplier<List<G>> rangeComputer, Parameter<?>... dependsOnParameters) {
		return new DefaultSubCollectionParameter<G, List<G>>(id, name, description, List.class, rangeComputer,
				new JsonStringToCollectionParser<G, List<G>>(() -> rangeComputer.get(), l -> ImmutableList.copyOf(l)),
				new ValidateIsSubcollection<G, List<G>>(rangeComputer), () -> new ArrayList<>(), dependsOnParameters);

	}

}
