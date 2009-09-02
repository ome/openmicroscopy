/*
 * org.openmicroscopy.shoola.env.data.views.DataHandlerView 
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
import java.sql.Timestamp;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImageLoader;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
* Provides methods to support annotation.
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
public interface DataHandlerView
	extends DataServicesView
{
	
	/**
	 * Loads the images imported during the passed period.
	 * 
	 * @param startTime		The lower bound of the period interval. 
	 * @param endTime		The upper bound of the interval. 
	 * @param userID		The id of the user the images belonged to.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadImages(Timestamp startTime, Timestamp endTime, 
								long userID, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(long pixelsID, Class rootNodeType,
			List<Long> ids, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(long pixelsID, TimeRefObject ref, 
			AgentEventListener observer);
	
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(Class rootNodeType, List<Long> ids, 
									AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(TimeRefObject ref, 
										AgentEventListener observer);

	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setRndSettings(Class rootNodeType, List<Long> ids, 
									AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setRndSettings(TimeRefObject ref, 
										AgentEventListener observer);
	
	/**
	 * Retrieves the objects specified by the context of the search.
	 * 
	 * @param context	The context of the search.
	 * @param observer	Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle advancedSearchFor(SearchDataContext context, 
										AgentEventListener observer);

	/**
	 * Loads the files of a given type. The type is one the constants
	 * defined by the {@link OmeroMetadataService}.
	 * 
	 * @param type 	The type to handle.
	 * @param userID The id of the user.
	 * @param observer Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadFiles(int type, long userID,
			AgentEventListener observer);
	
}
