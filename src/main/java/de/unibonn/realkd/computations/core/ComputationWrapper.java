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

package de.unibonn.realkd.computations.core;

import java.util.Optional;

import de.unibonn.realkd.algorithms.StoppableMiningAlgorithm;

/**
 * 
 * @author Pavel Tokmakov
 * @author Mario Boley
 * @author Björn Jacobs
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 * 
 */
public class ComputationWrapper<T> implements Computation<T> {

	private final CallableWithStopInterface<T> callable;

	private volatile boolean finished = false;

	private volatile boolean isRunning = false;

	private volatile boolean stopRequested = false;

	private volatile Optional<Long> startTime = Optional.empty();

	private volatile Optional<Long> endTime = Optional.empty();

	public ComputationWrapper(CallableWithStopInterface<T> callable) {
		this.callable = callable;
	}

	public Optional<Long> startTime() {
		return startTime;
	}

	public Optional<Long> terminationTime() {
		return endTime;
	}

	/**
	 * Implementation of {@link Computation#call} that first checks that computation
	 * is not already running and then that all registered parameters have been set
	 * consistently.
	 * 
	 * If both checks pass then {#link concreteCall} is called which is the hook for
	 * concrete subclasses to provide the implementation of the actual algorithm.
	 * @throws Exception 
	 * 
	 * @throws IllegalStateException
	 *             if either of the two initial checks does not pass.
	 * 
	 */
	@Override
	public final T call() throws Exception {
		if (running()) {
			throw new IllegalStateException("Computation already running");
		}

		stopRequested = false;
		isRunning = true;

		T result;

		startTime = Optional.of(System.currentTimeMillis());

		try {
			result = callable.call();
		} finally {
			isRunning = false;
			endTime = Optional.of(System.currentTimeMillis());
		}

		finished = true;

		return result;
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
		callable.requestStop();
		stopRequested = true;
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

}
