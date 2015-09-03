/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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


import java.sql.Timestamp;
import java.util.List;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import omero.gateway.SecurityContext;
import omero.gateway.model.SearchParameters;

import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;

/** 
* Provides methods to support annotation.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME3.0
*/
public interface DataHandlerView
	extends DataServicesView
{
	
	/**
	 * Loads the images imported during the passed period.
	 * 
	 * @param ctx The security context.
	 * @param startTime The lower bound of the period interval. 
	 * @param endTime The upper bound of the interval. 
	 * @param userID The id of the user the images belonged to.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadImages(SecurityContext ctx, Timestamp startTime,
			Timestamp endTime, long userID, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets
	 * if the rootType is <code>DatasetData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set of reference.
	 * @param rootNodeType The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param ids The identifiers of the nodes to apply settings to. 
	 * 				Mustn't be <code>null</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(SecurityContext ctx, long pixelsID,
			Class rootNodeType, List<Long> ids, AgentEventListener observer);
	
	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets
	 * if the rootType is <code>DatasetData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param ids The identifiers of the nodes to apply settings to. 
	 * 				Mustn't be <code>null</code>.
	 * @param def The 'pending' rendering settings
     * @param refImage The image the rendering settings belong to
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(SecurityContext ctx,
                Class rootNodeType, List<Long> ids, RndProxyDef def, ImageData refImage, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets
	 * if the rootType is <code>DatasetData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The identifier of the pixels set of reference.
	 * @param ref The time reference object.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(SecurityContext ctx, long pixelsID,
			TimeRefObject ref, AgentEventListener observer);
	
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType The type of nodes. Can either be 
	 *                     <code>ImageData</code>, <code>DatasetData</code>.
	 * @param ids The identifiers of the nodes to apply settings to. 
	 *            Mustn't be <code>null</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(SecurityContext ctx, Class rootNodeType,
			List<Long> ids, AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ctx The security context.
	 * @param ref The time reference object.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(SecurityContext ctx, TimeRefObject ref,
										AgentEventListener observer);

	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType The type of nodes. Can either be 
	 *                     <code>ImageData</code>, <code>DatasetData</code>.
	 * @param ids The identifiers of the nodes to apply settings to. 
	 *            Mustn't be <code>null</code>.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setMinMaxSettings(SecurityContext ctx, Class rootNodeType,
			List<Long> ids, AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ctx The security context.
	 * @param ref The time reference object.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setMinMaxSettings(SecurityContext ctx, TimeRefObject ref,
										AgentEventListener observer);
	
	/**
	 * Resets the rendering settings used by the owner of the images contained 
	 * in the specified datasets.
	 * If the rootType is <code>ImageData</code, resets the settings to the 
	 * passed images.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param ids			The identifiers of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setOwnerRndSettings(SecurityContext ctx,
			Class rootNodeType, List<Long> ids, AgentEventListener observer);

	/**
	 * Resets the rendering settings used by the owner of the images
	 * imported during a period of time
	 * 
	 * @param ctx The security context.
	 * @param ref The time reference object.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setOwnerRndSettings(SecurityContext ctx,
			TimeRefObject ref, AgentEventListener observer);
	
	/**
	 * Retrieves the objects specified by the context of the search.
	 * 
	 * @param ctx The security context.
	 * @param context The context of the search.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle advancedSearchFor(SecurityContext ctx,
	        SearchParameters context, AgentEventListener observer);

	/**
	 * Loads the files of a given type. The type is one the constants
	 * defined by the {@link OmeroMetadataService}.
	 * 
	 * @param ctx The security context.
	 * @param type The type to handle.
	 * @param userID The id of the user.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFiles(SecurityContext ctx, int type, long userID,
			AgentEventListener observer);
	
	/**
	 * Switches the user's group.
	 * 
	 * @param ctx The security context.
	 * @param experimenter The experimenter to handle.
	 * @param groupID The identifier of the group.
	 * @param observer Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle switchUserGroup(SecurityContext ctx,
		ExperimenterData experimenter, long groupID,
		AgentEventListener observer);
	
}
