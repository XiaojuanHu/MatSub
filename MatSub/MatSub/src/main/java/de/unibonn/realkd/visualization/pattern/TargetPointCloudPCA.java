package de.unibonn.realkd.visualization.pattern;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.algorithms.emm.PCAEvaluator;
import de.unibonn.realkd.algorithms.emm.PCAEvaluators;
import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

public class TargetPointCloudPCA implements PatternVisualization {

	TargetPointCloudPCA() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return pattern instanceof ExceptionalModelPattern
				&& checkTargetsAtLeastTwoNumeric(((ExceptionalModelPattern) pattern).descriptor().targetAttributes());
	}

	protected boolean checkTargetsAtLeastTwoNumeric(List<? extends Attribute<?>> targetAttributes) {
		if (targetAttributes.size() < 2) {
			return false;
		}
		for (Attribute<?> attribute : targetAttributes) {
			if (!(attribute instanceof MetricAttribute)) {
				return false;
			}
		}
		return true;
	}

	private List<JFChartPainter.Point> createCloudForRows(IndexSet rows, List<? extends Attribute<?>> attributes,
			PCAEvaluator pcaEvaluator) {
		if (attributes.size() == 1) {
			return createCloud(rows, attributes.get(0), pcaEvaluator);
		} else if (attributes.size() > 1) {
			return createCloud(rows, attributes.get(0), attributes.get(1), pcaEvaluator);
		}
		return newArrayList();
	}

	private List<JFChartPainter.Point> createCloud(IndexSet rows, Attribute<?> attribute, PCAEvaluator pcaEvaluator) {
		return createCloud(rows, attribute, attribute, pcaEvaluator);
	}

	private List<JFChartPainter.Point> createCloud(IndexSet rows, Attribute<?> attribute1, Attribute<?> attribute2,
			PCAEvaluator pcaEvaluator) {
		List<JFChartPainter.Point> pointCloud = newArrayList();
		// int size = attribute1.getDataTable().getSize();
		for (Integer i : rows) {
			pointCloud.add(new JFChartPainter.Point(pcaEvaluator.getDevFirstDimension(i),
					pcaEvaluator.getDevForDimension(i, 1)));
		}
		return pointCloud;
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		Subgroup<?> subgroup = ((ExceptionalModelPattern) pattern).descriptor();
		PCAEvaluator pcaEvaluator = PCAEvaluators.newPCAEvaluator(subgroup.getTargetTable(), subgroup.targetAttributes());

		JFChartPainter painter = JFChartPainter.PREVIEW;

		List<List<JFChartPainter.Point>> pointClouds = newArrayList();
		pointClouds.add(createCloudForRows(subgroup.getTargetTable().population().objectIds(),
				subgroup.targetAttributes(), pcaEvaluator));
		pointClouds.add(createCloudForRows(subgroup.supportSet(), subgroup.targetAttributes(), pcaEvaluator));

		return painter.createPointCloud("", pointClouds, "First Comp", "Second Comp");
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		return draw(pattern);
	}

}
