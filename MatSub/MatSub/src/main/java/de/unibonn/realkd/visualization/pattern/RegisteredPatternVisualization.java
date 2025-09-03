package de.unibonn.realkd.visualization.pattern;

import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.patterns.Pattern;

public enum RegisteredPatternVisualization implements PatternVisualization {

	FREQUENCY_PIE(new FrequencyPieVisualization()),

	COLORED_TARGET_CONTINGENCY_TABLE(new ColoredContingencyTableDifferenceMatrix()),

	COLORED_LOCAL_CONTINGENCY_TABLE(new ColoredLocalContingencyTableMatrix()),

	COLORED_GLOBAL_CONTINGENCY_TABLE(new ColoredGlobalContingencyTableMatrix()),
	
	FUNCTIONAL_PATTERN_ENTROPY_PLOT(new FunctionalPatternEntropyPlot()),
	
	// TARGET_POINT_CLOUD(new TargetPointCloud()),

	// TARGET_POINT_CLOUD_PCA(new TargetPointCloudPCA()),

	TARGET_POINT_CLOUD_WITH_LINES(new TargetPointCloudWithLines()),

	TARGET_SHIFT_BOXPLOT(new BoxPlotTargetShiftVisualization()),

	TARGET_UNIVARIATE_DENSITY_PLOT(new UnivariateDensityPlot()),
	
	TARGET_METRIC_HISTOGRAM(new MetricTargetHistogram()),
	
	TARGET_UNIVARIATE_CUMULATIVE_DENSITY_PLOT(new UnivariateCumulativeDensityPlot()),
	
	TARGET_CATEGORIC_HISTOGRAM(new CategoricTargetHistogram()),

	CONTROL_SHIFT_BOXPLOT(new BoxPlotControlShiftVisualization()),

	CONTROL_CATEGORIC_HISTOGRAM(new CategoricControlHistogram()),
	
	TWO_NUMERIC_ATTRIBUTE_PATTERN_SCATTER_PLOT(new TwoNumericAttributePatternScatterPlot()),

	LIFT_LOGPROBABILITY_STACKED_BARCHART(new LiftLogProbabilityStackedBarChart());

	private PatternVisualization patternView;

	private RegisteredPatternVisualization(PatternVisualization patternView) {
		this.patternView = patternView;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return this.patternView.isApplicable(pattern);
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		return this.patternView.draw(pattern);
	}
	
	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return this.patternView.drawDetailed(pattern);
	}
	
	@Override
	public BufferedImage getBufferedImage(Pattern<?> pattern, int width, int height) {
		return this.patternView.getBufferedImage(pattern, width, height);
	}

}
