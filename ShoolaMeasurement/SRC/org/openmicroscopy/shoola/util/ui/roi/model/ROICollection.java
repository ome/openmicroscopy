/*
 * roi.model.ROICollection 
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
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIMap;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIRelationship;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIRelationshipList;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIRelationshipMap;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShapeRelationship;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShapeRelationshipList;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShapeRelationshipMap;
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
public class ROICollection
	extends Component
	implements PropertyChangeListener
{
	private ROIMap					roiMap;
	private ROIRelationshipMap 		roiRelationshipMap;
	private ROIShapeRelationshipMap shapeRelationshipMap;
	static  long					lastID;
	
	
	public ROICollection()
	{
		roiMap = new ROIMap();
		roiRelationshipMap = new ROIRelationshipMap();
		shapeRelationshipMap = new ROIShapeRelationshipMap();
		generateInitialID();
	}
	
	private void generateInitialID()
	{
		lastID = 0;
	}
	
	public long getNextID()
	{
		lastID = lastID+1;
		return lastID;
	}
	
	public ROI createROI(long id)
	{
		ROI newROI = new ROI(getNextID());
		lastID = id+1;
		roiMap.add(newROI.getID(), newROI);
		return newROI;
	}
	
	public ROI createROI()
	{
		ROI newROI = new ROI(getNextID());
		roiMap.add(newROI.getID(), newROI);
		return newROI;
	}
	
	public TreeMap<Long, ROI> getROIMap()
	{
		return roiMap.getROIMap();
	}
	
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiMap.getROI(id);
	}

	public ROIShape getShape(long id, Coord3D coord) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		return roiMap.getShape(id, coord);
	}

	public ShapeList getShapeList(Coord3D coord) throws
														NoSuchShapeException
	{
		return roiMap.getShapeList(coord);
	}

	public void deleteROI(long id) throws NoSuchROIException
	{
		roiMap.deleteROI(id);
	}

	public void deleteShape(long id, Coord3D coord) 	
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiMap.deleteShape(id, coord);
	}

	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROIShapeCreationException, 
													   NoSuchROIException
	{
		roiMap.addShape(id, coord, shape);
	}	

	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
				   Coord3D end) 
												throws ROIShapeCreationException, 
												       NoSuchROIException, 
												       NoSuchShapeException
	{
		roiMap.propagateShape(id, selectedShape, start, end);
	}

	public void deleteShape(long id, Coord3D start, Coord3D end) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiMap.deleteShape(id, start, end);
	}

	public void addROIRelationship(ROIRelationship relationship)
	{
		roiRelationshipMap.add(relationship);
	}
	
	public void addROIShapeRelationship(ROIShapeRelationship relationship)
	{
		shapeRelationshipMap.add(relationship);
	}
	
	public void removeROIRelationship(long relationship)
	{
		roiRelationshipMap.remove(relationship);
	}
	
	public void removeROIShapeRelationship(long relationship)
	{
		shapeRelationshipMap.remove(relationship);
	}
	
	public boolean containsROIRelationship(long relationship)
	{
		return roiRelationshipMap.contains(relationship);
	}
	
	public boolean containsROIShapeRelationship(long relationship)
	{
		return shapeRelationshipMap.contains(relationship);
	}
	
	public ROIRelationshipList getROIRelationshipList(long roiID)
	{
		return roiRelationshipMap.getRelationshipList(roiID);
	}
	
	public ROIShapeRelationshipList getROIShapeRelationshipList(long roiID)
	{
		return shapeRelationshipMap.getRelationshipList(roiID);
	}
	
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

}


