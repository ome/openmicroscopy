/*
 * ome.api.IPojos
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.core.Image;

/**
 * Provides methods for dealing with the core "Pojos" of OME. Included are:
 * Projects, Datasets, Images, CategoryGroups, Categories, Classifications,
 * ImageAnnotations, and DatasetAnnotations.
 * 
 * <h3>Read API</h3>
 * <p>
 * The names of the methods correlate to how the function operates:
 * <ul>
 * <li><b>load</b>: start at container objects and work down toward the leaves, 
 * 					returning hierarchy (Proejct-&gt;Dataset-&gt;Image</li>
 * <li><b>find</b>: start at leave objects and work up to containers, returning hierarchy</li>
 * <li><b>get</b>: retrieves only leaves in the hierarchy (currently only Images)</li>
 * </ul>
 * </p>
 * <h4>Options Mechanism</h4>
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
 *  <li><b>group</b>(Integer): enables filtering on a per-group basis. The <b>experimenter</b>
 *  value is ignored if present and instead a similar filtering is done using all <b>experimenter</b>s
 *  in the given group.
 * 
 * </p>
 * <h3>Write API</h3>
 * <p>As outlined in TODO, the semantics of the Omero write API are based on three
 * rules:
 * <ol>
 * <li>IObject-valued fields for which <code>isLoaded()</code> returns false 
 * are assumed filterd</li>
 * <li>Collection-valued fields that are null are assumed filtered</li>
 * <li>Collection-valued fields for which 
 * <code>getDetails().isFiltered(String collectionName)</code> returns true 
 * are assumed filtered. TODO: should we accept isFiltered for all fields?
 * </ol>
 * In each of these cases, the server will reload that given field <b>before</b>
 * attempting to save the graph.  
 * </p>
 * <p>
 * For all write calls, the options map (see below) must contain the userId
 * and the userGroupId for the newly created objects. TODO umask.
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
public interface IPojos extends ServiceInterface {

    // ~ READ API
    // =========================================================================
    
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
	 * 			If <code>rootNodeIds==null</code>, <code>experimenter|group</code>
     *          must be set and filtering will be applied at the <i>Class</i>-level;
	 * 			e.g. to retrieve a user's Projects, or user's Datasets. 
	 * 			If <code>rootNodeIds!=null</code>, the result will be filtered
	 * 			by the <code>experimenter|group</code> at the <code>Image</code> and
	 * 			intermediate levels <i>if available</i>.
	 * @DEV.TODO should it be applied at all levels?
	 * @return  a set of hierarchy trees.
	 * 			The requested node as root and all of its descendants. The type
	 *         	of the returned value will be <code>rootNodeType</code>. 
	 */
	public <T extends IObject> Set<T> loadContainerHierarchy(
            @NotNull Class<T> rootNodeType, 
            @Validate(Long.class) Set<Long> rootNodeIds,
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
	 * 			<code>experimenter|group</code> may be applied at the top-level only
	 * 			or at each level in the hierarchy, but will not apply to the 
	 * 			leaf (Image) level.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @DEV.TODO decide on use of experimenter option
	 */
	public <T extends IObject> Set<IObject> findContainerHierarchies(
            @NotNull Class<T> rootNodeType, 
            @NotNull @Validate(Long.class) Set<Long> imagesIds,
			Map options);

	/**
	 * Finds all the annotations that have been attached to the specified
	 * <code>rootNodes</code> for the specified <code>annotatorIds</code>. 
     * This method looks for all annotations that have been attached to each 
     * of the specified objects. It then maps each <code>rootNodeId</code> 
     * onto the set of all annotations that were found for that node. If no 
     * annotations were found for that node, then the entry will be 
     * <code>null</code>. Otherwise it will be a <code>Set</code> 
     * containing {@link Annotation} objects.
	 * 
	 * @param rootNodeType
	 *            The type of the rootNodes 
	 *            Can be {@link Dataset} or {@link Image}. Not null. 
	 * @param rootNodeIds
	 *            Ids of the objects of type <code>rootNodeType</code>. Not null.
	 * @param annotatorIds
     *            Ids of the users for whom annotations should be retrieved.
     *            If null, all annotations returned.
     * @param options
	 *            Map as unused. 
	 *            No notion of <code>experimenter|group</code> or <code>leaves</code>
     *            or <code>counts</code>
	 * @return A map whose key is rootNodeId and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 */
	public <T extends IObject> Map findAnnotations(
            @NotNull Class<T> rootNodeType, 
            @NotNull @Validate(Long.class) Set<Long> rootNodeIds, 
            @Validate(Long.class) Set<Long> annotatorIds, 
            Map options);

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
	 *            <code>experimenter & group</code> are as 
	 *            {@link #findContainerHierarchies(Class, Set, Map)}
	 * @return A <code>Set</code> of hierarchy trees with all root nodes that were found.
	 */
	public <T extends IObject> Set<T> findCGCPaths(
            @NotNull @Validate(Long.class) Set<Long> imgIds, 
            @NotNull String algorithm, 
            Map options);
    
    /**
     * algorithm which given a set of images ids retrieve the CG-C hierarchy where 
     * we can classify the images 
     * constraint: if a category contains an image => the hierarchy CG-C
     * is not retrieved.
    */
    public final static String CLASSIFICATION_ME = "CLASSIFICATION_ME";
    
    /** 
     * algorithm which given a set of images ids retrieves the CG-C where 
     * category doesn't contain the image.
     **/
    public final static String CLASSIFICATION_NME = "CLASSIFICATION_NME";

    /** 
     * algorithm which given a set of image ids retrieves the CG-C containing the images.
    */
    public final static String DECLASSIFICATION = "DECLASSIFICATION";
	
    public final static Set<String> ALGORITHMS = new HashSet<String>(
            Arrays.asList(new String[]{
                    CLASSIFICATION_ME,
                    CLASSIFICATION_NME,
                    DECLASSIFICATION}));
    
    /**
	 * Retrieve a user's (or all users') images within any given container.  For example,
	 * all images in project.
	 * @param rootNodeType
	 *   		A Class which will have its hierarchy searched for Images. Not null. TODO types?
	 * @param rootNodeIds
	 * 			A set of ids of type <code>rootNodeType</code> Not null.
	 * @param options
	 *            Map as above. No notion of <code>leaves</code>.
	 *            <code>experimenter|group</code> apply at the Image level. 
	 * @return A set of images.
	 */
	public <T extends IObject> Set<Image> getImages(
            @NotNull Class<T> rootNodeType, 
            @NotNull @Validate(Long.class) Set<Long> rootNodeIds, 
            Map options);

	/**
	 * Retrieve a user's images. 
	 *  
	 * @param options
	 *            Map as above. No notion of <code>leaves</code>.
	 *            <code>experimenter|group</code> apply at the Image level and
	 *            <b>must be present</b>. 
	 * @return A set of images.
	 */
	public Set<Image> getUserImages(Map options);
	
	 /**
     * Retrieves <code>Experimenter</code> instances based on unique user name
     * @param names
     *          Set of user names for <code>Experimenter</code> 
     * @param options
     *          Map. Unused.
     * @return A map from username to <code>Experimenter</code>
     */
    public Map getUserDetails(
            @NotNull @Validate(String.class) Set<String> names, 
            Map options);

    /**
     * Counts the number of members in a collection for a given object. 
     * For example, if you wanted to retrieve the number of Images contained in 
     * a Dataset you would pass TODO.
     * @param class
     *          The fully-qualified classname of the object to be tested
     * @param property
     *          Name of the property on that class, omitting getters and setters.
     * @param ids
     *          Set of Longs, the ids of the objects to test
     * @param options
     *          Map. Unused.
     * @return A map from id integer to count integer
     */
     public Map getCollectionCount(
             @NotNull String type, 
             @NotNull String property, 
             @NotNull @Validate(Long.class) Set<Long> ids, 
             Map options);
	
     /** 
      * retrieves a collection with all members initialized ("loaded"). This
      * is useful when a collection has been nulled in a previous query.
      * @param dataObject
      *     Can be "unloaded".
      * @param collectionName
      *     <code>public final static String</code> from the IObject.class 
      * @param options
      *     Map. Unused.
      * @return
      *     An initialized collection.
      */
     public Collection retrieveCollection(
             @NotNull IObject dataObject, 
             @NotNull String collectionName, 
             Map options);
	

    // ~ WRITE API
    // =========================================================================

    /**
     * Creates the specified data object.
     * <p>
     * A “placeholder” parent object is created if the data object is to be
     * put in a collection.
     * </p>
     * <p>
     *  For example, if the  object is a <code>Dataset</code>, we first create 
     *  a <code>Project</code> as parent then we set the Dataset parent as 
     *  follows:
     *  <code>
     *      //pseudo-code TODO
     *      Project p = new Project(id,false);
     *      dataset.addProject(p);
     * </code>
     * then for each parent relationship a DataObject {@see ILink link} is 
     * created.
     * 
     * @param Pojo-based IObject. Supported: Project, Dataset, CategoryGroup, 
     *      Category, Annotation, Group, Experimenter. 
     *      Not null.
     * @options 
     *      Map as above.
     * @return the created object
     */
    public <T extends IObject> T createDataObject(
            @NotNull T object, 
            Map options);
    
    /**
     * convenience method to save network calls. Loops over the array of 
     * IObjects calling createDataObject.
     * @param dataObjects
     *      Array of Omero <code>IObjects</code>
     * @param options
     *      Map as above.
     * 
     * @see createDataObject
     */
    public IObject[] createDataObjects(
            @NotNull IObject[] dataObjects, 
            Map options);
    
    /**
     * Removes links between OmeroDataObjects e.g Project-Dataset, Dataset-Image
     * Note that the objects themselves aren't deleted, only the Link objects. 
     * TODO We will need to add a delete method.
     * 
     * @param dataObjectLinks
     *      Not null.
     * @param options
     *      Map as above.
     */
    public void unlink(
            @NotNull ILink[] dataOjectLinks, 
            Map options);
    
    /** typed convenience method for creating links. Functionality also availeble
     * from {@see createDataObject}
     * @param dataObjectLinks
     *      Array of links to be created.
     * @param options
     *      Map as above.
     * @return the created links
     */ 
    public ILink[] link(
            @NotNull ILink[] dataObjectLinks, 
            Map options);
    
    /**
     * Updates a data object.
     * <p>
     * To link or unlink objects to the specified object, we should call the 
     * methods link or unlink. TODO Or do we use for example 
     * dataset.setProjects(set of projects) to add. Tink has to be set  as follows
     * dataset->project and project->dataset.
     * 
     * Alternatively, you can make sure that the collection is <b>exactly</b>
     * how it should be in the database. If you can't guarantee this, it's best
     * to send all your collections back as <code>null</code>
     * @param dataObject
     *      Pojos-based IObject. Supported: supported: Project, Dataset, 
     *      CategoryGroup, Category, Annotation, Group, Experimenter.
     * @param options
     *      Map as above.
     * @return created data object
     */
    public <T extends IObject> T updateDataObject(
            @NotNull T dataObject, 
            Map options);
    
    /**
     * convenience method to save network calls. Loops over the array of IObjects
     * calling updateDataObject.
     * @param dataObjects
     * @param options
     *      Map as above.
     * @return created data objects.
     * @see updateDataObject
     */
    public IObject[] updateDataObjects(
            @NotNull IObject[] dataObjects, 
            Map options);
    
    /**
     * Deletes a data object. Currently this method takes a very conservative
     * approach and only tries to delete a single object (no cascading). The user
     * will have to delete objects in the appropriate order to prevent database
     * not null exceptions.
     * 
     * @param dataObject
     * @param options
     */
    public void deleteDataObject(
            @NotNull IObject dataObject, 
            Map options);
    
    /**
     * convenience method to save network calls. Loops over the array of IObjects
     * calling deleteDataObjects
     * 
     * @param dataObjects
     * @param options
     *      Map as above.
     */
    public void deleteDataObjects(
            @NotNull IObject[] dataObjects, 
            Map options);
    
}
