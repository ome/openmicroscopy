/*
 * ome.api.HierarchyBrowsing
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.api;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides methods to support browsing of Image hierarchies. This 
 * interface is specialized for the OMERO/Hibernate model. An adapter will
 * be needed to connect this to the existing client models.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 * @DEV.TODO Possibly change these methods to return an abstract super type 
 * @DEV.TODO or even a concrete for these or concrete type like "Project loadPDIHierary()"
 */
public interface HierarchyBrowsing
{

    /**
     * Loads a Project/Dataset/Image (P/D/I) hierarchy rooted by a given node.
     * <p>The root node can be either Project or Dataset.  A Project tree will
     * be represented by {@link Project}, {@link Dataset}, and
     * {@link Image} objects.  A Dataset tree will only have objects of
     * the latter two types.</p>
     * <p>This method also retrieves the Experimenters linked to the objects
     * in the tree &#151; an Experimenter is represented by an 
     * {@link Experimenter} object.  Similarly, all Images will be linked
     * to their Pixels &#151; represented by {@link Pixels} objects.</p>
     * <p>Note that objects are never duplicated.  For example, if an 
     * Experimenter owns all the objects in the retrieved tree, then those
     * objects will be linked to the <i>same</i> instance of
     * {@link Experimenter}.  Or if an Image is contained in more than
     * one Dataset in the retrieved tree, then all enclosing {@link Dataset}
     * objects will point to the <i>same</i> {@link Image} object.  And so
     * on.</p>
     * 
     * @param rootNodeType  The type of the root node.  Can either be
     *                      {@link Project} or {@link Dataset}.
     * @param rootNodeID    The id of the root node.
     * @return The requested node as root and all of its descendants.  The type
     *         of the returned value will be <code>rootNodeType</code>. 
     */
    public OMEModel loadPDIHierarchy(Class rootNodeType, int rootNodeID);
    
    /**
     * Same above. + 
     * Retrieves the datasetAnnotion and imageAnnotation for the specified user.
     * @param rootNodeType
     * @param rootNodeID
     * @param experimenterID
     * @return
     */
    public OMEModel loadPDIAnnotatedHierarchy(Class rootNodeType, 
                                            int rootNodeID, int experimenterID);
    
    
    /**
     * Loads a Category Group/Category/Image (CG/C/I) hierarchy rooted by a
     * given node.
     * <p>The root node can be either Category Group or Category.  A Category
     * Group tree will be represented by {@link CategoryGroup}, 
     * {@link Category}, and {@link Image} objects.  A Category tree
     * will only have objects of the latter two types.</p>
     * <p>This method also retrieves the Experimenters linked to the objects
     * in the tree &#151; an Experimenter is represented by an 
     * {@link Experimenter} object.  Similarly, all Images will be linked
     * to their Pixels &#151; represented by {@link Pixels} objects.</p>
     * <p>Note that objects are never duplicated.  For example, if an 
     * Experimenter owns all the objects in the retrieved tree, then those
     * objects will be linked to the <i>same</i> instance of
     * {@link Experimenter}.</p>
     * 
     * @param rootNodeType  The type of the root node.  Can either be
     *                      {@link CategoryGroup} or {@link Category}.
     * @param rootNodeID    The id of the root node.
     * @return The requested node as root and all of its descendants.  The type
     *         of the returned value will be <code>rootNodeType</code>. 
     */
    public OMEModel loadCGCIHierarchy(Class rootNodeType, int rootNodeID);
    
    /**
     * Same above. + 
     * Retrieves the imageAnnotation for the specified user.
     * @param rootNodeType
     * @param rootNodeID
     * @param experimenterID
     * @return
     */
    public OMEModel loadCGCIAnnotatedHierarchy(Class rootNodeType, 
                                            int rootNodeID, int experimenterID);
    
    /**
     * Finds the data trees in the Project/Dataset/Image (P/D/I) hierarchy that 
     * contain the specified Images. 
     * <p>This method will look for all the Datasets containing the specified 
     * Images and then for all Projects containing those Datasets.  In the 
     * returned trees, Projects will be represented by {@link Project}
     * objects, Datasets by {@link Dataset} objects, and Images by
     * {@link Image} objects.</p>
     * <p>This method returns a <code>Set</code> with all root nodes that were
     * found.  Every root node is linked to the found objects and so on until
     * the leaf nodes, which are {@link Image} objects.  Note that the type
     * of any root node in the returned set can be {@link Project}, 
     * {@link Dataset}, or {@link Image}.</p>
     * <p>For example, say that you pass in the ids of six Images: <code>i1, i2,
     * i3, i4, i5, i6</code>.  If the P/D/I hierarchy in the DB looks like this:
     * </p>
     * <pre>        
     *                __p1__
     *               /      \    
     *             _d1_    _d2_      d3
     *            /    \  /    \     |
     *           i1     i2     i3    i4    i5  i6
     * </pre>
     * <p>Then the returned set will contain <code>p1, d3, i5, i6</code>.  All
     * objects will be properly linked up.</p>
     * <p>Finally, this method will <i>only</i> retrieve the nodes that are 
     * connected in a tree to the specified leaf image nodes.  Back to the
     * previous example, if <code>d1</code> contained image <code>img500</code>,
     * then the returned object would <i>not</i> contain <code>img500</code>.
     * In a similar way, if <code>p1</code> contained <code>ds300</code> and 
     * this dataset weren't linked to any of the <code>i1, i2, i3, i4, i5, i6
     * </code> images, then <code>ds300</code> would <i>not</i> be part of the
     * returned tree rooted by <code>p1</code>.</p>
     * 
     * @param imgIDs Contains the ids of the Images that sit at the bottom of
     *               the trees.
     * @return A <code>Set</code> with all root nodes that were found.
     */
    public Set findPDIHierarchies(Set imgIDs);
    
