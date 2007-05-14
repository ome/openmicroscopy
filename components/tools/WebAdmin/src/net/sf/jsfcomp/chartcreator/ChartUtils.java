/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jsfcomp.chartcreator;

import java.awt.Color;
import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.SignalsDataset;
import org.jfree.data.xy.WindDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Cagatay Civici
 */
public class ChartUtils {
	
	private static String passthruImgAttributes[] = {
		"alt",
		"styleClass",
		"onclick",
		"ondblclick",
		"onmousedown",
		"onmouseup",
		"onmouseover",
		"onmousemove",
		"onmouseout",
		"onkeypress",
		"onkeydown",
		"onkeyup",
		"usemap",
    };
	
	public static void renderPassThruImgAttributes(ResponseWriter writer, UIComponent component) throws IOException{
		for(int i = 0 ; i < passthruImgAttributes.length ; i++) {
			Object value = component.getAttributes().get(passthruImgAttributes[i]);
			if(value != null) {
				writer.writeAttribute(passthruImgAttributes[i], value, null);
			}
		}
		//title attribute overlaps with the chart title so renamed to imgTitle to define img tag's title  
		if(component.getAttributes().get("imgTitle") != null) 
			writer.writeAttribute("title", component.getAttributes().get("imgTitle"), null);
	}

	public static PlotOrientation getPlotOrientation(String orientation) {
		if (orientation.equalsIgnoreCase("horizontal")) {
			return PlotOrientation.HORIZONTAL;
		} else if (orientation.equalsIgnoreCase("vertical")){
			return PlotOrientation.VERTICAL;
		} else {
			throw new RuntimeException("Unsupported plot orientation:" + orientation);
		}
	}
	
	public static Color getColor(String color) {
		// HTML colors (#FFFFFF format)
		if (color.startsWith("#")) {
			return new Color(Integer.parseInt(color.substring(1), 16));
		} else {
			// Colors by name
			if (color.equalsIgnoreCase("black"))
				return Color.black;
			if (color.equalsIgnoreCase("grey"))
				return Color.gray;
			if (color.equalsIgnoreCase("yellow"))
				return Color.yellow;
			if (color.equalsIgnoreCase("green"))
				return Color.green;
			if (color.equalsIgnoreCase("blue"))
				return Color.blue;
			if (color.equalsIgnoreCase("red"))
				return Color.red;
			if (color.equalsIgnoreCase("orange"))
				return Color.orange;
			if (color.equalsIgnoreCase("cyan"))
				return Color.cyan;
			if (color.equalsIgnoreCase("magenta"))
				return Color.magenta;
			if (color.equalsIgnoreCase("darkgray"))
				return Color.darkGray;
			if (color.equalsIgnoreCase("lightgray"))
				return Color.lightGray;
			if (color.equalsIgnoreCase("pink"))
				return Color.pink;
			if (color.equalsIgnoreCase("white"))
				return Color.white;
			
			throw new RuntimeException("Unsupported chart color:" + color);
		}
	}
	
	public static String resolveContentType(String output) {
		if(output.equalsIgnoreCase("png"))
			return "img/png";
		else if(output.equalsIgnoreCase("jpeg"))
			return "img/jpeg";
		else
			throw new RuntimeException("Unsupported output format:" + output);
	}
	
	//	Creates the chart with the given chart data
	public static JFreeChart createChartWithType(ChartData chartData) {
		JFreeChart chart = null;
		Object datasource = chartData.getDatasource();
		if (datasource instanceof PieDataset) {
			chart = createChartWithPieDataSet(chartData);
		} else if (datasource instanceof CategoryDataset) {
			chart = createChartWithCategoryDataSet(chartData);
		} else if (datasource instanceof XYDataset) {
			chart = createChartWithXYDataSet(chartData);
		} else {
			throw new RuntimeException("Unsupported chart type");
		}
		return chart;
	}
	
	public static void setGeneralChartProperties(JFreeChart chart, ChartData chartData) {
		chart.setBackgroundPaint(ChartUtils.getColor(chartData.getBackground()));
		chart.setTitle(chartData.getTitle());
		chart.setAntiAlias(chartData.isAntialias());
		chart.getPlot().setBackgroundPaint(ChartUtils.getColor(chartData.getForeground()));

		// Alpha transparency (100% means opaque)
		if (chartData.getAlpha() < 100) {
			chart.getPlot().setForegroundAlpha((float) chartData.getAlpha() / 100);
		}
	}

