/*
 * org.openmicroscopy.shoola.util.roi.figures.ROIFigure 
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
package org.openmicroscopy.shoola.util.roi.figures;

//Java imports

//Third-party libraries
import java.awt.Point;
import java.util.List;

import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;

/** 
 * Interface that all areas of the Euclidean space <b>R</b><sup>2</sup> must
 * implement.
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
public interface ROIFigure 
	extends Figure
{
		
	/** Identified the <code>IDLE</code> status. */
	public static final int IDLE = 0;
	
	/** Identified the <code>MOVING</code> status. */
	public static final int MOVING = 1;
	
	/** 
	 * Sets the measurement units of the ROIFigure. 
	 * 
	 * @param units The class holding the values of the 
	 * 				microns per pixel and whether the figure should display 
	 * 				the measurements in microns or pixels.
	 */
	public void setMeasurementUnits(MeasurementUnits units);
	
	/**
	 * Sets the passed ROI.
	 * 
	 * @param roi The roi to set.
	 */
	public void setROI(ROI roi);
	
	/**
	 * Sets the shape.
	 * 
	 * @param shape The shape to set.
	 */
	public void setROIShape(ROIShape shape);
	
	/**
	 * Returns the ROI.
	 * 
	 * @return See above.
	 */
	public ROI  getROI();
	
	/**
	 * Returns the ROI.
	 * 
	 * @return See above.
	 */
	public ROIShape getROIShape();
	
	/** 
	 * Calculates some measurements for the ROI.
	 * Does nothing if the ROI shape is <code>null</code>.
	 */
	public void calculateMeasurements();
	
	/**
	 * Returns the type of the ROI. 
	 * 
	 * @return See above.
	 */
	public String getType();
	
	/** 
     * Returns an array of {@link Point} contained in the ROIFigure. 
     * 
     * @return See above.
     */
	public List<Point> getPoints();

	/**
	 * Sets the status of the figure.
	 * 
	 * @param status The value to set.
	 */
	public void setStatus(int status);
	
	/**
	 * Returns the status of the figure.
	 * 
	 * @return See above.
	 */
	public int getStatus();
	
}


