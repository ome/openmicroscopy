/*
 * org.openmicroscopy.shoola.util.processing.chart.ImageData
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * The object holding information about the image.
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
public class ImageData
{
	
	/** Image data, values represent the different matrixes of the FLIM fit. */
	private List<Double> data; 
	
	/** The binning on the image. */
	private int binning;
	
	/** The width of the image. */
	private int width;
	
	/** The height of the image. */
	private int height;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param binning The height of the image.
	 */
	public ImageData(List<Double> data, int width, int height, int binning)
	{
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("No data");
		this.data = data;
		this.binning = binning;
		this.height = height;
		this.width = width;
	}
	
	/**
	 * Returns the width of the image.
	 * 
	 * @return See above.
	 */
	public int getWidth() { return width; }
	
	/** 
	 * Returns the height of the image.
	 * 
	 * @return See above.
	 */
	public int getHeight() { return height; }
	
	/**
	 * Returns the value for position x,y.
	 * 
	 * @param x See above.
	 * @param y See above.
	 * @return See above.
	 */
	public double getValue(int x, int y) { return data.get(x+y*width); }

	/**
	 * Returns the binning of the image.
	 * 
	 * @return See above.
	 */
	public int getBinning() { return binning; }
	
}
