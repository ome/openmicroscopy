/*
 * roi.model.ROIMap 
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

//Third-party libraries

//Application-internal dependencies
import java.util.TreeMap;

import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROICoordMap;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIIDMap;
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
public class ROIMap 
{
	
	ROICoordMap				roiCoordMap;
	ROIIDMap				roiIDMap;
	
	public ROIMap()
	{
		roiCoordMap = new ROICoordMap();
		roiIDMap	= new ROIIDMap();
	}
	
	public void add(Long id, ROI roi)
	{
		roiIDMap.add(id, roi);
		roiCoordMap.add(roi);
	}

	public TreeMap<Long, ROI>  getROIMap()
	{
		return roiIDMap.getROIMap();
	}
	
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiIDMap.getROI(id);
	}

	public ROIShape getShape(long id, Coord3D coord) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		return roiIDMap.getShape(id, coord);
	}

	public ShapeList getShapeList(Coord3D coord) throws
														NoSuchShapeException
	{	
		return roiCoordMap.getShapeList(coord);
	}

	public void deleteROI(long id) throws NoSuchROIException
	{
		ROI roi = roiIDMap.getROI(id);
		roiCoordMap.deleteROI(roi);
		roiIDMap.deleteROI(id);
	}

	public void deleteShape(long id, Coord3D coord) 	
											throws 	NoSuchROIException, 
													NoSuchShapeException
	{
		roiCoordMap.deleteShape(id, coord);
		roiIDMap.deleteShape(id, coord);
	}

	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROIShapeCreationException, 
												NoSuchROIException
	{
		roiIDMap.addShape(id, coord, shape);
		roiCoordMap.addShape(id, coord, shape);
	}		

	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
															   Coord3D end) 
												throws ROIShapeCreationException, 
													   NoSuchROIException, 
													   NoSuchShapeException
	{
		roiIDMap.propagateShape(id, selectedShape, start, end);
		ROIShape shape = roiIDMap.getShape(id, selectedShape);
		roiCoordMap.propagateShape(shape, start, end);
	}

	public void deleteShape(long id, Coord3D start, Coord3D end) 
													throws 	NoSuchROIException, 
															NoSuchShapeException
	{
		roiCoordMap.deleteShape(id, start, end);
		roiIDMap.deleteShape(id, start, end);
	}

}