	public static JFreeChart createChartWithCategoryDataSet(ChartData chartData) {
		JFreeChart chart = null;
		PlotOrientation plotOrientation = ChartUtils.getPlotOrientation(chartData.getOrientation());

		CategoryDataset dataset = (CategoryDataset) chartData.getDatasource();
		String type = chartData.getType();
		String xAxis = chartData.getXlabel();
		String yAxis = chartData.getYlabel();
		boolean is3d = chartData.isChart3d();
		boolean legend = chartData.isLegend();

		if (type.equalsIgnoreCase("bar")) {
			if (is3d == true) {
				chart = ChartFactory.createBarChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			} else {
				chart = ChartFactory.createBarChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
			}
			setBarOutline(chart, chartData);
		} else if (type.equalsIgnoreCase("stackedbar")) {
			if (is3d == true) {
				chart = ChartFactory.createStackedBarChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			} else {
				chart = ChartFactory.createStackedBarChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			}
			setBarOutline(chart, chartData);
		} else if (type.equalsIgnoreCase("line")) {
			if (is3d == true)
				chart = ChartFactory.createLineChart3D("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
			else
				chart = ChartFactory.createLineChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("area")) {
			chart = ChartFactory.createAreaChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("stackedarea")) {
			chart = ChartFactory.createStackedAreaChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("waterfall")) {
			chart = ChartFactory.createWaterfallChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("gantt")) {
			chart = ChartFactory.createGanttChart("", xAxis, yAxis,(IntervalCategoryDataset) dataset, legend, true, false);
		}
		setCategorySeriesColors(chart, chartData);
		return chart;
	}

	public static JFreeChart createChartWithPieDataSet(ChartData chartData) {
		PieDataset dataset = (PieDataset) chartData.getDatasource();
		String type = chartData.getType();
		boolean legend = chartData.isLegend();
		JFreeChart chart = null;

		if (type.equalsIgnoreCase("pie")) {
			if (chartData.isChart3d()) {
				chart = ChartFactory.createPieChart3D("", dataset, legend,true, false);
				PiePlot3D plot = (PiePlot3D) chart.getPlot();
				plot.setDepthFactor((float) chartData.getDepth() / 100);
			} else {
				chart = ChartFactory.createPieChart("", dataset, legend, true,false);
			}
		} 
		/*else if (type.equalsIgnoreCase("ring")) {
			chart = ChartFactory.createRingChart("", dataset, legend, true,false);
		}*/
		
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setNoDataMessage("No data available");
		plot.setStartAngle((float) chartData.getStartAngle());
		setPieSectionColors(chart, chartData);
		
		return chart;
	}

	public static JFreeChart createChartWithXYDataSet(ChartData chartData) {
		XYDataset dataset = (XYDataset) chartData.getDatasource();
		String type = chartData.getType();
		String xAxis = chartData.getXlabel();
		String yAxis = chartData.getYlabel();
		boolean legend = chartData.isLegend();

		JFreeChart chart = null;
		PlotOrientation plotOrientation = ChartUtils.getPlotOrientation(chartData.getOrientation());

		if (type.equalsIgnoreCase("timeseries")) {
			chart = ChartFactory.createTimeSeriesChart("", xAxis, yAxis,dataset, legend, true, false);
		} else if (type.equalsIgnoreCase("xyline")) {
			chart = ChartFactory.createXYLineChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("polar")) {
			chart = ChartFactory.createPolarChart("", dataset, legend, true,false);
		} else if (type.equalsIgnoreCase("scatter")) {
			chart = ChartFactory.createScatterPlot("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xyarea")) {
			chart = ChartFactory.createXYAreaChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xysteparea")) {
			chart = ChartFactory.createXYStepAreaChart("", xAxis, yAxis,dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("xystep")) {
			chart = ChartFactory.createXYStepChart("", xAxis, yAxis, dataset,plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("bubble")) {
			chart = ChartFactory.createBubbleChart("", xAxis, yAxis,(XYZDataset) dataset, plotOrientation, legend, true, false);
		} else if (type.equalsIgnoreCase("candlestick")) {
			chart = ChartFactory.createCandlestickChart("", xAxis, yAxis,(OHLCDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("boxandwhisker")) {
			chart = ChartFactory.createBoxAndWhiskerChart("", xAxis, yAxis,(BoxAndWhiskerXYDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("highlow")) {
			chart = ChartFactory.createHighLowChart("", xAxis, yAxis,(OHLCDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("histogram")) {
			chart = ChartFactory.createHistogram("", xAxis, yAxis,(IntervalXYDataset) dataset, plotOrientation, legend, true,false);
		} else if (type.equalsIgnoreCase("signal")) {
			chart = ChartFactory.createSignalChart("", xAxis, yAxis,(SignalsDataset) dataset, legend);
		} else if (type.equalsIgnoreCase("wind")) {
			chart = ChartFactory.createWindPlot("", xAxis, yAxis,(WindDataset) dataset, legend, true, false);
		}
		setXYSeriesColors(chart, chartData);
		return chart;
	}
	
	/**
	 * Series coloring
	 * Plot has no getRenderer so two methods for each plot type(categoryplot and xyplot)
	 */
	public static void setCategorySeriesColors(JFreeChart chart, ChartData chartData) {
		if(chart.getPlot() instanceof CategoryPlot) {
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			if (chartData.getColors() != null) {
				String[] colors = chartData.getColors().split(",");
				for (int i = 0; i < colors.length; i++) {
					plot.getRenderer().setSeriesPaint(i, ChartUtils.getColor(colors[i].trim()));
				}
			}
		}
	}
	
	public static void setXYSeriesColors(JFreeChart chart, ChartData chartData) {
		if(chart.getPlot() instanceof XYPlot && chartData.getColors() != null) {
				XYPlot plot = (XYPlot) chart.getPlot();
				String[] colors = chartData.getColors().split(",");
				for (int i = 0; i < colors.length; i++) {
				plot.getRenderer().setSeriesPaint(i, ChartUtils.getColor(colors[i].trim()));
			}
		}
	}
	
	public static void setPieSectionColors(JFreeChart chart, ChartData chartData) {
		if(chart.getPlot() instanceof PiePlot && chartData.getColors() != null) {
			String[] colors = chartData.getColors().split(",");
			for (int i = 0; i < colors.length; i++) {
				((PiePlot)chart.getPlot()).setSectionPaint(i, ChartUtils.getColor(colors[i].trim()));
			}
		}
	}
	
	/**
	 * Sets the outline of the bars
	 */
	public static void setBarOutline(JFreeChart chart, ChartData chartData) {
		CategoryPlot plot = (CategoryPlot) chart.getPlot();			
		BarRenderer barrenderer = (BarRenderer) plot.getRenderer();
		barrenderer.setDrawBarOutline(chartData.isOutline());
	}
}
