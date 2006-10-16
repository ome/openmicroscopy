/*
 * org.openmicroscopy.shoola.env.data.views.DataManagerView
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
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
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
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface DataManagerView
    extends DataServicesView
{
    

    /** Identifies the <code>Declassification</code> algorithm. */
    public static final int DECLASSIFICATION = 
                                OmeroDataService.DECLASSIFICATION;
    
    /**
     * Identifies the <code>Classification</code> algorithm with
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_ME = 
                                OmeroDataService.CLASSIFICATION_ME;
    
    /**
     * Identifies the <code>Classification</code> algorithm without
     * mutually exclusive rule.
     */
    public static final int CLASSIFICATION_NME = 
                            OmeroDataService.CLASSIFICATION_NME;
    
    /**
     * Reloads the hierarchy currently displayed.
     * 
     * @param rootNodeType          The type of the root node. Can only be one 
     *                              out of:
     *                              <code>ProjectData, CategoryGroupData</code>.
     * @param expandedNodes   The nodes in the hierarchy whose children
     *                              have been loaded. Can only be one out of:
     *                              <code>DatasetData, CategoryData</code>.
     * @param rootLevel             The level of the hierarchy either 
     *                              <code>GroupData</code> or 
     *                              <code>ExperimenterData</code>.
     * @param rootLevelID           The Id of the root.
     * @param observer              Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle refreshHierarchy(Class rootNodeType,
                                        List expandedNodes,
                                        Class rootLevel, long rootLevelID,
                                        AgentEventListener observer);
    
    /**
     * Retrieves the hierarchies specified by the 
     * parameters.
     * 
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      <code>ProjectData, DatasetData, 
     *                      CategoryGroupData, CategoryData</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers. Passed
     *                      <code>null</code> to retrieve all the top-most
     *                      container specified by the rootNodeType.
     * @param withLeaves    Passes <code>true</code> to retrieve the images.
     *                      <code>false</code> otherwise.   
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadContainerHierarchy(Class rootNodeType,
                                            Set rootNodeIDs, boolean withLeaves,
            								Class rootLevel, long rootLevelID,
                                            AgentEventListener observer);
    
    /**
     * Retrieves the images specified by the parameters.
     * 
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImages(AgentEventListener observer);
    
    /**
     * Retrieves the images container in the specified root nodes.
     * 
     * @param nodeType 		The type of the node. Can only be one out of:
     *                      <code>DatasetData, CategoryData</code>.       
     * @param nodeIDs 		The id of the node.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle getImages(Class nodeType, Set nodeIDs, Class rootLevel, 
            					long rootLevelID, AgentEventListener observer);
    
    /**
     * Creates a new <code>DataObject</code> whose parent is specified by the
     * ID.
     * 
     * @param userObject    The type of <code>DataObject</code> to create.
     * @param parent 		The parent of the <code>DataObject</code>.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle createDataObject(DataObject userObject, DataObject parent,
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
     * Removes the specified <code>DataObject</code> from the specified 
     * parents.
     * 
     * @param userObjects 	The <code>DataObject</code>s to remove.
     * @param parent 		The parent of the <code>DataObject</code>.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle removeDataObjects(List userObjects, DataObject parent, 
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
    
    /**
     * Counts the number of items contained in the specified containers.
     * 
     * @param rootNodeIDs   Collection of top-most containers.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle countContainerItems(Set rootNodeIDs, 
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
     * @param observer          Callback handler.
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
     * @param data              The Annotation object to update.
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
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
     * @param data              The annotation object to delete. 
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle deleteAnnotation(DataObject annotatedObject,
                                        AnnotationData data,
                                        AgentEventListener observer);
    
    /**
     * Retrieves all the annotations linked to the specified node type.
     * 
     * @param nodeType  The type of the node. One out of the following types:
     *                  <code>DatasetData, ImageData</code>.      
     * @param nodeID    The id of the node.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadAnnotations(Class nodeType, long nodeID,
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
     * @param image     The <code>ImageData</code> object the thumbnail is for.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadThumbnail(ImageData image, int maxWidth, 
                                   int maxHeight, AgentEventListener observer);
    
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
     * @param imageIDs      The id of the images.
     * @param algorithm     One of the following constants:
     *                      {@link OmeroDataService#DECLASSIFICATION},
     *                      {@link OmeroDataService#CLASSIFICATION_ME},
     *                      {@link OmeroDataService#CLASSIFICATION_NME}.
     * @param observer      Callback handler.
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
     * Loads existing objects not contained in the specifed containers.
     * @param nodeType      The type of the node. One out of the following 
     *                      types:
     *                      <code>DatasetData</code>, <code>ProjectData</code>,
     *                      <code>CategoryData</code> or 
     *                      <code>CategoryGroupData</code>.      
     * @param nodeIDs       The id of the nodes.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID   The Id of the root.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadExistingObjects(Class nodeType, Set nodeIDs, 
                                    Class rootLevel, long rootLevelID,
                                        AgentEventListener observer);

    /**
     * Adds the specified items to the parent.
     * 
     * @param parent    The <code>DataObject</code> to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle addExistingObjects(DataObject parent, Set children, 
                                    AgentEventListener observer);
    
    /**
     * Adds the specified items to the parent.
     * 
     * @param objects   The objects to update.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle addExistingObjects(Map objects, 
                                    AgentEventListener observer);
    
    /**
     * 
     * @param toPaste
     * @param toCut
     * @param observer
     * @return
     */
    public CallHandle cutAndPaste(Map toPaste, Map toCut, 
                                    AgentEventListener observer);
    
}
