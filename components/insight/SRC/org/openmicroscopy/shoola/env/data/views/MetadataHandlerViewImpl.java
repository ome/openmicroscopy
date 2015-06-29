/*
 * org.openmicroscopy.shoola.env.data.views.MetadataHandlerViewImpl 
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

//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedImageLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DataFilter;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver;
import org.openmicroscopy.shoola.env.data.views.calls.FileAnnotationCheckLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FileUploader;
import org.openmicroscopy.shoola.env.data.views.calls.FilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FilesetLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RelatedContainersLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ScriptsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.TabularDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * Implementation of the {@link MetadataHandlerView} interface.
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
class MetadataHandlerViewImpl
	implements MetadataHandlerView
{
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadContainers(SecurityContext, Class, long,
	 * long, AgentEventListener)
	 */
	public CallHandle loadContainers(SecurityContext ctx, Class type, long id,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new RelatedContainersLoader(ctx, type, id, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadThumbnails(SecurityContext, ImageData,
	 * Set, int, int, AgentEventListener)
	 */
	public CallHandle loadThumbnails(SecurityContext ctx, ImageData image,
		Set<Long> userIDs, int thumbWidth, int thumbHeight, 
		AgentEventListener observer)
	{
		BatchCallTree cmd = new ThumbnailLoader(ctx, image, thumbWidth,
				thumbHeight, userIDs);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadStructuredData(SecurityContext, DataObject,
	 * long, AgentEventListener)
	 */
	public CallHandle loadStructuredData(SecurityContext ctx, Object dataObject,
								long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx,
						StructuredAnnotationLoader.ALL, dataObject, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadStructuredData(SecurityContext, List,
	 * long, boolean, AgentEventListener)
	 */
	public CallHandle loadStructuredData(SecurityContext ctx,
		List<DataObject> data, long userID, boolean viewed,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx,
						StructuredAnnotationLoader.ALL, data, userID, viewed);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadExistingAnnotations(SecurityContext, Class,
	 * long, long, AgentEventListener)
	 */
	public CallHandle loadExistingAnnotations(SecurityContext ctx,
		Class annotation, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx, annotation,
														userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#checkFileAnnotationDeletion(SecurityContext, List, List, AgentEventListener)
	 */
	public CallHandle checkFileAnnotationDeletion(SecurityContext ctx, List<FileAnnotationData> annotations, List<DataObject> referenceObjects, AgentEventListener observer) {
		BatchCallTree cmd = new FileAnnotationCheckLoader(ctx, annotations, referenceObjects);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadExistingAnnotations(SecurityContext,
	 * Class, long, AgentEventListener)
	 */
	public CallHandle loadExistingAnnotations(List<SecurityContext> ctx,
		Class annotation, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx, annotation,
														userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#saveData(SecurityContext, Collection, List,
	 * List, long, AgentEventListener)
	 */
	public CallHandle saveData(SecurityContext ctx,
		Collection<DataObject> data, List<AnnotationData> toAdd,
		List<Object> toRemove, List<Object> metadata, long userID,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationSaver(ctx, data,
								toAdd, toRemove, metadata, userID, false);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#saveBatchData(SecurityContext, Collection,
	 * List, List, long, AgentEventListener)
	 */
	public CallHandle saveBatchData(SecurityContext ctx,
		Collection<DataObject> data, List<AnnotationData> toAdd,
		List<Object> toRemove, long userID,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationSaver(ctx, data,
									toAdd, toRemove, null, userID, true);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#saveBatchData(SecurityContext, TimeRefObject,
	 * List, List, long, AgentEventListener)
	 */
	public CallHandle saveBatchData(SecurityContext ctx,
		TimeRefObject refObject, List<AnnotationData> toAdd,
		List<Object> toRemove, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationSaver(ctx, refObject,
									toAdd, toRemove, userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFile(SecurityContext, File, long,
	 * 										AgentEventListener)
	 */
	public CallHandle loadFile(SecurityContext ctx, File file, long fileID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesLoader(ctx, file, fileID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFile(SecurityContext, File, long, int,
	 * 										AgentEventListener)
	 */
	public CallHandle loadFile(SecurityContext ctx, File file, long fileID,
			int index, AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesLoader(ctx, file, fileID, index);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadOriginalFiles(Collection, AgentEventListener)
	 */
	public CallHandle loadOriginalFiles(SecurityContext ctx,
		Collection<Long> pixelsID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ArchivedFilesLoader(ctx, pixelsID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadOriginalImage(SecurityContext, List, File,
	 * String, boolean, AgentEventListener)
	 */
	public CallHandle loadArchivedImage(SecurityContext ctx, List<Long> imageIDs,
			File path, boolean override,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ArchivedImageLoader(ctx, imageIDs, path,
		        override);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadRatings(SecurityContext, Class, List, long,
	 * 										AgentEventListener)
	 */
	public CallHandle loadRatings(SecurityContext ctx, Class nodeType,
		List<Long> nodeIDs, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx,
 				StructuredAnnotationLoader.RATING, nodeType, nodeIDs, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#filterByAnnotation(SecurityContext, Class, List,
	 * Class, List, long, AgentEventListener)
	 */
	public CallHandle filterByAnnotation(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, List<String> terms,
		long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new DataFilter(ctx, annotationType, nodeType,
				nodeIds, terms, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#filterByAnnotated(SecurityContext, Class, List,
	 * Class, boolean, long, AgentEventListener)
	 */
	public CallHandle filterByAnnotated(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, boolean annotated,
		long userID, AgentEventListener observer) 
	{
		BatchCallTree cmd = new DataFilter(ctx, annotationType, nodeType,
				nodeIds, annotated, userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#filterData(SecurityContext, Class, List,
	 * FilterContext, long, AgentEventListener)
	 */
	public CallHandle filterData(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, FilterContext context, long userID,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new DataFilter(ctx, nodeType, nodeIds, context,
				userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#createDataObject(SecurityContext, DataObject, DataObject,
	 * 									Collection, AgentEventListener)
	 */
	public CallHandle createDataObject(SecurityContext ctx, DataObject parent,
		DataObject data, Collection children, AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectSaver(ctx, parent, data, children);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#saveFile(SecurityContext, FileAnnotationData, File, int, 
	 * 								DataObject, AgentEventListener)
	 */
	public CallHandle saveFile(SecurityContext ctx,
			FileAnnotationData fileAnnotation, File file, int index,
			DataObject linkTo, AgentEventListener observer)
	{
		BatchCallTree cmd = new ArchivedFilesSaver(ctx, fileAnnotation, file,
			index, linkTo);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadAnnotation(SecurityContext, long, AgentEventListener)
	 */
	public CallHandle loadAnnotation(SecurityContext ctx, long annotationID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx, annotationID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#updateDataObjects(SecurityContext, List, AgentEventListener)
	 */
	public CallHandle updateDataObjects(SecurityContext ctx, 
			List<DataObject> objects, AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectSaver(ctx, objects, null,
				DataObjectSaver.UPDATE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#submitFiles(SecurityContext,
	 * MessengerDetails, AgentEventListener)
	 */
	public CallHandle submitFiles(SecurityContext ctx, MessengerDetails details,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FileUploader(details);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadRatings(SecurityContext, Object, long,
	 * 										AgentEventListener)
	 */
	public CallHandle loadROIMeasurement(SecurityContext ctx,
		Object dataObject, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx,
 				StructuredAnnotationLoader.ROI_MEASUREMENT, dataObject,
 					userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFiles(SecurityContext, Map, AgentEventListener)
	 */
	public CallHandle loadFiles(SecurityContext ctx,
		Map<FileAnnotationData, File> files, AgentEventListener observer)
	{
		return loadFiles(ctx, false, files,observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFiles(SecurityContext, boolean, Map,
	 * AgentEventListener)
	 */
	public CallHandle loadFiles(SecurityContext ctx, boolean zipDirectory,
	        Map<FileAnnotationData, File> files, AgentEventListener observer)
	{
	    BatchCallTree cmd = new FilesLoader(ctx, files, zipDirectory);
	    return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadScripts(SecurityContext, long, boolean,
	 * AgentEventListener)
	 */
	public CallHandle loadScripts(SecurityContext ctx, long userID, boolean all,
			AgentEventListener observer)
	{
		int index = ScriptsLoader.DEFAULT_SCRIPTS;
		if (all) index = ScriptsLoader.ALL_SCRIPTS;
		BatchCallTree cmd = new ScriptsLoader(ctx, userID, index);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadScript(SecurityContext, long, AgentEventListener)
	 */
	public CallHandle loadScript(SecurityContext ctx, long scriptID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ScriptsLoader(ctx, scriptID,
				ScriptsLoader.SINGLE_SCRIPT);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadTabularData(SecurityContext, TableParameters,
	 * long, AgentEventListener)
	 */
	public CallHandle loadTabularData(SecurityContext ctx,
			TableParameters parameters, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new TabularDataLoader(ctx, parameters, userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFileset(SecurityContext, long,
	 * AgentEventListener)
	 */
	public CallHandle loadFileset(SecurityContext ctx, long imageId,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesetLoader(ctx, imageId);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadAnnotations(SecurityContext, Class, List,
	 * Class, List, List, AgentEventListener)
	 */
	public CallHandle loadAnnotations(SecurityContext ctx, Class<?> rootType,
			List<Long> rootIDs, Class<?> annotationType, List<String> nsInclude,
			List<String> nsExlcude, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(ctx, rootType,
			rootIDs, annotationType, nsInclude, nsExlcude);
		return cmd.exec(observer);
	}

	
    /**
     * Implemented as specified by the view interface.
     * @see MetadataHandlerView#annotateData(SecurityContext, Map, Map, long,
     * AgentEventListener)
     */
    public CallHandle annotateData(SecurityContext ctx,
            Map<DataObject, List<AnnotationData>> toAdd,
            Map<DataObject, List<AnnotationData>> toRemove, long userID,
            AgentEventListener observer) {
        BatchCallTree cmd = new StructuredAnnotationSaver(ctx, toAdd, toRemove,
                userID);
        return cmd.exec(observer);
    }
}
