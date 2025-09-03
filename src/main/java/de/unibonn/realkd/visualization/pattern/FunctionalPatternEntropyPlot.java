package de.unibonn.realkd.visualization.pattern;

import static de.unibonn.realkd.patterns.functional.ExpectedMutualInformation.EXPECTED_MUTUAL_INFORMATION;
import static de.unibonn.realkd.patterns.models.table.MutualInformation.MUTUAL_INFORMATION;
import static de.unibonn.realkd.visualization.JFChartPainter.DETAILED;
import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;
import static de.unibonn.realkd.visualization.JFChartPainter.categoryDataset;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.functional.CoDomainEntropy;
import de.unibonn.realkd.patterns.functional.FunctionalPattern;

public class FunctionalPatternEntropyPlot implements PatternVisualization {

	private static final String RANGE_AXIS_TITLE = "bits";

	FunctionalPatternEntropyPlot() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return (pattern instanceof FunctionalPattern);
	}

	private CategoryDataset dataset(FunctionalPattern pattern) {
		String[] columns = new String[] { "H(Y)", "I(X;Y)", "E[I(X;Y)]" };
		double[] values = new double[] { pattern.value(CoDomainEntropy.CODOMAIN_ENTROPY),
				pattern.value(MUTUAL_INFORMATION), pattern.value(EXPECTED_MUTUAL_INFORMATION) };
		String[] series = columns;
		return categoryDataset(series, columns, values);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		return PREVIEW.stackedBarChart("", dataset((FunctionalPattern) pattern), RANGE_AXIS_TITLE, 2);
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return DETAILED.stackedBarChart("Information contents", dataset((FunctionalPattern) pattern), RANGE_AXIS_TITLE, 2);
	}

}
