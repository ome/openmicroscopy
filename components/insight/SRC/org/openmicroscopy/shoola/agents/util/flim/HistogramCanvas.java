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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orderedData The ordered data.
	 * @param data The data to display.
	 * @param bins The number of bins.
	 */
	HistogramCanvas(List<Double> orderedData, ImageData data, int bins)
	{
		if (data == null)
			throw new IllegalArgumentException("No data to display.");
		if (orderedData == null)
			throw new IllegalArgumentException("No data to display.");
		if (bins <= 0) bins = 1;
		this.bins = bins;
		this.orderedData = orderedData;
		this.data = data;
		chart = new HistogramChart(this, orderedData, bins);
		init();
	}
	
	/** 
	 * Overridden to build the chart. 
	 * 
	 * @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		size(500, 400);
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
		map = new HeatMap(this, data, chart); 
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

		if (map == null) return;
		Color c = new Color(102, 102, 102);
		pushMatrix();
		translate(20, 20);
		fill(c.getRGB());
		rect(0, 0, 100, 15);
		fill(Color.white.getRGB());
		text("Heatmap", 40, 12);
		noFill();
		strokeWeight(2);
		stroke(c.getRGB());
		rect(0, 0, 200, 215);
		translate(0, 15);
		map.draw();
		popMatrix();
	}
	
}
