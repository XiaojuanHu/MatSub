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

package de.unibonn.realkd.algorithms;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import de.unibonn.realkd.common.base.ValidationException;
import de.unibonn.realkd.common.parameter.DefaultParameterContainer;
import de.unibonn.realkd.common.parameter.Parameter;
import de.unibonn.realkd.common.parameter.ParameterContainer;
import de.unibonn.realkd.common.parameter.ParameterListener;
import de.unibonn.realkd.patterns.Pattern;

/**
 * This abstract class jointly with the base implementations of the
 * {@link Parameter} interfaces provides a default implementation for the
 * interface {@link MiningAlgorithm} and also for the specialization
 * {@link StoppableMiningAlgorithm}. In order to implement a new algorithm,
 * developers have to create a concrete subclass of this class that
 * <ul>
 * <li>in its constructor registers all parameters via
 * {@link #registerParameter} that are supposed to be checked for consistency
 * and to be visible to clients (command line, visual UIs),</li>
 * 
 * <li>implements the abstract method {@link #concreteCall} which is called by
 * this class as an entry point into the computation of the actual algorithm,
 * and</li>
 * 
 * <li>optionally override the method {@link #onStopRequest()} to implement
 * additional behavior when clients request stop of the algorithm.</li>
 * </ul>
 * <br>
 * <p>
 * Note that this class only provides the management of a stop request flag. The
 * actual stopping behavior has to be handled on the level of concrete
 * subclasses, which are supposed to regularly query {@link #stopRequested} in
 * order to provide a reasonable stopping behavior.
 * </p>
 * 
 * @see Parameter
 * 
 * @author Pavel Tokmakov
 * @author Mario Boley
 * @author Björn Jacobs
 * 
 * @since 0.1
 * 
 * @version 0.4.0
 * 
 */
public abstract class AbstractMiningAlgorithm<T extends Pattern<?>>
		implements StoppableMiningAlgorithm, ParameterListener, ParameterContainer {

	private volatile boolean finished = false;

	private volatile boolean isRunning = false;
	
	private volatile boolean stopRequested = false;

	private volatile Optional<Long> startTime = Optional.empty();

	private volatile Optional<Long> endTime = Optional.empty();

	private final DefaultParameterContainer parameterContainer;

	public AbstractMiningAlgorithm() {
		parameterContainer = new DefaultParameterContainer();
	}

	public Optional<Long> startTime() {
		return startTime;
	}

	public Optional<Long> terminationTime() {
		return endTime;
	}

	/**
	 * Implementation of {@link MiningAlgorithm#call} that first checks that
	 * algorithm is not already running and then that all registered parameters have
	 * been set consistently.
	 * 
	 * If both checks pass then {#link concreteCall} is called which is the hook for
	 * concrete subclasses to provide the implementation of the actual algorithm.
	 * 
	 * @throws IllegalStateException
	 *             if either of the two initial checks does not pass.
	 * 
	 * @see #registerParameter(Parameter)
	 */
	@Override
	public final Collection<T> call() throws ValidationException {
		if (running()) {
			throw new IllegalStateException(
					"Algorithm already running. No concurrent execution of the same algorithm possible: "
							+ this.toString());
		}

		validate();

		stopRequested = false;
		isRunning = true;

		Collection<T> results;

		startTime = Optional.of(System.currentTimeMillis());

		try {
			results = concreteCall();
		} finally {
			isRunning = false;
		}

		finished = true;
		endTime = Optional.of(System.currentTimeMillis());

		return results;
	}

	@Override
	public final List<Parameter<?>> getTopLevelParameters() {
		return this.parameterContainer.getTopLevelParameters();
	}

	@Override
	public final boolean running() {
		return isRunning;
	}
	
	@Override
	public final boolean finished() {
		return finished;
	}

	/**
	 * Sets stop flag and will be reset on new call. By calling this method the
	 * algorithm is requested to stop as soon as possible.
	 * 
	 * <p>
	 * The non-final method {@link #onStopRequest} is called before the internal
	 * stop request flag is set. This provides a hook for concrete subclasses to
	 * implement special behavior on stop request, which typically would be related
	 * to finalizing intermediate results.
	 * 
	 */
	@Override
	public final void requestStop() {
		onStopRequest();

		stopRequested = true;
	}

	/**
	 * The method that should be implemented with algorithm logic. The method is
	 * execute from the abstract class' context whenever the algorithm's call()
	 * method is called.
	 * 
	 * @return The results of the algorithm execution.
	 * @throws ValidationException
	 */
	protected abstract Collection<T> concreteCall() throws ValidationException;

	/**
	 * Can be overridden optionally by sub-classes if classes need special behavior
	 * when stop-request is given.
	 */
	protected void onStopRequest() {
		;
	}

	/**
	 * Hook for concrete sub-classes for registering parameters. Parameters must be
	 * registered in order compatible with dependencies.
	 * 
	 * @see Parameter
	 */
	protected final void registerParameter(Parameter<?> parameter) {
		parameterContainer.addParameter(parameter);
		parameter.addListener(this);
	}

	/**
	 * Provides information to subclasses if a stop was requested. This information
	 * must be polled regularly during {#link #concreteCall} in order for subclasses
	 * to reasonably implement the behavior specified in
	 * {@link StoppableMiningAlgorithm}.
	 * 
	 */
	public final boolean stopRequested() {
		return stopRequested;
	}

	public void notifyValueChanged(Parameter<?> parameter) {
		if (this.isRunning) {
			throw new IllegalStateException("Cannot reset parameter value while algorithm is running.");
		}
	}

}
