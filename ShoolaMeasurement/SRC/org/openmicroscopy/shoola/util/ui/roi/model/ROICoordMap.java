/*
 * roi.model.ROICoordMap 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.roi.model;


//Java imports
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

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
		for(int c = start.c; c < end.c ; c++)
			for(int t = start.t; t < end.t ; t++)
				for(int z = start.z; z < end.z ; z++)
					if(!coordMap.containsKey(new Coord3D(c, t, z)))
						return false;
		return true;
	}
	
	public  ShapeList getShapeList(Coord3D coord) throws NoSuchShapeException
	{
		if(!containsKey(coord))
			throw new NoSuchShapeException();
		return coordMap.get(coord);
	}
	
	public  SortedMap<Coord3D,ShapeList> getShapeList(Coord3D start, Coord3D end) 
													throws NoSuchShapeException
	{
		if(!containsKey(start, end))
			throw new NoSuchShapeException();
		return coordMap.subMap(start, end);
	}
	
	public void deleteROI(ROI roi)
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
	
	public void deleteShape(long id, Coord3D coord)
	{
		ShapeList shapeList = coordMap.get(coord);
		shapeList.deleteShape(id);
		if(shapeList.getList().size()==0)
			coordMap.remove(coord);
	}
	
	public void deleteShape(long id, Coord3D start, Coord3D end) 
													throws NoSuchShapeException
	{
		if(!containsKey(start, end))
			throw new NoSuchShapeException();
		
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
	
	public void propagateShape(ROIShape selectedShape, Coord3D start, Coord3D end)
	{
		createShapeList(start, end);
		TreeMap<Coord3D, ShapeList> subMap = (TreeMap<Coord3D, ShapeList>) coordMap.subMap(start, end);
		Iterator shapeListIterator = subMap.keySet().iterator();
		while(shapeListIterator.hasNext())
		{
			ShapeList list = (ShapeList)shapeListIterator.next();
			list.add(selectedShape.getID(), new ROIShape(selectedShape.getROI(), list.getCoord3D(), selectedShape));
		}
	}
	
	private void createShapeList(Coord3D coord)
	{
		ShapeList shapeList;
		shapeList = new ShapeList(coord);
		System.err.println("ROICoordMap: Created Shapelist");
		coordMap.put(coord, shapeList);
		System.err.println("ROICoordMap: added Shapelist to : " + coord.c + " " + coord.z);
	}
	
	private void createShapeList(Coord3D start, Coord3D end)
	{
		for( int c = start.c ; c < end.c ; c++) 
			for( int t = start.t ; t < end.t ; t++)
				for( int z = start.z ; z < end.z ; z++)
				{
					ShapeList shapeList;
					Coord3D coord = new Coord3D(c, t, z);
					if(!coordMap.containsKey(coord))
					{
						shapeList = new ShapeList(coord);
						coordMap.put(coord, shapeList);
					}
				}
	}
	
}
