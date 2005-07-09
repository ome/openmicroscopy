/*
 * pojos.HierarchyBrowsing
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

package org.openmicroscopy.omero.shoolaadapter;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;

/** 
 * Provides methods to support browsing of Image hierarchies.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/09 19:50:41 $)
 * </small>
 * @since OME2.2
 */
public interface PojoOmeroService
{

    /**
     * Loads a Project/Dataset/Image (P/D/I) hierarchy rooted by a given node.
     * <p>The root node can be either Project or Dataset.  A Project tree will
     * be represented by {@link ProjectData}, {@link DatasetData}, and
     * {@link ImageData} objects.  A Dataset tree will only have objects of
     * the latter two types.</p>
     * <p>This method also retrieves the Experimenters linked to the objects
     * in the tree &#151; an Experimenter is represented by an 
     * {@link ExperimenterData} object.  Similarly, all Images will be linked
     * to their Pixels &#151; represented by {@link PixelsData} objects.</p>
     * <p>Note that objects are never duplicated.  For example, if an 
     * Experimenter owns all the objects in the retrieved tree, then those
     * objects will be linked to the <i>same</i> instance of
     * {@link ExperimenterData}.  Or if an Image is contained in more than
     * one Dataset in the retrieved tree, then all enclosing {@link DatasetData}
     * objects will point to the <i>same</i> {@link ImageData} object.  And so
     * on.</p>
     * 
     * @param rootNodeType  The type of the root node.  Can either be
     *                      {@link ProjectData} or {@link DatasetData}.
     * @param rootNodeID    The id of the root node.
     * @return The requested node as root and all of its descendants.  The type
     *         of the returned value will be <code>rootNodeType</code>. 
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID);
    
    /**
     * Same above. + 
     * Retrieves the datasetAnnotion and imageAnnotation for the specified user.
     * @param rootNodeType
     * @param rootNodeID
     * @param experimenterID
     * @return
     */
    public DataObject loadPDIAnnotatedHierarchy(Class rootNodeType, 
                                            int rootNodeID, int experimenterID);
    
    /**
     * Loads a Category Group/Category/Image (CG/C/I) hierarchy rooted by a
     * given node.
     * <p>The root node can be either Category Group or Category.  A Category
     * Group tree will be represented by {@link CategoryGroupData}, 
     * {@link CategoryData}, and {@link ImageData} objects.  A Category tree
     * will only have objects of the latter two types.</p>
     * <p>This method also retrieves the Experimenters linked to the objects
     * in the tree &#151; an Experimenter is represented by an 
     * {@link ExperimenterData} object.  Similarly, all Images will be linked
     * to their Pixels &#151; represented by {@link PixelsData} objects.</p>
     * <p>Note that objects are never duplicated.  For example, if an 
     * Experimenter owns all the objects in the retrieved tree, then those
     * objects will be linked to the <i>same</i> instance of
     * {@link ExperimenterData}.</p>
     * 
     * @param rootNodeType  The type of the root node.  Can either be
     *                      {@link CategoryGroupData} or {@link CategoryData}.
     * @param rootNodeID    The id of the root node.
     * @return The requested node as root and all of its descendants.  The type
     *         of the returned value will be <code>rootNodeType</code>. 
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID);
    
    /**
     * Same above. + 
     * Retrieves the imageAnnotation for the specified user.
     * @param rootNodeType
     * @param rootNodeID
     * @param experimenterID
     * @return
     */
    public DataObject loadCGCIAnnotatedHierarchy(Class rootNodeType, 
                                            int rootNodeID, int experimenterID);
    
    /**
     * Finds the data trees in the Project/Dataset/Image (P/D/I) hierarchy that 
     * contain the specified Images. 
     * <p>This method will look for all the Datasets containing the specified 
     * Images and then for all Projects containing those Datasets.  In the 
     * returned trees, Projects will be represented by {@link ProjectData}
     * objects, Datasets by {@link DatasetData} objects, and Images by
     * {@link ImageData} objects.</p>
     * <p>This method returns a <code>Set</code> with all root nodes that were
     * found.  Every root node is linked to the found objects and so on until
     * the leaf nodes, which are {@link ImageData} objects.  Note that the type
     * of any root node in the returned set can be {@link ProjectData}, 
     * {@link DatasetData}, or {@link ImageData}.</p>
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
    
    public Set findCGCIAnnotatedHierarchies(Set imgIDs, int experimenterID);
    
    /**
     * Finds the data paths in the Category Group/Category/Image (CG/C/I) 
     * hierarchy that don't end with the specified Images.
     * This method is the analogous of the 
     * {@link #findCGCIHierarchies(Set) findCGCIHierarchies}.
     * The semantics is exactly the same.
     * 
     * @param imgIDs Contains the ids of the Images that sit at the bottom of
     *               the trees.
     * @return A <code>Set</code> with all root nodes that were found.
     */
    public Set findCGCIExcludedHierarchies(Set imgIDs);
    
    /**
     * Finds all the annotations that have been attached to the specified
     * ids
     * This method looks for all the <i>valid</i> annotations that have
     * been attached to each of the specified ids.  It then maps each
     * id onto the set of all annotations that were found for that
     * id.  If no annotations were found, then the entry
     * will be <code>null</code>.  Otherwise it will be a <code>Set</code>
     * containing {@link AnnotationData} objects.
     * 
     * @param clazz type of the ids.
     * @param iDs Contains the ids.
     * @param experimenterId user for whom annotatiosn should be received
     * @return A map whose key is Image id and value the <code>Set</code>
     *         of all annotations for that Image.
     */
    public Map findAnnotations(Class clazz, Set iDs, int experimenterId);    
}
