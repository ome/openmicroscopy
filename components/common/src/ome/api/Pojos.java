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
 * Provides methods for dealing with the core "Pojos" of OME. Included are:
 * Projects, Datasets, Images, CategoryGroups, Categories, Classifications,
 * ImageAnnotations, and DatasetAnnotations.
 * 
 * <p>
 * The names of the methods correlate to how the function operates:
 * <ul>
 * <li><b>load</b>: start at container objects and work down toward the leaves, 
 * 					returning hierarchy (Proejct-&gt;Dataset-&gt;Image</li>
 * <li><b>find</b>: start at leave objects and work up to containers, returning hierarchy</li>
 * <li><b>get</b>: retrieves only leaves in the hierarchy (currently only Images)</li>
 * </ul>
 * </p>
 * <h3>Options Mechanism</h3>
 * <p>
 * The options are used to add some constraints to the generic method e.g. load hierarchy 
 * trees images <i>for a given user</i>. This mechanism should give us enough flexibility 
 * to extend the API if necessary, e.g. in some cases we might want to retrieve the images 
 * with or without annotations
 * </p>
 * <p>
 * Most methods take such an <code>options</code> map which is built on the
 * client-side using {@link omero.client.OptionBuilder an option builder.} The
 * currently supported options are:
 * <ul>
 * 	<li><b>annotator</b>(Integer): 
 * 	If key exists but value null, annotations are retrieved for all objects in the hierarchy where they exist;
 * 	if a valid experimenterID, annotations are only retrieved for that user. May 
 * 	not be used be all methods.
 * <b>Default: all annotations</b></li>
 * 	<li><b>leaves</b>(Boolean): if FALSE omits images from the returned hierarchy.
 * 	May not be used by all methods. <b>Default: true</b></li>
 * 	<li><b>experimenter</b>(Integer): inables filtering on a per-experimenter basis.
 * 	This option has a method-specific (and possibly context-specific) meaning. Please 
 * 	see the individual methods.</li>
 * </p>
 *  
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME1.0
 * @DEV.TODO possibly move optionBuilder to the common code. ome.common.utils
 * @DEV.TODO possibly move map description to marker interface with a see also link
 * @DEV.TODO add throws statements where necessary (IllegalArgument, ...)
 *
 */
public interface Pojos {

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * <p>
	 * This method also retrieves the Experimenters linked to the objects in the
	 * tree. Similarly, all Images will be linked to their Pixel objects if included.
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
	 *   		The type of the root node. Can be {@link Project}, 
	 *   		{@link Dataset}, {@link CategoryGroup}, or {@link Category}. 
	 *   		Cannot be null.
	 * @param rootNodeIds
	 *      	The ids of the root nodes. Can be null if an Experimenter
	 *      	is specified in <code>options</code>, otherwise an Exception
	 *      	is thrown to prevent all images in the entire database 
	 *      	from being downloaded.
	 * @param options
	 * 			Map as above. <code>annotator</code> and <code>leaves</code> used. 
	 * 			If <code>rootNodeIds==null</code>, <code>experimenter</code> must
	 * 			be set and filtering will be applied at the <i>Class</i>-level; i.e 
	 * 			e.g. to retrieve a user's Projects, or user's Datasets. 
	 * 			If <code>rootNodeIds!=null</code>, the result will be filtered
	 * 			by the <code>experimenter</code> at the <code>Image</code> and
	 * 			intermediate levels <i>if available</i>.
	 * @DEV.TODO should it be applied at all levels?
	 * @return  a set of hierarchy trees.
	 * 			The requested node as root and all of its descendants. The type
	 *         	of the returned value will be <code>rootNodeType</code>. 
	 */
	public Set loadContainerHierary(Class rootNodeType, Set rootNodeIds,
			Map options);

	/**
	 * Retrieves hierarchy trees in various hierarchies that
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
	 * 			Can be {@link Project} or {@link CategoryGroup}. Not null.
	 * @param imagesIds
	 *     		Contains the ids of the Images that sit at the bottom of the
	 *      	trees. Not null.
	 * @param options
	 * 			Map as above. <code>annotator</code> used.
	 * 			<code>experimenter</code> may be applied at the top-level only
	 * 			or at each level in the hierarchy, but will not apply to the 
	 * 			leaf (Image) level.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @DEV.TODO decide on use of experimenter option
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
	 * @param rootNodeType
	 *            The type of the rootNodes 
	 *            Can be {@link Dataset} or {@link Image}. Not null. 
	 * @param rootNodeIds
	 *            Ids of the objects of type <code>rootNodeType</code>. Not null.
	 * @param options
	 *            Map as above. <code>annotator</code> used to filter. 
	 *            No notion of <code>experimenter</code> or <code>leaves</code>
	 * @return A map whose key is rootNodeId and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 */
	public Map findAnnotations(Class rootNodeType, Set rootNodeIds, Map options);

	/**
	 * Retrieves paths in the Category Group/Category/Image (CG/C/I) hierarchy.
	 * <p>
	 * Because of the mutually exclusive rule of CG/C hierarchy, this method is quite tricky
	 * We want to retrieve all Category Group/Category paths that end with the specified leaves.
	 * </p><p> 
	 * We also want to retrieve the all Category Group/Category paths that don’t end with the 
	 * specified leaves, note that in that case because of the mutually exclusive constraint the 
	 * categories which don’t contain a specified leaf but which is itself contained in a group 
	 * which already has a category ending with the specified leaf is excluded.
	 * </p><p>  
	 * This is <u>more</u> restrictive than may be imagined. The goal is to 
	 * find CGC paths to which an Image <B>MAY</b> be attached.
	 * </p>
	 * @param imgIDs
	 *            the ids of the Images that sit at the bottom of the
	 *            CGC trees. Not null.
	 * @param algorithm, specify the search algorithm for finding paths.
	 * @param options
	 *            Map as above. No notion of <code>annotator</code> or <code>leaves</code>.
	 *            <code>experimenter</code> is as 
	 *            {@link #findContainerHierarchies(Class, Set, Map)}
	 * @return A <code>Set</code> of hierarchy trees with all root nodes that were found.
	 */
	public Set findCGCPaths(Set imgIds, int algorithm, Map options);

	/**
	 * Retrieve a user's (or all users') images within any given container.  For example,
	 * all images in project1.
	 * @param rootNodeType
	 *   		A Class which will have its hierarchy searched for Images. Not null.
	 * @param rootNodeIds
	 * 			A set of ids of type <code>rootNodeType</code>
	 * @param options
	 *            Map as above. No notion of <<code>leaves</code>.
	 *            <code>experimenter</code> applies at the Image level. 
	 * @return A set of images.
	 */
	public Set getImages(Class rootNodeType, Set rootNotIds, Map options);

	/**
	 * Retrieve a user's images. 
	 *  
	 * @param options
	 *            Map as above. No notion of <<code>leaves</code>.
	 *            <code>experimenter</code> applies at the Image level and
	 *            <b>must be present</b>. 
	 * @return A set of images.
	 */
	public Set getUserImages(Map options);

}
