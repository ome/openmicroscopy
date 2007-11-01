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
 * Helper class hosting units parameters.
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
	
	/** The number of microns per pixel along the x-axis. */
	private double micronsPixelX;

	/** The number of microns per pixel along the y-axis. */
	private double micronsPixelY;
	
	/** The number of microns per pixel along the z-axis. */
	private double micronsPixelZ;
	
	/** Display measurements in Microns. */
	private boolean inMicrons;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param x		The number of microns per pixel along the x-axis.
	 * @param y		The number of microns per pixel along the y-axis.
	 * @param z		The number of microns per pixel along the z-axis.
	 * @param inM	Passed <code>true</code> if the unit is in microns,
	 * 				<code>false</code> otherwise.
	 */
	public MeasurementUnits(double x, double y, double z, boolean inM)
	{
		micronsPixelX = x;
		micronsPixelY = y;
		micronsPixelZ = z;
		inMicrons = inM;
	}
	
	/**
	 * Returns the number of the microns in a pixel in the x-axis.
	 * 
	 * @return See above.
	 */
	public double getMicronsPixelX() { return micronsPixelX; }
	
	/**
	 * Returns the number of the microns in a pixel in the y-axis.
	 * 
	 * @return See above.
	 */
	public double getMicronsPixelY() { return micronsPixelY; }
	
	/**
	 * Returns the number of the microns in a pixel in the z-axis.
	 * 
	 * @return See above.
	 */
	public double getMicronsPixelZ() { return micronsPixelZ; }
	
	/**
	 * Shows the measurements in microns.
	 * 
	 * @return See above.
	 */
	public boolean isInMicrons() { return inMicrons; }

	/** 
	 * Sets the microns in a pixel in the x-axis.
	 * 
	 * @param x The value to set.
	 */
	public void setMicronsPixelX(double x) { micronsPixelX = x; }
	
	/** 
	 * Sets the microns in a pixel in the y-axis.
	 * 
	 * @param y The value to set.
	 */
	public void setMicronsPixelY(double y) { micronsPixelY = y; }
	
	/** 
	 * Sets the microns in a pixel in the z-axis.
	 * 
	 * @param z The value to set.
	 */
	public void setMicronsPixelZ(double z) { micronsPixelZ = z; }
	
	/**
	 * Sets the measurements in microns if the passed value is 
	 * <code>true</code>, in pixels otherwise.
	 * 
	 * @param show The value to set.
	 */
	public void setInMicrons(boolean show) { inMicrons = show; }
	
}


