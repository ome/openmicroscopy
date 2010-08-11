/*
 * pojos.util.ROIComponent
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos.util;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import pojos.MaskData;
import pojos.ROIData;
import omero.model.ImageI;

/** 
 * Component hosting the ROI.
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

public class ROIComponent
{
	
	/** The map of Colour to ROI.*/
	private Map<Integer, ROIData> roiColour;
	
	/** The Image Id of the component. */
	private long imageId;
	
	/**
	 * Instantiate the ROIComponent. 
	 * @param imageId The image id.
	 */
	public ROIComponent(long imageId)
	{
		this.imageId = imageId;
		roiColour = new HashMap<Integer, ROIData>();
	}
	
	/**
	 * Adds the Masks in the maskSet to the component.
	 * 
	 * @param maskSet See above.
	 */
	public void addMasks(Map<Integer, MaskData> maskSet)
	{
		Iterator<Integer> colourIterator = maskSet.keySet().iterator();
		ROIData roi;
		int colour;
		while (colourIterator.hasNext())
		{
			colour = colourIterator.next();
			if (!roiColour.containsKey(colour))
			{
				roi = new ROIData();
				roi.setImage(new ImageI(imageId, false));
				roiColour.put(colour, roi);
			}
			else
				roi = roiColour.get(colour);
			roi.addShapeData(maskSet.get(colour));
		}	
	}
	
	/**
	 * Returns all the ROI in the component as a list.
	 * @return Sea above.
	 */
	public List<ROIData> getROI()
	{
		List<ROIData> roiList = new ArrayList<ROIData>();
		Iterator<Integer> colourIterator = roiColour.keySet().iterator();
		while (colourIterator.hasNext())
		{
			roiList.add(roiColour.get(colourIterator.next()));
		}
		return roiList;
	}

}
