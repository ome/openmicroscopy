/*
* pojos.util.ROIClass
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;
import pojos.MaskData;
import pojos.ROIData;

import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;


/**
 *
 * Coordinating class between Masks and (z,t) this will hold the list of 
 * Masks related to that ROI.
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ROIClass
{
	/**  
	 * Map of the coordinates and mask objects, may have more than one 
	 * mask on one plane. 
	 */
	private  Map<Coord3D, List<MaskClass>> maskMap;
	
	/**
	 * Instantiate the ROIClass and create the maskMap.
	 */
	ROIClass()
	{
		maskMap = new HashMap<Coord3D, List<MaskClass>>();
	}
	
	/**
	 * Add a mask to the ROIMap, this will store the mask and it's z,t
	 * @param mask See above.
	 * @param z See above.
	 * @param t See above. 
	 */
	public void addMask(MaskClass mask, int z, int t)
	{
		List<MaskClass> maskList;
		Coord3D coord = new Coord3D(z, t);
		if(maskMap.containsKey(coord))
			maskList = maskMap.get(coord);
		else
		{
			maskList = new ArrayList<MaskClass>();
			maskMap.put(coord, maskList);
		}
		maskList.add(mask);
	}
	
	/**
	 * Create the roi for the 
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public ROIData getROI(ImageData image) throws IOException
	{
		ROIData roi = new ROIData();
		roi.setId(image.getId());
		
		Iterator<Coord3D> coordIterator = maskMap.keySet().iterator();
		while(coordIterator.hasNext())
		{
			Coord3D coord = coordIterator.next();
			List<MaskClass> maskList = maskMap.get(coord);
			for(MaskClass mask : maskList)
			{
				MaskData toSaveMask = mask.asMaskData(coord.getZSection(), 
												coord.getTimePoint());		
				roi.addShapeData(toSaveMask);
			}
		}
		
		return roi;
	}
	
}
