/*
 * org.openmicroscopy.shoola.util.processing.chart.Range
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

//Application-internal dependencies

/**
 * Utility class
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
public class Range
{
	
	/** The minimum value in the range.*/
	private double min;

	/** The maximum value in the range.*/
	private double max;
	
	/** Flag indicating if the value is set.*/
	private boolean set;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param min The minimum value.
	 * @param max The maximum value.
	 */
	public Range(double min, double max)
	{
		if (min > max) {
			double v = max;
			max = min;
			min = v;
		}
		this.min = min;
		this.max = max;
		this.set = true;
	}
	
	/** Creates the range.*/
	public Range()
	{
		this(0, 0);
		this.set = false;
	}
	
	/**
	 * Adds a value to the range.
	 * @param value The value to add.
	 */
	public void addValue(double value)
	{
		set = true;
		min = Math.min(value, min);
		max = Math.max(value, max);	
	}
	
	/**
	 * Returns the range. 
	 * 
	 * @return See above.
	 */
	public double getRange() { return max-min; }

	/**
	 * Returns the minimum value.
	 * 
	 * @return See above.
	 */
	public double getMin() { return min; }
	
	/**
	 * Returns the maximum value.
	 * 
	 * @return See above.
	 */
	public double getMax() { return max; }
	
}
