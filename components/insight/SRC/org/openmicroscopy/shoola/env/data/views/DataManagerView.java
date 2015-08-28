/*
 * org.openmicroscopy.shoola.env.data.views.DataManagerView
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.model.TransferableObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ImageData;

/** 
* Provides methods to support data management.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
*              <a href="mailto:a.falconi@dundee.ac.uk">
*                  a.falconi@dundee.ac.uk</a>
* @version 2.2
* @since OME2.2
*/
public interface DataManagerView
 	extends DataServicesView
{
	
	/**
	 * Retrieves the hierarchies specified by the parameters.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType  The type of the root node. Can only be one out of:
	 *                      <code>ProjectData</code>, <code>DatasetData</code>
	 *                      or <code>ScreenData</code>.
	 * @param rootNodeIDs   A set of the IDs of top-most containers. Passed
	 *                      <code>null</code> to retrieve all the top-most
	 *                      container specified by the rootNodeType.
	 * @param withLeaves    Passes <code>true</code> to retrieve the images.
	 *                      <code>false</code> otherwise.
	 * @param userID		The identifier of the user.
	 * @param observer      Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, List<Long> rootNodeIDs, boolean withLeaves,
			long userID, AgentEventListener observer);

	/**
	 * Retrieves the images for the specified user.
	 * 
	 * @param ctx The security context.
	 * @param userID The ID of the user.
	 * @param orphan Indicates to load all the images or only the images not
	 * in any container.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadImages(SecurityContext ctx, long userID,
			boolean orphan, AgentEventListener observer);

	/**
	 * Retrieves the images container in the specified root nodes.
	 * 
	 * @param ctx The security context.
	 * @param nodeType 		The type of the node. Can only be one out of:
	 *                      <code>DatasetData</code>.       
	 * @param nodeIDs 		The id of the node.
	 * @param userID		The Id of the user.
	 * @param observer      Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle getImages(SecurityContext ctx, Class nodeType,
			List nodeIDs, long userID, AgentEventListener observer);

	/**
	 * Creates a new <code>DataObject</code> whose parent is specified by the
	 * ID.
	 * 
	 * @param ctx The security context.
	 * @param userObject The type of <code>DataObject</code> to create.
	 * @param parent The parent of the <code>DataObject</code>.  
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle createDataObject(SecurityContext ctx,
		DataObject userObject, DataObject parent, AgentEventListener observer);

	/**
	 * Counts the number of items contained in the specified containers.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeIDs   Collection of top-most containers.
	 * @param observer      Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle countContainerItems(SecurityContext ctx, Set rootNodeIDs,
			AgentEventListener observer);

	/**
	 * Loads a thumbnail for each specified <code>ImageData</code> object.
	 * As thumbnails are retrieved from <i>OMEIS</i>, they're posted back to
	 * the <code>observer</code> through <code>DSCallFeedbackEvent</code>s.
	 * Each thumbnail will be posted in a single event; the <code>observer
	 * </code> can then call the <code>getPartialResult</code> method to 
	 * retrieve a <code>ThumbnailData</code> object for that thumbnail. The 
	 * final <code>DSCallOutcomeEvent</code> will have no result.
	 * Thumbnails are generated respecting the <code>X/Y</code> ratio of the
	 * original image and so that their area doesn't exceed <code>maxWidth*
	 * maxHeight</code>.
	 * 
	 * @param ctx The security context.
	 * @param image The <code>ImageData</code> object the thumbnail is for.
	 * @param maxWidth The maximum acceptable width of the thumbnails.
	 * @param maxHeight The maximum acceptable height of the thumbnails.
	 * @param userID The id of the user the thumbnails are for.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadThumbnail(SecurityContext ctx, ImageData image,
			int maxWidth, int maxHeight, long userID, 
			AgentEventListener observer);

	/**
	 * Adds the specified items to the parent.
	 * 
	 * @param ctx The security context.
	 * @param parent The <code>DataObject</code>s to update. Either a 
	 *                  <code>ProjectData</code> or <code>DatasetData</code>.
	 * @param children  The items to add.
	 * @param observer  Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle addExistingObjects(SecurityContext ctx,
		Collection parent, Collection children, AgentEventListener observer);

	/**
	 * Adds the specified items to the parent.
	 * 
	 * @param ctx The security context.
	 * @param objects   The objects to update.
	 * @param observer  Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle addExistingObjects(SecurityContext ctx, Map objects,
			Map toRemove, AgentEventListener observer);

	/**
	 * Cuts and Pastes.
	 * 
	 * @param ctx The security context.
	 * @param toPaste   Map of objects to paste into
	 *                  where the key is the <code>DataObject</code> to paste 
	 *                  into and the value is a set of <code>DataObject</code>
	 *                  to copy.
	 * @param toCut     Map of objects to cut from
	 *                  where the key is the <code>DataObject</code> to cut  
	 *                  from and the value is a set of <code>DataObject</code>
	 *                  to remove.
	 * @param admin 	Pass <code>true</code> to indicate to handle 
	 * 					experimenters, <code>false</code> otherwise.
	 * @param observer  Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle cutAndPaste(SecurityContext ctx, Map toPaste, Map toCut,
			boolean admin, AgentEventListener observer);

	/**
	 * Loads the emission wavelengths for the given set of pixels.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID  The id of the pixels set.
	 * @param userID   If the id is specified i.e. not <code>-1</code>, 
     * 				   load the color associated to the channel, 
     * 				   <code>false</code> otherwise.
	 * @param observer  Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadChannelsData(SecurityContext ctx, long pixelsID,
			long userID, AgentEventListener observer);

	/**
	 * Reloads the hierarchy currently displayed.
	 * 
	 * @param rootNodeType	The type of the root node. Can either be 
	 *                      <code>ProjectData</code>
	 * @param m The user and the nodes to refresh.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle refreshHierarchy(Class rootNodeType,
			Map<SecurityContext, List> m, AgentEventListener observer);

	/**
	 * Retrieves the images imported by the specified user during various
	 * periods of time. The passed map is a map whose keys are indexes
	 * identifying a period of time and the values are time objects.
	 * 
	 * @param ctx The security context.
	 * @param userID	The user's id.
	 * @param m			The data to handle. Mustn't be <code>null</code>.
	 * @param observer 	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle countExperimenterImages(SecurityContext ctx, long userID,
			Map<Integer, TimeRefObject> m, AgentEventListener observer);

	/**
	 * Loads the tags containing tags and creates a pseudo hierarchy.
	 * 
	 * @param ctx The security context.
	 * @param id        	The id of the tag or <code>-1</code>.
	 * @param dataObject    Pass <code>true</code> to load the 
	 * 						<code>DataObject</code> related 
     * 						to the tags, <code>false</code> otherwise.
     * @param topLevel		Pass <code>true</code> to indicate to load the
     * 						from the <code>Tag Set</code>, <code>false</code>
     * 						otherwise. This parameters will be taken into
     * 						account if the passed id is not negative.
	 * @param userID		The user's identifier.
	 * @param groupID		The user group's identifier.
	 * @param observer 		Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadTags(SecurityContext ctx, Long id, boolean dataObject,
		boolean topLevel, long userID, long groupID,
		AgentEventListener observer);
	
	/**
	 * Loads to the wells contained within the specified plate.
	 * 
	 * @param ctx The security context.
	 * @param ids 		Map whose keys are the plate ID and values are the 
	 * 					screen acquisition ID or <code>-1</code>.
	 * @param userID 	The Id of the user.
	 * @param observer  Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadPlateWells(SecurityContext ctx,
		Map<Long, Long> ids, long userID, AgentEventListener observer);
	
	/**
	 * Deletes the passed collection.
	 * 
	 * @param values	The collection of object to delete.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle delete(Map<SecurityContext, Collection<DeletableObject>>
	values, AgentEventListener observer);

	/**
	 * Controls if the passed files can be imported.
	 * 
	 * @param list 		The collection of files to handle.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle checkFileFormat(List<File> list,
			AgentEventListener observer);

	/**
	 * Loads the repositories.
	 * 
	 * @param ctx The security context.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadRepositories(SecurityContext ctx, long userID,
			AgentEventListener observer);
	
	/**
	 * Loads the parents of the specified annotation.
	 * 
	 * @param ctx The security context.
	 * @param annotationId The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadParentsOfAnnotation(SecurityContext ctx,
			long annotationId, AgentEventListener observer);

	/**
	 * Moves the passed collection to another group.
	 * 
	 * @param object The objects to transfer.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle changeGroup(TransferableObject object,
			AgentEventListener observer);

	/**
	 * Checks if the image is a large image or not.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The identifier of the pixels set.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle isLargeImage(SecurityContext ctx, long pixelsID,
			AgentEventListener observer);

	/**
	 * Saves the channels. Applies the changes to all the images contained in
	 * the specified objects. This could be datasets, plates or images.
	 * 
	 * @param ctx The security context.
	 * @param channels The channels to update.
	 * @param objects The objects to apply the changes to. If the objects are
	 * datasets, then all the images within the datasets will be updated.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle saveChannelData(SecurityContext ctx,
			List<ChannelData> channels, List<DataObject> objects,
			AgentEventListener observer);
	
	/**
	 * Loads to the plate hosting the specified images.
	 * 
	 * @param ctx The security context.
	 * @param ids The collection of image's ids.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadPlateFromImage(SecurityContext ctx,
		Collection<Long> ids, AgentEventListener observer);
	
	/**
	 * Given a list of IDs of a given type. Determines the filesets that will be
	 * split. Returns the a Map with fileset's ids as keys and the
	 * values if the map:
	 * Key = <code>True</code> value: List of image's ids that are contained.
	 * Key = <code>False</code> value: List of image's ids that are missing
	 * so the delete or change group cannot happen.
	 * 
	 * @param objects The objects to handle
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle getImagesBySplitFilesets(
		Map<SecurityContext, List<DataObject>> objects,
		AgentEventListener observer);
}
