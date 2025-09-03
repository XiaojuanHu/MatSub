package de.unibonn.realkd.visualization.pattern;

import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.regression.LinearRegressionModel;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

public class TargetPointCloudWithLines extends TargetPointCloud {

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (!(pattern.descriptor() instanceof Subgroup)) {
			return false;
		}
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		return (subgroup.referenceModel() instanceof LinearRegressionModel
				&& checkExactlyTwoMetricTargets(subgroup.targetAttributes()));
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();

		JFChartPainter painter = JFChartPainter.PREVIEW;
		List<JFChartPainter.Point> localPoints = JFChartPainter.createPoints(subgroup.supportSet(),
				(MetricAttribute) subgroup.targetAttributes().get(0),
				(MetricAttribute) subgroup.targetAttributes().get(1));
		List<JFChartPainter.Point> globalPoints = JFChartPainter.createPoints(
				subgroup.getTargetTable().population().objectIds(),
				(MetricAttribute) subgroup.targetAttributes().get(0),
				(MetricAttribute) subgroup.targetAttributes().get(1));

		XYSeries localPointSeries = new XYSeries("localPoints");
		XYSeries localLineSeries = new XYSeries("localLine");

		XYSeries globalPointSeries = new XYSeries("globalPoints");
		XYSeries globalLineSeries = new XYSeries("globalLine");
		for (JFChartPainter.Point point : globalPoints) {
			globalPointSeries.add(point.x, point.y);
			globalLineSeries.add(point.x, ((LinearRegressionModel) subgroup.referenceModel()).predict(point.x));
			localLineSeries.add(point.x, ((LinearRegressionModel) subgroup.localModel()).predict(point.x));
		}
		for (JFChartPainter.Point point : localPoints) {
			localPointSeries.add(point.x, point.y);
		}

		final XYSeriesCollection pointsDataset = new XYSeriesCollection();
		pointsDataset.addSeries(globalPointSeries);
		pointsDataset.addSeries(localPointSeries);
		final XYSeriesCollection linePointsDataset = new XYSeriesCollection();
		linePointsDataset.addSeries(globalLineSeries);
		linePointsDataset.addSeries(localLineSeries);

		return painter.createPointCloudWithLines("", subgroup.targetAttributes().get(0).caption(),
				subgroup.targetAttributes().get(1).caption(), pointsDataset, linePointsDataset);
	}
}
