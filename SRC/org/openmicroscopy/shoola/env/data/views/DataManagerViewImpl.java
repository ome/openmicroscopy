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
import org.openmicroscopy.shoola.env.data.views.calls.AdminLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ContainerCounterLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DMRefreshLoader;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectRemover;
import org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ExistingObjectsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ExperimenterImagesCounter;
import org.openmicroscopy.shoola.env.data.views.calls.FilesChecker;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.PlateWellsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.TagsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.DataObject;
import pojos.ExperimenterData;
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
	 * @see DataManagerView#loadContainerHierarchy(Class, List, boolean, long,
	 * 						AgentEventListener)
	 */
	public CallHandle loadContainerHierarchy(Class rootNodeType,
			List<Long> rootNodeIDs, boolean withLeaves, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new DMLoader(rootNodeType, rootNodeIDs, withLeaves,
				userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadImages(long, AgentEventListener)
	 */
	public CallHandle loadImages(long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#getImages(Class, List, long, AgentEventListener)
	 */
	public CallHandle getImages(Class nodeType, List nodeIDs, long userID, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(nodeType, nodeIDs, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#createDataObject(DataObject, DataObject, 
	 * 										AgentEventListener)
	 */
	public CallHandle createDataObject(DataObject userObject, DataObject parent,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectSaver(userObject, parent,
				DataObjectSaver.CREATE);
		return cmd.exec(observer);
	} 

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#countContainerItems(Set, AgentEventListener)
	 */
	public CallHandle countContainerItems(Set rootIDs, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ContainerCounterLoader(rootIDs);
		return cmd.exec(observer);  
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadThumbnail(ImageData, int, int, long,
	 *                                          AgentEventListener)
	 */
	public CallHandle loadThumbnail(ImageData image, int maxWidth, 
			int maxHeight, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ThumbnailLoader(image, maxWidth, maxHeight, 
				userID);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#addExistingObjects(Collection, Collection, 
	 *                                          AgentEventListener)
	 */
	public CallHandle addExistingObjects(Collection parents, Collection children, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(parents, children);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#addExistingObjects(Map, 
	 *                                          AgentEventListener)
	 */
	public CallHandle addExistingObjects(Map toPaste, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(toPaste, null);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#cutAndPaste(Map, Map, AgentEventListener)
	 */
	public CallHandle cutAndPaste(Map toPaste, Map toCut, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExistingObjectsSaver(toPaste, toCut);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadChannelsData(long, long, AgentEventListener)
	 */
	public CallHandle loadChannelsData(long pixelsID, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ChannelMetadataLoader(pixelsID, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#changePassword(String, String, AgentEventListener)
	 */
	public CallHandle changePassword(String oldPassword, String newPassword, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(oldPassword, newPassword);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#updateExperimenter(ExperimenterData, 
	 * 										AgentEventListener)
	 */
	public CallHandle updateExperimenter(ExperimenterData exp, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(exp);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#getDiskSpace(long, AgentEventListener)
	 */
	public CallHandle getDiskSpace(long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(userID, AdminLoader.SPACE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#refreshHierarchy(Class, Map, AgentEventListener)
	 */
	public CallHandle refreshHierarchy(Class rootNodeType,
			Map<Long, List> m, AgentEventListener observer)
	{
		BatchCallTree cmd = new DMRefreshLoader(rootNodeType, m);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#countExperimenterImages(long, Map, 
	 * 												AgentEventListener)
	 */
	public CallHandle countExperimenterImages(long userID, 
			Map<Integer, TimeRefObject> m,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExperimenterImagesCounter(userID, m);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadTags(Long, boolean, boolean, long,
	 *  AgentEventListener)
	 */
	public CallHandle loadTags(Long id, boolean dataObject, boolean topLevel,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new TagsLoader(id, dataObject, topLevel, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#loadPlateWells(Map, long, AgentEventListener)
	 */
	public CallHandle loadPlateWells(Map<Long, Long> ids, long userID, 
									AgentEventListener observer)
	{
		BatchCallTree cmd = new PlateWellsLoader(ids, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#delete(Collection, AgentEventListener)
	 */
	public CallHandle delete(Collection<DeletableObject> values, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new DataObjectRemover(values);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataManagerView#delete(DeletableObject, AgentEventListener)
	 */
	public CallHandle delete(DeletableObject value, AgentEventListener observer) 
	{
		BatchCallTree cmd = new DataObjectRemover(value);
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
  
}
