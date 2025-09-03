package de.unibonn.realkd.visualization;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.AbstractPieLabelDistributor;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import de.unibonn.realkd.common.IndexSet;
import de.unibonn.realkd.data.table.attribute.Attribute;

public class JFChartPainter {

	public static final JFChartPainter PREVIEW = new JFChartPainter(PreviewVisualizationStyle.get());

	public static final JFChartPainter DETAILED = new JFChartPainter(DetailedVisualizationStyle.get());

	// private static final String SERVLET_CHART_URL_PREFIX =
	// "/servlet/DisplayChart?filename=";
	// private static final int TRANSPARENT = 0;
	// call standard style instance
	private final VisualizationStyle style;

	private JFChartPainter(VisualizationStyle style) {
		this.style = style;
	}

	private void formatJFreeChart(JFreeChart chart) {
		chart.setBackgroundPaint(null);
		chart.setBorderPaint(null);
		chart.setBorderVisible(style.borderVisible());
		chart.getPlot().setBackgroundPaint(style.backgroundColor());
		chart.getPlot().setBackgroundAlpha(style.backgroundAlpha());
		chart.getPlot().setOutlinePaint(null);
	}

	private void formatAxis(Axis axis) {
		axis.setTickLabelFont(style.tickLabelFont());
		axis.setLabelFont(style.axisLabelFont());
		axis.setTickLabelPaint(style.tickLabelPaint());
		axis.setLabelPaint(style.axisTickLabelPaint());

		if (axis instanceof NumberAxis) {
			((NumberAxis) axis).setNumberFormatOverride(new ReducedEnglishDecimalFormat());
		}
	}

	public JFreeChart colorMatrix(String title, String xLabel, String yLabel,
			BiFunction<Integer, Integer, Double> values, List<String> columnNames, List<String> rowNames,
			boolean detailed) {
		float leftPadding = 0.3f;
		float bottomPadding = 0.3f;
		int numberOfCols = columnNames.size();
		int numberOfRows = rowNames.size();
		JFreeChart chart = ChartFactory.createXYLineChart(title, "", "", new XYSeriesCollection(new XYSeries("")),
				PlotOrientation.VERTICAL, false, true, false);
		formatJFreeChart(chart);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.getDomainAxis().setRange(0, numberOfCols + leftPadding);
		plot.getDomainAxis().setVisible(false);
		plot.getRangeAxis().setRange(0, numberOfRows + bottomPadding);
		plot.getRangeAxis().setVisible(false);
		// add cells
		addContingencyTableCells(plot, values, numberOfCols, numberOfRows, numberOfCols, leftPadding, bottomPadding,
				detailed);

		Font attributeFont = style.axisLabelFont();

		// add first attribute to bottom
		XYTextAnnotation a1 = new XYTextAnnotation(xLabel, leftPadding + numberOfCols / 2.0, 0.1);
		if (detailed) {
			a1.setFont(attributeFont);
		}
		plot.addAnnotation(a1);
		// add second attribute to left
		XYTextAnnotation a2 = new XYTextAnnotation(yLabel, 0.1, bottomPadding + numberOfRows / 2.0);
		a2.setRotationAngle(-Math.PI / 2);
		if (detailed) {
			a2.setFont(attributeFont);
		}
		plot.addAnnotation(a2);
		if (detailed) {
			addContingencyTableDetails(plot, columnNames, "horizontal", leftPadding);
			addContingencyTableDetails(plot, rowNames, "vertical", bottomPadding);
		}

		return chart;

		// ChartRenderingInfo info = new ChartRenderingInfo();
		// return formatHtmlURL(session, chart, info);
	}

