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
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.DataObject;
import pojos.ImageData;

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
     * Loads a data hierarchy rooted by a given node.
     * <p>The root node can be one out of: Project, Dataset, Category Group, or
     * Category.  Image and Dataset items are retrieved with annotations.
     * The final <code>DSCallOutcomeEvent</code> will contain the requested node
     * as root and all of its descendants.</p>
     * <p>A Project tree will be represented by <code>ProjectData, 
     * DatasetData, and ImageData</code> objects. A Dataset tree
     * will only have objects of the latter two types.</p>
     * <p>A Category Group tree will be represented by <code>CategoryGroupData, 
     * CategoryData</code>, and <code>ImageData</code> objects. A Category 
     * tree will only have objects of the latter two types.</p>
     * <p>So the object returned in the <code>DSCallOutcomeEvent</code> will be
     * a <code>ProjectData, DatasetData, CategoryGroupData</code> or
     * <code>CategoryData</code> depending on whether you asked for a Project, 
     * Dataset, Category Group, or Category tree.</p>
     * 
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      <code>ProjectData, DatasetData</code>.
     * @param nodesID       The id of the root nodes.
     * @param userID   		The Id of the user.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadHierarchy(Class rootNodeType, List nodesID,
    								long userID, AgentEventListener observer);
    
    /**
     * Finds the data trees in the Project/Dataset/Image (P/D/I) hierarchy that 
     * contain the specified images. 
     * <p>This method will look for all the Datasets containing the specified 
     * Images (represented by the passed in <code>ImageData</code>s) and then
     * all Projects containing those Datasets. Projects will be represented by
     * <code>ProjectData</code>s and Datasets by <code>DatasetData</code>
     * objects &#151; dataset annotations are retrieved too.</p>
     * <p>The object returned in the <code>DSCallOutcomeEvent</code> will be a
     * <code>Set</code> with all root nodes that were found. Every root node
     * is linked to the found objects and so on until the leaf nodes, which are
     * the <i>passed in</i> <code>ImageData</code>s. Note that the type of
     * any root node in the returned set can be <code>ProjectData</code>, 
     * <code>DatasetData</code>, or <code>ImageData</code>.</p>
     * <p>For example, say that you pass in six <code>ImageData</code>s:
     * <code>i1, i2, i3, i4, i5, i6</code>.  If the P/D/I hierarchy in the DB
     * looks like this:</p>
     * <pre>        
     *                __p1__
     *               /      \    
     *             _d1_    _d2_      d3
     *            /    \  /    \     |
     *           i1     i2     i3    i4    i5  i6
     * </pre>
     * <p>Then the returned set will contain <code>p1, d3, i5, i6</code>.  All
     * objects will be properly linked to the <code>i1, i2, i3, i4, i5, i6
     * </code> objects you passed in to this method &#151; <code>p1, d1, d2, d3
     * </code> will obvioulsy be new objects though.</p>
     * <p>Finally, this method will <i>only</i> retrieve the nodes that are 
     * connected in a tree to the specified leaf Image nodes.  Back to the
     * previous example, if <code>d1</code> contained Image <code>img500</code>,
     * then the returned object would <i>not</i> contain <code>img500</code>.
     * In a similar way, if <code>p1</code> contained <code>ds300</code> and 
     * this dataset weren't linked to any of the <code>i1, i2, i3, i4, i5, i6
     * </code> Images, then <code>ds300</code> would <i>not</i> be part of the
     * returned tree rooted by <code>p1</code>.</p>
     * 
     * @param ids           Contains ids, one for each leaf Image node.
     * @param userID   		The Id of the user.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findPDIHierarchies(List ids, long userID, 
    									AgentEventListener observer);
    
    /**
     * Loads a thumbnail for each specified <code>ImageData</code> object.
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
     * @param imgs 		Contains <code>ImageData</code> objects, one
     *                  for each thumbnail to retrieve.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param userID	The id of the user the thumbnails are for.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadThumbnails(Collection<ImageData> imgs, int maxWidth, 
    								int maxHeight, long userID, 
    								AgentEventListener observer);
    
    /**
     * Loads a full size image for each specified <code>ImageData</code> object.
     * As thumbnails are retrieved from server, they're posted back to
     * the <code>observer</code> through <code>DSCallFeedbackEvent</code>s.
     * Each thumbnail will be posted in a single event; the <code>observer
     * </code> can then call the <code>getPartialResult</code> method to 
     * retrieve a <code>ThumbnailData</code> object for that thumbnail. The 
     * final <code>DSCallOutcomeEvent</code> will have no result.
     * 
     * @param imgs 		Contains <code>ImageData</code> objects, one
     *                  for each thumbnail to retrieve.
     * @param userID	The id of the user the thumbnails are for.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImagesAsThumbnails(Collection<ImageData> imgs, 
    								long userID, 
    								AgentEventListener observer);
    
    /**
     * Loads the images corresponding to the given collection of ids.
     * <p>Image items are retrieved with annotations.
     * The final <code>DSCallOutcomeEvent</code> will contain the requested node
     * as root and all of its descendants.</p>
     * <p>An Image will be represented by <code>ImageData</code> objects. </p>
     * 
     * @param imagesID      The id of the images nodes.
     * @param userID		The user's id.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImages(List imagesID, long userID, 
    							AgentEventListener observer);
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param userObject    The <code>DataObject</code> to save.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateDataObject(DataObject userObject,
                                        AgentEventListener observer);
    
    /**
     * Loads the emission wavelengths for the given set of pixels.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadChannelsData(long pixelsID, 
                                        AgentEventListener observer);
    
    /**
     * Removes the specified <code>DataObject</code> from the specified 
     * parents.
     * 
     * @param userObjects   The <code>DataObject</code>s to remove.
     * @param parent        The parent of the <code>DataObject</code>.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle removeDataObjects(Set userObjects, DataObject parent, 
                                    AgentEventListener observer);
    
    /**
     * Removes the specified <code>DataObject</code> from the specified 
     * parents.
     * 
     * @param objects   The <code>DataObject</code>s to remove.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle removeDataObjects(Map objects, 
                                    AgentEventListener observer);
    
}
