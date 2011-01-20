/*
 * org.openmicroscopy.shoola.util.roi.io.ServerROIStrategy
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.io;



//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.ROI;

/**
 * Converts server ROI into the corresponding UI objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ServerROIStrategy
{

	/** Used to read the ROIs from the server. */
	private InputServerStrategy  inputStrategy;


	/** Creates a new instance. */
	public ServerROIStrategy()
	{
		inputStrategy = new InputServerStrategy();
	}
	
	/**
	 * Converts the ROIs hosted in the passed collection and returns the 
	 * collection.
	 * 
	 * @param rois The server side ROI to convert.
	 * @param component
	 * @param readOnly Is the ROI read only.
	 * @throws NoSuchROIException
	 * @throws ROICreationException
	 * @returns See above.
	 */
	public List<ROI> read(Collection rois, ROIComponent component, 
			boolean readOnly)
		throws NoSuchROIException, ROICreationException, 
				ROICreationException	   
	{
		if (rois == null || rois.size() == 0) return new ArrayList<ROI>();
		return inputStrategy.readROI(rois, component, readOnly);
	}
	
}
