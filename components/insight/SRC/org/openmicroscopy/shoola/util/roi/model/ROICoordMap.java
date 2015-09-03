/*
 *  org.openmicroscopy.shoola.util.roi.model.ROICoordMap 
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

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
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
public class ROICoordMap 
{
	/** The Map of the coord3D-->Shapelist. */
	private TreeMap<Coord3D, ShapeList> coordMap;
	
	/** The constructor of the ROICoordMap. */
	public ROICoordMap()
	{
		coordMap = new TreeMap<Coord3D, ShapeList>(new Coord3D());
	}
	
	/**
	 * Return true if the map contains the key coord.
	 * @param coord See above.
	 * @return See above.
	 */
	public boolean containsKey(Coord3D coord)
	{
		return coordMap.containsKey(coord);
	}
	
	/**
	 * Return true if the map contains an Shapelist from start to end.
	 * @param start see above.
	 * @param end see above.
	 * @return see above.
	 */
	public boolean containsKey(Coord3D start, Coord3D end)
	{
	//	for(int c = start.c; c < end.c ; c++)
		for(int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
			for(int z = start.getZSection(); z < end.getZSection() ; z++)
				if(!coordMap.containsKey(new Coord3D(z, t)))
						return false;
		return true;
	}
	
	/**
	 * Get the ROIShapeList for the plane coord.
	 * @param coord the plane the list is on.
	 * @return see above.
	 * @throws NoSuchROIException Thrown if no ROIShapes on coord.
	 */
	public  ShapeList getShapeList(Coord3D coord) throws NoSuchROIException
	{
		if(!containsKey(coord))
			throw new NoSuchROIException("No ROIShape on coord " + coord);
		return coordMap.get(coord);
	}
	
	/**
	 * Get the shapeLists for planes [start, end], this list is sorted by 
	 * Coord3D. 
	 * @param start The start plane.
	 * @param end The end plane.
	 * @return see above.
	 * @throws NoSuchROIException Thrown if the map does not contain planes in
	 * all [start, end].
	 */
	public  SortedMap<Coord3D,ShapeList> getShapeList(Coord3D start, Coord3D end) 
													throws NoSuchROIException
	{
		if(!containsKey(start, end))
			throw new NoSuchROIException();
		return coordMap.subMap(start, end);
	}
	
	/**
	 * Delete the ROI with id from coordMap and all ROIShapes.
	 * @param roi id of the ROI.
	 * @throws NoSuchROIException Thrown if the ROI does not exist.
	 */
	public void deleteROI(ROI roi) throws NoSuchROIException
	{
		TreeMap<Coord3D, ROIShape> roiMap = roi.getShapes();
		Iterator<ROIShape> roiIterator = roiMap.values().iterator();
		while(roiIterator.hasNext())
		{
			ROIShape shape = roiIterator.next();
			ShapeList list = coordMap.get(shape.getCoord3D());
			list.deleteShape(shape.getID());
		}
	}
	
	/**
	 * Delete the ROIShape on coord in ROI with id from coordMap.
	 * @param id id of the ROI.
	 * @param coord the plane.
	 * @throws NoSuchROIException Thrown if the ROI or ROIShape does not exist.
	 */
	public void deleteShape(long id, Coord3D coord) throws NoSuchROIException
	{
		ShapeList shapeList = coordMap.get(coord);
		shapeList.deleteShape(id);
		if(shapeList.getList().size()==0)
			coordMap.remove(coord);
	}
	
	/**
	 * Add the ROI to the CoordMap, if the ROIShapeList for a coord does not 
	 * exist then create it before adding the ROIShapes in the ROI to the list.
	 * @param roi the roi to be added.
	 */
	public void add(ROI roi)
	{
		TreeMap<Coord3D, ROIShape> roiMap = roi.getShapes();
		Iterator roiIterator = roiMap.keySet().iterator();
		while(roiIterator.hasNext())
		{
			ROIShape shape = roiMap.get(roiIterator.next());
			if(!coordMap.containsKey(shape.getCoord3D()))
				createShapeList(shape.getCoord3D());
			ShapeList shapeList = coordMap.get(shape.getCoord3D());
			shapeList.add(roi.getID(), shape);
		}
	}
	
	/**
	 * Add the ROIShape to the CoordMap, if the ROIShapeList for a coord does not 
	 * exist then create it before adding the ROIShape to the list.
	 * @param id the roi to be added.
	 * @param coord the coord of the ROIShape.
	 * @param shape the ROIShape to add.
	 */
	public void addShape(long id, Coord3D coord, ROIShape shape)
	{
		if(!coordMap.containsKey(coord))
			createShapeList(coord);
		ShapeList shapeList = coordMap.get(coord);
		shapeList.add(id, shape);
	}
	
	/**
	 * Create the ROIShape list for the plane coord.
	 * @param coord the plane.
	 */
	private void createShapeList(Coord3D coord)
	{
		ShapeList shapeList;
		shapeList = new ShapeList(coord);
		coordMap.put(coord, shapeList);
	}
	
}
