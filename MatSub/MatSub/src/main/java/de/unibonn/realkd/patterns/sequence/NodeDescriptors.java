/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
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
package de.unibonn.realkd.patterns.sequence;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

/**
 * @author Sandy
 *
 */
public class NodeDescriptors {
	
	public static <T> NodeDescriptor<T> createNodeDescriptor(T value) {
		return new NodeDescriptorImplementation<T>(value);
	}

	public static class NodeDescriptorImplementation<T> implements NodeDescriptor<T> {
		
		private T value;
		private Map<Integer, NodeDescriptor<T>> children;
		
		public NodeDescriptorImplementation(T value) {
			this.value = value;
			this.children = newHashMap();
		}
		
		public T value() {
			return value;
		}
		
		public void value(T value) {
			this.value = value;
		}

		@Override
		public Map<Integer, NodeDescriptor<T>> children() {
			return this.children;
		}
	}
	
}
