/*
 * org.openmicroscopy.shoola.env.data.views.MetadataHandlerView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.FilesLoader;
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
 * @since OME3.0
 */
public interface MetadataHandlerView
	extends DataServicesView
{

	/** Indicates to load the original file if original file is not set. */
	public static final int ORIGINAL_FILE = FilesLoader.ORIGINAL_FILE;
	
	/** Indicates to load the file annotation if original file is not set. */
	public static final int FILE_ANNOTATION = FilesLoader.FILE_ANNOTATION;
	
	/** Indicates to load the metadata file linked to the image. */
	public static final int METADATA_FROM_IMAGE =
			FilesLoader.METADATA_FROM_IMAGE;
	
	/** Identifies that the file is of type movie. */
	public static final int MOVIE = OmeroMetadataService.MOVIE;
	
	/** Identifies that the file is of type other. */
	public static final int OTHER = OmeroMetadataService.OTHER;
	
	/** Identifies that the tag and tag sets used but not owned. */
	public static final int TAG_NOT_OWNED = OmeroMetadataService.TAG_NOT_OWNED;
	
	/**
	 * Loads all the containers containing the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param ctx The security context.
	 * @param nodeType The class identifying the object.
	 * Mustn't be <code>null</code>.
	 * @param nodeID The id of the node.
	 * @param userID Pass <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
         * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadContainers(SecurityContext ctx, Class nodeType,
		long nodeID, long userID, AgentEventListener observer);

	/**
	 * Loads all the ratings attached by a given user to the specified objects.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param ctx The security context.
	 * @param nodeType The class identifying the object.
	 * Mustn't be <code>null</code>.
	 * @param nodeIDs The collection of ids of the passed node type.
	 * @param userID Pass <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
         * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadRatings(SecurityContext ctx, Class nodeType,
		List<Long> nodeIDs, long userID, AgentEventListener observer);
	
	/**
	 * Loads the thumbnails associated to the passed image i.e.
	 * one thumbnail per specified user.
	 * 
	 * @param ctx The security context.
	 * @param image The image to handle.
	 * @param userIDs The collection of users.
	 * @param thumbWidth The width of the thumbnail.
	 * @param thumbHeight The height of the thumbnail.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadThumbnails(SecurityContext ctx, ImageData image,
		Set<Long> userIDs, int thumbWidth, int thumbHeight,
		AgentEventListener observer);

	/**
	 * Loads all annotations related the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param ctx The security context.
	 * @param dataObject The object to handle. Mustn't be <code>null</code>.
	 * @param userID Pass <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadStructuredData(SecurityContext ctx, Object dataObject,
			long userID, AgentEventListener observer);
	
	/**
	 * Loads all annotations related the specified objects.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param ctx The security context.
	 * @param data The objects to handle. Mustn't be <code>null</code>.
	 * @param userID Pass <code>-1</code> if no user specified.
	 * @param viewed Pass <code>true</code> to load the rendering settings 
	 * related to the objects, <code>false<code> otherwise.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadStructuredData(SecurityContext ctx,
		List<DataObject> data, long userID, boolean viewed,
		AgentEventListener observer);
	
	/**
	 * Loads all {@link DataObject}s the given annotations ({@link FileAnnotationData}) are linked to
	 * @param ctx The security context.
	 * @param annotations The annotations ({@link FileAnnotationData})
	 * @param toBeDeletedFromIds The DataObjects from which the FileAnnotations should be removed
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle checkFileAnnotationDeletion(SecurityContext ctx,
	        List<FileAnnotationData> annotations, List<DataObject> toBeDeletedFromIds,
			AgentEventListener observer);
	
	/**
	 * Loads the existing annotations defined by the annotation type
	 * linked to a given type of object.
	 * Loads all the annotations if the object's type is <code>null</code>.
	 * 
	 * @param ctx The security context.
	 * @param annotation The annotation type. Mustn't be <code>null</code>.
	 * @param userID The id of the user the annotations are owned by,
	 * or <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadExistingAnnotations(SecurityContext ctx,
			Class annotation, long userID, AgentEventListener observer);

	/**
	 * Loads the existing annotations defined by the annotation type
	 * linked to a given type of object.
	 * Loads all the annotations if the object's type is <code>null</code>.
	 * 
	 * @param ctx The security contexts.
	 * @param annotation The annotation type. Mustn't be <code>null</code>.
	 * @param userID The id of the user the annotations are owned by,
	 * or <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
         * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadExistingAnnotations(List<SecurityContext> ctx,
			Class annotation, long userID, AgentEventListener observer);
	
	/**
	 * Saves the object, adds (resp. removes) annotations to (resp. from)
	 * the object if any.
	 * 
	 * @param ctx The security context.
	 * @param data The data objects to handle.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param metadata The acquisition metadata.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveData(SecurityContext ctx, Collection<DataObject> data,
		List<AnnotationData> toAdd, List<Object> toRemove,
		List<Object> metadata, long userID, AgentEventListener observer);
	
	/**
	 * Saves the objects contained in the passed <code>DataObject</code>s,
	 * adds (respectively removes) annotations to (respectively from)
	 * the object if any.
	 * 
	 * @param ctx The security context.
	 * @param data The data objects to handle.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveBatchData(SecurityContext ctx,
		Collection<DataObject> data, List<AnnotationData> toAdd,
		List<Object> toRemove, long userID,
		AgentEventListener observer);
	
	/**
	 * Saves the objects contained in the passed <code>DataObject</code>s,
	 * adds (respectively removes) annotations to (respectively from)
	 * the object if any.
	 * 
	 * @param ctx The security context.
	 * @param timeRefObject The object hosting the time period.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveBatchData(SecurityContext ctx,
		TimeRefObject timeRefObject, List<AnnotationData> toAdd,
		List<Object> toRemove, long userID,
		AgentEventListener observer);
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param ctx The security context.
	 * @param file The file to copy the date into.
	 * @param fileID The id of the original file.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFile(SecurityContext ctx, File file, long fileID,
			AgentEventListener observer);
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param ctx The security context.
	 * @param file The file to copy the date into.
	 * @param fileID The id of the original file.
	 * @param index The index of the files
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFile(SecurityContext ctx, File file, long fileID,
			int index, AgentEventListener observer);
	
	/**
	 * Loads the annotation corresponding to the passed id.
	 * 
	 * @param ctx The security context.
	 * @param annotationID The id of the annotation file.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadAnnotation(SecurityContext ctx, long annotationID,
							AgentEventListener observer);
	
	/**
	 * Loads the original files related to a given pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The collection of the pixels sets.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadOriginalFiles(SecurityContext ctx,
		Collection<Long> pixelsID, AgentEventListener observer);
	
	/**
	 * Loads the archived files related to the specified image.
	 * 
	 * @param ctx The security context.
	 * @param imageIDs The ids of the pixels set related to the image.
	 * @param location The location where to store the files.
	 * @param override Flag indicating to override the existing file if it
	 *                 exists, <code>false</code> otherwise.
	 * @param zip Pass <code>true</code> to create a zip file
	 * @param keepOriginalPaths Pass <code>true</code> to preserve the original folder structure
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadArchivedImage(SecurityContext ctx, List<Long> imageIDs,
		File location, boolean override, boolean zip, boolean keepOriginalPaths,
		AgentEventListener observer);
	
	/**
	 * Filters by annotation.
	 * 
	 * @param ctx The security context.
	 * @param nodeType The type of node.
	 * @param nodeIds The collection of nodes to filter.
	 * @param annotationType The type of annotation to filter by.
	 * @param terms The terms to filter by.
	 * @param userID The ID of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterByAnnotation(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, List<String> terms,
		long userID, AgentEventListener observer);

	/**
	 * Filters by annotated nodes.
	 * 
	 * @param ctx The security context.
	 * @param nodeType The type of node.
	 * @param nodeIds The collection of nodes to filter.
	 * @param annotationType The type of annotation to filter by.
	 * @param annotated Pass <code>true</code> to retrieve the annotated nodes,
	 * <code>false</code> otherwise.
	 * @param userID The ID of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterByAnnotated(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, boolean annotated,
		long userID, AgentEventListener observer);
	
	/**
	 * Filters the data.
	 * 
	 * @param ctx The security context.
	 * @param nodeType The type of node.
	 * @param nodeIds The collection of nodes to filter.
	 * @param context The filtering context.
	 * @param userID The ID of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle filterData(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, FilterContext context, long userID,
		AgentEventListener observer);

	/** 
	 * Creates a new <code>DataObject</code> and adds the children to the
	 * newly created node.
	 * 
	 * @param ctx The security context.
	 * @param parent The parent of the <code>DataObject</code> to create
	 * or <code>null</code> if no parent specified.
	 * @param data The <code>DataObject</code> to create.
	 * @param children The nodes to add to the newly created
	 * <code>DataObject</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle createDataObject(SecurityContext ctx, DataObject parent,
			DataObject data, Collection children, AgentEventListener observer);
	
	/**
	 * Saves the file back to the server.
	 * 
	 * @param ctx The security context.
	 * @param fileAnnotation The file to save back to the server.
	 * @param file The id of the file if previously saved.
	 * @param index One of the constants defined by this class.
	 * @param linkTo The <code>DataObject</code> to link the annotation to.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveFile(SecurityContext ctx,
		FileAnnotationData fileAnnotation, File file, int index,
		DataObject linkTo, AgentEventListener observer);

	/**
	 * Updates the data objects. This method will for now only be implemented
	 * for the plate or wells.
	 * 
	 * @param ctx The security context.
	 * @param objects The objects to update. Mustn't be <code>null</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateDataObjects(SecurityContext ctx,
		List<DataObject> objects, AgentEventListener observer);

	/**
	 * Submits the files to the QA system.
	 * 
	 * @param ctx The security context.
	 * @param details Object containing the information to send.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle submitFiles(SecurityContext ctx, MessengerDetails details,
			AgentEventListener observer);
	
	/**
	 * Loads the measurements related the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param ctx The security context.
	 * @param dataObject The object to handle. Mustn't be <code>null</code>.
	 * @param userID Pass <code>-1</code> if no user specified.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadROIMeasurement(SecurityContext ctx, Object dataObject,
			long userID, AgentEventListener observer);

	/**
         * Loads the original files hosted by the file annotation.
         * 
         * @param ctx The security context.
         * @param files The files to handle. Mustn't be <code>null</code>.
         * @param observer Call-back handler.
         * @return A handle that can be used to cancel the call.
         */
        public CallHandle loadFiles(SecurityContext ctx,
            Map<FileAnnotationData, File> files, AgentEventListener observer);
    
	/**
	 * Loads the original files hosted by the file annotation.
	 * 
	 * @param ctx The security context.
	 * @param zipDirectory Pass <code>true</code> to zip the directory
	 * hosting the downloaded files, <code>false</code> otherwise.
	 * @param files The files to handle. Mustn't be <code>null</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFiles(SecurityContext ctx, boolean zipDirectory,
		Map<FileAnnotationData, File> files, AgentEventListener observer);
	
	/**
	 * Loads the scripts.
	 * 
	 * @param ctx The security context.
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @param all Pass <code>true</code> to retrieve all the scripts uploaded
	 * ones and the default ones, <code>false</code>.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadScripts(SecurityContext ctx, long userID, boolean all,
			AgentEventListener observer);
	
	/**
	 * Loads the specified script.
	 * 
	 * @param ctx The security context.
	 * @param scriptID The id of the script.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadScript(SecurityContext ctx, long scriptID,
			AgentEventListener observer);

	/**
	 * Loads the specified tabular data.
	 * 
	 * @param ctx The security context.
	 * @param parameters The parameters indicating the data to load.
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadTabularData(SecurityContext ctx,
		TableParameters parameters, long userID, AgentEventListener observer);
	
	/**
	 * Loads the specified tabular data.
	 * 
	 * @param ctx The security context.
	 * @param imageId The id of the image.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFileset(SecurityContext ctx,
		long imageId, AgentEventListener observer);
	
	/**
	 * Loads the annotations of the given type linked to the specified objects.
	 * Returns a map whose keys are the object's id and the values are a
	 * collection of annotation linked to that object.
	 * 
	 * @param ctx The security context.
	 * @param rootType The type of object the annotations are linked to e.g.
	 * Image.
	 * @param rootIDs The collection of object's ids the annotations are linked
	 * to.
	 * @param annotationType The type of annotation to load.
	 * @param nsInclude The annotation's name space to include if any.
	 * @param nsExlcude The annotation's name space to exclude if any.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadAnnotations(SecurityContext ctx, Class<?> rootType,
		List<Long> rootIDs, Class<?> annotationType, List<String> nsInclude,
		List<String> nsExlcude, AgentEventListener observer);

	/**
	 * Saves the object, adds (resp. removes) annotations to (resp. from)
	 * the object if any.
	 * 
	 * @param ctx The security context.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
    public CallHandle annotateData(SecurityContext ctx,
        Map<DataObject, List<AnnotationData>> toAdd,
        Map<DataObject, List<AnnotationData>> toRemove, long userID,
        AgentEventListener observer);
}
