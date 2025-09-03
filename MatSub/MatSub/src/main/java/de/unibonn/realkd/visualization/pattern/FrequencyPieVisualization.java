package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;

import java.awt.Color;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.patterns.Frequency;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.association.Association;
import de.unibonn.realkd.visualization.JFChartPainter;
import de.unibonn.realkd.visualization.PreviewVisualizationStyle;

public class FrequencyPieVisualization implements PatternVisualization {

	/**
	 * constructor only to be invoked from Visualization Register
	 */
	FrequencyPieVisualization() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (pattern instanceof Association) {
			return false;
		} else if (pattern.hasMeasure(Frequency.FREQUENCY)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		String[] items = { "Coverage", "" };
		double freqency = pattern.value(Frequency.FREQUENCY);
		double[] values = { freqency, 1 - freqency };
		return PREVIEW.createPieChart("", items, values, true,
				new Color[] { PreviewVisualizationStyle.get().subPopulationColor(),
						PreviewVisualizationStyle.get().subPopulationComplementColor() });
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		String[] items = { "Coverage", "" };
		double freqency = pattern.value(Frequency.FREQUENCY);
		double[] values = { freqency, 1 - freqency };
		return JFChartPainter.DETAILED.createPieChart("", items, values, true,
				new Color[] { PreviewVisualizationStyle.get().subPopulationColor(),
						PreviewVisualizationStyle.get().subPopulationComplementColor() });
	}

}
