/*
 * org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.model.util;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasurementUnits
{	
	/** The number of microns per pixel in the X-Axis. */
	private double micronsPixelX;

	/** The number of microns per pixel in the Y-Axis. */
	private double micronsPixelY;
	
	/** The number of microns per pixel in the Z-Axis. */
	private double micronsPixelZ;
	
	/** Display measurements in Microns. */
	private boolean inMicrons;
	
	public MeasurementUnits(double x, double y, double z, boolean inM)
	{
		micronsPixelX = x;
		micronsPixelY = y;
		micronsPixelZ = z;
		inM = inMicrons;
	}
	
	/**
	 * Get the microns in a pixel in the X-Axis.
	 * @return see above.
	 */
	public double getMicronsPixelX()
	{
		return micronsPixelX;
	}
	
	/**
	 * Get the microns in a pixel in the Y-Axis.
	 * @return see above.
	 */
	public double getMicronsPixelY()
	{
		return micronsPixelY;
	}
	
	/**
	 * Get the microns in a pixel in the Z-Axis.
	 * @return see above.
	 */
	public double getMicronsPixelZ()
	{
		return micronsPixelZ;
	}
	
	/**
	 * Show the measurements in microns.
	 * @return see above.
	 * 
	 */
	public boolean showInMicrons()
	{
		return inMicrons;
	}

	/**
	 * Set the microns in a pixel in the X-Axis.
	 */
	public void setMicronsPixelX(double x)
	{
		micronsPixelX = x;
	}
	
	/**
	 * Set the microns in a pixel in the Y-Axis.
	 */
	public void setMicronsPixelY(double y)
	{
		micronsPixelY = y;
	}
	
	/**
	 * Set the microns in a pixel in the Z-Axis.
	 */
	public void setMicronsPixelZ(double z)
	{
		micronsPixelZ = z;
	}
	
	/**
	 * Set the showing of the measurements in microns.
	 * 
	 */
	public void setShowInMicrons(boolean show)
	{
		inMicrons = show;
	}
}


