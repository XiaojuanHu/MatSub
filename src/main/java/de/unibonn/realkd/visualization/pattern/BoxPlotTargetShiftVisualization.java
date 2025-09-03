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
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

public class BoxPlotTargetShiftVisualization implements PatternVisualization {

	/**
	 * constructor only to be invoked from Visualization Register
	 */
	BoxPlotTargetShiftVisualization() {
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (!(pattern.descriptor() instanceof Subgroup)) {
			return false;
		}

		Subgroup<?> descriptor = (Subgroup<?>) pattern.descriptor();

		return (descriptor.targetAttributes().size() == 1
				&& descriptor.targetAttributes().get(0) instanceof MetricAttribute);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		Subgroup<?> descriptor = (Subgroup<?>) pattern.descriptor();
		MetricAttribute targetAttribute = (MetricAttribute) descriptor.targetAttributes().get(0);
		DefaultBoxAndWhiskerCategoryDataset boxDataset = boxDataSet(descriptor, targetAttribute);
		return JFChartPainter.PREVIEW.createBoxPlotChart("", boxDataset, targetAttribute.caption());
	}
	
	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		Subgroup<?> descriptor = (Subgroup<?>) pattern.descriptor();
		MetricAttribute targetAttribute = (MetricAttribute) descriptor.targetAttributes().get(0);
		DefaultBoxAndWhiskerCategoryDataset boxDataset = boxDataSet(descriptor, targetAttribute);
		return JFChartPainter.DETAILED.createBoxPlotChart("", boxDataset, targetAttribute.caption());
	}

	private DefaultBoxAndWhiskerCategoryDataset boxDataSet(Subgroup<?> descriptor, MetricAttribute targetAttribute) {
		IndexSet supportSet = descriptor.supportSet();
		List<Double> subgroupValues = StreamSupport.stream(supportSet.spliterator(), false)
				.filter(i -> !(targetAttribute.valueMissing(i)))
				.map(i -> targetAttribute.value(i)).collect(Collectors.toList());
		List<Double> globalValues=ImmutableList.copyOf(targetAttribute.nonMissingValues());

		DefaultBoxAndWhiskerCategoryDataset boxDataset = new DefaultBoxAndWhiskerCategoryDataset();
		boxDataset.add(subgroupValues, "subgroup", "");
		boxDataset.add(globalValues, "global", "");
		return boxDataset;
	}

}
