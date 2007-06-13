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
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKeys;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes;
import org.openmicroscopy.shoola.util.roi.figures.PointAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.io.XMLFileIOStrategy;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROICollection;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationship;
import org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipList;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
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
public class ROIComponent 
	extends Component 
{
		
	/** The default color of the text. */
	private static final Color			TEXT_COLOR = Color.ORANGE;
	
	/** The default color of the measurement text. */
	private static final Color			MEASUREMENT_COLOR = 
											new Color(255, 204, 102, 255);
	
	/** The default color used to fill area. */
	private static final Color			FILL_COLOR = 
											new Color(220, 220, 220, 32);

	/** The default color used to fill area alpha'ed <sp>. */
	private static final Color			FILL_COLOR_ALPHA = 
											new Color(220, 220, 220, 0);
	
	/** The default color of the text. */
	private static final double			FONT_SIZE = 10.0;
	
	/** The default width of the stroke. */
	private static final double			STROKE_WIDTH = 1.0;
	
	/** The default color of the stroke. */
	private static final Color			STROKE_COLOR = Color.WHITE;
	
	/** The default color of the stroke alpha'ed <sp> to transparent. */
	private static final Color			STROKE_COLOR_ALPHA = 
											new Color(255, 255, 255, 128);
	/** The main object for storing and manipulating ROIs. */
	private ROICollection				roiCollection;

	/** The object used to load and save ROIs. */
	private XMLFileIOStrategy			ioStrategy;
	
	/** The number of microns per pixel in the X-Axis. */
	private double						micronsPixelX;

	/** The number of microns per pixel in the Y-Axis. */
	private double						micronsPixelY;

	/** The number of microns per pixel in the Y-Axis. */
	private double						micronsPixelZ;

	/** 
	 * Creates a new instance. Initializes an collection to keep 
	 * track of the existing ROI.
	 */
	public ROIComponent()
	{
		roiCollection = new ROICollection();
		micronsPixelX = 0;
		micronsPixelY = 0;
		micronsPixelZ = 0;
	}

	/**
	 * Set the number of microns per pixel in the x-axis. 
	 * @param x see above.
	 * 
	 */
	public void setMicronsPixelX(double x)
	{
		micronsPixelX = x;
	}

	/**
	 * Get the number of microns per pixel in the x-axis. 
	 * @return microns see above.
	 * 
	 */
	public double getMicronsPixelX()
	{
		return micronsPixelX;
	}
	
	/**
	 * Set the number of microns per pixel in the y-axis. 
	 * @param y see above.
	 * 
	 */
	public void setMicronsPixelY(double y)
	{
		micronsPixelY = y;
	}
	
	/**
	 * Get the number of microns per pixel in the y-axis. 
	 * @return microns see above.
	 * 
	 */
	public double getMicronsPixelY()
	{
		return micronsPixelY;
	}
	
	/**
	 * Set the number of microns per pixel in the z-axis. 
	 * @param z see above.
	 * 
	 */
	public void setMicronsPixelZ(double z)
	{
		micronsPixelZ = z;
	}
	
	/**
	 * Get the number of microns per pixel in the z-axis. 
	 * @return microns see above.
	 * 
	 */
	public double getMicronsPixelZ()
	{
		return micronsPixelZ;
	}

	/**
     * Helper method to set the attributes of the newly created figure.
     * 
     * @param fig The figure to handle.
     */
    private void setFigureAttributes(ROIFigure fig)
    {
    	AttributeKeys.FONT_SIZE.set(fig, FONT_SIZE);
		AttributeKeys.TEXT_COLOR.set(fig, TEXT_COLOR);
		AttributeKeys.STROKE_WIDTH.set(fig, STROKE_WIDTH);
		DrawingAttributes.SHOWMEASUREMENT.set(fig, false);
		DrawingAttributes.MEASUREMENTTEXT_COLOUR.set(fig, MEASUREMENT_COLOR);
		DrawingAttributes.SHOWTEXT.set(fig, false);
    	if (fig instanceof PointAnnotationFigure) {
    		AttributeKeys.FILL_COLOR.set(fig, FILL_COLOR_ALPHA);
    		AttributeKeys.STROKE_COLOR.set(fig, STROKE_COLOR_ALPHA);
    	} else {
    		AttributeKeys.FILL_COLOR.set(fig, FILL_COLOR);
    		AttributeKeys.STROKE_COLOR.set(fig, STROKE_COLOR);
    	}
	 }
    
    /**
     * Helper method to set the annotations of the newly created shape.
     * 
     * @param shape The shape to handle.
     */
    private void setShapeAnnotations(ROIShape shape)
	{
    	ROIFigure fig = shape.getFigure();
		String type = fig.getType();
		if (type != null) AnnotationKeys.FIGURETYPE.set(shape, type);
		
		ROIShape s = fig.getROIShape();
		AnnotationKeys.INMICRONS.set(s, false);
		AnnotationKeys.MICRONSPIXELX.set(s,  getMicronsPixelX());
		AnnotationKeys.MICRONSPIXELY.set(s,  getMicronsPixelY());
	}
    
	/**
    * Removes the specified figure from the display.
    * 
    * @param figure The figure to remove.
	 * @throws NoSuchROIException 
    */
    void removeROI(ROIFigure figure) throws NoSuchROIException
    {
    	if (figure == null) return;
    	long id = figure.getROI().getID();
    	Coord3D coord = figure.getROIShape().getCoord3D();
    	deleteShape(id, coord);	
    }
    
    /**
	 * Creates a <code>ROI</code> from the passed figure.
	 * 
	 * @param figure The figure to create the <code>ROI</code> from.
	 * @param currentPlane The plane to add figure to. 
	 * @return Returns the created <code>ROI</code>.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	ROI createROI(ROIFigure figure, Coord3D currentPlane)
		throws ROICreationException,  
			NoSuchROIException
	{
		ROI roi = createROI();
		ROIShape newShape = new ROIShape(roi, currentPlane, figure, 
							figure.getBounds());
		addShape(roi.getID(), currentPlane, newShape);
		return roi;
	}
	
    /**
     * Adds the specified figure to the display.
     * 
     * @param figure The figure to add.
     * @param currentPlane The plane to add figure to.
     * @return returns the newly created ROI. 
     * @throws NoSuchROIException 
     * @throws ROICreationException 
     */
    public ROI addROI(ROIFigure figure, Coord3D currentPlane) throws 
    													ROICreationException, 
    													NoSuchROIException
    {
    	if (figure == null) throw new NullPointerException("Figure param null.");
    	setFigureAttributes(figure);
    	ROI roi = null;
    	roi = createROI(figure, currentPlane);
		if (roi == null) throw new ROICreationException("Unable to create ROI.");
    	ROIShape shape = figure.getROIShape();
    	setShapeAnnotations(shape);
    	AnnotationKeys.ROIID.set(shape, roi.getID());
    	return roi;
    }
	
	/** 
	 * Saves the current ROI data to passed stream. 
	 * 
	 * @param output The output stream to write the ROI into.
	 * @throws ParsingException Thrown if an error occurs while creating the 
	 * 							xml element. 
	 */
	public void saveROI(OutputStream output) 
		throws ParsingException
	{
		if (output == null)
			throw new NullPointerException("No input stream specified.");
		if (ioStrategy == null) ioStrategy = new XMLFileIOStrategy();
		ioStrategy.write(output, this);
	}
	
	/**
	 * Loads the ROIs. This method should be invoked straight after creating
	 * the component.
	 * 
	 * 
	 * @param input The stream with the previously saved ROIs or 
	 * 				<code>null</code> if no ROIs previously saved.
	 * @throws ParsingException				Thrown when an error occured
	 * 										while parsing the stream.
	 * @throws NoSuchROIException		 	Tried to access a ROI which does not
	 * 									   	Exist. In this case most likely 
	 * 										reason is that a 
	 * 										LineConnectionFigure tried
	 * 									   	to link to ROIShapes which have not 
	 * 									   	been created yet.
	 * @throws ROICreationException		 	Thrown while trying to create an 
	 * 										ROI.
	 */
	public void loadROI(InputStream input) 
		throws NoSuchROIException, ParsingException, ROICreationException
				
	{
		if (input == null)
			throw new NullPointerException("No input stream specified.");
		if (ioStrategy == null) ioStrategy = new XMLFileIOStrategy();
		ioStrategy.read(input, this);
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
	public ROI createROI(long id)
		throws ROICreationException
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
	public ROI createROI()
		throws 	ROICreationException
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
	public ROI getROI(long id) 
		throws NoSuchROIException
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
	 */
	public ROIShape getShape(long id, Coord3D coord) 
	throws 	NoSuchROIException
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
	 * @throws NoSuchROIException if no shapes are on plance Coord then 
	 * 								throw NoSuchShapeException.
	 */
	public ShapeList getShapeList(Coord3D coord) throws
	NoSuchROIException
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
	 */ 
	public void deleteShape(long id, Coord3D coord) 	
	throws 	NoSuchROIException
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
	 * @throws ROICreationException	- Exception will be thrown if the 
	 * 										  ROIShape cannot be created.
	 * @throws NoSuchROIException			- Exception will be thrown id the ROI
	 * 										  the ROIShape should be added to 	
	 * 										  does not exist. 
	 */
	public 	void addShape(long id, Coord3D coord, ROIShape shape) 
												throws 	ROICreationException, 
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
	 * @throws NoSuchROIException			- Exception if ROI with id does not
	 * 											exist.
	 * @throws ROICreationException			- if the ROI cannot be created.
	 */
	public void propagateShape(long id, Coord3D selectedShape, Coord3D start, 
			Coord3D end) 
												throws  ROICreationException, 
														NoSuchROIException
	
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
	 */
	public void deleteShape(long id, Coord3D start, Coord3D end) 
	throws 	NoSuchROIException
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
	  * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	  */
	 public void propertyChange(PropertyChangeEvent evt) {
		 // TODO Auto-generated method stub

	 }

}


