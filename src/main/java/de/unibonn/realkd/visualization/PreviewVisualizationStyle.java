package de.unibonn.realkd.visualization;

import static de.unibonn.realkd.common.base.Lazy.lazy;

import java.awt.Color;
import java.awt.Font;

import de.unibonn.realkd.common.base.Lazy;

public class PreviewVisualizationStyle implements VisualizationStyle {

	private static final Lazy<VisualizationStyle> INSTANCE = lazy(() -> new PreviewVisualizationStyle());

	public static final VisualizationStyle get() {
		return INSTANCE.get();
	}

	// declared style constants
	private final int backgroundAlpha = 0;
	private final boolean borderVisible = false;
	// font style
	private final Font cellFont = new Font("Arial", Font.PLAIN, 10);
	private final Font textAnnotation = new Font("Arial", Font.PLAIN, 10);
	private final Font itemFont = new Font("Arial", Font.BOLD, 8);
	private final Font legendFont = new Font("Arial", Font.BOLD, 10);
	private final Font tickLabelFont = new Font("Arial", Font.PLAIN, 8);
	private final Font labelFont = new Font("Arial", Font.PLAIN, 10);
	// paint style
	private final Color tickLabelPaint = Color.black;
	private final Color axisTickLabelPaint = Color.black;
	private final Color domainGridLinePaint = Color.black;
	private final Color itemPaint = Color.white;
	private final Color BACKGROUND_COLOR = Color.white;
	private final Color SUBPOPULATION_COLOR = new Color(255, 99, 71);
	private final Color GLOBAL_POPULATION_COLOR = new Color(71, 99, 255);
	private final Color SUBPOPULATION_COMPLEMENT_COLOR = new Color(155, 155, 155);

	private PreviewVisualizationStyle() {
		; // not to be instantiated
	}

	@Override
	public Color subPopulationComplementColor() {
		return SUBPOPULATION_COMPLEMENT_COLOR;
	}

	@Override
	public Color globalPopulationColor() {
		return GLOBAL_POPULATION_COLOR;
	}

	@Override
	public Color subPopulationColor() {
		return SUBPOPULATION_COLOR;
	}

	@Override
	public Color backgroundColor() {
		return BACKGROUND_COLOR;
	}

	@Override
	public Color itemPaint() {
		return itemPaint;
	}

	@Override
	public Color domainGridLinePaint() {
		return domainGridLinePaint;
	}

	@Override
	public Color axisTickLabelPaint() {
		return axisTickLabelPaint;
	}

	@Override
	public Color tickLabelPaint() {
		return tickLabelPaint;
	}

	@Override
	public Font axisLabelFont() {
		return labelFont;
	}

	@Override
	public Font tickLabelFont() {
		return tickLabelFont;
	}

	@Override
	public Font legendFont() {
		return legendFont;
	}

	@Override
	public Font itemFont() {
		return itemFont;
	}

	@Override
	public Font textAnnotation() {
		return textAnnotation;
	}

	@Override
	public Font cellFont() {
		return cellFont;
	}

	@Override
	public boolean borderVisible() {
		return borderVisible;
	}

	@Override
	public int backgroundAlpha() {
		return backgroundAlpha;
	}

}