    /**
     * Same above. + 
     * Retrieves the datasetAnnotion and imageAnnotation for the specified user.
     * @param imgIDs
     * @param experimenterID
     * @return
     */
     public Set findPDIAnnotatedHierarchies(Set imgIDs, int experimenterID);
    
    /**
     * Finds the data trees in the Category Group/Category/Image (CG/C/I) 
     * hierarchy that contain the specified Images.
     * This method is the analogous of the 
     * {@link #findPDIHierarchies(Set) findPDIHierarchies}
     * method for the Category Group/Category/Image hierarchy.  The semantics
     * is exaclty the same, so refer to that method's documentation for the
     * gory details.
     * 
     * @param imgIDs Contains the ids of the Images that sit at the bottom of
     *               the trees.
     * @return A <code>Set</code> with all root nodes that were found.
     */
    public Set findCGCIHierarchies(Set imgIDs);
    
    /**
     * Same above. + 
     * Retrieves the imageAnnotation for the specified user.
     * @param imgIDs
     * @param experimenterID
     * @return
     */
    public Set findCGCIAnnotatedHierarchies(Set imgIDs, int experimenterID);
    
    /**
     * Finds data paths in the Category Group/Category/Image (CG/C/I) 
     * hierarchy.
     * This method is similar to {@link #findCGCIHierarchies(Set) findCGCIHierarchies} 
     * but returns only CategoryGroups and Categories. If <code>contained</code>
     * is true the CGC paths are returned which lead to this image.
     * If <code>contained</code> is <code>false</code>, all paths are excluded
     * for which an image is contained in a <code><b>CategoryGroup</b></code>.
     * This is <u>more</u> restrictive than may be imagined.
     * 
     * @param imgIDs Contains the ids of the Images that sit at the bottom of the trees.
     * @param contained controls the contained or not contained semantics of the query.
     * @return A <code>Set</code> with all root nodes that were found.
     */
    public Set findCGCPaths(Set imgIDs, boolean contained);
  
    
    /**
     * Finds all the annotations that have been attached to the specified
     * Images.
     * This method looks for all the <i>valid</i> annotations that have
     * been attached to each of the specified Images.  It then maps each
     * Image id onto the set of all annotations that were found for that
     * Image.  If no annotations were found for that Image, then the entry
     * will be <code>null</code>.  Otherwise it will be a <code>Set</code>
     * containing {@link Annotation} objects.
     * 
     * @param imgIDs Contains the ids of the Images.
     * @return A map whose key is Image id and value the <code>Set</code>
     *         of all annotations for that Image.
     */
    public Map findImageAnnotations(Set imgIDs);
    
    /**
     * Finds all the annotations that have been attached to the specified
     * Images by a given Experimenter.
     * This method looks for all the <i>valid</i> annotations that have
     * been attached to each of the specified Images by the Experimenter
     * whose id is <code>experimenterID</code>.  It then maps each
     * Image id onto the set of all annotations that were found for that
     * Image.  If no annotations were found for that Image, then the entry
     * will be <code>null</code>.  Otherwise it will be a <code>Set</code>
     * containing {@link Annotation} objects.
     * 
     * @param imgIDs Contains the ids of the Images.
     * @param experimenterID The id of the Experimenter that owns the
     *                       annotations.
     * @return A map whose key is Image id and value the <code>Set</code>
     *         of all annotations for that Image.
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs, int experimenterID);
    
    /**
     * Finds all the annotations that have been attached to the specified
     * Datasets.
     * This method looks for all the <i>valid</i> annotations that have
     * been attached to each of the specified Datasets.  It then maps each
     * Dataset id onto the set of all annotations that were found for that
     * Dataset.  If no annotations were found for that Dataset, then the
     * entry will be <code>null</code>.  Otherwise it will be a <code>Set
     * </code> containing {@link Annotation} objects.
     * 
     * @param datasetIDs Contains the ids of the Datasets.
     * @return A map whose key is Dataset id and value the <code>Set</code>
     *         of all annotations for that Dataset.
     */
    public Map findDatasetAnnotations(Set datasetIDs);
    
    /**
     * Finds all the annotations that have been attached to the specified
     * Datasets by a given Experimenter.
     * This method looks for all the <i>valid</i> annotations that have
     * been attached to each of the specified Datasets by the Experimenter
     * whose id is <code>experimenterID</code>.  It then maps each
     * Dataset id onto the set of all annotations that were found for that
     * Dataset.  If no annotations were found for that Image, then the entry
     * will be <code>null</code>.  Otherwise it will be a <code>Set</code>
     * containing {@link Annotation} objects.
     * 
     * @param datasetIDs Contains the ids of the Datasets.
     * @param experimenterID The id of the Experimenter that owns the
     *                       annotations.
     * @return A map whose key is Dataset id and value the <code>Set</code>
     *         of all annotations for that Dataset.
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs, int experimenterID);
    
}
