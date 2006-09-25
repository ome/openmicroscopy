/*
 * org.openmicroscopy.shoola.env.data.OmeroDataService
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

package org.openmicroscopy.shoola.env.data;





//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.DataObject;

/** 
 * List of methods to retrieve data using OMERO.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface OmeroDataService
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
     * Identifies the count property.
     */
    public static final String IMAGES_PROPERTY = "images";
    
    /**
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     * 
     * @param rootNodeType  The top-most type which will be searched for 
     *                      Can be <code>Project</code> or
     *                      <code>CategoryGroup</code>. 
     *                      Mustn't be <code>null</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers. 
     *                      Passed <code>null</code> to retrieve all top-most
     *                      nodes e.g. all user's projects.
     * @param withLeaves   	Passed <code>true</code> to retrieve the images,
     *                      <code>false</code> otherwise.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @return  A set of hierarchy trees.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
                                    boolean withLeaves, Class rootLevel, 
                                    long rootLevelID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves hierarchy trees in various hierarchies that
     * contain the specified Images.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * 
     * <p>
     * This method will look for all the containers containing the specified
     * Images and then for all containers containing those containers and on
     * up the container hierarchy. 
     * </p>
     * <p>
     * This method returns a <code>Set</code> with all root nodes that were
     * found. Every root node is linked to the found objects and so on until the
     * leaf nodes, which are <code>Image</code> objects. Note that the type of
     * any root node in the returned set can be the given rootNodeType, any of
     * its containees or an <code>Image</code>.
     * </p>
     * <p>
     * For example, say that you pass in the ids of six Images: <code>i1, i2,
     * i3, i4, i5, i6</code>.
     * If the P/D/I hierarchy in the DB looks like this:
     * </p>
     * 
     * <pre>
     *                  __p1__
     *                 /      \    
     *               _d1_    _d2_      d3
     *              /    \  /    \     |
     *             i1     i2     i3    i4    i5  i6
     * </pre>
     * 
     * <p>
     * Then the returned set will contain <code>p1, d3, i5, i6</code>. All
     * objects will be properly linked up.
     * </p>
     * <p>
     * Finally, this method will <i>only</i> retrieve the nodes that are
     * connected in a tree to the specified leaf image nodes. Back to the
     * previous example, if <code>d1</code> contained image
     * <code>img500</code>, then the returned object would <i>not</i>
     * contain <code>img500</code>. In a similar way, if <code>p1</code>
     * contained <code>ds300</code> and this dataset weren't linked to any of
     * the <code>i1, i2, i3, i4, i5, i6
     * </code> images, then <code>ds300</code>
     * would <i>not</i> be part of the returned tree rooted by <code>p1</code>.
     * </p>
     * 
     * @param rootNodeType  Top-most type which will be searched for 
     *                      Can be <code>Project</code> or
     *                      <code>CategoryGroup</code>. 
     *                      Mustn't be <code>null</code>.
     * @param leavesIDs     Set of ids of the Images that sit at the bottom of
     *                      the trees. Mustn't be <code>null</code>.
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @return A <code>Set</code> with all root nodes that were found.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs,
            						Class rootLevel, long rootLevelID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Finds all the annotations that have been attached to the specified
     * <code>rootNodes</code>. This method looks for all the <i>valid</i>
     * annotations that have been attached to each of the specified objects. It
     * then maps each <code>rootNodeID</code> onto the set of all annotations
     * that were found for that node. If no annotations were found for that
     * node, then the entry will be <code>null</code>. Otherwise it will be a
     * <code>Set</code> containing <code>Annotation</code> objects.
     * 
     * @param nodeType      The type of the rootNodes. It can either be
     *                      <code>Dataset</code> or <code>Image</code>.
     *                      Mustn't be <code>null</code>. 
     * @param nodeIDs       TheIds of the objects of type
     *                      <code>rootNodeType</code>. 
     *                      Mustn't be <code>null</code>.
     * @param annotatorIDs  The Ids of the users for whom annotations should be 
     *                      retrieved. If <code>null</code>, all annotations 
     *                      are returned.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs)
        throws DSOutOfServiceException, DSAccessException;
     
    /**
     * Retrieves paths in the Category Group/Category/Image (CG/C/I) hierarchy.
     * <p>
     * Because of the mutually exclusive rule of CG/C hierarchy, this method
     * is quite tricky.
     * We want to retrieve all Category Group/Category paths that end with
     * the specified leaves.
     * </p>
     * <p> 
     * We also want to retrieve the all Category Group/Category paths that
     * don’t end with the specified leaves, note that in that case because of 
     * the mutually exclusive constraint the categories which don’t contain a
     * specified leaf but which is itself contained in a group which already
     * has a category ending with the specified leaf is excluded.
     * </p>
     * <p>  
     * This is <u>more</u> restrictive than may be imagined. The goal is to 
     * find CGC paths to which an Image <B>MAY</b> be attached.
     * </p>
     * 
     * @param imgIDs    Set of ids of the images that sit at the bottom of the
     *                  CGC trees. Mustn't be <code>null</code>.
     * @param algorithm The search algorithm for finding paths. One of the 
     *                  following constants: {@link #DECLASSIFICATION},
     *                  {@link #CLASSIFICATION_ME},
     *                  {@link #CLASSIFICATION_NME}.
     * @return A <code>Set</code> of hierarchy trees with all root nodes 
     * that were found.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set findCGCPaths(Set imgIDs, int algorithm)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves the images contained in containers specified by the 
     * node type.
     * 
     * @param nodeType  	The type of container. Can either be Project, 
     * 						Dataset, CategoryGroup, Category or Image.
     * @param nodeIDs   	Set of node ids..
     * @param rootLevel		The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootLevelID	The Id of the root.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set getImages(Class nodeType, Set nodeIDs, Class rootLevel, 
            			long rootLevelID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves the images imported by the current user.
     * 
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set getUserImages()
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Counts the number of items in a collection for a given object.
     * Returns a map which key is the passed rootNodeID and the value is 
     * the number of items contained in this object.
     * 
     * @param rootNodeType 	The type of container. Can either be Dataset 
     * 						and Category.
     * @param property		One of the properties defined by this class.
     * @param rootNodeIDs	Set of root node IDs.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Map getCollectionCount(Class rootNodeType, String property,
            						Set rootNodeIDs)
    	throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Creates the specified annotation for the specified
     * <code>DataObject</code>. The updated <code>DataObject</code> is
     * then returned.
     * 
     * @param annotatedObject   The object to annotate.
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation to create. 
     *                          Mustn't be <code>null</code>.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject createAnnotationFor(DataObject annotatedObject,
                                            AnnotationData data)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Removes the specified annotation from the specified
     * <code>DataObject</code>. The updated <code>DataObject</code> is
     * then returned.
     * 
     * @param annotatedObject   The object to annotate.
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation data to remove. 
     *                          Mustn't be <code>null</code>.
     * @return See above.                  
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject removeAnnotationFrom(DataObject annotatedObject, 
                                            AnnotationData data)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Updates the specified annotation. The updated <code>DataObject</code> is
     * then returned.
     * 
     * @param annotatedObject   The object to annotate.
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation data to update. 
     *                          Mustn't be <code>null</code>.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject updateAnnotationFor(DataObject annotatedObject, 
                                        AnnotationData data)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Creates a new <code>DataObject</code> and links it to the specified 
     * parent. The parent will be <code>null</code> if the
     * <code>DataObject</code> to create is either a <code>Project</code>,
     * <code>CategoryGroup</code>.
     * 
     * @param newObject The <code>DataObject</code> to create.
     *                  Mustn't be <code>null</code>.
     * @param parent    The parent of the <code>DataObject</code>. Can be 
     *                  <code>null</code> if <code>DataObject</code> to create
     *                  is either a <code>Project</code>,
     *                  <code>CategoryGroup</code>.
     * @return          The newly created <code>DataObject</code>
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject createDataObject(DataObject newObject, DataObject parent)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Unlinks the specified <code>DataObject</code> and the parent object.
     * 
     * @param child     The child to remove. Mustn't be <code>null</code>.
     * @param parent    The child's parent. The parent is <code>null</code> if 
     *                  the child is a top node container i.e. 
     *                  <code>Project</code> or <code>Categorygroup</code>.
     * @return          The updated parent.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject removeDataObject(DataObject child, DataObject parent)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param object    The <code>DataObject</code> to update.
     * @return          The updated object.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public DataObject updateDataObject(DataObject object)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Adds the images to the specified categories.
     * 
     * @param images        The images to classify.
     * @param categories    The categories to add the images to.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public void classify(Set images, Set categories)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Removes the images from the specified categories.
     * 
     * @param images        The images to declassify
     * @param categories    The categories to remove the images from.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public void declassify(Set images, Set categories)
        throws DSOutOfServiceException, DSAccessException;

    /**
     * Loads the objects that be added to the node.
     * 
     * @param nodeType  The top-most type which will be searched for.
     *                  Can be <code>Project</code>, <code>CategoryGroup</code>,
     *                  <code>Dataset</code> or <code>Category</code>.
     *                  Mustn't be <code>null</code>.
     * @param nodeIDs   A set of the IDs of top-most containers. 
     *                  Mustn't be <code>null</code>.
     * @param rootLevel The level of the hierarchy either <code>GroupData</code>
     *                  or <code>ExperimenterData</code>.
     * @param rootID    The Id of the root.
     * @return  A set of existing nodes.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set loadExistingObjects(Class nodeType, Set nodeIDs, Class rootLevel, 
                                long rootID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Adds the given objects to the specified node.
     * 
     * @param parent    The <code>DataObject</code> to update. Either a 
     *                  <code>ProjectData</code> or <code>DatasetData</code>.
     * @param children  The items to add.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public void addExistingObjects(DataObject parent, Set children)
        throws DSOutOfServiceException, DSAccessException;
    
}
