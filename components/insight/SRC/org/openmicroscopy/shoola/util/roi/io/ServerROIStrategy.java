/*
 * org.openmicroscopy.shoola.util.roi.io.InputStrategy 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.io;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.ROI;

import pojos.ImageData;
import pojos.ROIData;

/**
 * Converts server ROI into the corresponding UI objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ServerROIStrategy
{

	/** Used to read the ROIs from the server. */
	private InputServerStrategy  inputStrategy;
	
	/** Used to write the ROIs to the server. */
	private OutputServerStrategy outputStrategy;


	/** Creates a new instance. */
	public ServerROIStrategy()
	{
		inputStrategy = new InputServerStrategy();
		outputStrategy = new OutputServerStrategy();
	}
	
	/**
	 * Converts the ROIs hosted in the passed collection and returns the 
	 * collection.
	 * 
	 * @param rois The server side ROI to convert.
	 * @param component
	 * @param userID The identifier of the user currently logged in.
	 * @throws NoSuchROIException
	 * @throws ROICreationException
	 * @return See above.
	 */
	public List<ROI> read(Collection rois, ROIComponent component, long userID)
		throws NoSuchROIException, ROICreationException, 
				ROICreationException	   
	{
		if (rois == null || rois.size() == 0) return new ArrayList<ROI>();
		return inputStrategy.readROI(rois, component, userID);
	}
	
	/**
	 * Writes the ROI.
	 * 
	 * @param component The ROI component.
	 * @param image The image the ROI is on.
	 * @param index One of the constants defined by {@link ROIComponent} class.
	 * @param userID The id of the user currently logged in.
	 * @throws Exception 
	 */
	public List<ROIData> write(ROIComponent component, ImageData image,
			int index, long userID)
		throws Exception
	{
		if (component.getROIMap().size() == 0)
			return new ArrayList<ROIData>();
		return outputStrategy.writeROI(component, image, index, userID);
	}

}