	private void addContingencyTableCells(XYPlot plot, BiFunction<Integer, Integer, Double> values, int columns,
			int rows, int numberOfCols, float leftPadding, float bottomPadding, boolean detailed) {
		int x = 0;
		int y = 0;
		int precision = 2;
		// Font cellFont = new Font("Arial", Font.PLAIN, 10);
		if (detailed) {
			precision = 4;
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				double difference = values.apply(j, i);
				Color color = getColor((float) difference);
				plot.addAnnotation(
						new XYShapeAnnotation(new Rectangle2D.Float(x + leftPadding, y + bottomPadding, 1f, 1f),
								new BasicStroke(), color, color));
				XYTextAnnotation a = new XYTextAnnotation(
						String.format(Locale.ENGLISH, "%." + precision + "f", difference), 0.5 + x + leftPadding,
						0.5 + y + bottomPadding);
				a.setFont(style.cellFont());
				plot.addAnnotation(a);
				x += 1;
				if (x % numberOfCols == 0) {
					y += 1;
					x = 0;
				}
			}
		}
	}

	/*
	 * adds detailed categories
	 */
	private void addContingencyTableDetails(XYPlot plot, List<String> bins, String direction, float padding) {
		double i = 0.5;
		if (direction != "vertical" && direction != "horizontal") {
			try {
				throw new Exception("direction argument should be either 'horizontal' or 'vertical'");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (String category : bins) {
			XYTextAnnotation a = null;
			if (direction == "vertical") {
				a = new XYTextAnnotation(category, 0.2, padding + i);
				a.setRotationAngle(-Math.PI / 2);
			} else if (direction == "horizontal") {
				a = new XYTextAnnotation(category, padding + i, 0.2);
			}
			// a.setFont(new Font("Arial", Font.PLAIN, 12));
			a.setFont(style.textAnnotation());
			plot.addAnnotation(a);
			i++;
		}
	}

	public JFreeChart createPointCloudWithLines(String title, String xAxisTitle, String yAxisTitle,
			XYSeriesCollection pointsDataset, XYSeriesCollection linePointsDataset) {
		final JFreeChart chart = ChartFactory.createScatterPlot(title, "", "", null, PlotOrientation.VERTICAL, false,
				true, false);
		formatJFreeChart(chart);

		XYPlot cloudPlot = (XYPlot) chart.getPlot();
		cloudPlot.setDataset(0, linePointsDataset);
		cloudPlot.setDataset(1, pointsDataset);
		cloudPlot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

		ValueAxis domainAxis = cloudPlot.getDomainAxis();
		domainAxis.setLabel(xAxisTitle);
		formatAxis(domainAxis);

		ValueAxis rangeAxis = cloudPlot.getRangeAxis();
		rangeAxis.setLabel(yAxisTitle);
		formatAxis(rangeAxis);

		XYItemRenderer pointsRenderer = cloudPlot.getRenderer();
		pointsRenderer.setSeriesPaint(0, style.globalPopulationColor());
		pointsRenderer.setSeriesPaint(1, style.subPopulationColor());
		double size = 2.0;
		double delta = size / 2.0;
		Shape circle = new Ellipse2D.Double(-delta, -delta, size, size);
		Shape rectangle = new Rectangle2D.Double(-delta, -delta, size, size);
		pointsRenderer.setSeriesShape(0, circle);
		pointsRenderer.setSeriesShape(1, rectangle);
		cloudPlot.setRenderer(1, pointsRenderer);

		final XYLineAndShapeRenderer linePointsRenderer = new XYLineAndShapeRenderer();
		linePointsRenderer.setSeriesPaint(0, Color.black);
		linePointsRenderer.setSeriesLinesVisible(0, true);
		linePointsRenderer.setSeriesShapesVisible(0, false);

		linePointsRenderer.setSeriesPaint(1, Color.green);
		linePointsRenderer.setSeriesLinesVisible(1, true);
		linePointsRenderer.setSeriesShapesVisible(1, false);
		cloudPlot.setRenderer(0, linePointsRenderer);

		return chart;
	}

	public JFreeChart createPointCloud(String title, List<List<Point>> pointClouds, String xAxisTitle,
			String yAxisTitle) {
		XYSeriesCollection cloud = new XYSeriesCollection();
		int i = 0;
		List<Double> meansX = newArrayList(), minsX = newArrayList(), maxsX = newArrayList(), meansY = newArrayList(),
				minsY = newArrayList(), maxsY = newArrayList();
		for (List<Point> pointCloud : pointClouds) {
			i++;
			String seriesName = "cloud" + i;
			XYSeries pointCloudAsSeries = new XYSeries(seriesName);
			for (Point point : pointCloud) {
				pointCloudAsSeries.add(point.x, point.y);
			}
			cloud.addSeries(pointCloudAsSeries);

			double meanx = 0, meany = 0, minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minY = Double.MAX_VALUE,
					maxY = -Double.MAX_VALUE;
			for (Point point : pointCloud) {
				meanx += point.x;
				minX = Math.min(minX, point.x);
				maxX = Math.max(maxX, point.x);
				meany += point.y;
				minY = Math.min(minY, point.y);
				maxY = Math.max(maxY, point.y);
			}
			meansX.add(meanx / pointCloud.size());
			minsX.add(minX);
			maxsX.add(maxX);
			meansY.add(meany / pointCloud.size());
			minsY.add(minY);
			maxsY.add(maxY);
		}

		final JFreeChart chart = ChartFactory.createScatterPlot(title, "", "", cloud, PlotOrientation.VERTICAL, false,
				true, false);
		formatJFreeChart(chart);

		XYPlot cloudPlot = (XYPlot) chart.getPlot();
		cloudPlot.getRenderer(0).setSeriesPaint(0, style.globalPopulationColor());
		cloudPlot.getRenderer(0).setSeriesPaint(1, style.subPopulationColor());
		cloudPlot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

		cloudPlot.addAnnotation(new XYLineAnnotation(meansX.get(0), minsY.get(0), meansX.get(0), maxsY.get(0),
				new BasicStroke(), Color.orange));
		cloudPlot.addAnnotation(new XYLineAnnotation(minsX.get(0), meansY.get(0), maxsX.get(0), meansY.get(0),
				new BasicStroke(), Color.orange));

		ValueAxis domainAxis = cloudPlot.getDomainAxis();
		domainAxis.setLabel(xAxisTitle);
		formatAxis(domainAxis);

		ValueAxis rangeAxis = cloudPlot.getRangeAxis();
		rangeAxis.setLabel(yAxisTitle);
		formatAxis(rangeAxis);

		XYItemRenderer renderer = cloudPlot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		double size = 2.0;
		double delta = size / 2.0;
		Shape circle = new Ellipse2D.Double(-delta, -delta, size, size);
		Shape rectangle = new Rectangle2D.Double(-delta, -delta, size, size);
		renderer.setSeriesShape(0, circle);
		renderer.setSeriesShape(1, rectangle);

		return chart;
	}

	/**
	 * 
	 * @param title
	 *            the title of the chart
	 * @param items
	 *            the names of the items in the chart
	 * @param values
	 *            array of values the length of which must match length of items
	 * @param isPatternView
	 * @param colors
	 *            array of colors of length less or equal than length of
	 *            items/values (this way a prefix of the items can be assigned
	 *            specific colors)
	 * @return
	 */
	public JFreeChart createPieChart(String title, String[] items, double[] values, boolean isPatternView,
			Color[] colors) {

		if (items.length != values.length) {
			throw new IllegalArgumentException("values length (" + String.valueOf(values.length)
					+ ") does not match items length (" + String.valueOf(items.length) + ")");
		}

		DefaultPieDataset dataset = new DefaultPieDataset();
		for (int i = 0; i < items.length; i++) {
			dataset.setValue(items[i], values[i]);
		}
		JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, false, true);
		formatJFreeChart(chart);

		PiePlot piePlot = (PiePlot) chart.getPlot();
		piePlot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));

		for (int i = 0; i < colors.length; i++) {
			piePlot.setSectionPaint(items[i], colors[i]);
		}

		LegendTitle legend = chart.getLegend();
		legend.setMargin(0, 0, 0, 0);

		Color transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f);
		piePlot.setLabelOutlinePaint(transparent);
		piePlot.setLabelBackgroundPaint(transparent);
		piePlot.setLabelShadowPaint(transparent);

		formatJFreeChart(chart);

		if (isPatternView == true) {
			// legend.setPosition(RectangleEdge.TOP);
			// legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
			// legend.setBackgroundPaint(Color.WHITE);
			// piePlot.setLegendItemShape(new Rectangle(7, 7));
			// legend.setItemFont((new Font("Arial", Font.PLAIN, font)));
			legend.visible = false;
			piePlot.setSimpleLabels(true);

			piePlot.setLabelDistributor(new AbstractPieLabelDistributor() {

				private static final long serialVersionUID = -4019237079417959318L;

				@Override
				public int getItemCount() {
					return 1;
				}

				public void distributeLabels(double minY, double height) {
				}
			});
			// piePlot.setLabelFont(new Font("Arial", Font.PLAIN, font));
			piePlot.setLabelFont(style.axisLabelFont());
			piePlot.setLabelGap(-0.3);
			piePlot.setMaximumLabelWidth(0.3);
		} else if (isPatternView == false) {
			// legend.setItemPaint(Color.LIGHT_GRAY);
			// ......legend item paint changed to black
			legend.setItemPaint(PreviewVisualizationStyle.get().itemPaint());
			legend.setPosition(RectangleEdge.BOTTOM);
			legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
			legend.setBackgroundPaint(null);
			// legend.setItemFont((new Font("Arial", Font.PLAIN, 7)));
			legend.setItemFont(style.legendFont());
			piePlot.setLegendItemShape(new Rectangle(16, 9));
			piePlot.setLabelGenerator(null);
		}

		piePlot.setLabelLinksVisible(false);
		piePlot.setBackgroundAlpha(style.backgroundAlpha());
		piePlot.setOutlinePaint(null);
		piePlot.setShadowPaint(null);

		return chart;
	}

	public JFreeChart createBoxPlotChart(String title, DefaultBoxAndWhiskerCategoryDataset boxDataset,
			String valueAxisLabel) {

		String categoryAxisLabel = "";
		boolean legend = false;

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, categoryAxisLabel, valueAxisLabel, boxDataset,
				legend);
		formatJFreeChart(chart);

		CategoryPlot categoryplot = chart.getCategoryPlot();
		categoryplot.setOrientation(PlotOrientation.HORIZONTAL);
		categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		CategoryAxis domainAxis = categoryplot.getDomainAxis();
		domainAxis.setTickLabelsVisible(true);
		domainAxis.setMaximumCategoryLabelLines(1);
		domainAxis.setMaximumCategoryLabelWidthRatio((float) 0.3);
		formatAxis(domainAxis);

		ValueAxis rangeAxis = categoryplot.getRangeAxis();

		double min = BoxPlotUtils.getMinRange(boxDataset, boxDataset.getRowCount() - 1, 0);
		double max = BoxPlotUtils.getMaxRange(boxDataset, boxDataset.getRowCount() - 1, 0);
		double diff = Math.abs(max - min);
		double minRange = (diff != 0.0) ? min - diff * 0.1 : min - 0.1;
		double maxRange = (diff != 0.0) ? max + diff * 0.1 : max + 0.1;
		((NumberAxis) rangeAxis).setRange(minRange, maxRange);
		formatAxis(rangeAxis);

		BoxAndWhiskerRenderer boxandwhiskerrender = new BoxAndWhiskerRendererWithOutliers();
		categoryplot.setRenderer(boxandwhiskerrender);
		boxandwhiskerrender.setMaximumBarWidth(0.2);

		if (boxDataset.getRowCount() == 2) {
			boxandwhiskerrender.setSeriesPaint(0, style.subPopulationColor());
			boxandwhiskerrender.setSeriesPaint(1, style.globalPopulationColor());
		} else {
			boxandwhiskerrender.setSeriesPaint(0, style.globalPopulationColor());
			domainAxis.setLabelPaint(Color.white);
			domainAxis.setTickLabelPaint(Color.white);
			rangeAxis.setLabelPaint(Color.white);
			rangeAxis.setTickLabelPaint(Color.white);
		}

		boxandwhiskerrender.setMeanVisible(true);
		boxandwhiskerrender.setMedianVisible(true);

		return chart;
	}

	public JFreeChart createHistogramPlot(String title, List<Double> values) {

		final int bins = 9;
		double[] value = new double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			value[i] = values.get(i);
		}

		HistogramDataset histogramDataset = new HistogramDataset();
		histogramDataset.addSeries("key", value, bins);

		String xAxisLabel = "";
		String yAxisLabel = "";
		boolean legend = false;
		boolean tooltips = false;
		boolean urls = false;

		JFreeChart chart = ChartFactory.createHistogram(title, xAxisLabel, yAxisLabel, histogramDataset,
				PlotOrientation.VERTICAL, legend, tooltips, urls);
		formatJFreeChart(chart);

		XYPlot xyplot = chart.getXYPlot();
		xyplot.getRenderer(0).setSeriesPaint(0, style.subPopulationColor());
		xyplot.getRenderer(0).setSeriesPaint(1, style.globalPopulationColor());

		// xyplot.setBackgroundAlpha(uiStyle.TRANSPARENT);
		ValueAxis domainAxis = xyplot.getDomainAxis();
		formatAxis(domainAxis);

		ValueAxis rangeAxis = xyplot.getRangeAxis();
		formatAxis(rangeAxis);

		// test!!
		domainAxis.setLabelPaint(Color.white);
		domainAxis.setTickLabelPaint(Color.white);
		rangeAxis.setLabelPaint(Color.white);
		rangeAxis.setTickLabelPaint(Color.white);

		return chart;

		// ChartRenderingInfo info = new ChartRenderingInfo();
		//
		// return formatHtmlURL(session, chart, info);
	}

	public static class Series {
		public String label;
		public List<? extends Object> columns;
		public List<Double> values;

		private Series(String label, List<? extends Object> columns, List<Double> values) {
			this.label = label;
			this.columns = columns;
			this.values = values;
		}
	}

	public static CategoryDataset categoryDataset(String[] rows, String[] columns, double[] values) {
		if (rows.length != columns.length || rows.length != values.length) {
			throw new IllegalArgumentException("array lengths do not match");
		}
		DefaultCategoryDataset categorydataset = new DefaultCategoryDataset();

		for (int i = 0; i < values.length; i++) {
			categorydataset.addValue(values[i], rows[i], columns[i]);
		}
		return categorydataset;
	}

	public static Series series(String label, List<? extends Object> columns, List<Double> values) {
		return new Series(label, columns, values);
	}

	/**
	 * Converts a set of series (rows) into a category dataset. Treatment of
	 * incompatible columns (i.e., some series specify columns that others do not)
	 * depends on JFreeChart.
	 *
	 */
	public static CategoryDataset categoryDataset(Series... series) {
		DefaultCategoryDataset categorydataset = new DefaultCategoryDataset();
		for (Series s : series) {
			for (int i = 0; i < s.columns.size(); i++) {
				categorydataset.addValue(s.values.get(i), s.label, s.columns.get(i).toString());
			}
		}
		return categorydataset;
	}

	public JFreeChart createLayeredBarChart(String title, CategoryDataset categorydataset) {
		String value = "";
		String categ = "";
		boolean hasLegend = false;
		boolean hasTooltip = false;
		boolean url = true;
		JFreeChart chart = ChartFactory.createBarChart(title, categ, value, categorydataset, PlotOrientation.VERTICAL,
				hasLegend, hasTooltip, url);
		formatJFreeChart(chart);

		CategoryPlot categoryplot = chart.getCategoryPlot();
		categoryplot.setDomainGridlinePaint(style.domainGridLinePaint());
		categoryplot.setRowRenderingOrder(SortOrder.DESCENDING);

		CategoryAxis domainAxis = categoryplot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		domainAxis.setMaximumCategoryLabelWidthRatio((float) 0.7);
		domainAxis.setTickLabelsVisible(true);
		formatAxis(domainAxis);

		/*
		 * for (int i = 0; i < columns.length; i++) { if (i != 0 && i != columns.length
		 * / 2 && i != columns.length - 1 && i != columns.length / 2 - 1) {
		 * domainAxis.setTickLabelPaint(columns[i], new Color(0, 0, 0, Color.OPAQUE)); }
		 * }
		 */

		ValueAxis rangeAxis = categoryplot.getRangeAxis();
		formatAxis(rangeAxis);

		LayeredBarRenderer layeredbarrenderer = new LayeredBarRenderer();
		categoryplot.setRenderer(layeredbarrenderer);
		layeredbarrenderer.setSeriesPaint(0, style.subPopulationColor());
		layeredbarrenderer.setSeriesPaint(1, style.globalPopulationColor());

		return chart;

		// ChartRenderingInfo info = new ChartRenderingInfo();
		//
		// return formatHtmlURL(session, chart, info);
	}

	public JFreeChart stackedBarChart(String title, CategoryDataset dataset, String rangeAxisLabel,
			int... benchmarkSeries) {
		String domainAxisLabel = "";
		boolean hasLegend = false;
		boolean hasTooltip = false;
		boolean url = false;

		JFreeChart chart = ChartFactory.createStackedBarChart(title, domainAxisLabel, rangeAxisLabel, dataset,
				PlotOrientation.HORIZONTAL, hasLegend, hasTooltip, url);
		formatJFreeChart(chart);

		CategoryPlot categoryplot = chart.getCategoryPlot();

		categoryplot.setDomainGridlinesVisible(false);

		categoryplot.setRowRenderingOrder(SortOrder.ASCENDING);
		categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis categoryAxis = categoryplot.getDomainAxis();
		// categoryAxis.setTickLabelsVisible(false);
		categoryAxis.setVisible(false);
		// categoryAxis.setLabel("log prob."); //seems to be ignored
		//
		// CategoryAxis domainAxis = categoryplot.getDomainAxis();
		// domainAxis.setLowerMargin(0.15);
		// formatAxis(domainAxis);

		ValueAxis rangeAxis = categoryplot.getRangeAxis();
		formatAxis(rangeAxis);
		// columns
		for (Object string : dataset.getColumnKeys()) {
			CategoryTextAnnotation categorytextannotation = new CategoryTextAnnotation(string.toString(),
					(Comparable<?>) string, rangeAxis.getRange().getUpperBound());
			categorytextannotation.setFont(style.textAnnotation());
			categorytextannotation.setTextAnchor(TextAnchor.TOP_RIGHT);
			categorytextannotation.setCategoryAnchor(CategoryAnchor.START);
			categoryplot.addAnnotation(categorytextannotation);
		}

		StackedBarRenderer renderer = (StackedBarRenderer) categoryplot.getRenderer();
		renderer.setDrawBarOutline(true);
		renderer.setBarPainter(new StandardBarPainter());
		// renderer.setSeriesOutlinePaint(0, Color.gray);
		for (int i = 0; i < dataset.getRowCount(); i++) {
			renderer.setSeriesOutlinePaint(i, style.outlineColor());
			renderer.setSeriesPaint(i, style.defaultQuantityColor());
		}

		for (int seriesIndex : benchmarkSeries) {
			renderer.setSeriesPaint(seriesIndex, style.benchmarkQuantityColor());
			renderer.setSeriesOutlinePaint(seriesIndex, style.outlineColor());
		}

		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setMaximumBarWidth(0.25);

		return chart;
	}

	// private String formatHtmlURL(HttpSession session, JFreeChart chart,
	// ChartRenderingInfo info) throws IOException {
	// String result;
	// result = ServletUtilities.saveChartAsPNG(chart, width, height, info,
	// session);
	// result = session.getServletContext().getContextPath()
	// + SERVLET_CHART_URL_PREFIX + result;
	// return result;
	// }

	// public String createNoPic(HttpSession session) {
	// return session.getServletContext().getContextPath()
	// + SERVLET_CHART_URL_PREFIX + "nopic.jpg";
	// }

	/*
	 * from red to blue
	 */
	private Color getColor(float diff) {
		float rLow = 1f, gLow = 1f, bLow = 1f;
		float rHigh = 1f, gHigh = 0f, bHigh = 0f;
		float vLow = 0, vHigh = 1;
		if (diff > 0f) {
			rHigh = 0f;
			gHigh = 0f;
			bHigh = 1f;
		}
		float x = (float) Math.sqrt(Math.abs(diff));

		float prop = (x - vLow) / (vHigh - vLow);

		float rVal = rLow + prop * (rHigh - rLow);
		float gVal = gLow + prop * (gHigh - gLow);
		float bVal = bLow + prop * (bHigh - bLow);

		return new Color(rVal, gVal, bVal);
	}

	public JFreeChart functionsPlot(String title, List<Function<Double, Double>> functions, double min, double max,
			String xAxisLabel) {
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		int i = 0;
		for (Function<Double, Double> function : functions) {
			Function2D density = x -> function.apply(x);
			XYDataset dataset = DatasetUtilities.sampleFunction2D(density, min, max, 50, "Normal" + i++);
			seriesCollection.addSeries(((XYSeriesCollection) dataset).getSeries(0));
		}
		final JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, "", seriesCollection,
				PlotOrientation.VERTICAL, false, true, false);

		formatJFreeChart(chart);

		XYPlot normalPlot = (XYPlot) chart.getPlot();
		normalPlot.getRenderer().setSeriesPaint(1, PreviewVisualizationStyle.get().subPopulationColor());
		normalPlot.getRenderer().setSeriesPaint(0, PreviewVisualizationStyle.get().globalPopulationColor());

		NumberAxis domainAxis = (NumberAxis) normalPlot.getDomainAxis();
		formatAxis(domainAxis);

		NumberAxis rangeAxis = (NumberAxis) normalPlot.getRangeAxis();
		formatAxis(rangeAxis);

		return chart;
	}

	/**
	 * Creates a list of Points for all pairs of attribute values for indices in
	 * rows where both attribute values are present.
	 */
	public static List<Point> createPoints(IndexSet rows, Attribute<Double> attribute1, Attribute<Double> attribute2) {
		List<Point> pointCloud = newArrayList();
		for (Integer row : rows) {
			if (attribute1.valueMissing(row) || attribute2.valueMissing(row)) {
				continue;
			}
			pointCloud.add(new Point(attribute1.value(row), attribute2.value(row)));
		}
		return pointCloud;
	}

	@SuppressWarnings("serial")
	public class DataArrayLengthException extends Exception {

		public DataArrayLengthException(String details) {
			super(details);
		}
	}

	public static class Point {
		public double x;
		public double y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}