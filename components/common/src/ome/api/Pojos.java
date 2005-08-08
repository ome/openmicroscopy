/*
 * ome.api.Pojos
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

// Java imports
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies

/**
 * Provides methods to for dealing with the core "Pojos" of OME. Included are:
 * Projects, Datasets...
 * 
 * TODO: following description should possibly be moved to a 
 * marker interface with a see also link
 * The names of the methods correlate to how the function:
 * <ul>
 * <li><b>load</b>: start at container objects and work down toward the leaves</li>
 * <li><b>find</b>: start at leave objects and work up to containers</li>
 * <li><b>get</b>: only concerned with one level in the hierarchy</li>
 * </ul>
 * Most methods take a <code>options</code> map which is built on the
 * client-side using {@link omero.client.OptionBuilder an option builder.} The
 * currently supported options are:
 * <ul>
 * <li><b>annotator</b>(Integer): </li>
 * <li><b>leaves</b>(Boolean): </li>
 * <li><b>experimenter</b>(Integer): </li>
 * </ul>
 * <u>Note</u>: Each option has a method-specific (and possibly
 * context-specific) meaning.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME1.0
 * @TODO.DEV possibly move optionBuilder to the common code. ome.common.utils
 */
public interface Pojos {

	/**
	 * Loads a container hierarchy rooted by a given node.
	 * <p>
	 * This method also retrieves the Experimenters linked to the objects in the
	 * tree. Similarly, all Images will be linked to their Pixel objects.
	 * </p>
	 * <p>
	 * Note that objects are never duplicated. For example, if an Experimenter
	 * owns all the objects in the retrieved tree, then those objects will be
	 * linked to the <i>same</i> instance of {@link Experimenter}. Or if an
	 * Image is contained in more than one Dataset in the retrieved tree, then
	 * all enclosing {@link Dataset} objects will point to the <i>same</i>
	 * {@link Image} object. And so on.
	 * </p>
	 * 
	 * @param rootNodeType
	 *            The type of the root node. 
	 *            Can be {@link Project}, {@link Dataset}, {@link CategoryGroup}, or {@link Category}.
	 * @param rootNodeIds
	 *            The ids of the root nodes.
	 * @return The requested node as root and all of its descendants. The type
	 *         of the returned value will be <code>rootNodeType</code>. 
	 */
	public Set loadContainerHierary(Class rootNodeType, Set rootNodeIds,
			Map options);

	/**
	 * Finds the data trees in various hierarchies that
	 * contain the specified Images.
	 * <p>
	 * This method will look for all the containers containing the specified
	 * Images and then for all containers containing those containers and on
	 * up the container hierarchy. 
	 * </p>
	 * <p>
	 * This method returns a <code>Set</code> with all root nodes that were
	 * found. Every root node is linked to the found objects and so on until the
	 * leaf nodes, which are {@link Image} objects. Note that the type of any
	 * root node in the returned set can be the given rootNodeType, any of its
	 * containees or an {@link Image image}.
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
	 * @param rootNodeType top-most type which will be searched for 
	 * 			Can be {@link Project} or {@link CategoryGroup}
	 * @param imagesIds
	 *            Contains the ids of the Images that sit at the bottom of the
	 *            trees. Not null.
	 * @return A <code>Set</code> with all root nodes that were found.
	 */
	public Set findContainerHierarchies(Class rootNodeType, Set imagesIds,
			Map options);

	/**
	 * Finds all the annotations that have been attached to the specified
	 * <code>rootNodes</code>. This method looks for all the <i>valid</i>
	 * annotations that have been attached to each of the specified objects. It
	 * then maps each <code>rootNodeId</code> onto the set of all annotations
	 * that were found for that node. If no annotations were found for that
	 * node, then the entry will be <code>null</code>. Otherwise it will be a
	 * <code>Set</code> containing {@link Annotation} objects.
	 * 
	 * <h3>Options:</h3>
	 * TODO
	 * 
	 * @param rootNodeType
	 *            The type of the rootNodes 
	 *            Can be {@link Dataset} or {@link Image} 
	 * @param rootNodeIds
	 *            Ids of the objects of type <code>rootNodeType</code>
	 * @param options
	 *            Map of options, as described above
	 * @return A map whose key is rootNodeId and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 */
	public Set findAnnotations(Class rootNodeType, Set rootNodeIds, Map options);

	/**
	 * Finds data paths in the Category Group/Category/Image (CG/C/I) hierarchy.
	 * This method is similar to
	 * {@link #findCGCIHierarchies(Set) findCGCIHierarchies} but returns only
	 * CategoryGroups and Categories. If <code>contained</code> is true the
	 * CGC paths are returned which lead to this image. If
	 * <code>contained</code> is <code>false</code>, all paths are excluded
	 * for which an image is contained in a <code><b>CategoryGroup</b></code>.
	 * This is <u>more</u> restrictive than may be imagined.
	 * 
	 * @param imgIDs
	 *            Contains the ids of the Images that sit at the bottom of the
	 *            trees. Not null.
	 * @param options
	 *            Map of options, as described above
	 * @return A <code>Set</code> with all root nodes that were found.
	 */
	public Set findCGCPaths(Set imgIds);

	
	public Set getImages(Class rootNodeType, Set rootNotIds, Map options);

	public Set getUserImages(Map options);

}
