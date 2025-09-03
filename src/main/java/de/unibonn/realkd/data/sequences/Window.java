/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.sequences;

import java.util.Objects;

/**
 *
 * @author Ali Doku
 * @author Sandy Moens
 * 
 * @since 0.7.1
 * 
 * @version 0.7.1
 */
public class Window {

	public static Window window() {
		return new Window(-1, -1);
	}

	public static Window window(double start, double end) {
		return new Window(start, end);
	}

	private double start;
	private double end;

	private Window(double start, double end) {
		this.start = start;
		this.end = end;
	}

	public double start() {
		return this.start;
	}

	public double end() {
		return this.end;
	}

	public double size() {
		return this.end - this.start + 1;
	}

	public Window start(double start) {
		return window(start, this.end);
	}

	public Window end(double end) {
		return window(this.start, end);
	}

	@Override
	public String toString() {
		return "[" + this.start + "," + this.end + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.end, this.start);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Window other = (Window) obj;
		if (Double.doubleToLongBits(this.end) != Double.doubleToLongBits(other.end))
			return false;
		if (Double.doubleToLongBits(this.start) != Double.doubleToLongBits(other.start))
			return false;
		return true;
	}
}
