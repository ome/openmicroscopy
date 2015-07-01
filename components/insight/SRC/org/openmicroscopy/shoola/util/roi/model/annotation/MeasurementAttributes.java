/*
 * org.openmicroscopy.shoola.util.roi.model.MeasurementAttributes 
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
package org.openmicroscopy.shoola.util.roi.model.annotation;


import java.awt.Color;

import org.jhotdraw.draw.AttributeKey;

import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;


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
public class MeasurementAttributes
	extends DrawingAttributes 
{
	
	/** Should the figure show the measurement text. */
	public static final AttributeKey<Boolean> SHOWMEASUREMENT = 
		new AttributeKey<Boolean>("ShowMeasurement", false);
	
	/** Should the figure show the measurement text. */
	public static final AttributeKey<Boolean> SHOWID = 
		new AttributeKey<Boolean>("ShowID", false);
	
	/** Should the figure's size be changed while keeping proportions. */
	public static final AttributeKey<Boolean> SCALE_PROPORTIONALLY =
		new AttributeKey<Boolean>("ScaleProportionally", false);
	
	/** The colour of the measurement text. */
	public static final AttributeKey<Color> MEASUREMENTTEXT_COLOUR = 
		new AttributeKey<Color>("MeasurementTextColour", 
				IOConstants.DEFAULT_MEASUREMENT_TEXT_COLOUR);
    
	/** create instance. */
	private MeasurementAttributes()
	{
	    // no code req'd
	}

	/** Get a copy of the instance. */
	public static MeasurementAttributes get()
	{	
		if (ref == null)
			 // it's ok, we can call this constructor
			ref = new MeasurementAttributes();
		return ref;
	}

	/** Private ref. */
	private static MeasurementAttributes ref;
}
