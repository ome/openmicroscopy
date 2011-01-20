/*
 * org.openmicroscopy.shoola.env.data.views.MetadataHandlerView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views;


//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * Provides methods to handle the annotations.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface MetadataHandlerView
	extends DataServicesView
{

	/** Identifies that the file is of type protocol. */
	public static final int		EDITOR_PROTOCOL = 
			OmeroMetadataService.EDITOR_PROTOCOL;
	
	/** Identifies that the file is of type experiment. */
	public static final int		EDITOR_EXPERIMENT = 
		OmeroMetadataService.EDITOR_EXPERIMENT;
	
	/** Identifies that the file is of type movie. */
	public static final int		MOVIE = OmeroMetadataService.MOVIE;
	
	/** Identifies that the file is of type other. */
	public static final int		OTHER = OmeroMetadataService.OTHER;
	
	/**
	 * Loads all the containers containing the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param nodeType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param nodeID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadContainers(Class nodeType, long nodeID, long userID,
							AgentEventListener observer);

	/**
	 * Loads all the ratings attached by a given user to the specified objects.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param nodeType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param nodeIDs	The collection of ids of the passed node type.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadRatings(Class nodeType, List<Long> nodeIDs, 
								long userID, AgentEventListener observer);
	
	/**
	 * Loads the thumbnails associated to the passed image i.e. 
	 * one thumbnail per specified user.
	 * 
	 * @param image			The image to handle.
	 * @param userIDs		The collection of users.
	 * @param thumbWidth	The width of the thumbnail.
	 * @param thumbHeight	The height of the thumbnail.
	 * @param observer		Call-back handler.
     * @return A handle that can be used to cancel the call.
     */
	public CallHandle loadThumbnails(ImageData image, Set<Long> userIDs, 
						int thumbWidth, int thumbHeight, 
						AgentEventListener observer);

	/**
	 * Loads all annotations related the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param dataObject	The object to handle. Mustn't be <code>null</code>.
	 * @param userID		Pass <code>-1</code> if no user specified.
	 * @param observer  	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadStructuredData(Object dataObject, long userID,
										AgentEventListener observer);
	
	/**
	 * Loads all annotations related the specified objects.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param data		The objects to handle. Mustn't be <code>null</code>.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param viewed	Pass <code>true</code> to load the rendering settings 
	 * 					related to the objects, <code>false<code>
	 * 					otherwise.
	 * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadStructuredData(List<DataObject> data, long userID,
			boolean viewed, AgentEventListener observer);
	
	/**
	 * Loads the existing annotations defined by the annotation type
	 * linked to a given type of object.
	 * Loads all the annotations if the object's type is <code>null</code>.
	 * 
	 * @param annotation 	The annotation type. Mustn't be <code>null</code>.
	 * @param userID		The id of the user the annotations are owned by,
	 * 						or <code>-1</code> if no user specified.
	 * @param observer  	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadExistingAnnotations(Class annotation, long userID, 
									AgentEventListener observer);

	/**
	 * Saves the object, adds (resp. removes) annotations to (resp. from)
	 * the object if any.
	 * 
	 * @param data		The data objects to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param metadata	The acquisition metadata.
	 * @param userID	The id of the user.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveData(Collection<DataObject> data, 
					List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
					List<Object> metadata,	long userID, 
					AgentEventListener observer);
	
	/**
	 * Saves the objects contained in the passed <code>DataObject</code>s, 
	 * adds (respectively removes) annotations to (respectively from)
	 * the object if any.
	 * 
	 * @param data		The data objects to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveBatchData(Collection<DataObject> data, 
					List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
						long userID, AgentEventListener observer);
	
	/**
	 * Saves the objects contained in the passed <code>DataObject</code>s, 
	 * adds (respectively removes) annotations to (respectively from)
	 * the object if any.
	 * 
	 * @param timeRefObject The object hosting the time period.
	 * @param toAdd			Collection of annotations to add.
	 * @param toRemove		Collection of annotations to remove.
	 * @param userID		The id of the user.
	 * @param observer		Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveBatchData(TimeRefObject timeRefObject, 
					List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
						long userID, AgentEventListener observer);
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param file		The file to copy the date into.
	 * @param fileID	The id of the original file.
	 * @param size		The size of the file.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFile(File file, long fileID, long size, 
							AgentEventListener observer);
	
	/**
	 * Loads the annotation corresponding to the passed id.
	 * 
	 * @param annotationID	The id of the annotation file.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadAnnotation(long annotationID, 
							AgentEventListener observer);
	
	/**
	 * Loads the original files related to a given pixels set.
	 * 
	 * @param pixelsID The collection of the pixels sets.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadOriginalFiles(Collection<Long> pixelsID, 
							AgentEventListener observer);
	
	/**
	 * Filters by annotation.
	 * 
	 * @param nodeType			The type of node.
	 * @param nodeIds			The collection of nodes to filter.
	 * @param annotationType 	The type of annotation to filter by.
	 * @param terms				The terms to filter by.		
	 * @param userID			The ID of the user.
	 * @param observer			Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterByAnnotation(Class nodeType, List<Long> nodeIds, 
			Class annotationType, List<String> terms, long userID,
			AgentEventListener observer);

	/**
	 * Filters by annotated nodes.
	 * 
	 * @param nodeType			The type of node.
	 * @param nodeIds			The collection of nodes to filter.
	 * @param annotationType 	The type of annotation to filter by.
	 * @param annotated			Pass <code>true</code> to retrieve the 
	 *                          annotated nodes, <code>false</code> otherwise.
	 * @param userID			The ID of the user.
	 * @param observer			Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterByAnnotated(Class nodeType, List<Long> nodeIds, 
			Class annotationType, boolean annotated, long userID,
			AgentEventListener observer);
	
	/**
	 * Filters the data.
	 * 
	 * @param nodeType	The type of node.
	 * @param nodeIds	The collection of nodes to filter.
	 * @param context	The filtering context.
	 * @param userID	The ID of the user.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterData(Class nodeType, List<Long> nodeIds,
			FilterContext context, long userID, AgentEventListener observer);

	/** 
	 * Creates a new <code>Dataobject</code> and adds the children to the
	 * newly created node.
	 * 
	 * @param parent	The parent of the <code>DataObject</code> to create
	 * 					or <code>null</code> if no parent specified.
	 * @param data		The <code>DataObject</code> to create.
	 * @param children	The nodes to add to the newly created 
	 * 					<code>DataObject</code>.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle createDataObject(DataObject parent, DataObject data,
							Collection children, AgentEventListener observer);
	
	/**
	 * Saves the file back to the server.
	 * 
	 * @param fileAnnotation	The file to save back to the server.
	 * @param file				The id of the file if previously saved.
	 * @param index				One of the constants defined by this class.
	 * @param linkTo			The <code>DataObject</code> the
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveFile(FileAnnotationData fileAnnotation, File file, 
			int index, DataObject linkTo, AgentEventListener observer);

	/**
	 * Updates the data objects. This method will for now only be implemented
	 * for the plate or wells.
	 * 
	 * @param objects	The objects to update. Mustn't be <code>null</code>.
	 * @param observer	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateDataObjects(List<DataObject> objects, 
			AgentEventListener observer);

	/**
	 * Submits the files to the QA system.
	 * 
	 * @param details  Object containing the information to send.
	 * @param observer Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle submitFile(MessengerDetails details, 
			AgentEventListener observer);
	
	/**
	 * Loads the measurements related the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param dataObject	The object to handle. Mustn't be <code>null</code>.
	 * @param userID		Pass <code>-1</code> if no user specified.
	 * @param observer  	Call-back handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadROIMeasurement(Object dataObject, long userID,
										AgentEventListener observer);
	
}
