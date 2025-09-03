package de.unibonn.realkd.algorithms.derived;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import de.unibonn.realkd.algorithms.AbstractMiningAlgorithm;
import de.unibonn.realkd.algorithms.AlgorithmCategory;
import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;
import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.patterns.Pattern;

/**
 * <p>
 * Algorithm that provides a modified (usually simplified) access to some other
 * entailed algorithm by wrapping any or all of its parameters.
 * </p>
 * <p>
 * Parameters that are not wrapped can either be exposed to clients of this
 * class or simply be hidden.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.1
 * 
 * @version 0.1.1
 * 
 * @see DerivedAlgorithms
 * @see ParameterTerminator
 * 
 */
public final class AlgorithmWithParameterFacade extends AbstractMiningAlgorithm {

	private final StoppableMiningAlgorithm entailedAlgorithm;

	// private final List<ParameterWrapper> orderedParameterSelectors;

	private final List<ParameterTerminator> orderedOnExecutationTerminators;

	private final String name;

	private final String stringRepresentation;

	/**
	 * Constructor only to be invoked by factory methods in
	 * {@link DerivedAlgorithms}
	 * 
	 * @param entailedAlgorithm
	 *            the wrapped algorithm that is called by this wrapper
	 * @param orderedParameterWrappers
	 *            parameter wrappers to be activated in the given order at
	 *            execution time
	 * @param parameters
	 *            all parameters that this algorithm should publish
	 * @param name
	 *            the name of this algorithm
	 */
	AlgorithmWithParameterFacade(StoppableMiningAlgorithm entailedAlgorithm,
			List<ParameterWrapper> orderedParameterWrappers, String stringRepresentation, String name) {
		this.entailedAlgorithm = entailedAlgorithm;
		// this.orderedParameterSelectors = orderedParameterWrappers;
		this.name = name;
		this.stringRepresentation = stringRepresentation;

		// find all adapters among parameter wrappers
		LinkedHashSet<ParameterAdapter> adapters = orderedParameterWrappers.stream()
				.filter(wrapper -> wrapper instanceof ParameterAdapter).map(adapter -> (ParameterAdapter) adapter)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// find all terminators
		LinkedHashSet<ParameterTerminator> terminators = orderedParameterWrappers.stream()
				.filter(wrapper -> wrapper instanceof ParameterTerminator).map(adapter -> (ParameterTerminator) adapter)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Map<Parameter<?>, Parameter<?>> parameterToInfluencedExposed = new HashMap<>();

		// find all terminators that wrap a parameter that an
		// exposed parameter depends on
		Set<Parameter<?>> parametersThatInfluenceExposed = new HashSet<>();
		for (ParameterAdapter adapter : adapters) {
			adapter.getWrappedParameter().dfsTraverseDependentParameters(parametersThatInfluenceExposed::add);
			adapter.getWrappedParameter().dfsTraverseDependentParameters(
					p -> parameterToInfluencedExposed.put(p, adapter.getWrappedParameter()));
		}

		LinkedHashSet<ParameterTerminator> onConstructionTerminators = terminators.stream()
				.filter(terminator -> parametersThatInfluenceExposed.contains(terminator.getWrappedParameter()))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// find all terminator that depend on an exposed parameter
		Set<Parameter<?>> exposedParameters = adapters.stream().map(adapater -> adapater.getWrappedParameter())
				.collect(Collectors.toSet());
		Set<ParameterTerminator> forcedOnExecutionTerminators = terminators.stream()
				.filter(terminator -> !Collections.disjoint(
						terminator.getWrappedParameter().getDependsOnParametersTransitively(), exposedParameters))
				.collect(Collectors.toSet());

		Map<Parameter<?>, Set<String>> terminatedParametersToUpstreamExposed = new HashMap<>();
		terminators
				.forEach(
						t -> terminatedParametersToUpstreamExposed
								.put(t.getWrappedParameter(),
										Sets.intersection(t.getWrappedParameter().getDependsOnParametersTransitively(),
												exposedParameters).stream().map(p -> p.getName())
										.collect(Collectors.toSet())));

		// check for conflicting terminators
		SetView<ParameterTerminator> conflictingTerminators = Sets.intersection(onConstructionTerminators,
				forcedOnExecutionTerminators);
		if (!conflictingTerminators.isEmpty()) {
			throw new IllegalArgumentException(
					"Could not construct facade algorithm because the following parameters' terminators can neither be executed on construction nor on execution of algorithm due to their dependencies with exposed parameters: "
							+ conflictingTerminators.stream()
									.map(t -> t.getWrappedParameter().getName() + " (influences "
											+ parameterToInfluencedExposed.get(t.getWrappedParameter()).getName()
											+ " but is influenced by "
											+ terminatedParametersToUpstreamExposed.get(t.getWrappedParameter()) + ")")
									.collect(Collectors.toSet()));
		}

		// set and lock on construction terminators
		onConstructionTerminators.forEach(terminator -> terminator.setParameter());

		// find and publish all parameters of all adapters
		List<Parameter<?>> parameters = new ArrayList<>();
		adapters.stream().map(wrapper -> ((ParameterAdapter) wrapper).getAllParameters())
				.forEachOrdered(parameters::addAll);
		parameters.forEach(this::registerParameter);

		// store on execution parameters
		orderedOnExecutationTerminators = terminators.stream()
				.filter(terminator -> !onConstructionTerminators.contains(terminator)).collect(Collectors.toList());
	}

	/**
	 * Applies parameter selectors in order of parameter dependency order given
	 * by entailed algorithm.
	 * @throws ValidationException 
	 */
	@Override
	protected final Collection<? extends Pattern<?>> concreteCall() throws ValidationException {
		for (ParameterTerminator terminator : orderedOnExecutationTerminators) {
			terminator.setParameter();
		}

		return this.entailedAlgorithm.call();
	}

	@Override
	protected final void onStopRequest() {
		entailedAlgorithm.requestStop();
	}

	@Override
	public final String caption() {
		return name;
	}

	@Override
	public final String description() {
		return entailedAlgorithm.description();
	}

	@Override
	public final AlgorithmCategory getCategory() {
		return entailedAlgorithm.getCategory();
	}

	@Override
	public final String toString() {
		return stringRepresentation;
	}

}
