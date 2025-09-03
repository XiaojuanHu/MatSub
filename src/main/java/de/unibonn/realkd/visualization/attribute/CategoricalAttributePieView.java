package de.unibonn.realkd.visualization.attribute;

import java.awt.Color;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import de.unibonn.realkd.visualization.JFChartPainter;

public class CategoricalAttributePieView implements AttributeView {

	@Override
	public boolean isApplicable(Attribute<?> attribute) {
		return (attribute instanceof CategoricAttribute);
	}

	@Override
	public JFreeChart draw(Attribute<?> attribute) {

		JFChartPainter painter = JFChartPainter.PREVIEW;
		CategoricAttribute<?> categoricalAttribute = (CategoricAttribute<?>) attribute;
		int numOfCategories = categoricalAttribute.categories().size();
		String[] items = new String[numOfCategories];
		double[] values = new double[numOfCategories];

		for (int i = 0; i < numOfCategories; i++) {
			items[i] = categoricalAttribute.categories().get(i).toString();
			values[i] = categoricalAttribute.categoryFrequencies().get(i);
			// / categoricalAttribute.getValues().size();
		}

		// String result;

		JFreeChart chart = painter.createPieChart("", items, values, false,
				new Color[] {});
		return chart;
	}

	@Override
	public int getDefaultWidth() {
		return 220;
	}

	@Override
	public int getDefaultHeight() {
		return 220;
	}

}
