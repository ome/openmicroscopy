/*
 * org.openmicroscopy.shoola.util.roi.model.ROIList 
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

package org.openmicroscopy.shoola.util.roi.model;

import java.util.TreeMap;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.model.util.LongComparator;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class ShapeList 
{
	/** The Treemap that stores all the ROIShapes fgor the current plane. */
	private TreeMap<Long, ROIShape> roiList;
	
	/** The current plane of the Shapelist. */
	private Coord3D 				coord;
	
	/**
	 * Create the Shapelist for plane coord. 
	 * @param coord see above.
	 */
	public ShapeList(Coord3D coord)
	{
		roiList = new TreeMap<Long, ROIShape>(new LongComparator());
		this.coord = coord;
	}
	
	/**
	 * Get the shape list.
	 * @return see above.
	 */
	public TreeMap<Long, ROIShape> getList()
	{
		return roiList;
	}
	
	/**
	 * Get the coord of the plane the shapelist is associated with.
 	 * @return see above.
	 */
	public Coord3D getCoord3D()
	{
		return coord;
	}
	
	/**
	 * Add the ROIShape belonging to ROI to the shapelist.
	 * @param id the roi id.
	 * @param shape the shape to add.
	 */
	public void add(long id, ROIShape shape)
	{
		roiList.put(id, shape);
	}
	
	/**
	 * Get the ROIShape in ROI with id from the ShapeList.
	 * @param id see above.
	 * @return see above.
	 * @throws NoSuchROIException Thrown if the ROIShape does not exist.
	 */
	public ROIShape getShape(long id) throws NoSuchROIException
	{
		if(roiList.containsKey(id))
			return roiList.get(id);
		else
			throw new NoSuchROIException("No such ROIShape : " + id);
	}
	
	/**
	 * Return true if the ShapeList contains the ROIShape in ROI id.
	 * @param id see above.
	 * @return see above.
	 */
	public boolean containsKey(long id)
	{
		return roiList.containsKey(id);
	}
	
	/**
	 * Delete the ROIShape in ROI from shapeList.
	 * @param id The id of the ROI.
	 * @throws NoSuchROIException thrown if the Shape does not exist.
	 */
	public void deleteShape(long id) throws NoSuchROIException
	{
		if(roiList.containsKey(id))
			roiList.remove(id);
		else
			throw new NoSuchROIException("No such ROIShape : " + id);
	}
	
}


