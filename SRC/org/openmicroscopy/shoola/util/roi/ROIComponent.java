/*
 * org.openmicroscopy.shoola.util.roi.ROIComponent 
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
package org.openmicroscopy.shoola.util.roi;


//Java imports
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.roi.io.XMLFileIOStrategy;
import org.openmicroscopy.shoola.util.roi.io.XMLIOStrategy;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROICollection;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * The ROI Component is the main interface to the object which control the 
 * creations, storage, deletion and manipulation of ROIs. 
 * 
 * The ROIComponent also accesses(currently) the IOStrategy for loading and 
 * saving ROIs.
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
public 	class ROIComponent 
		extends Component 
		implements PropertyChangeListener
{
	/** The main object for storing and manipulating ROIs. */
	private ROICollection		roiCollection;
	
	/** The object used to load and save ROIs. */
	private XMLIOStrategy		ioStrategy;
	
	/** 
	 * ROIComponent instatiates a basic FileIO strategy and roiCollection 
	 * object.
	 */
	public ROIComponent()
	{
		roiCollection = new ROICollection();
		ioStrategy = new XMLFileIOStrategy();
	}
	
	/** 
	 * Save the current ROI data to file. 
	 * @param filename name of the file to save to, including path.
	 * @throws IOException 
	 */
	public void saveROI(String filename) throws IOException
	{
		ioStrategy.write(filename, this);
	}
	
	/**
	 * Load the ROIs from file to the roiComponent. 
	 * 
	 * @param filename file name to load ROIs from.
	 * @throws IOException	- file handling error.
	 * @throws ROIShapeCreationException - If an error occured while creating 
	 * 									   ROIShape, basic assumption is this is 
	 * 									   linked to memory issues.
	 * @throws NoSuchROIException		 - Tried to access a ROI which does not
	 * 									   Exist. In this case most likely reason
	 * 									   is that a LineConnectionFigure tried
	 * 									   to link to ROIShapes which have not 
	 * 									   been created yet.
	 * @throws ROICreationException		 - See ROIShapeCreationException.
	 */
	public void loadROI(String filename) throws IOException, ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		ioStrategy.read(filename, this);
	}
	
	/**
	 * Generate the next ID for a new ROI. This method will possibly be replaced 
	 * with a call to the database for the generation of an ROI id.
	 * 
	 * @return see above.
	 */
	public long getNextID()
	{
		return roiCollection.getNextID();
	}
	
	/**
	 * Create a ROI with an ROI id == id. This method is called from the IO
	 * strategy to create a pre-existing ROI from file.
	 * 
	 * Note : if a ROI is created with the same ID the new ROI will replace the
	 * old one.
	 * @param id - ROI id. 
	 * @return see above.
	 * @throws ROICreationException			If an error occured while creating 
	 * 									   	an ROI, basic assumption is this is 
	 * 									   	linked to memory issues.
	 */
	public ROI createROI(long id) 				throws ROICreationException
	{
		return roiCollection.createROI(id);
	}
	
	/**
	 * Create a new ROI, assign it an ROI from the getNextID call.
	 * @return new ROI. 
	 * @throws ROICreationException			If an error occured while creating 
	 * 									   	an ROI, basic assumption is this is 
	 * 									   	linked to memory issues.
	 */
	public ROI createROI() 						throws 	ROICreationException
	{
		return roiCollection.createROI();
	}
	
	/**
	 * Get the roiMap which is the TreeMap containing the ROI, ROI.id pairs. 
	 * It is an ordered Tree. 
	 * 
	 * @return See above.
	 */
	public TreeMap<Long, ROI>  getROIMap()
	{
		return roiCollection.getROIMap();
	}
	
	/**
	 * Get the ROI with the id == id. This is obtained by a searh of the ROIMap. 
	 * @param id the ROI.id that is being requested.
	 * @return the ROI with id 
	 * @throws NoSuchROIException if a ROI.id is used which does not exist this
	 * 								exception is thrown.
	 */
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiCollection.getROI(id);
	}
	
	/**
	 * Get the RIOShape which is part of the ROI id, and exists on the plane
	 * coord. This method looks up the ROIIDMap (TreeMap) for the ROI with id 
	 * and then looks up that ROIs TreeMap for the ROIShape on the plane coord.
	 * @param id - id of the ROI the ROIShape is a member of.
	 * @param coord - the plane where the ROIShape sits.
	 * @return See Above.
	 * @throws NoSuchROIException	if a ROI.id is used which does not exist this
	 * 								exception is thrown.
	 * @throws NoSuchShapeException	if the ROI does not contain the plane
	 * 								coord then this exception is thrown.
	 */
	public ROIShape getShape(long id, Coord3D coord) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		return roiCollection.getShape(id, coord);
	}
	
	/** 
	 * Return the list of ROIShapes which reside on the plane coord. ShapeList is
	 * an object which contains a TreeMap of the ROIShapes and ROIId of those shapes.
	 * 
	 * 
	 * @param coord
	 * @return see above. 
	 * @throws NoSuchShapeException if no shapes are on plance Coord then 
	 * 								throw NoSuchShapeException.
	 */
	public ShapeList getShapeList(Coord3D coord) throws
														NoSuchShapeException
	{
		return roiCollection.getShapeList(coord);
	}
	
	/** 
	 * Delete the ROI and all it's ROIShapes from the system.
	 * 
	 * @param id ROI to delete. 
	 * @throws NoSuchROIException	This exception is thrown in the ROI to be
	 * 								deleted does not exist.
	 */
	public void deleteROI(long id) throws NoSuchROIException
	{
		roiCollection.deleteROI(id);
	}
	
	/** 
	 * Delete the ROIShape from the ROI with id. 
	 * 
	 * @param id ROI id inwhich the ROIShape is a member.
	 * @param coord	the plane on which the ROIShape resides. 
	 * @throws NoSuchROIException	This exception is thrown in the ROI 
	 * 								does not exist.
	 * @throws NoSuchShapeException The exception is thrown if the ROIShape 
	 * 								does not exist on the plane coord. 
	 */
	public void deleteShape(long id, Coord3D coord) 	
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiCollection.deleteShape(id, coord);
	}
	
	/**
	 * Add a ROIShape to the ROI.id at coord. The ROIShape should be created 
	 * before hand.
	 * 
	 * Note : if a shape already exist with ROI.id and Coord.coord it will
	 * be replaced with the new ROIShape. 
	 * Note : if the shape is inserted in multiple places the system will behave
	 * oddly. 
	 * 
	 * @param id ROI id
	 * @param coord plane
	 * @param shape shape to add
	 * @throws ROIShapeCreationException	- Exception will be thrown if the 
	 * 										  ROIShape cannot be created.
	 * @throws NoSuchROIException			- Exception will be thrown id the ROI
	 * 										  the ROIShape should be added to 	
	 * 										  does not exist. 
	 */
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws ROIShapeCreationException, 
													   NoSuchROIException
	{
		roiCollection.addShape(id, coord, shape);
	}	
	
	/**
	 * This method will create new versions of the ROIShape belonging to ROI.id
	 * on plane coord and propagate it from plane start to end. If the shape 
	 * exists on a plane between start and end it will not be overwritten.
	 *
	 * Note : iteration for planes occurs through z then t.
	 *
	 * @param id ROI id.
	 * @param selectedShape plane where shape is to be duplicated from.
	 * @param start plane to propagate from
	 * @param end 	plane to propagate to 
	 * @throws ROIShapeCreationException	- Exception if shape cannot be created.
	 * @throws NoSuchROIException			- Exception if ROI with id does not
	 * 											exist.
	 * @throws NoSuchShapeException			- Exception if a ROIShape belonging 
	 * 											to ROI.id does not exist on plane
	 * 											coord.
	 */
	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
															   Coord3D end) 
												throws ROIShapeCreationException, 
												       NoSuchROIException, 
												       NoSuchShapeException
	{
		roiCollection.propagateShape(id, selectedShape, start, end);
	}
	
	/**
	 * Delete the ROIShape belonging to ROI.id from plane start to plane end.
	 * This method requires that the object belongs on all planes from start 
	 * to end, if it does not then it will throw an NoSuchShapeException.
	 * 
	 * Note : iteration for planes occurs through z then t.
	 * 
	 * @param id ROI id.
	 * @param start start plane
	 * @param end end plane
	 * @throws NoSuchROIException - returned if no such ROI exists.
	 * @throws NoSuchShapeException - see above.
	 */
	public void deleteShape(long id, Coord3D start, Coord3D end) 
												throws 	NoSuchROIException, 
														NoSuchShapeException
	{
		roiCollection.deleteShape(id, start, end);
	}
	
	/**
	 * Add an ROIRelationship to the system, the ROIRelationship will be parsed
	 * to see what has to be setup to create relationships. This is a relationship
	 * between ROIs. 
	 * 
	 * @param relationship
	 */
	public void addROIRelationship(ROIRelationship relationship)
	{
		roiCollection.addROIRelationship(relationship);
	}
	
	/**
	 * Add an ROIShapeRelationship to the system, the ROIShapeRelationship will 
	 * be parsed to see what has to be setup to create relationships. This is a 
	 * relationship between ROIShapes. 
	 * 
	 * @param relationship
	 */
	public void addROIShapeRelationship(ROIShapeRelationship relationship)
	{
		roiCollection.addROIShapeRelationship(relationship);
	}
	
	/**
	 * Remove the ROIRelationship with id from the system.
	 * @param relationship the id of the relationship being removed.
	 */
	public void removeROIRelationship(long relationship)
	{
		roiCollection.removeROIRelationship(relationship);
	}
	
	
	/**
	 * Remove the ROIShapeRelationship with id from the system.
	 * @param relationship the id of the relationship being removed.
	 */
	public void removeROIShapeRelationship(long relationship)
	{
		roiCollection.removeROIShapeRelationship(relationship);
	}
	
	/**
	 * Return true if the ROIRelationship exists in the system.
	 * 
	 * @param relationship the id of the relationship.
	 * @return see above.
	 */public boolean containsROIRelationship(long relationship)
	{
		return roiCollection.containsROIRelationship(relationship);
	}
	
	 /**
	 * Return true if the ROIShapeRelationship exists in the system.
	 * 
	 * @param relationship the id of the relationship.
	 * @return see above.
	 */
	public boolean containsROIShapeRelationship(long relationship)
	{
		return roiCollection.containsROIShapeRelationship(relationship);
	}
	
	/**
	 * Return the ROIRelationships which relate to ROI with id.
	 * @param roiID id to find relationships which belong to it.
	 * @return see above.
	 */
	public ROIRelationshipList getROIRelationshipList(long roiID)
	{
		return roiCollection.getROIRelationshipList(roiID);
	}
	
	/**
	 * Return the ROIShapeRelationships which relate to ROIShape with ROI id.
	 * @param roiID id to find relationships which belong to it.
	 * @return see above.
	 */
	public ROIShapeRelationshipList getROIShapeRelationshipList(long roiID)
	{
		return roiCollection.getROIShapeRelationshipList(roiID);
	}
	
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

}


