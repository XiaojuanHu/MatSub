package de.unibonn.realkd.visualization.attribute;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.visualization.JFChartPainter;

public class NumericAttributeHistogramView implements AttributeView {

	@Override
	public boolean isApplicable(Attribute<?> attribute) {
		return (attribute instanceof MetricAttribute);
	}

	@Override
	public JFreeChart draw(Attribute<?> attribute) {

		JFChartPainter painter = JFChartPainter.PREVIEW;
		MetricAttribute defaultMetricAttribute = (MetricAttribute) attribute;

		List<Double> values = new ArrayList<>(
				defaultMetricAttribute.nonMissingValues());

		return painter.createHistogramPlot("", values);
	}

	@Override
	public int getDefaultWidth() {
		return 220;
	}

	@Override
	public int getDefaultHeight() {
		return 124;
	}
}
