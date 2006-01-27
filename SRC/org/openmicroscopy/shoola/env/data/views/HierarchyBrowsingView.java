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
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
import pojos.CategoryData;

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

    /** Identifies the <code>Declassification</code> algorithm. */
    public static final int DECLASSIFICATION = 0;
    
    /**
     * Identifies the <code>Classification</code> algorithm with
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_ME = 1;
    
    /**
     * Identifies the <code>Classification</code> algorithm without
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_NME = 2;
    
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
     *                      <code>ProjectData, DatasetData, 
     *                      CategoryGroupData, CategoryData</code>.
     * @param nodeID        The id of the root node.
     * @param rootLevel     The Level of the root.
     * @param rootID        The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadHierarchy(Class rootNodeType, int nodeID, 
                                    int rootLevel, int rootID,
                                    AgentEventListener observer);
    
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
     * @param ids Contains ids, one for each leaf Image node.
     * @param observer     Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findPDIHierarchies(Set ids, AgentEventListener observer);
    
    /**
     * Finds the data trees in the Category Group/Category/Image (CG/C/I) 
     * hierarchy that contain the specified images.
     * This method is the analogous of the 
     * {@link #findPDIHierarchies(Set, AgentEventListener) findPDIHierarchies}
     * method for the Category Group/Category/Image hierarchy.  The semantics
     * is exaclty the same, so refer to that method's documentation for the
     * gory details.  (Obviously, a Category Group will be represented by a
     * <code>CategoryGroupData</code> object and a Category by a <code> 
     * CategoryData</code> object.)
     * 
     * @param ids Contains ids, one for each leaf image node.
     * @param observer     Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findCGCIHierarchies(Set ids, AgentEventListener observer);
    
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
     * @param imgs Contains <code>ImageData</code> objects, one
     *                      for each thumbnail to retrieve.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadThumbnails(Set imgs, int maxWidth, int maxHeight,
                                     AgentEventListener observer);
    
    /**
     * Loads all Category Group/Category paths that end or don't end with the 
     * specified Image, depending on the value of the <code>classified</code>
     * argument.
     * <p>If the <code>classified</code> argument is <code>true</code>, this 
     * method loads all the Category nodes under which was classified the Image
     * whose id is <code>imageID</code>, and then all the Category Group nodes
     * that contain those Categories.  If <code>false</code>, then it does the
     * opposite: it loads all the Categories the given Image doesn't belong in,
     * and then all the Category Groups that contain those Categories.
     * This method returns all the matching Category Groups (as <code>
     * CategoryGroupData</code> objects) in a <code>Set</code>, which is the
     * result object of the <code>DSCallOutcomeEvent</code>.
     * Those objects will also be linked to the matching Categories (represented
     * by <code>CategoryData</code> objects).  For example, assume the CG/C/I 
     * hierarchy in the DB looks like this:</p>
     * <pre>        
     *           cg1       cg2
     *             \      /   \    
     *             c1    c2    c3      
     *               \  /  \    \
     *                i1    i2   i3    
     * </pre>
     * <p>Then if you specify the id of Image <code>i1</code> and pass 
     * <code>true</code> for <code>classified</code> to this method, the 
     * returned set will contain <code>cg1, cg2</code>.  Moreover, <code>cg1
     * </code> will be linked to <code>c1</code> and <code>cg2</code> to <code>
     * c2</code>.  If you specify <code>false</code> for <code>classified</code>
     * (and again the id of Image <code>i1</code>), then you will get <code>
     * cg2</code> and it will be linked to <code>c3</code>.</p> 
     * 
     * @param imageID   The id of the Image.
     * @param algorithm  One of the following constants:
     *                  {@link OmeroPojoService#DECLASSIFICATION},
     *                  {@link OmeroPojoService#CLASSIFICATION_ME},
     *                  {@link OmeroPojoService#CLASSIFICATION_NME}.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadClassificationPaths(int imageID, int algorithm,
                                              AgentEventListener observer);
    
    /**
     * Classifies the specified Images under the given Category.
     * This method has no return value, so the result object of the <code>
     * DSCallOutcomeEvent</code> will be <code>null</code>.
     * 
     * @param category  The data object that represents the Category.
     * @param imgIDs    The ids of the Images to classify.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle classify(CategoryData category, Set imgIDs, 
                               AgentEventListener observer);
    
    /**
     * Removes the specified Images from the specified Category.
     * This method has no return value, so the result object of the <code>
     * DSCallOutcomeEvent</code> will be <code>null</code>.
     * 
     * @param data  The data object that represents the Category.
     * @param imgIDs    The ids of the Images to declassify.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle declassify(CategoryData data, Set imgIDs, 
                                 AgentEventListener observer);
    
    /**
     * Retrieves all the annotations linked to the specified node type.
     * 
     * @param nodeType The type of the node. Can only be one out of:
     *                      <code>DatasetData, ImageData</code>.       
     * @param nodeID The id of the node.
     * @param history Pass <code>true</code> to retrieve all the annotations
     * related to the specified rootNode even if they are no longer valid.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadAnnotations(Class nodeType, int nodeID,
                                        boolean history,
                                        AgentEventListener observer);
    
    /** 
     * Creates an annotation of the specified type for the specified node.
     * 
     * @param nodeType The type of the node. Can only be one out of:
     *                      <code>DatasetData, ImageData</code>.   
     * @param nodeID The id of the node.
     * @param txt The textual annotation.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle createAnnotation(Class nodeType, int nodeID, String txt,  
                                        AgentEventListener observer);
    
    /**
     * Updates the specified annotation.
     * 
     * @param nodeType The type of the node. Can only be one out of:
     *                      <code>DatasetData, ImageData</code>.  
     * @param nodeID The id of the node.
     * @param data The Annotation object to update.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateAnnotation(Class nodeType, int nodeID, 
                             AnnotationData data, AgentEventListener observer);
    
    /**
     * Deletes the specified annotation.
     * 
     * @param nodeType The type of the node. Can only be one out of:
     *                      <code>DatasetData, ImageData</code>. 
     * @param data The Annotation object to delete.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle deleteAnnotation(Class nodeType, AnnotationData data,
                                        AgentEventListener observer);
    
}
