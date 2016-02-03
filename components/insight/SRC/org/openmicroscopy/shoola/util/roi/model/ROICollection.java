/*
 * org.openmicroscopy.shoola.util.roi.model.ROICollection 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

//Third-party libraries


import omero.gateway.model.FolderData;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIMap;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationshipMap;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipMap;
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
public class ROICollection
	extends Component
	implements PropertyChangeListener
{
	
	/** A TreeMap object of the ROI, id and ROIShapeList Coord Map.*/
	private ROIMap					roiMap;
	
	/** The ROIRelationships Map relating ROI to relationships. */
	private ROIRelationshipMap 		roiRelationshipMap;
	
	/** The ROIShapeRelatiions map relating ROIShapes to Relationships. */
	private ROIShapeRelationshipMap shapeRelationshipMap;
	
	/** The last id assigned to a ROI. */
	private long					lastID;
	
	/**
	 * The ROICollection contains the separate objects which manage the 
	 * ROI, ROIShape and Relationships. 
	 *
	 */
	public ROICollection()
	{
		roiMap = new ROIMap();
		roiRelationshipMap = new ROIRelationshipMap();
		shapeRelationshipMap = new ROIShapeRelationshipMap();
		generateInitialID();
	}
	
	/** 
	 * Create a staring ROI. This is the method that most likely will call the
	 * DB and get assigned an ROI. The resulting id will then offset all ROI's 
	 * created from this point, so that when the ROIs are saved back to the DB 
	 * the id's can map correcly to the DB's ids.
	 */
	private void generateInitialID()
	{
		lastID = 0;
	}

	/**
	 * Get the next id in the system. 
	 * @return next id.
	 */
	public long getNextID()
	{
		lastID = lastID+1;
		return lastID;
	}
	
	/**
	 * Returns true if the roiComponent contains the roi with id.
	 * @param id see above.
	 * @return see above.
	 */
	public boolean containsROI(long id)
	{
		return roiMap.containsROI(id);
	}

	/**
	 * Create an ROI with id. Add the ROI to the ROIMap, specifying if the ROI
	 * is clientside.
	 * 
	 * @param id see above.
	 * @param clientSideObject see above.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 * @param folders The folders the ROI is part of
	 * @return see above.
	 */
	public ROI createROI(long id, boolean clientSideObject, 
			boolean editable, boolean deletable, boolean annotatable, Collection<FolderData> folders)
	{
		ROI newROI = new ROI(id, clientSideObject, editable, deletable, 
				annotatable);
		if (folders != null)
		    newROI.getFolders().addAll(folders);
		if (lastID < id) lastID = id+1;
		roiMap.add(newROI.getID(), newROI);
		return newROI;
	}
	
	/**
	 * Create a new ROI, assign it an id and add it to the ROIMap.
	 * @return newly created ROI.
	 */
	public ROI createROI()
	{
		ROI newROI = new ROI(getNextID(), true, true, true, true);
		roiMap.add(newROI.getID(), newROI);
		return newROI;
	}
	
	/**
	 * Get the Map containing the ROI. 
	 * @return return the ROIMap.
	 */
	public TreeMap<Long, ROI> getROIMap()
	{
		return roiMap.getROIMap();
	}
	
	/**
	 * Return the ROI with id.
	 * @param id see above.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROI does not exist.
	 */
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiMap.getROI(id);
	}

	/**
	 * Get the ROIShape with ROI id and on plane coord.
	 * @param id id of the ROI.
	 * @param coord plane where ROIShape resides.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if ROIShape does not exist.
	 */
	public ROIShape getShape(long id, Coord3D coord) 
												throws 	NoSuchROIException
														
	{
		return roiMap.getShape(id, coord);
	}

	/**
	 * Get the ROIShapeList for ROIShapes on plane coord
	 * @param coord plane where ROIShapes resides.
	 * @return see above.
	 * @throws NoSuchROIException Throw exception if no shapes on plane.
	 */
	public ShapeList getShapeList(Coord3D coord) throws
														NoSuchROIException
	{
		return roiMap.getShapeList(coord);
	}

	/**
	 * Delete the ROI with id.
	 * @param id see above.
	 * @throws NoSuchROIException Throw exception if ROI does not exist.
	 */
	public void deleteROI(long id) throws NoSuchROIException
	{
		roiMap.deleteROI(id);
	}

	/**
	 * Delete the ROIShape with ROI.Id id and on plane coord. If the ROI 
	 * will not contain any ROIShapes after this shape is deleted, delete ROI.
	 * @param id id of the ROI.
	 * @param coord coord of the ROIShape.
	 * @throws NoSuchROIException Throw exception if the ROI or ROIShape 
	 * does not exist.
	 */
	public void deleteShape(long id, Coord3D coord) 	
												throws 	NoSuchROIException
														
	{
		roiMap.deleteShape(id, coord);
	}

	
	/**
	 * Add the ROIShape shape to the ROI on plane coord. 
	 * @param id id of the ROI.
	 * @param coord coord of the plane.
	 * @param shape the ROIShape.
	 * @throws ROICreationException Thrown if shape already exists in ROI.
	 * @throws NoSuchROIException Thrown if ROI does not exist.
	 */
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROICreationException, 
													   NoSuchROIException
	{
		roiMap.addShape(id, coord, shape);
	}	

	/**
	 * Propagate the ROIShape on plane 'selectedShape' through the planes 
	 * [start, end] 
	 * @param id ROI id where the selected shape belongs.
	 * @param selectedShape The coord of the ROIShape.
	 * @param start the start of the plane to propagate from.
	 * @param end the end plane to propagate to.
	 * @throws ROICreationException Thrown if an ROIShape already exists in ROI
	 * between [start,end].
	 * @throws NoSuchROIException Thrown if ROI does not exist.
	 */
	public List<ROIShape> propagateShape(long id, Coord3D selectedShape, 
			Coord3D start,  Coord3D end) 
			throws ROICreationException, NoSuchROIException									       
	{
		return roiMap.propagateShape(id, selectedShape, start, end);
	}

	
	/**
	 * Delete the ROIShape from the ROI from [start, end], if there are missing
	 * ROIShapes between [start, end] the component will to the next plane. 
	 * @param id ROI id where the selected shape belongs.
	 * @param start the start of the plane to delete from.
	 * @param end the end plane to delete to.
	 * @throws NoSuchROIException Thrown if ROI does not exist.
	 */
	public void deleteShape(long id, Coord3D start, Coord3D end) 
												throws 	NoSuchROIException
														
	{
		roiMap.deleteShape(id, start, end);
	}

	/**
	 * Create a relationship in the ROI relationship map.
	 * @param relationship the relationship to add to the map.
	 */
	public void addROIRelationship(ROIRelationship relationship)
	{
		roiRelationshipMap.add(relationship);
	}
	
	/**
	 * Create a relationship in the ROIShape relationship map.
	 * @param relationship the relationship to add to the map.
	 */
	public void addROIShapeRelationship(ROIShapeRelationship relationship)
	{
		shapeRelationshipMap.add(relationship);
	}
	
	/**
	 * Remove relationship from the ROIRelationshipMap.
	 * @param relationship the id of the relationship.
	 */
	public void removeROIRelationship(long relationship)
	{
		roiRelationshipMap.remove(relationship);
	}
	
	/**
	 * Remove relationship from the ROIShapeRelationshipMap.
	 * @param relationship the id of the relationship.
	 */
	public void removeROIShapeRelationship(long relationship)
	{
		shapeRelationshipMap.remove(relationship);
	}
	
	/**
	 * Return true if relationship exists in the ROI relationshipmap.
	 * @param relationship the id of the relationship.
	 * @return see above.
	 */
	public boolean containsROIRelationship(long relationship)
	{
		return roiRelationshipMap.contains(relationship);
	}
	
	/**
	 * Return true if relationship exists in the ROIShape relationshipmap.
	 * @param relationship the id of the relationship.
	 * @return see above.
	 */
	public boolean containsROIShapeRelationship(long relationship)
	{
		return shapeRelationshipMap.contains(relationship);
	}
	
	/** 
	 * Get the ROIRelationshipList, the list of all relationships 
	 * associated with the ROI.
	 * @param roiID the id of the ROI.
	 * @return see above.
	 */
	public ROIRelationshipList getROIRelationshipList(long roiID)
	{
		return roiRelationshipMap.getRelationshipList(roiID);
	}
	
	/** 
	 * Get the ROIShapeRelationshipList, the list of all relationships 
	 * associated with the ROIShapes of ROI.
	 * @param roiID the id of the ROI.
	 * @return see above.
	 */
	public ROIShapeRelationshipList getROIShapeRelationshipList(long roiID)
	{
		return shapeRelationshipMap.getRelationshipList(roiID);
	}
	
	
	/* (non-Javadoc)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

}


