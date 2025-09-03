package edu.uab.consapt.tool;

public class ChartMetaInfoBean {

	private String title;
	private String xAxisName;
	private String yAxisName;

	public ChartMetaInfoBean(String title, String xAxisName, String yAxisName) {
		this.title = title;
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
	}

	public String getTitle() {
		return title;
	}

	public String getXAxisName() {
		return xAxisName;
	}

	public String getYAxisName() {
		return yAxisName;
	}
}
