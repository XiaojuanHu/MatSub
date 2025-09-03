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

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

/**
 * 
 * <p>
 * Implements a Queue backed by a Stack
 * </p>
 * 
 * @author Panagiotis Mandros
 * @param <N>
 *
 */
public class StackBackedQueue<N> implements Queue<N> {

	private Stack<N> stack;

	public StackBackedQueue() {
		stack = new Stack<N>();
	}

	@Override
	public int size() {
		return stack.size();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return stack.contains(o);
	}

	@Override
	public Iterator<N> iterator() {
		return stack.iterator();
	}

	@Override
	public Object[] toArray() {
		return stack.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return stack.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return stack.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return stack.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends N> c) {
		return stack.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return stack.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return stack.retainAll(c);
	}

	@Override
	public void clear() {
		stack.clear();
	}

	@Override
	public boolean add(N e) {
		return stack.add(e);
	}

	@Override
	public boolean offer(N e) {
		return stack.add(e);
	}

	@Override
	public N remove() {
		try {
			return stack.pop();
		} catch (EmptyStackException e) {
			throw new NoSuchElementException();
		}
	}

	@Override
	public N poll() {
		if (stack.isEmpty()) {
			return null;
		} else {
			return stack.pop();
		}
	}

	@Override
	public N element() {
		try {
			return stack.peek();
		} catch (EmptyStackException e) {
			throw new NoSuchElementException();
		}
	}

	@Override
	public N peek() {
		if (stack.isEmpty()) {
			return null;
		} else {
			return stack.peek();
		}
	}

}
