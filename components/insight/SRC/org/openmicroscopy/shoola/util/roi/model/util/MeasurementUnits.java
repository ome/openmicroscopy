/*
 * org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import omero.model.Length;
import omero.model.enums.UnitsLength;

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
	
	/** The size of a pixel along the x-axis. */
	private Length pixelSizeX;

	/** The size of a pixel along the y-axis. */
	private Length pixelSizeY;
	
	/** The size of a pixel along the z-axis. */
	private Length pixelSizeZ;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param x		The size of a pixel along the x-axis.
	 * @param y		The size of a pixel along the y-axis.
	 * @param z		The size of a pixel along the z-axis.
	 */
	public MeasurementUnits(Length x, Length y, Length z)
	{
		setPixelSizes(x, y, z);
	}
	
	/**
	 * Returns size of a pixel in the x-axis.
	 * 
	 * @return See above.
	 */
	public Length getPixelSizeX() { return pixelSizeX; }
	
	/**
	 * Returns size of a pixel in the y-axis.
	 * 
	 * @return See above.
	 */
	public Length getPixelSizeY() { return pixelSizeY; }
	
	/**
	 * Returns size of a pixel in the z-axis.
	 * 
	 * @return See above.
	 */
	public Length getPixelSizeZ() { return pixelSizeZ; }
	
	public void setPixelSizes(Length x, Length y, Length z) {
		if (checkUnits(x, y, z)) {
			this.pixelSizeX = x;
			this.pixelSizeY = y;
			this.pixelSizeZ = z;
		} else
			throw new IllegalArgumentException(
					"Can't use different units for the pixel sizes.");
	}

	public UnitsLength getUnit() {
		return pixelSizeX.getUnit();
	}

	/**
	 * Checks if each Length has the same unit
	 * @param values The values to check
	 * @return <code>true</code> if all units are the same,
	 *         <code>false</code> otherwise
	 */
	private boolean checkUnits(Length... values) {
		// TODO: Might be better to check if all units are of type pixel or
		// all units are !pixel (and convert these to a common base unit)
		UnitsLength unit = null;
		for (Length value : values) {
			if (value == null)
				continue;
			if (unit == null) {
				unit = value.getUnit();
				continue;
			} else if (!value.getUnit().equals(unit)) {
				return false;
			}
		}
		return true;
	}
	
}


