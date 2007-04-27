/*
 * measurement.model.ROIAttributes 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement.model;

//Java imports

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;

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
public class ROIAttributes 
	extends AttributeKeys
{
	public static final AttributeKey<Long> ROIID = 
		new AttributeKey<Long>("ROIID", null);
	public static final AttributeKey<Double> ROISHAPEAREA = 
		new AttributeKey<Double>("ROIShapeArea", null);
	public static final AttributeKey<Double> ROISHAPELENGTH = 
		new AttributeKey<Double>("ROIShapeLength", null);
	public static final AttributeKey<Double> ROISHAPEANGLE = 
		new AttributeKey<Double>("ROIShapeAngle", null);
	
	private ROIAttributes()
	{
	    // no code req'd
	}

	public static ROIAttributes get()
	{	
		if (ref == null)
			 // it's ok, we can call this constructor
			ref = new ROIAttributes();		
		return ref;
	}

	public Object clone()
		throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
		 // that'll teach 'em
	}

	private static ROIAttributes ref;
}


