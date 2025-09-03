package de.unibonn.realkd.visualization.pattern;

import java.util.List;
import java.util.function.BiFunction;

import org.jfree.chart.JFreeChart;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.patterns.subgroups.Subgroup;
import de.unibonn.realkd.visualization.JFChartPainter;

/**
 * Visualization applicable to all patterns with subgroup descriptor and two
 * target attributes locally modeled by a contingency table.
 * 
 * @author Mario Boley
 * 
 * @since 0.3.0
 * 
 * @version 0.3.0
 *
 */
public class ColoredLocalContingencyTableMatrix implements PatternVisualization {

	ColoredLocalContingencyTableMatrix() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return ((pattern.descriptor() instanceof Subgroup)
				&& (((Subgroup<?>) pattern.descriptor()).targetAttributes().size() == 2)
				&& (((Subgroup<?>) pattern.descriptor()).localModel() instanceof ContingencyTable));
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		ContingencyTable localModel = (ContingencyTable) subgroup.localModel();

		Attribute<?> firstAttribute = subgroup.targetAttributes().get(0);
		Attribute<?> secondAttribute = subgroup.targetAttributes().get(1);
		List<String> xValues = localModel.dimension(0).binCaptions();
		List<String> yValues = localModel.dimension(1).binCaptions();

		JFChartPainter painter = JFChartPainter.PREVIEW;

		BiFunction<Integer, Integer, Double> values = (x, y) -> {
			ImmutableList<Integer> bins = ImmutableList.of(x, y);
			return localModel.probabilityOfBins(bins);
		};
		return painter.colorMatrix("", firstAttribute.caption(), secondAttribute.caption(), values, xValues, yValues, false);
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		Subgroup<?> subgroup = (Subgroup<?>) pattern.descriptor();
		ContingencyTable localModel = (ContingencyTable) subgroup.localModel();

		Attribute<?> firstAttribute = subgroup.targetAttributes().get(0);
		Attribute<?> secondAttribute = subgroup.targetAttributes().get(1);
		List<String> xValues = localModel.dimension(0).binCaptions();
		List<String> yValues = localModel.dimension(1).binCaptions();

		BiFunction<Integer, Integer, Double> values = (x, y) -> {
			ImmutableList<Integer> bins = ImmutableList.of(x, y);
			return localModel.probabilityOfBins(bins);
		};

		return JFChartPainter.DETAILED.colorMatrix("Local contingency table", firstAttribute.caption(),
				secondAttribute.caption(), values, xValues, yValues, true);
	}

}
