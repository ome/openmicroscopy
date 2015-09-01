/*
 * org.openmicroscopy.shoola.util.roi.model.ROIMap 
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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class ROIMap 
{
	
	/** The class to map the Coord3D to the list of ROI on that Coord. */
	private ROICoordMap	 roiCoordMap;
	
	/** The class to map the ROI ID to a particular ROI. */
	private ROIIDMap	roiIDMap;

	/** 
	 * This is the general class to map ROI ID's to ROI, Coord3D to lists
	 * of ROIs and manage them.
	 */
	public ROIMap()
	{
		roiCoordMap = new ROICoordMap();
		roiIDMap = new ROIIDMap();
	}
	
	/**
	 * Returns true if the roiIDMap contains the roi with id.
	 * @param id see above.
	 * @return see above.
	 */
	public boolean containsROI(long id)
	{
		return roiIDMap.containsKey(id);
	}
	
	/** 
	 * Add an ROI with id to the maps. This maps the ROI to id in the ROIIDMap,
	 * and adds the ROIshapes of that ROI to the list of ROIShapes at Coord3D 
	 * in the ROICoordMap.
	 * @param id The id of the ROI.
	 * @param roi The ROI to add. 
	 */
	public void add(Long id, ROI roi)
	{
		roiIDMap.add(id, roi);
		roiCoordMap.add(roi);
	}

	/**
	 * Get the ROIID map of the ROIIDMap class. This is used to 
	 * @return see above. 
	 */
	public TreeMap<Long, ROI> getROIMap()
	{
		return roiIDMap.getROIMap();
	}
	
	/**
	 * Return the roi with id 
	 * @param id the id of the ROI you wish returned.
	 * @return see above.
	 * @throws NoSuchROIException
	 */
	public ROI getROI(long id) 
		throws NoSuchROIException
	{
		return roiIDMap.getROI(id);
	}

	/**
	 * Returns the ROIShape within ROI with id on plane coord.
	 * 
	 * @param id the id of the ROI you wish returned.
	 * @param coord the plane on which the ROIShape resides.
	 * @return see above.
	 * @throws NoSuchROIException
	 */
	public ROIShape getShape(long id, Coord3D coord) 
		throws NoSuchROIException
	{
		return roiIDMap.getShape(id, coord);
	}

	/**
	 * Gets the Shapelist for the coord plane.
	 * 
	 * @param coord see above.
	 * @return see above.
	 * @throws NoSuchROIException
	 */
	public ShapeList getShapeList(Coord3D coord) 
		throws NoSuchROIException
	{	
		return roiCoordMap.getShapeList(coord);
	}

	/**
	 * Delete the ROI and all associated ROIShapes on the ROI.
	 * @param id The id of the ROI to delete.
	 * @throws NoSuchROIException
	 */
	public void deleteROI(long id) throws NoSuchROIException
	{
		ROI roi = roiIDMap.getROI(id);
		roiCoordMap.deleteROI(roi);
		roiIDMap.deleteROI(id);
	}

	/**
	 * Delete the ROIShape of ROI id on plane coord.
	 * @param id The id of the ROI containing the ROIShape to delete.
	 * @param coord The plane of the ROIShape to delete.
	 * @throws NoSuchROIException
	 */
	public void deleteShape(long id, Coord3D coord) 	
											throws 	NoSuchROIException	
	{
		roiCoordMap.deleteShape(id, coord);
		roiIDMap.deleteShape(id, coord);
	}

	/**
	 * Add ROIShape to ROI id on plane coord. If an ROIShape already exists on
	 * the plane coord, replace it.
	 * @param id The id of the ROI containing the ROI Shape.
	 * @param coord The coord of the plane.
	 * @param shape The shape to add.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROICreationException, 
												NoSuchROIException
	{
		roiIDMap.addShape(id, coord, shape);
		roiCoordMap.addShape(id, coord, shape);
	}		

	/** 
	 * Propagate the Shape identified on coord selectedShape contained within
	 * ROI id, from the start coord to the end coord, iterating through all 
	 * planes. If an ROIShape exists on a plane between start and end it will
	 * be replaced by the new ROIShape.
	 * @param id The id of the ROI containing the ROI Shape.
	 * @param selectedShape The coord of the plane containing the shape to 
	 * 			propagate.
	 * @param start The start coord to propagate from.
	 * @param end The end coord to propagate to.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	public List<ROIShape> propagateShape(long id, Coord3D selectedShape, 
												Coord3D start, Coord3D end) 
												throws ROICreationException, 
													   NoSuchROIException
	{
		List<ROIShape> addedList = new ArrayList<ROIShape>();
		if (!roiIDMap.containsKey(id))
			throw new NoSuchROIException("No ROI with id : "+ id);
		ROI roi = roiIDMap.getROI(id);
		int mint = Math.min(start.getTimePoint(), end.getTimePoint());
		int maxt = Math.max(start.getTimePoint(), end.getTimePoint());
		int minz = Math.min(start.getZSection(), end.getZSection());
		int maxz = Math.max(start.getZSection(), end.getZSection());
		maxt = maxt+1;
		maxz = maxz+1;
		ROIShape shape = roi.getShape(selectedShape);
		ROIShape newShape;
		Coord3D newCoord;
		//for(int c = start.c; c < end.c ; c++)
		for (int t = mint; t < maxt ; t++)
			for (int z = minz; z < maxz ; z++)
			{
				newCoord = new Coord3D(z, t);
				if (selectedShape.equals(newCoord))
					continue;
				if (roi.containsShape(newCoord))
					continue;
				//deleteShape(id, newCoord);
				newShape = new ROIShape(roi, newCoord, shape);
				newShape.getFigure().setClientObject(true);
				newShape.getFigure().setObjectDirty(true);
				addShape(id, newCoord, newShape);
				addedList.add(newShape);
			}
		return addedList;
	}

	/** 
	 * Delete the Shape contained within ROI id, from the start coord to the 
	 * end coord, iterating through all planes. 
	 * If an ROIShape does not exist on the plane it will be skipped, it will 
	 * not throw an exception.
	 * @param id The id of the ROI containing the ROI Shape.
	 * @param start The start coord to propagate from.
	 * @param end The end coord to propagate to.
	 * @throws NoSuchROIException
	 */
	public void deleteShape(long id, Coord3D start, Coord3D end) 
													throws 	NoSuchROIException
	{
		if(!roiIDMap.containsKey(id))
			throw new NoSuchROIException("No ROI with id : "+ id);
		ROI roi = roiIDMap.getROI(id);
		int mint = Math.min(start.getTimePoint(), end.getTimePoint());
		int maxt = Math.max(start.getTimePoint(), end.getTimePoint());
		int minz = Math.min(start.getZSection(), end.getZSection());
		int maxz = Math.max(start.getZSection(), end.getZSection());
		maxt = maxt+1;
		maxz = maxz+1;
		Coord3D newCoord;
		//for(int c = start.c; c < end.c ; c++)
		for (int t = mint; t < maxt ; t++)
			for (int z = minz; z < maxz ; z++)
			{
				newCoord = new Coord3D(z, t);
				if (roi.containsShape(newCoord))
					this.deleteShape(id, newCoord);
			}
	}	

}


