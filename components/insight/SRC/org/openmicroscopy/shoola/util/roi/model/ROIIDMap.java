/*
 *  org.openmicroscopy.shoola.util.roi.model.ROIIDMap 
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
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
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
public class ROIIDMap 
{
	
	/** The TreeMap with the ROI--ROI.id mapping. */
	private TreeMap<Long, ROI> 	roiMap;

	/** 
	 * ROI ID Map, this class is the ADT to map between ROI id and ROI. The
	 * ROIIDMap is a tree map, so all the ROI's will be ordered in the map
	 * by ID for iteration.
	 */
	@SuppressWarnings("unchecked")
	public ROIIDMap()
	{
		roiMap = new TreeMap<Long, ROI>(new LongComparator());
	}

	/**
	 * Returns true if the map contains ROI ID id.
	 * @param id see above.
	 * @return see above.
	 */
	public boolean containsKey(long id)
	{
		return roiMap.containsKey(id);
	}
	
	/**
	 * Add ROI roi with ID id to the map. If an ROI exists with ID id, it will 
	 * be overwritten by this new ROI.
	 * @param id The ROI ID of the roi.
	 * @param roi The ROI being added to the map. 
	 */
	public void add(long id, ROI roi)
	{
		roiMap.put(id, roi);
	}
	
	/** 
	 * Return the roi map.
	 * @return see above.
	 */
	public TreeMap<Long, ROI> getROIMap()
	{
		return roiMap;
	}
	
	/** 
	 * Gets the ROI with id. 
	 * 
	 * @param id see above.
	 * @return see above.
	 * @throws NoSuchROIException
	 */
	public ROI getROI(long id) throws NoSuchROIException
	{
		if(!roiMap.containsKey((Long)id))
			throw new NoSuchROIException("No ROI with ID : " + id);
		return roiMap.get((Long)id);
	}
	
	/**
	 * Returns the ROIShape within ROI with id on plane coordinates.
	 * 
	 * @param id the id of the ROI you wish returned.
	 * @param coord the plane on which the ROIShape resides.
	 * @return see above.
	 * @throws NoSuchROIException
	 */
	public ROIShape getShape(long id, Coord3D coord) throws NoSuchROIException
	{
		if(!roiMap.containsKey((Long)id))
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id);
		ROI roi;
		roi = roiMap.get(id);
		if (roi == null)
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id);
		if (!roi.containsShape(coord))
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id + 
				" and Coord : " + coord);
		
		ROIShape shape = roi.getShape(coord);
		if (shape == null)
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id + 
				" and Coord : " + coord);
		return shape;
	}
	
	/**
	 * Deletes the ROI and all associated ROIShapes on the ROI.
	 * 
	 * @param id The id of the ROI to delete.
	 * @throws NoSuchROIException
	 */
	public void deleteROI(long id) throws NoSuchROIException
	{
		if (!roiMap.containsKey((Long) id))
			throw new NoSuchROIException("Cannot delete ROI with id : " 
				+ id);
		roiMap.remove(id);
	}
	
	/**
	 * Add ROIShape to ROI id on plane coordinates. 
	 * If an ROIShape already exists on the plane coordinates, replace it.
	 * 
	 * @param id The id of the ROI containing the ROI Shape.
	 * @param coord The coordinates of the plane.
	 * @param shape The shape to add.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
		throws ROICreationException, NoSuchROIException	
	{
		ROI roi;
		if (!roiMap.containsKey((Long) id))
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id);
	
		roi = roiMap.get(id);
		if (roi == null)
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id + 
				" and Coord : " + coord);
		roi.addShape(shape);
	}		

	/**
	 * Delete the ROIShape of ROI id on plane coordinates.
	 * 
	 * @param id The id of the ROI containing the ROIShape to delete.
	 * @param coord The plane of the ROIShape to delete.
	 * @throws NoSuchROIException
	 */
	public void deleteShape(long id, Coord3D coord) 
		throws 	NoSuchROIException 
															
	{
		if (!roiMap.containsKey(id))
			throw new NoSuchROIException("No ROI with id : " + id);
		ROI roi = roiMap.get(id);
		if (!roi.containsShape(coord))
			throw new NoSuchROIException("No ROIShape with ROI ID : " + id + 
				" and Coord : " + coord);
		roi.deleteShape(coord);
		if (roi.getShapes().size() == 0)
			roiMap.remove(id);
	}
	
}


