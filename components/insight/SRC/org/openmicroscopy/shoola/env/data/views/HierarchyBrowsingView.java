/*
 * org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView
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
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.ThumbnailSetLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.DataObject;

/** 
 * Provides methods to support browsing of image hierarchies.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface HierarchyBrowsingView
    extends DataServicesView
{

	/** 
	 * Indicates that the thumbnails are associated to an 
	 * <code>ExperimenterData</code>.
	 */
	public static final int EXPERIMENTER = ThumbnailSetLoader.EXPERIMENTER;
	
	/** 
	 * Indicates that the thumbnails are associated to an <code>FileData</code>.
	 */
	public static final int FS_FILE = ThumbnailSetLoader.FS_FILE;
	
	/** 
	 * Indicates that the thumbnails are associated to an 
	 * <code>ImageData</code>.
	 */
	public static final int IMAGE = ThumbnailSetLoader.IMAGE;
	
    /**
     * Loads a data hierarchy rooted by a given node.
     * <p>The root node can be one out of: Project, Dataset.
     *  Image and Dataset items are retrieved with annotations.
     * The final <code>DSCallOutcomeEvent</code> will contain the requested node
     * as root and all of its descendants.</p>
     * <p>A Project tree will be represented by <code>ProjectData, 
     * DatasetData, and ImageData</code> objects. A Dataset tree
     * will only have objects of the latter two types.</p>
     * <p>So the object returned in the <code>DSCallOutcomeEvent</code> will be
     * a <code>ProjectData, DatasetData depending on whether you asked for a 
     * Project, Dataset tree.</p>
     * 
     * @param ctx The security context.
     * @param rootNodeType The type of the root node. Can only be one out of:
     *                      <code>ProjectData, DatasetData</code>.
     * @param nodesID The id of the root nodes.
     * @param userID The identifier of the user.  
     * @param observer Call-back handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadHierarchy(SecurityContext ctx, Class rootNodeType,
    		List nodesID, long userID, AgentEventListener observer);
    
    /**
     * Loads a thumbnail for each specified <code>DataObject</code> object, 
     * it can either be <code>ImageData</code>, <code>FileData</code> or
     * <code>ExperimenterData</code>.
     * As thumbnails are retrieved from server, they're posted back to
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
     * @param imgs Contains the objects, one for each thumbnail to retrieve.
     * @param maxWidth The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userID The id of the user the thumbnails are for.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadThumbnails(SecurityContext ctx, 
    		Collection<DataObject> imgs, int maxWidth, int maxHeight,
    		long userID, int type, AgentEventListener observer);
    
    /**
     * Loads a full size image for each specified <code>DataObject</code> 
     * object, it can either be <code>ImageData</code>, <code>FileData</code> or
     * <code>ExperimenterData</code>.
     * As thumbnails are retrieved from server, they're posted back to
     * the <code>observer</code> through <code>DSCallFeedbackEvent</code>s.
     * Each thumbnail will be posted in a single event; the <code>observer
     * </code> can then call the <code>getPartialResult</code> method to 
     * retrieve a <code>ThumbnailData</code> object for that thumbnail. The 
     * final <code>DSCallOutcomeEvent</code> will have no result.
     * 
     * @param ctx The security context.
     * @param imgs Contains the objects, one for each thumbnail to retrieve.
     * @param userID The id of the user the thumbnails are for.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImagesAsThumbnails(SecurityContext ctx, 
    		Collection<DataObject> imgs, long userID, 
    		AgentEventListener observer);

}
