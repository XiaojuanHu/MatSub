package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.visualization.JFChartPainter.DETAILED;
import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;
import static de.unibonn.realkd.visualization.JFChartPainter.categoryDataset;
import static java.util.stream.IntStream.range;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.logical.LogicalDescriptor;

public class LiftLogProbabilityStackedBarChart implements PatternVisualization {

	private static final String RANGE_AXIS_TITLE = "log probability";

	LiftLogProbabilityStackedBarChart() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		// return pattern instanceof Association;
		return (pattern.descriptor() instanceof LogicalDescriptor);
	}

	private CategoryDataset dataset(LogicalDescriptor descriptor) {

		int length = descriptor.size();

		String series[] = new String[length + 1];
		String column[] = new String[length + 1];
		double[] values = new double[length + 1];

		// values[0] = Math.log(((Association) pattern).getFrequency());
		values[0] = Math.log(descriptor.supportSet().size() / (double) descriptor.population().size());
		series[0] = "s0";
		column[0] = "frequency";

		int i = 1;

		for (Proposition prop : descriptor.elements()) {
			values[i] = Math.log((double) prop.supportCount() / descriptor.population().size());
			series[i] = "s" + i;
			column[i] = "expected freq.";
			i++;
		}

		return categoryDataset(series, column, values);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		LogicalDescriptor descriptor = (LogicalDescriptor) pattern.descriptor();
		int[] benchmarkSeries = range(1, descriptor.size() + 1).toArray();
		return PREVIEW.stackedBarChart("", dataset(descriptor), RANGE_AXIS_TITLE, benchmarkSeries);
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		LogicalDescriptor descriptor = (LogicalDescriptor) pattern.descriptor();
		int[] benchmarkSeries = range(1, descriptor.size() + 1).toArray();
		return DETAILED.stackedBarChart("Stacked entropy plot", dataset(descriptor), RANGE_AXIS_TITLE, benchmarkSeries);
	}

}
