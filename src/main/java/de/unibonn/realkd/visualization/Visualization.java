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
package de.unibonn.realkd.visualization;

import java.awt.image.BufferedImage;

/**
 * Generic interface for drawing visualizations of a certain type of object. The
 * general contract that is applied is that a user of the class should first
 * test if the visualization is applicable. If true then the visualization can
 * be applied using the draw method.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * @since 0.3.0
 * @version 0.3.0
 */
public interface Visualization<X> {

	/**
	 * Tests if a the object is applicable for the target visualization
	 * 
	 * @param object
	 *            the object that should be visualized
	 * @return true if the object can be visualized, otherwise false
	 */
	public boolean isApplicable(X object);

	/**
	 * Draws a regular visualization of the object and returns the buffered image
	 * containing the visualization.
	 * 
	 * @param object
	 *            the object to be visualized
	 * @param width
	 *            the width of the image
	 * @param height
	 *            the height of the image
	 * @return the buffered image containing the visualization
	 */
	public BufferedImage getBufferedImage(X object, int width, int height);

}
