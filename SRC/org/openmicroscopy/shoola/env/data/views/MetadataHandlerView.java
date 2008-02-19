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
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.StructuredDataLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.ImageData;

/** 
 * 
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

	/**
	 * Loads the tags related to the object identified the the passed type
	 * and ID. Retrieves the tags created by the specified user if the 
	 * id is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadTags(Class rootType, long rootID, long userID,
			AgentEventListener observer);
	
	public CallHandle loadAttachments(Class rootType, long rootID, long userID,
			AgentEventListener observer);

	public CallHandle loadViewedBy(long imageID, long pixelsID,
									AgentEventListener observer);

	public CallHandle loadContainers(Class type, long id, long userID,
							AgentEventListener observer);

	public CallHandle loadUrls(Class type, long id, int i, 
					AgentEventListener observer);

	public CallHandle loadThumbnails(ImageData image, Set<Long> userIDs, 
						int thumbWidth, int thumbHeight, 
						AgentEventListener observer);

	public CallHandle loadStructuredData(Object dataObject, long userID,
										AgentEventListener observer);
	
}
