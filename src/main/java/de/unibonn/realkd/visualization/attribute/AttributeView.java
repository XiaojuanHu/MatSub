package de.unibonn.realkd.visualization.attribute;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.data.table.attribute.Attribute;

public interface AttributeView {

	public int getDefaultWidth();
	
	public int getDefaultHeight();
	
	public boolean isApplicable(Attribute<?> attribute);

	public JFreeChart draw(Attribute<?> attribute);

}
