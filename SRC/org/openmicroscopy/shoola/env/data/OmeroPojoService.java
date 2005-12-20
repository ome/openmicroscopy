/*
 * org.openmicroscopy.shoola.env.data.OmeroPojoService
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

/** 
 * List of methods to retrieve data using OMERO.
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
public interface OmeroPojoService
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
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * 
     * @param rootNodeType top-most type which will be searched for 
     *                      Can be <code>Project</code> or
     *                      <code>CategoryGroup</code>. 
     *                      Mustn't be <code>null</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves.   Passed <code>true</code> to retrieve the images,
     *                      <code>false</code> otherwise.
     * @return  A set of hierarchy trees.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
                                    boolean withLeaves)
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
     * @return A <code>Set</code> with all root nodes that were found.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs)
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
     * @param nodeType  The type of the rootNodes. It can either be
     *                  <code>Dataset</code> or <code>Image</code>.
     *                  Mustn't be <code>null</code>. 
     * @param nodeIDs   TheIds of the objects of type <code>rootNodeType</code>.
     *                  Mustn't be <code>null</code>.
     * @param history   Passed <code>true</code> to retrieve all the annotations
     *                  related to the specified rootNode even if they are no
     *                  longer valid.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Map findAnnotations(Class nodeType, Set nodeIDs, boolean history)
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
     * @param nodeType  The type of container. Can be either Project, Dataset,
     *                  CategoryGroup, Category.
     * @param nodeIDs   Set of containers' IDS.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    public Set getImages(Class nodeType, Set nodeIDs)
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
    
}
