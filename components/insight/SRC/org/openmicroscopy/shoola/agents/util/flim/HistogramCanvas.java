/*
 * org.openmicroscopy.shoola.agents.util.flim.HistogramCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.flim;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

//Third-party libraries
import processing.core.PApplet;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.processing.chart.FillType;
import org.openmicroscopy.shoola.util.processing.chart.HeatMap;
import org.openmicroscopy.shoola.util.processing.chart.HistogramChart;
import org.openmicroscopy.shoola.util.processing.chart.ImageData;

/**
 * Component displaying the histogram.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class HistogramCanvas 
	extends PApplet
{

	/** The histogram. */
	private HistogramChart chart;
	
	/** The heatMap. */
	private HeatMap map;
	
	/** The object holding the data. */
	private ImageData data;
	
	/** The ordered data to display. */
	private List<Double> orderedData;
	
	/** The number of bins.*/
	private int bins;

	/** Flag indicating to show or hide the heatMap.*/
	private boolean showHeatMap;
	
	/** The button used to open or close the heatMap.*/
	private Rectangle button;
	
	/** Flag indicating that open or close heatMap.*/
	private boolean displayHeatMap = true;
	
	/** The value by which to translate the heatMap along the X-axis.*/
	private int translateX = 0;
	
	/** The value by which to translate the heatMap along the X-axis.*/
	private int translateY = 0;

	/**
	 * Creates a new instance.
	 * 
	 * @param orderedData The ordered data.
	 * @param data The data to display.
	 * @param bins The number of bins.
	 */
	HistogramCanvas(List<Double> orderedData, ImageData data, int bins)
	{
		this(orderedData, data, bins, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orderedData The ordered data.
	 * @param data The data to display.
	 * @param bins The number of bins.
	 * @param showHeatMap 	Pass <code>true</code> to show the heatMap,
	 * 						<code>false</code> otherwise.
	 */
	HistogramCanvas(List<Double> orderedData, ImageData data, int bins, 
			boolean showHeatMap)
	{
		if (data == null)
			throw new IllegalArgumentException("No data to display.");
		if (orderedData == null)
			throw new IllegalArgumentException("No data to display.");
		if (bins <= 0) bins = 1;
		this.bins = bins;
		this.orderedData = orderedData;
		this.data = data;
		button = new Rectangle(2, 2, 12, 12);
		chart = new HistogramChart(this, orderedData, bins);
		map = new HeatMap(this, data, chart); 
		this.showHeatMap = showHeatMap;
		init();
	}
	/**
	 * Returns the colour of the bin containing the value.
	 * 
	 * @param value See above.
	 * @return See above.
	 */
	public int findColour(double value)
	{
		return chart.findColour(value);
	}
	
	/** 
	 * Overridden to build the chart. 
	 * 
	 * @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		Dimension d = new Dimension(500, 400);
		setSize(d);
		smooth();
		textFont(createFont("Helvetica", 10));
		textSize(10);
		chart.transposeAxes(false);
		chart.setXAxisLabel("Bins");
		chart.setYAxisLabel("Frequency");
		chart.setLineColour(Color.black.getRGB());
		chart.setLineWidth(2);
		//Scale line graph to use same space as bar graph.
		chart.setMinY(chart.getMinX()); 
		chart.setMaxY(chart.getMaxY());  
		chart.setPointSize(4);
		chart.setDrawRGB(true);
		chart.setRGB(true, -1.1, 1.0);
		chart.showXAxis(true);
		chart.showYAxis(true);
		chart.setGradientFill(FillType.GRADIENT);
		
	}

	/** Draws the histogram.
	 *
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (chart == null) return;
		background(222);
		//noLoop();
		// Draw the bar chart first, then overlay the line chart.
		chart.draw(1, 1, width-2, height-2);

		if (map == null || !showHeatMap) return;
		Color c = new Color(102, 102, 102);
		pushMatrix();
		translate(translateX, translateY);
		fill(c.getRGB());
		int h = 15;
		rect(0, 0, data.getWidth(), h);
		fill(Color.black.getRGB());
		rect(button.x, button.y, button.width, button.height);
		fill(Color.white.getRGB());
		text("Heatmap", 40, 12);
		if (!displayHeatMap) {
			popMatrix();
			return;
		}
		noFill();
		strokeWeight(2);
		stroke(c.getRGB());
		rect(0, 0, data.getWidth(), data.getHeight()+h);
		translate(0, 15);
		map.draw();
		popMatrix();
	}
	
	/**
	 * Overridden to handle mouse pressed event.
	 * @see {@link PApplet#mousePressed()}
	 */
	public void mousePressed()
	{
		int x = mouseX-translateX;
		int y = mouseY-translateY;
		if (button.contains(x, y)) {
			displayHeatMap = !displayHeatMap;
			draw();
			return;
		}
	}
	
}
