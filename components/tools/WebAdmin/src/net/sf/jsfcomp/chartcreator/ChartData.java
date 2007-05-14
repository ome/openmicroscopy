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

import java.io.Serializable;

/**
 * @author Cagatay Civici
 * Holds the chart information to be rendered
 */
public class ChartData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object datasource;

	private int width;

	private int height;

	private int alpha;

	private int explode;
	
	private int depth;

	private int startAngle;

	private String title;

	private String type;

	private String background;
	
	private String foreground;

	private String xlabel;

	private String ylabel;

	private String orientation;

	private String colors;

	private boolean chart3d;

	private boolean legend;

	private boolean antialias;

	private boolean outline;
	
	private String output;
	
	public ChartData() {}

	public ChartData(UIChart chart) {
		this.datasource = chart.getDatasource();
		this.width = chart.getWidth();
		this.height = chart.getHeight();
		this.alpha = chart.getAlpha();
		this.explode = chart.getExplode();
		this.depth = chart.getDepth();
		this.startAngle = chart.getStartAngle();
		this.title = chart.getTitle();
		this.type = chart.getType();
		this.background = chart.getBackground();
		this.foreground = chart.getForeground();
		this.xlabel = chart.getXlabel();
		this.ylabel = chart.getYlabel();
		this.orientation = chart.getOrientation();
		this.colors = chart.getColors();
		this.chart3d = chart.getIs3d();
		this.legend = chart.getLegend();
		this.antialias = chart.getAntialias();
		this.outline = chart.getOutline();
		this.output = chart.getOutput();
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getExplode() {
		return explode;
	}

	public void setExplode(int explode) {
		this.explode = explode;
	}
	
	public boolean isAntialias() {
		return antialias;
	}

	public void setAntialias(boolean antialias) {
		this.antialias = antialias;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}
	
	public String getForeground() {
		return foreground;
	}

	public void setForeground(String foreground) {
		this.foreground = foreground;
	}

	public boolean isChart3d() {
		return chart3d;
	}

	public void setChart3d(boolean chart3d) {
		this.chart3d = chart3d;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	public Object getDatasource() {
		return datasource;
	}

	public void setDatasource(Object datasource) {
		this.datasource = datasource;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isLegend() {
		return legend;
	}

	public void setLegend(boolean legend) {
		this.legend = legend;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean isOutline() {
		return outline;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}

	public int getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(int startAngle) {
		this.startAngle = startAngle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getXlabel() {
		return xlabel;
	}

	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}

	public String getYlabel() {
		return ylabel;
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
}