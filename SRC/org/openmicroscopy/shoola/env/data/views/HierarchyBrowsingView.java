/*
 * org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEventListener;

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
     * <p>A Project tree will be represented by <code>ProjectSummary, 
     * DatasetSummaryLinked, and ImageSummary</code> objects.  A Dataset tree
     * will only have objects of the latter two types.</p>
     * <p>A Category Group tree will be represented by <code>CategoryGroupData, 
     * CategoryData</code>, and <code>ImageSummary</code> objects.  A Category 
     * tree will only have objects of the latter two types.</p>
     * <p>So the object returned in the <code>DSCallOutcomeEvent</code> will be
     * a <code>ProjectSummary, DatasetSummaryLinked, CategoryGroupData</code> or
     * <code>CategoryData</code> depending on whether you asked for a Project, 
     * Dataset, Category Group, or Category tree.</p>
     * 
     * @param rootNodeType  The type of the root node.  Can only be one out of:
     *                      <code>ProjectSummary, DatasetSummaryLinked, 
     *                      CategoryGroupData, CategoryData</code>.
     * @param nodeID        The id of the root node.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadHierarchy(Class rootNodeType, int nodeID,
                                    AgentEventListener observer);
    
    /**
     * Finds the data trees in the Project/Dataset/Image (P/D/I) hierarchy that 
     * contain the specified images. 
     * <p>This method will look for all the Datasets containing the specified 
     * Images (represented by the passed in <code>ImageSummary</code>s) and then
     * all Projects containing those Datasets.  Projects will be represented by
     * <code>ProjectSummary</code>s and Datasets by <code>DatasetSummaryLinked
     * </code> objects &#151; dataset annotations are retrieved too.</p>
     * <p>The object returned in the <code>DSCallOutcomeEvent</code> will be a
     * <code>Set</code> with all root nodes that were found.  Every root node
     * is linked to the found objects and so on until the leaf nodes, which are
     * the <i>passed in</i> <code>ImageSummary</code>s.  Note that the type of
     * any root node in the returned set can be <code>ProjectSummary</code>, 
     * <code>DatasetSummaryLinked</code>, or <code>ImageSummary</code>.</p>
     * <p>For example, say that you pass in six <code>ImageSummary</code>s:
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
     * connected in a tree to the specified leaf image nodes.  Back to the
     * previous example, if <code>d1</code> contained image <code>img500</code>,
     * then the returned object would <i>not</i> contain <code>img500</code>.
     * In a similar way, if <code>p1</code> contained <code>ds300</code> and 
     * this dataset weren't linked to any of the <code>i1, i2, i3, i4, i5, i6
     * </code> images, then <code>ds300</code> would <i>not</i> be part of the
     * returned tree rooted by <code>p1</code>.</p>
     * 
     * @param imgSummaries Contains <code>ImageSummary</code> objects, one
     *                     for each leaf image node.
     * @param observer     Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findPDIHierarchies(Set imgSummaries,
                                         AgentEventListener observer);
    
    /**
     * Finds the data trees in the Category Group/Category/Image (CG/C/I) 
     * hierarchy that contain the specified images.
     * This method is the analogous of the 
     * {@link #findPDIHierarchies(Set, AgentEventListener) findPDIHierarchies}
     * method for the Category Group/Category/Image hierarchy.  The semantics
     * is exaclty the same, so refer to that method's documentation for the
     * gory details.
     * 
     * @param imgSummaries Contains <code>ImageSummary</code> objects, one
     *                     for each leaf image node.
     * @param observer     Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findCGCIHierarchies(Set imgSummaries,
                                          AgentEventListener observer);
    
    /**
     * Loads a thumbnail for each specified <code>ImageSummary</code> object.
     * As thumbnails are retrieved from <i>OMEIS</i>, they're posted back to
     * the <code>observer</code> through <code>DSCallFeedbackEvent</code>s.
     * Each thumbnail will be posted in a single event; the <code>observer
     * </code> can then call the <code>getPartialResult</code> method to 
     * retrieve a <code>ThumbnailData</code> object for that thumbnail.  The 
     * final <code>DSCallOutcomeEvent</code> will have no result.
     * Thumbnails are generated respecting the <code>X/Y</code> ratio of the
     * original image and so that their area doesn't exceed <code>maxWidth*
     * maxHeight</code>.
     * 
     * @param imgSummaries Contains <code>ImageSummary</code> objects, one
     *                      for each thumbnail to retrieve.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadThumbnails(Set imgSummaries, 
                                     int maxWidth, int maxHeight,
                                     AgentEventListener observer);
    
    /**
     * 
     * @param imageID
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadClassificationPaths(int imageID, 
                            AgentEventListener observer);
    
}
