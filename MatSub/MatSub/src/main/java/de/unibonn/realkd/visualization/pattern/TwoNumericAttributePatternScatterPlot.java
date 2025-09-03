package de.unibonn.realkd.visualization.pattern;

import static com.google.common.collect.Lists.newArrayList;
import static de.unibonn.realkd.common.IndexSets.difference;
import static de.unibonn.realkd.visualization.JFChartPainter.DETAILED;
import static de.unibonn.realkd.visualization.JFChartPainter.PREVIEW;

import java.util.List;

import org.jfree.chart.JFreeChart;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.MetricAttribute;
import de.unibonn.realkd.patterns.LocalPatternDescriptor;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.TableSubspaceDescriptor;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.pmm.PureModelSubgroup;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

/**
 * A 2d-scatterplot that visualizes either the target attributes of a subgroup
 * pattern or all attributes that a pattern refers to (in case not a subgroup).
 * In both cases, the visualization is only applicable when there are exactly
 * two attributes considered.
 * 
 * @author Ruafang Xu
 * 
 * @since 0.0.1
 * 
 * @version 0.5.0
 *
 */
public class TwoNumericAttributePatternScatterPlot implements PatternVisualization {

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		if (!(pattern.descriptor() instanceof LocalPatternDescriptor)
				|| !(pattern.descriptor() instanceof TableSubspaceDescriptor)) {
			return false;
		}
		return checkExactlyTwoNumericAttributes(getRelevantAttributes(pattern));
	}

	private List<? extends Attribute<?>> getRelevantAttributes(Pattern<?> pattern) {
		if (pattern instanceof ExceptionalModelPattern || pattern instanceof PureModelSubgroup) {
			return ((Subgroup<?>) pattern.descriptor()).targetAttributes();
		} else {
			return ((TableSubspaceDescriptor) pattern.descriptor()).getReferencedAttributes();
		}
	}

	private boolean checkExactlyTwoNumericAttributes(List<? extends Attribute<?>> attributes) {
		if (attributes.size() != 2) {
			return false;
		}
		for (Attribute<?> attribute : attributes) {
			if (!(attribute instanceof MetricAttribute)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		List<? extends Attribute<?>> attributes = getRelevantAttributes(pattern);
		List<List<JFChartPainter.Point>> pointLists = pointLists(pattern, attributes);
		return PREVIEW.createPointCloud("", pointLists, attributes.get(0).caption(), attributes.get(1).caption());
	}

	private List<List<JFChartPainter.Point>> pointLists(Pattern<?> pattern, List<? extends Attribute<?>> attributes) {
		List<List<JFChartPainter.Point>> pointLists = newArrayList();
		IndexSet complementRows = difference(pattern.population().objectIds(),
			((LocalPatternDescriptor) pattern.descriptor()).supportSet());
		pointLists.add(JFChartPainter.createPoints(complementRows, (MetricAttribute) attributes.get(0),
				(MetricAttribute) attributes.get(1)));
		pointLists.add(JFChartPainter.createPoints(((LocalPatternDescriptor) pattern.descriptor()).supportSet(),
				(MetricAttribute) attributes.get(0), (MetricAttribute) attributes.get(1)));
		return pointLists;
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		List<? extends Attribute<?>> attributes = getRelevantAttributes(pattern);
		List<List<JFChartPainter.Point>> pointLists = pointLists(pattern, attributes);
		return DETAILED.createPointCloud("Scatter plot", pointLists, attributes.get(0).caption(), attributes.get(1).caption());
	}

}
