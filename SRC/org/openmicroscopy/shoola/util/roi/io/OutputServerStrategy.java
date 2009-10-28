/*
* org.openmicroscopy.shoola.util.roi.io.OutputServerStrategy
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import omero.model.Roi;

import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.model.ROI;

import pojos.ROIData;
import pojos.ShapeData;

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
 * @since 3.0-Beta4
 */
public class OutputServerStrategy 
{
	
	/** The ROIComponent to serialise. */
	private ROIComponent component;
	
	/** The list of ROI to be supplied to the server. */
	private List<ROIData>  ROIList;
	
	/**
	 * Instantiate the class.
	 */
	OutputServerStrategy()
	{
		
	}
	
	
	/**
	 * Write the ROI from the ROI component to the server. 
	 * @param component See above.
	 */
	public void writeROI(ROIComponent component)
	{
		this.component = component;
		this.ROIList = new ArrayList<ROIData>();
		parseROI();
		writeROIData();
	}
	
	/**
	 * Parse the ROI in the ROIComponent to create the appropriate ROIDAta 
	 * object to supply to the server.
	 */
	private void parseROI()
	{
		TreeMap<Long, ROI> map = component.getROIMap();
		Iterator<ROI> roiIterator = map.values().iterator();
		while(roiIterator.hasNext())
		{
			ROI roi = roiIterator.next();
			ROIData serverROI = createServerROI(roi);
			ROIList.add(serverROI);
		}
	}

	/**
	 * Write the list of ROIData to the server
	 */
	private void writeROIData()
	{
		
	}
	
	/**
	 * Creates an ROIData object from an ROI. 
	 * @param roi See above.
	 * @return See above.
	 */
	private ROIData createServerROI(ROI roi)
	{
		//ROIData roiData = new ROIData();
		return null;
	}
	
}

