/*
 * org.openmicroscopy.shoola.agents.util.flim.XYChartCanvas 
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
import processing.core.PVector;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.processing.chart.XYChart;

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
public class XYChartCanvas 
	extends PApplet
{
	/** The background colour of the windows. */
	final static Color windowsBackground = new Color(230,230,230);

	/** The XYChart to plot. */
	private XYChart chart;
	
	/** The border between the chart and the window. */
	private int border = 10;
	
	/**
	 * Instatiate the chart.
	 */
	XYChartCanvas()
	{
		chart = new XYChart(this);
		init();	
	}
	
	/**
	 * Set the data for the chart.
	 * @param data The data.
	 */
	public void setData(List<PVector> data)
	{
		chart.setData(data);
		chart.setYAxisLabel("count");
		chart.setXAxisLabel("time");
		chart.showXAxis(true);
		chart.showYAxis(true);
	}
	
	/**
	 * Setup the PApplet.
	 */
	public void setup()
	{
		setSize(1000, 250);
		smooth();
		//textFont(createFont("Helvetica", 10));
		//textSize(10);
		
	}
	
	/**
	 * Draw the Chart.
	 */
	public void draw()
	{
		background(windowsBackground.getRGB());
		pushMatrix();
		translate(border,0);
		pushStyle();
			fill(Color.white.getRGB());
			noStroke();
			rect(0,0,this.width-border*2, this.height);
		popStyle();
		translate(border,0);
		if(chart.getXData().length>2)
			chart.draw(1,1,this.width-border*3,this.height);
		popMatrix();
	}
	
	
}
