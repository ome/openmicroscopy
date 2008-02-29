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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEventListener;

import pojos.AnnotationData;
import pojos.DataObject;
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
	 * userID is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadTextualAnnotations(Class rootType, long rootID, 
									long userID, AgentEventListener observer);
	
	/**
	 * Loads the tags related to the object identified the the passed type
	 * and ID. Retrieves the tags created by the specified user if the 
	 * userID is not <code>-1</code>.
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
	
	/**
	 * Loads all the files attached by a given user to the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadAttachments(Class rootType, long rootID, long userID,
			AgentEventListener observer);

	/**
	 * Loads all the containers containing the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param imageID	The image's id. 
	 * @param pixelsID	The id of the pixels set.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadViewedBy(long imageID, long pixelsID,
									AgentEventListener observer);

	/**
	 * Loads all the containers containing the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadContainers(Class rootType, long rootID, long userID,
							AgentEventListener observer);

	/**
	 * Loads all the Urls attached by a given user to the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadUrls(Class rootType, long rootID, long userID, 
					AgentEventListener observer);
	
	/**
	 * Loads all the ratings attached by a given user to the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param rootType	The class identifying the object.
	 * 					Mustn't be <code>null</code>.
	 * @param rootID	The id of the node.
	 * @param userID	Pass <code>-1</code> if no user specified.
	 * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadRatings(Class rootType, long rootID, long userID, 
			AgentEventListener observer);

	public CallHandle loadThumbnails(ImageData image, Set<Long> userIDs, 
						int thumbWidth, int thumbHeight, 
						AgentEventListener observer);

	/**
	 * Loads all annotations related the specified object.
	 * Retrieves the files if the userID is not <code>-1</code>.
	 * 
	 * @param dataObject	The object to handle. Mustn't be <code>null</code>.
	 * @param userID		Pass <code>-1</code> if no user specified.
	 * @param observer  	Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadStructuredData(DataObject dataObject, long userID,
										AgentEventListener observer);
	
	/**
	 * Annotates the object identified by the passed type and id.
	 * 
	 * @param type			The type of object to annotate.
	 * @param id			The id of the object.
	 * @param annotation	The annotation to create.
	 * @param observer  	Callback handler.
     * @return A handle that can be used to cancel the call.
	 */
	public CallHandle annotate(Class type, long id, AnnotationData annotation,
							AgentEventListener observer);
	
}
