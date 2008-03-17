/*
 * org.openmicroscopy.shoola.env.data.views.MetadataHandlerViewImpl 
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
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FileDownloader;
import org.openmicroscopy.shoola.env.data.views.calls.RelatedContainersLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
import pojos.DataObject;
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
	 * @see MetadataHandlerView#loadTags(Class, long, long, AgentEventListener)
	 */
	public CallHandle loadTags(Class rootType, long rootID, long userID,
							AgentEventListener observer)
	{
		 BatchCallTree cmd = new StructuredAnnotationLoader(
				 	StructuredAnnotationLoader.TAG, rootType, rootID, userID);
	     return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadAttachments(Class, long, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadAttachments(Class rootType, long rootID, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(
			 	StructuredAnnotationLoader.ATTACHMENT, rootType, rootID, 
			 	userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadViewedBy(long, long, AgentEventListener)
	 */
	public CallHandle loadViewedBy(long imageID, long pixelsID, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsLoader(pixelsID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadContainers(Class, long, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadContainers(Class type, long id, long userID,
									AgentEventListener observer)
	{
		BatchCallTree cmd = new RelatedContainersLoader(type, id, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadUrls(Class, long, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadUrls(Class rootType, long rootID, long userID, 
							AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(
			 				StructuredAnnotationLoader.URL, rootType, rootID, 
			 					userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadRatings(Class, long, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadRatings(Class rootType, long rootID, long userID, 
							AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(
 				StructuredAnnotationLoader.RATING, rootType, rootID, 
 					userID);
		return cmd.exec(observer);
	}
	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadThumbnails(ImageData, Set, int, int, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadThumbnails(ImageData image, Set<Long> userIDs, 
			int thumbWidth, int thumbHeight, AgentEventListener observer)
	{
		BatchCallTree cmd = new ThumbnailLoader(image, thumbWidth, thumbHeight,
							userIDs);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadStructuredData(DataObject, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadStructuredData(DataObject dataObject, 
								long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(
						StructuredAnnotationLoader.ALL, dataObject, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#annotate(Class, long, AnnotationData, 
	 * 									AgentEventListener)
	 */
	public CallHandle annotate(Class type, long id, AnnotationData annotation, 
							AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(type, id, annotation, 
									AnnotationSaver.CREATE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadTextualAnnotations(Class, long, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadTextualAnnotations(Class rootType, long rootID, 
							long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(
 				StructuredAnnotationLoader.TEXTUAL, rootType, rootID, 
 					userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadExistingAnnotations(Class, Class, 
	 * 											AgentEventListener)
	 */
	public CallHandle loadExistingAnnotations(Class annotation, Class type, 
									AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationLoader(annotation, type);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#saveData(DataObject, List, List, long, 
	 * 									AgentEventListener)
	 */
	public CallHandle saveData(DataObject dataObject, 
			List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new StructuredAnnotationSaver(dataObject, 
									toAdd, toRemove, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadFile(File, long, int, 
	 * 										AgentEventListener)
	 */
	public CallHandle loadFile(File file, long fileID, long size, 
				AgentEventListener observer)
	{
		BatchCallTree cmd = new FileDownloader(file, fileID, size); 
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see MetadataHandlerView#loadOriginalFile(long, AgentEventListener)
	 */
	public CallHandle loadOriginalFile(long pixelsID, 
										AgentEventListener observer) 
	{
		BatchCallTree cmd = new ArchivedFilesLoader(pixelsID); 
		return cmd.exec(observer);
	}
	
}
