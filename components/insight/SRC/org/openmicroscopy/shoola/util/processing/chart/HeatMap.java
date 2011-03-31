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
import java.awt.Color;

//Third-party libraries
import processing.core.PApplet;
//Application-internal dependencies

/** 
 * 
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
	PApplet parent;
	
	/** The original Image Data. */
	ImageData imageData;
	
	/** The histogram chart. */
	HistogramChart histogramChart;
	
	/**
	 * Create an instance of the Heatmap.
	 * @param parent
	 */
	public HeatMap(PApplet parent, ImageData data, HistogramChart chart)
	{
		this.parent = parent;
		this.imageData = data;
		this.histogramChart = chart;
	}
	
	/** 
	 * Render the heatmap from xOrigin, yOrigin, using width and height.
	 * @param xOrigin See above.
	 * @param yOrigin See above.
	 * @param width See above.
	 * @param height See above.
	 */
	public void draw()
	{
		parent.pushStyle();
		parent.noStroke();
		for(int x = 0; x < imageData.getWidth() ; x++)
			for( int y = 0 ; y < imageData.getHeight() ; y++)
			{
				double value = imageData.getValue(x,y);
				int colour = histogramChart.findColour(value);
				parent.fill(colour);
				parent.rect(x*imageData.getBinning(), y*imageData.getBinning(), 
						imageData.getBinning(), imageData.getBinning());
			}
		parent.popStyle();

	}
	
}
