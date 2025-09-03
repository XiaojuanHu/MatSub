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
package de.unibonn.realkd.visualization.pattern;

import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.visualization.Visualization;

/**
 * Interface class for drawing pattern visualizations.
 * 
 * @author Mario Boley
 * @author Sandy Moens
 * @since 0.3.0
 * @version 0.3.0
 */
public interface PatternVisualization extends Visualization<Pattern<?>> {

	/**
	 * Draws a regular visualization of the pattern using JFreeChart.
	 * 
	 * @param pattern
	 *            the pattern to be visualized
	 * @return the visual representation of the object using JFreeChart
	 */
	public JFreeChart draw(Pattern<?> pattern);

	/**
	 * Draws a detailed visualization of the pattern using JFreeChart. Generally, a
	 * detailed visualization is displayed in a larger size and therefore allowing
	 * more detail in the drawing.
	 * 
	 * @param pattern
	 *            the pattern to be visualized
	 * @return the visual representation of the object using JFreeChart
	 */
	public JFreeChart drawDetailed(Pattern<?> object);

	default public BufferedImage getBufferedImage(Pattern<?> pattern, int width, int height) {
		if (width > 1000) {
			return drawDetailed(pattern).createBufferedImage(width, height);
		}
		return draw(pattern).createBufferedImage(width, height);
	}

}
