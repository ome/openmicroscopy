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


//Java imports
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

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
 * @since OME3.0
 */
public class ROICoordMap 
{
	private TreeMap<Coord3D, ShapeList> coordMap;
	
	public ROICoordMap()
	{
		coordMap = new TreeMap<Coord3D, ShapeList>(new Coord3D());
	}
	
	public boolean containsKey(Coord3D coord)
	{
		return coordMap.containsKey(coord);
	}
	
	public boolean containsKey(Coord3D start, Coord3D end)
	{
	//	for(int c = start.c; c < end.c ; c++)
		for(int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
			for(int z = start.getZSection(); z < end.getZSection() ; z++)
				if(!coordMap.containsKey(new Coord3D( t, z)))
						return false;
		return true;
	}
	
	public  ShapeList getShapeList(Coord3D coord) throws NoSuchROIException
	{
		if(!containsKey(coord))
			throw new NoSuchROIException("No ROIShape on coord " + coord);
		return coordMap.get(coord);
	}
	
	public  SortedMap<Coord3D,ShapeList> getShapeList(Coord3D start, Coord3D end) 
													throws NoSuchROIException
	{
		if(!containsKey(start, end))
			throw new NoSuchROIException();
		return coordMap.subMap(start, end);
	}
	
	public void deleteROI(ROI roi) throws NoSuchROIException
	{
		TreeMap<Coord3D, ROIShape> roiMap = roi.getShapes();
		Iterator roiIterator = roiMap.keySet().iterator();
		while(roiIterator.hasNext())
		{
			ROIShape shape = (ROIShape)roiIterator.next();
			ShapeList list = coordMap.get(shape.getCoord3D());
			list.deleteShape(shape.getID());
		}
	}
	
	public void deleteShape(long id, Coord3D coord) throws NoSuchROIException
	{
		ShapeList shapeList = coordMap.get(coord);
		shapeList.deleteShape(id);
		if(shapeList.getList().size()==0)
			coordMap.remove(coord);
	}
	
	public void add(ROI roi)
	{
		TreeMap<Coord3D, ROIShape> roiMap = roi.getShapes();
		Iterator roiIterator = roiMap.keySet().iterator();
		while(roiIterator.hasNext())
		{
			ROIShape shape = roiMap.get(roiIterator.next());
			if(!coordMap.containsKey(shape.getCoord3D()));
				createShapeList(shape.getCoord3D());
			ShapeList shapeList = coordMap.get(shape.getCoord3D());
			shapeList.add(roi.getID(), shape);
		}
	}
	
	public void addShape(long id, Coord3D coord, ROIShape shape)
	{
		if(!coordMap.containsKey(coord))
			createShapeList(coord);
		ShapeList shapeList = coordMap.get(coord);
		shapeList.add(id, shape);
	}
	
	private void createShapeList(Coord3D coord)
	{
		ShapeList shapeList;
		shapeList = new ShapeList(coord);
		coordMap.put(coord, shapeList);
	}
	
	private void createShapeList(Coord3D start, Coord3D end)
	{
	//	for( int c = start.c ; c < end.c ; c++) 
		for(int t = start.getTimePoint(); t < end.getTimePoint() ; t++)
			for(int z = start.getZSection(); z < end.getZSection() ; z++)
			{
					ShapeList shapeList;
					Coord3D coord = new Coord3D(t, z);
					if(!coordMap.containsKey(coord))
					{
						shapeList = new ShapeList(coord);
						coordMap.put(coord, shapeList);
					}
				}
	}
	
}
