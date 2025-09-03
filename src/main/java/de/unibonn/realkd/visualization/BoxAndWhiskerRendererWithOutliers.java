package de.unibonn.realkd.visualization;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleEdge;

class BoxAndWhiskerRendererWithOutliers extends BoxAndWhiskerRenderer {

	private static final long serialVersionUID = 5333532066659110976L;

	@Override
	public void drawHorizontalItem(Graphics2D g2,
			CategoryItemRendererState state, Rectangle2D dataArea,
			CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis,
			CategoryDataset dataset, int row, int column) {

		BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset) dataset;

		double categoryEnd = domainAxis.getCategoryEnd(column,
				getColumnCount(), dataArea, plot.getDomainAxisEdge());
		double categoryStart = domainAxis.getCategoryStart(column,
				getColumnCount(), dataArea, plot.getDomainAxisEdge());
		double categoryWidth = Math.abs(categoryEnd - categoryStart);

		double yy = categoryStart;
		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();

		if (seriesCount > 1) {
			double seriesGap = dataArea.getHeight() * getItemMargin()
					/ (categoryCount * (seriesCount - 1));
			double usedWidth = (state.getBarWidth() * seriesCount)
					+ (seriesGap * (seriesCount - 1));

			double offset = (categoryWidth - usedWidth) / 2;
			yy = yy + offset + (row * (state.getBarWidth() + seriesGap));
		} else {

			double offset = (categoryWidth - state.getBarWidth()) / 2;
			yy = yy + offset;
		}

		g2.setPaint(getItemPaint(row, column));
		Stroke s = getItemStroke(row, column);
		g2.setStroke(s);

		RectangleEdge location = plot.getRangeAxisEdge();

		Number xQ1 = bawDataset.getQ1Value(row, column);
		Number xQ3 = bawDataset.getQ3Value(row, column);
		Number xMax = bawDataset.getMaxRegularValue(row, column);
		Number xMin = bawDataset.getMinRegularValue(row, column);

		Shape box = null;
		if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {

			double xxQ1 = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea,
					location);
			double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea,
					location);
			double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(),
					dataArea, location);
			double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(),
					dataArea, location);
			double yymid = yy + state.getBarWidth() / 2.0;

			// draw the box...
			box = new Rectangle2D.Double(Math.min(xxQ1, xxQ3), yy,
					Math.abs(xxQ1 - xxQ3), state.getBarWidth());
			if (this.getFillBox()) {
				g2.fill(box);
			}

			Paint outlinePaint = getItemOutlinePaint(row, column);
			if (this.getUseOutlinePaintForWhiskers()) {
				g2.setPaint(outlinePaint);
			}

			// draw the upper shadow...
			g2.draw(new Line2D.Double(xxMax, yymid, xxQ3, yymid));
			g2.draw(new Line2D.Double(xxMax, yy, xxMax, yy
					+ state.getBarWidth()));

			// draw the lower shadow...
			g2.draw(new Line2D.Double(xxMin, yymid, xxQ1, yymid));
			g2.draw(new Line2D.Double(xxMin, yy, xxMin, yy
					+ state.getBarWidth()));

			g2.setStroke(getItemOutlineStroke(row, column));
			g2.setPaint(outlinePaint);
			g2.draw(box);
		}
		// draw mean - SPECIAL AIMS REQUIREMENT...
		g2.setPaint(this.getArtifactPaint());
		double aRadius; // average radius
		if (this.isMeanVisible()) {
			Number xMean = bawDataset.getMeanValue(row, column);
			if (xMean != null) {
				double xxMean = rangeAxis.valueToJava2D(xMean.doubleValue(),
						dataArea, location);
				aRadius = state.getBarWidth() / 4;
				// here we check that the average marker will in fact be
				// visible before drawing it...
				if ((xxMean > (dataArea.getMinX() - aRadius))
						&& (xxMean < (dataArea.getMaxX() + aRadius))) {
					Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMean
							- aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
					g2.fill(avgEllipse);
					g2.draw(avgEllipse);
				}
			}
		}

		// draw median...
		if (this.isMedianVisible()) {
			Number xMedian = bawDataset.getMedianValue(row, column);
			if (xMedian != null) {
				double xxMedian = rangeAxis.valueToJava2D(
						xMedian.doubleValue(), dataArea, location);
				g2.draw(new Line2D.Double(xxMedian, yy, xxMedian, yy
						+ state.getBarWidth()));
			}
		}

		// collect entity and tool tip information...
		if (state.getInfo() != null && box != null) {
			EntityCollection entities = state.getEntityCollection();
			if (entities != null) {
				addItemEntity(entities, dataset, row, column, box);
			}
		}

		drawMinMax(g2, state, dataArea, rangeAxis, row, column, bawDataset, yy,
				location);
	}

	private void drawMinMax(Graphics2D g2, CategoryItemRendererState state,
			Rectangle2D dataArea, ValueAxis rangeAxis, int row, int column,
			BoxAndWhiskerCategoryDataset bawDataset, double yy,
			RectangleEdge location) {
		g2.setPaint(getItemPaint(row, column));
		double aRadius = state.getBarWidth() / 4;
		Number min = BoxPlotUtils.getMinOutlier(bawDataset, row, column);
		Number max = BoxPlotUtils.getMaxOutlier(bawDataset, row, column);

		if (min != null) {
			double xxMin = rangeAxis.valueToJava2D(min.doubleValue(), dataArea,
					location);
			if ((xxMin > (dataArea.getMinX() - aRadius))
					&& (xxMin < (dataArea.getMaxX() + aRadius))) {
				Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMin
						- aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
				g2.fill(avgEllipse);
				g2.draw(avgEllipse);
			}
		}
		if (max != null) {
			double xxMax = rangeAxis.valueToJava2D(max.doubleValue(), dataArea,
					location);
			if ((xxMax > (dataArea.getMinX() - aRadius))
					&& (xxMax < (dataArea.getMaxX() + aRadius))) {
				Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMax
						- aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
				g2.fill(avgEllipse);
				g2.draw(avgEllipse);
			}
		}
	}
}