/*
 * roi.model.ROIIDMap 
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
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.roi.model.util.LongComparator;

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
public class ROIIDMap 
{
	private TreeMap<Long, ROI> 	roiMap;
	
	
	public ROIIDMap()
	{
		roiMap = new TreeMap<Long, ROI>(new LongComparator());
	}
	
	public boolean containsKey(long id)
	{
		return roiMap.containsKey(id);
	}
	
	public void add(long id, ROI roi)
	{
		roiMap.put(id, roi);
	}
	
	public TreeMap<Long, ROI> getROIMap()
	{
		return roiMap;
	}
	
	public ROI getROI(long id) throws NoSuchROIException
	{
		try 
		{
			return roiMap.get((Long)id);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
	}
	
	public ROIShape getShape(long id, Coord3D coord) throws NoSuchROIException,
															NoSuchShapeException
	{
		ROI roi;
		try 
		{
			roi = roiMap.get(id);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
		return roi.getShape(coord);
	}
	
	public void deleteROI(long id) throws NoSuchROIException
	{
		try 
		{
			roiMap.remove(id);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
	}
	

	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROIShapeCreationException,
													   NoSuchROIException	
	{
		ROI roi;
		try
		{
			roi = roiMap.get(id);
		}
		catch(Exception e)
		{
			throw new NoSuchROIException(e);
		}
		roi.addShape(shape);
	}		

	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
															   Coord3D end) 
												throws ROIShapeCreationException,
													   NoSuchROIException, 
													   NoSuchShapeException
	{
		if(!roiMap.containsKey(id))
			throw new NoSuchROIException();
		ROI roi = roiMap.get(id);
		if(roi.containsKey(start, end))
			throw new ROIShapeCreationException();
		roi.propagateShape(id, selectedShape, start, end);
	}

	public void deleteShape(long id, Coord3D start, Coord3D end) 
													throws 	NoSuchROIException, 
															NoSuchShapeException
	{
		if(!roiMap.containsKey(id))
			throw new NoSuchROIException();
		ROI roi = roiMap.get(id);
		if(!roi.containsKey(start, end))
			throw new NoSuchShapeException();
		roi.deleteShape(start, end);
		if(roi.getShapes().size()==0)
			roiMap.remove(id);
	}
	
	public void deleteShape(long id, Coord3D coord) 
													throws 	NoSuchROIException, 
															NoSuchShapeException
	{
		if(!roiMap.containsKey(id))
			throw new NoSuchROIException();
		ROI roi = roiMap.get(id);
		if(!roi.containsKey(coord))
			throw new NoSuchShapeException();
		roi.deleteShape(coord);
		if(roi.getShapes().size()==0)
			roiMap.remove(id);
	}
}


