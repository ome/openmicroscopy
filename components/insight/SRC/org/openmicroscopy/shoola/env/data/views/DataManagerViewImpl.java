/*
 * org.openmicroscopy.shoola.env.data.views.DataManagerViewImpl
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

package org.openmicroscopy.shoola.env.data.views;

//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.model.TransferableObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationParentLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ChannelDataSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ContainerCounterLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectRemover;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectTransfer;
import org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ExperimenterImagesCounter;
import org.openmicroscopy.shoola.env.data.views.calls.FilesChecker;
import org.openmicroscopy.shoola.env.data.views.calls.ImageSplitChecker;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.PixelsDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.PlateWellsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RepositoriesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.TagsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

import pojos.ChannelData;
import pojos.DataObject;
import pojos.ImageData;

/** 
* Implementation of the {@link DataManagerView} implementation.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
*              <a href="mailto:a.falconi@dundee.ac.uk">
*                  a.falconi@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
class DataManagerViewImpl
	implements DataManagerView
{

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadContainerHierarchy(SecurityContext, Class, List,
	 * boolean, long, AgentEventListener)
	 */
	public CallHandle loadContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, List<Long> rootNodeIDs, boolean withLeaves,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new DMLoader(ctx, rootNodeType, rootNodeIDs,
				withLeaves, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadImages(SecurityContext, long, boolean,
	 * AgentEventListener)
	 */
	public CallHandle loadImages(SecurityContext ctx, long userID,
			boolean orphan, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(ctx, userID, orphan);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#getImages(SecurityContext, Class, List, long,
	 * AgentEventListener)
	 */
	public CallHandle getImages(SecurityContext ctx, Class nodeType,
			List nodeIDs, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(ctx, nodeType, nodeIDs, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#createDataObject(SecurityContext, DataObject,
	 * DataObject, AgentEventListener)
	 */
	public CallHandle createDataObject(SecurityContext ctx, 
		DataObject userObject, DataObject parent,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectSaver(ctx, userObject, parent,
				DataObjectSaver.CREATE);
		return cmd.exec(observer);
	} 

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#countContainerItems(Set, AgentEventListener)
	 */
	public CallHandle countContainerItems(SecurityContext ctx, Set rootIDs,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ContainerCounterLoader(ctx, rootIDs);
		return cmd.exec(observer);  
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadThumbnail(SecurityContext, ImageData, int, int,
	 * long, AgentEventListener)
	 */
	public CallHandle loadThumbnail(SecurityContext ctx, ImageData image,
		int maxWidth, int maxHeight, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ThumbnailLoader(ctx, image, maxWidth, maxHeight,
				userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#addExistingObjects(SecurityContext, Collection,
	 * Collection, AgentEventListener)
	 */
	public CallHandle addExistingObjects(SecurityContext ctx, 
		Collection parents, Collection children, AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(ctx, parents, children,
				false);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#addExistingObjects(SecurityContext, Map, Map
	 *                                          AgentEventListener)
	 */
	public CallHandle addExistingObjects(SecurityContext ctx, Map toPaste,
			Map toRemove, AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(ctx, toPaste, toRemove,
				false);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#cutAndPaste(SecurityContext, Map, Map, boolean,
	 * AgentEventListener)
	 */
	public CallHandle cutAndPaste(SecurityContext ctx, Map toPaste, Map toCut,
		boolean admin, AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(ctx, toPaste, toCut,
				admin);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadChannelsData(SecurityContext, long, long,
	 * AgentEventListener)
	 */
	public CallHandle loadChannelsData(SecurityContext ctx, long pixelsID,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ChannelMetadataLoader(ctx, pixelsID, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#refreshHierarchy(Class, Map, AgentEventListener)
	 */
	public CallHandle refreshHierarchy(Class rootNodeType,
			Map<SecurityContext, List> m, AgentEventListener observer)
	{
		BatchCallTree cmd = new DMRefreshLoader(rootNodeType, m);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#countExperimenterImages(SecurityContext, long, Map,
	 * 												AgentEventListener)
	 */
	public CallHandle countExperimenterImages(SecurityContext ctx, long userID,
			Map<Integer, TimeRefObject> m,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExperimenterImagesCounter(ctx, userID, m);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadTags(SecurityContext, Long, boolean, boolean,
	 * long, long, AgentEventListener)
	 */
	public CallHandle loadTags(SecurityContext ctx, Long id, boolean dataObject,
			boolean topLevel, long userID, long groupID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new TagsLoader(ctx, id, dataObject, topLevel,
				userID, groupID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadPlateWells(Map, long, AgentEventListener)
	 */
	public CallHandle loadPlateWells(SecurityContext ctx, Map<Long, Long> ids,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new PlateWellsLoader(ctx, ids, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#delete(Map, AgentEventListener)
	 */
	public CallHandle delete(Map<SecurityContext, Collection<DeletableObject>>
	 values, AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectRemover(values);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#checkFileFormat(List, AgentEventListener)
	 */
	public CallHandle checkFileFormat(List<File> list,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesChecker(list);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadRepositories(SecurityContext, long,
	 * AgentEventListener)
	 */
	public CallHandle loadRepositories(SecurityContext ctx, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RepositoriesLoader(ctx, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadParentsOfAnnotation(SecurityContext, long,
	 * AgentEventListener)
	 */
	public CallHandle loadParentsOfAnnotation(SecurityContext ctx,
			long annotationId, AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationParentLoader(ctx, annotationId);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#changeGroup(TransferableObject, AgentEventListener)
	 */
	public CallHandle changeGroup(TransferableObject object,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectTransfer(object);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#isLargeImage(SecurityContext, long,
	 * AgentEventListener)
	 */
	public CallHandle isLargeImage(SecurityContext ctx, long pixelsID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new PixelsDataLoader(ctx, pixelsID,
				PixelsDataLoader.SIZE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#saveChannelData(SecurityContext, List, List,
	 * AgentEventListener)
	 */
	public CallHandle saveChannelData(SecurityContext ctx,
			List<ChannelData> channels, List<DataObject> objects,
			AgentEventListener observer) {
		BatchCallTree cmd = new ChannelDataSaver(ctx, channels, objects);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadPlateFromImage(SecurityContext, Collection,
	 * AgentEventListener)
	 */
	public CallHandle loadPlateFromImage(SecurityContext ctx,
			Collection<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new PlateWellsLoader(ctx, ids);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#getImagesBySplitFilesets(Map,
	 * List, AgentEventListener)
	 */
	public CallHandle getImagesBySplitFilesets(
			Map<SecurityContext, List<DataObject>> objects,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ImageSplitChecker(objects);
		return cmd.exec(observer);
	}
}
