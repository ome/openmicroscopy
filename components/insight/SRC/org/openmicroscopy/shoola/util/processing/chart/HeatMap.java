/*
 * org.openmicroscopy.shoola.util.processing.chart.HeatMap
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.util.processing.chart;

//Java imports

//Third-party libraries
import processing.core.PApplet;

//Application-internal dependencies

/**
 * The class displaying heatmap.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class HeatMap
{
	
	/** The parent applet. */
	private PApplet parent;
	
	/** The original Image Data. */
	private ImageData imageData;
	
	/** The histogram chart. */
	private HistogramChart histogramChart;
	
	/**
	 * Create an instance of the Heatmap.
	 * 
	 * @param parent The parent applet.
	 * @param data The original Image Data.
	 * @param chart The chart to use.
	 */
	public HeatMap(PApplet parent, ImageData data, HistogramChart chart)
	{
		if (parent == null)
			throw new IllegalArgumentException("No PApplet specified.");
		if (chart == null)
			throw new IllegalArgumentException("No Histogram specified.");
		if (data == null)
			throw new IllegalArgumentException("No Image data specified.");
		this.parent = parent;
		this.imageData = data;
		this.histogramChart = chart;
	}
	
	/** 
	 * Renders the heatmap from xOrigin, yOrigin, using width and height.
	 */
	public void draw()
	{
		parent.pushStyle();
		parent.noStroke();
		double value;
		int binning;
		for (int x = 0; x < imageData.getWidth() ; x++)
			for (int y = 0 ; y < imageData.getHeight() ; y++) {
				value = imageData.getValue(x,y);
				parent.fill(histogramChart.findColour(value));
				binning = imageData.getBinning();
				parent.rect(x*binning, y*binning, binning, binning);
			}
		parent.popStyle();
	}

}
