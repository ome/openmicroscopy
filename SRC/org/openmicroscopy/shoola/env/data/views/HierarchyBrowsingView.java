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
import org.openmicroscopy.shoola.env.data.OmeroService;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
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

    /** Identifies the <code>Declassification</code> algorithm. */
    public static final int DECLASSIFICATION = 
                                OmeroService.DECLASSIFICATION;
    
    /**
     * Identifies the <code>Classification</code> algorithm with
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_ME = 
                                OmeroService.CLASSIFICATION_ME;
    
    /**
     * Identifies the <code>Classification</code> algorithm without
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_NME = 
                            OmeroService.CLASSIFICATION_NME;
    
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
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadHierarchy(Class rootNodeType, long nodeID, 
                                    Class rootLevel, long rootLevelID,
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
     * @param ids           Contains ids, one for each leaf Image node.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findPDIHierarchies(Set ids, Class rootLevel,
                                long rootLevelID, AgentEventListener observer);
    
    /**
     * Finds the data trees in the Category Group/Category/Image (CG/C/I) 
     * hierarchy that contain the specified images.
     * This method is the analogous of the 
     * {@link #findPDIHierarchies(Set, Class, int, AgentEventListener)} 
     * method for the Category Group/Category/Image hierarchy. The semantics
     * is exaclty the same, so refer to that method's documentation for the
     * gory details.  (Obviously, a Category Group will be represented by a
     * <code>CategoryGroupData</code> object and a Category by a <code> 
     * CategoryData</code> object.)
     * 
     * @param ids           Contains ids, one for each leaf image node.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle findCGCIHierarchies(Set ids,  Class rootLevel,
                                 long rootLevelID, AgentEventListener observer);
    
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
     * @param imageIDs  The id of the images.
     * @param algorithm  One of the following constants:
     *                  {@link OmeroService#DECLASSIFICATION},
     *                  {@link OmeroService#CLASSIFICATION_ME},
     *                  {@link OmeroService#CLASSIFICATION_NME}.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadClassificationPaths(Set imageIDs, int algorithm,
                                              AgentEventListener observer);
    
    /**
     * Adds the images to the specified categories.
     * 
     * @param images        The images to classify.        
     * @param categories    Collection of <code>CategoryData</code>.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle classify(Set images, Set categories, 
                                AgentEventListener observer);
    
    /**
     * Removes the images from the categories.
     * 
     * @param images        The images to declassify.     
     * @param categories    Collection of <code>CategoryData</code>.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle declassify(Set images, Set categories, 
                                AgentEventListener observer);
    
    /**
     * Retrieves all the annotations linked to the specified node type.
     * 
     * @param nodeType      The type of the node. One of the following types:
     *                      <code>DatasetData</code>, <code>ImageData</code>.       
     * @param nodeID        The id of the node.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadAnnotations(Class nodeType, long nodeID,
                                        AgentEventListener observer);
    
    /** 
     * Creates an annotation of the specified type for the specified node.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          One of the following type:
     *                          <code>DatasetData</code>,
     *                          <code>ImageData</code>.  
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation to create.
     *                          Mustn't be <code>null</code>.
     * @param observer Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle createAnnotation(DataObject annotatedObject,
                                        AnnotationData data,  
                                        AgentEventListener observer);
    
    /**
     * Updates the specified annotation.
     * 
     * @param annotatedObject   The annotated <code>DataObject</code>.
     *                          One of the following type:
     *                          <code>DatasetData</code>,
     *                          <code>ImageData</code>.   
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation object to update. 
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle         that can be used to cancel the call.
     */
    public CallHandle updateAnnotation(DataObject annotatedObject,
                                        AnnotationData data,
                                        AgentEventListener observer);
    
    /**
     * Deletes the specified annotation.
     * 
     * @param annotatedObject   The annotated <code>DataObject</code>.
     *                          One of the following type:
     *                          <code>DatasetData</code>,
     *                          <code>ImageData</code>.   
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation object to update. 
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle deleteAnnotation(DataObject annotatedObject,
                                        AnnotationData data,
                                        AgentEventListener observer);
    
    /**
     * Loads the images corresponding to the given collection of ids.
     * <p>Image items are retrieved with annotations.
     * The final <code>DSCallOutcomeEvent</code> will contain the requested node
     * as root and all of its descendants.</p>
     * <p>An Image will be represented by <code>ImageData</code> objects. </p>
     * 
     * @param imagesID      The id of the images nodes.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImages(Set imagesID, Class rootLevel,
                                long rootLevelID, AgentEventListener observer);
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param userObject    The <code>DataObject</code> to save.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateDataObject(DataObject userObject,
                                        AgentEventListener observer);
    
}
