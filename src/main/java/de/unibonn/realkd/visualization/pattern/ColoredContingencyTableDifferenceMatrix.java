package de.unibonn.realkd.visualization.pattern;

import java.util.List;
import java.util.function.BiFunction;

import org.jfree.chart.JFreeChart;

import com.google.common.collect.ImmutableList;

import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.emm.ExceptionalModelPattern;
import de.unibonn.realkd.patterns.models.table.ContingencyTable;
import de.unibonn.realkd.visualization.JFChartPainter;

/**
 * Visualization applicable to EMM patterns with two target attributes. It
 * computes and displays a discrete 2-dimensional contingency table for the
 * pattern and colors each cell in that table according to the deviation of its
 * probability between the global and the local data.
 * 
 * @author Sandy Moens
 * 
 * @since 0.0.1
 * 
 * @version 0.3.0
 *
 */
public class ColoredContingencyTableDifferenceMatrix implements PatternVisualization {

	ColoredContingencyTableDifferenceMatrix() {
		;
	}

	@Override
	public boolean isApplicable(Pattern<?> pattern) {
		return ((pattern instanceof ExceptionalModelPattern)
				&& (((ExceptionalModelPattern) pattern).descriptor().targetAttributes().size() == 2)
				&& (((ExceptionalModelPattern) pattern).descriptor()
						.referenceModel() instanceof ContingencyTable));
	}

	@Override
	public JFreeChart draw(Pattern<?> pattern) {
		ExceptionalModelPattern emPattern = (ExceptionalModelPattern) pattern;
		ContingencyTable referenceModel = (ContingencyTable) emPattern.descriptor().referenceModel();
		ContingencyTable localModel = (ContingencyTable) emPattern.descriptor().localModel();

		Attribute<?> firstAttribute = emPattern.descriptor().targetAttributes().get(0);
		Attribute<?> secondAttribute = emPattern.descriptor().targetAttributes().get(1);
		List<String> xValues = localModel.dimension(0).binCaptions();
		List<String> yValues = localModel.dimension(1).binCaptions();

		JFChartPainter painter = JFChartPainter.PREVIEW;

		BiFunction<Integer, Integer, Double> values = (x, y) -> {
			ImmutableList<Integer> bins = ImmutableList.of(x, y);
			return localModel.probabilityOfBins(bins) - referenceModel.probabilityOfBins(bins);
		};
		return painter.colorMatrix("", firstAttribute.caption(), secondAttribute.caption(), values, xValues,
				yValues, false);
	}

	@Override
	public JFreeChart drawDetailed(Pattern<?> pattern) {
		ExceptionalModelPattern emPattern = (ExceptionalModelPattern) pattern;
		ContingencyTable referenceModel = (ContingencyTable) emPattern.descriptor().referenceModel();
		ContingencyTable localModel = (ContingencyTable) emPattern.descriptor().localModel();

		Attribute<?> firstAttribute = emPattern.descriptor().targetAttributes().get(0);
		Attribute<?> secondAttribute = emPattern.descriptor().targetAttributes().get(1);
		List<String> xValues = localModel.dimension(0).binCaptions();
		List<String> yValues = localModel.dimension(1).binCaptions();

		BiFunction<Integer, Integer, Double> values = (x, y) -> {
			ImmutableList<Integer> bins = ImmutableList.of(x, y);
			return localModel.probabilityOfBins(bins) - referenceModel.probabilityOfBins(bins);
		};

		return JFChartPainter.DETAILED.colorMatrix("Contingency table difference", firstAttribute.caption(), secondAttribute.caption(), values, xValues,
				yValues, true);
	}

}
