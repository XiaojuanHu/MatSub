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
package de.unibonn.realkd.visualization;

import java.awt.Color;
import java.awt.Font;

/**
 * @author Mario Boley
 * 
 * @since 0.5.0
 * 
 * @version 0.5.0
 *
 */
public interface VisualizationStyle {

	Color subPopulationComplementColor();

	Color globalPopulationColor();

	Color subPopulationColor();

	default Color outlineColor() {
		return Color.BLACK;
	}

	Color backgroundColor();

	Color itemPaint();

	Color domainGridLinePaint();

	Color axisTickLabelPaint();

	Color tickLabelPaint();

	Font axisLabelFont();

	Font tickLabelFont();

	Font legendFont();

	Font itemFont();

	Font textAnnotation();

	Font cellFont();

	boolean borderVisible();

	int backgroundAlpha();

	static Color BENCHMARK_QUANTITY_COLOR=new Color(0x87, 0xCE, 0xff);
	
	default Color benchmarkQuantityColor() {
		return BENCHMARK_QUANTITY_COLOR;
	}

	static Color DEFAULT_QUANITITY_COLOR=new Color(0xff, 0xae, 0xb9);
	
	default Color defaultQuantityColor() {
		 return DEFAULT_QUANITITY_COLOR;
	}

}