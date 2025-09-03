package de.unibonn.realkd.visualization.pattern;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.subgroups.ControlledSubgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

public class BoxPlotControlShiftVisualization implements PatternVisualization {

	/**
	 * constructor only to be invoked from Visualization Register
	 */
	BoxPlotControlShiftVisualization() {
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (!(pattern.descriptor() instanceof ControlledSubgroup<?,?>)) {
			return false;
		}

		ControlledSubgroup<?,?> descriptor = (ControlledSubgroup<?,?>) pattern.descriptor();

		return (descriptor.controlAttributes().size() == 1
				&& descriptor.controlAttributes().get(0) instanceof MetricAttribute);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		ControlledSubgroup<?,?> descriptor = (ControlledSubgroup<?,?>) pattern.descriptor();
		JFChartPainter painter = JFChartPainter.PREVIEW;
		MetricAttribute controlAttribute = (MetricAttribute) descriptor.controlAttributes().get(0);
		IndexSet supportSet = descriptor.supportSet();
		List<Double> subgroupValues = StreamSupport.stream(supportSet.spliterator(), false)
				.filter(i -> !(controlAttribute.valueMissing(i)))
				.map(i -> controlAttribute.value(i)).collect(Collectors.toList());
		List<Double> globalValues=ImmutableList.copyOf(controlAttribute.nonMissingValues());

		DefaultBoxAndWhiskerCategoryDataset boxDataset = new DefaultBoxAndWhiskerCategoryDataset();
		boxDataset.add(subgroupValues, "subgroup", "");
		boxDataset.add(globalValues, "global", "");

		return painter.createBoxPlotChart("", boxDataset, controlAttribute.caption());
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return draw(pattern);
	}

}
