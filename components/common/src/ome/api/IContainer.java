/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.core.Image;
import ome.parameters.Parameters;

/**
 * Provides methods for dealing with the core "Pojos" of OME. Included are:
 * Projects, Datasets, Images.
 * 
 * <h3>Read API</h3>
 * <p>
 * The names of the methods correlate to how the function operates:
 * <ul>
 * <li><b>load</b>: start at container objects and work down toward the
 * leaves, returning hierarchy (Proejct-&gt;Dataset-&gt;Image</li>
 * <li><b>find</b>: start at leave objects and work up to containers,
 * returning hierarchy</li>
 * <li><b>get</b>: retrieves only leaves in the hierarchy (currently only
 * Images)</li>
 * </ul>
 * </p>
 * <h4>Options Mechanism</h4>
 * <p>
 * The options are used to add some constraints to the generic method e.g. load
 * hierarchy trees images <i>for a given user</i>. This mechanism should give
 * us enough flexibility to extend the API if necessary, e.g. in some cases we
 * might want to retrieve the images with or without annotations
 * </p>
 * <p>
 * Most methods take such an <code>options</code> map which is built on the
 * client-side using {@link omero.client.OptionBuilder an option builder.} The
 * currently supported options are:
 * <ul>
 * <li><b>annotator</b>(Integer): If key exists but value null, annotations
 * are retrieved for all objects in the hierarchy where they exist; if a valid
 * experimenterID, annotations are only retrieved for that user. May not be used
 * be all methods. <b>Default: all annotations</b></li>
 * <li><b>leaves</b>(Boolean): if FALSE omits images from the returned
 * hierarchy. May not be used by all methods. <b>Default: true</b></li>
 * <li><b>experimenter</b>(Integer): inables filtering on a per-experimenter
 * basis. This option has a method-specific (and possibly context-specific)
 * meaning. Please see the individual methods.</li>
 * <li><b>group</b>(Integer): enables filtering on a per-group basis. The
 * <b>experimenter</b> value is ignored if present and instead a similar
 * filtering is done using all <b>experimenter</b>s in the given group.
 * 
 * </p>
 * <h3>Write API</h3>
 * <p>
 * As outlined in TODO, the semantics of the Omero write API are based on three
 * rules:
 * <ol>
 * <li>IObject-valued fields for which <code>isLoaded()</code> returns false
 * are assumed filtered</li>
 * <li>Collection-valued fields that are null are assumed filtered</li>
 * <li>Collection-valued fields for which
 * <code>getDetails().isFiltered(String collectionName)</code> returns true
 * are assumed filtered. TODO: should we accept isFiltered for all fields?
 * </ol>
 * In each of these cases, the server will reload that given field <b>before</b>
 * attempting to save the graph.
 * </p>
 * <p>
 * For all write calls, the options map (see below) must contain the userId and
 * the userGroupId for the newly created objects. TODO umask.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME1.0
 * @DEV.TODO possibly move optionBuilder to the common code. ome.common.utils
 * @DEV.TODO possibly move map description to marker interface with a see also
 *           link
 * @DEV.TODO add throws statements where necessary (IllegalArgument, ...)
 * 
 */
public interface IContainer extends ServiceInterface {

    // ~ READ API
    // =========================================================================
	
    /**
     * Retrieves hierarchy trees rooted by a given node (unless orphan is
     * specified -- See below)
     * <p>
     * This method also retrieves the Experimenters linked to the objects in the
     * tree. Similarly, all Images will be linked to their Pixel objects if
     * included.
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
     *            The type of the root node. Can be {@link Project},
     *            {@link Dataset}, {@link Screen} or {@link Plate}.
     *            Cannot be null.
     * @param rootNodeIds
     *            The ids of the root nodes. Can be null if an Experimenter is
     *            specified in <code>options</code>, otherwise an Exception
     *            is thrown to prevent all images in the entire database from
     *            being downloaded.
     * @param options
     *            Parameters as above. <code>annotator</code>, <code>leaves</code>,
     *            <code>orphan</code>, <code>acquisition data</code> used. 
     *            <code>acquisition data</code> is only relevant for images
     *            and taken into account if the images are loaded.
     *            If <code>rootNodeIds==null</code>,
     *            <code>experimenter|group</code> must be set and filtering
     *            will be applied at the <i>Class</i>-level; e.g. to retrieve a
     *            user's Projects, or user's Datasets. If
     *            <code>rootNodeIds!=null</code>, the result will be filtered
     *            by the <code>experimenter|group</code> at the
     *            <code>Image</code> and intermediate levels <i>if available</i>.
     *            Due to the amount of data potentially linked a Screen/Plate,
     *            the <code>leaves</code> option is not taken into account.
     *            when the root node is a {@link Screen}.
     *            <code>orphan</code> implies that objects which are not contained
     *            in an object of rootNodeType should also be returned.
     * @DEV.TODO should it be applied at all levels?
     * @return a set of hierarchy trees. The requested node as root and all of
     *           its descendants. The type of the returned value will be
     *           <code>rootNodeType</code>, unless <code>orphan</code> is specified
     *           in which case objects of type <code>rootNodeType</code> and below
     *           may be returned.
     */
    public <T extends IObject> Set<IObject> loadContainerHierarchy(@NotNull
    Class<T> rootNodeType, @Validate(Long.class)
    Set<Long> rootNodeIds, Parameters options);

    /**
     * Retrieves hierarchy trees in various hierarchies that contain the
     * specified Images.
     * <p>
     * This method will look for all the containers containing the specified
     * Images and then for all containers containing those containers and on up
     * the container hierarchy.
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
     *                   __p1__
     *                  /      \    
     *                _d1_    _d2_      d3
     *               /    \  /    \     |
     *              i1     i2     i3    i4    i5  i6
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
     * @param rootNodeType
     *            top-most type which will be searched for Can be
     *            {@link Project}. Not null.
     * @param imagesIds
     *            Contains the ids of the Images that sit at the bottom of the
     *            trees. Not null.
     * @param options
     *            Parameters as above. <code>annotator</code> used.
     *            <code>experimenter|group</code> may be applied at the
     *            top-level only or at each level in the hierarchy, but will not
     *            apply to the leaf (Image) level.
     * @return A <code>Set</code> with all root nodes that were found.
     * @DEV.TODO decide on use of experimenter option
     */
    public <T extends IObject> Set<IObject> findContainerHierarchies(@NotNull
    Class<T> rootNodeType, @NotNull
    @Validate(Long.class)
    Set<Long> imagesIds, Parameters options);

    /**
     * Retrieve a user's (or all users') images within any given container. For
     * example, all images in project, applying temporal filtering or
     * pagination.
     * 
     * @param rootNodeType
     *            A Class which will have its hierarchy searched for Images. Not
     *            null. TODO types?
     * @param rootNodeIds
     *            A set of ids of type <code>rootNodeType</code> Not null.
     * @param options
     *            Parameters as above. No notion of <code>leaves</code>.
     *            <code>experimenter|group</code> apply at the Image level.
     *            OPTIONS: - startTime and/or endTime should be
     *            Timestamp.valueOf("YYYY-MM-DD hh:mm:ss.ms");
     *            <p>
     *            <code>limit</code> and <code>offset</code> are applied at 
     *            the Image-level. That is, calling with Dataset.class, 
     *            limit == 10 and offset == 0 will
     *            first perform one query to get an effective set of
     *            rootNodeIds, then getImages will be called with an effective
     *            rootNodeType of Image.class and the new ids.
     *            </p>.
     *            <code>acquisition data</code> is only relevant for images.
     * @return A set of images.
     * @see ome.util.builders.PojoOptions#paginate(int, int)
     * @see ome.util.builders.PojoOptions#startTime(java.sql.Timestamp)
     * @see ome.util.builders.PojoOptions#endTime(java.sql.Timestamp)
     */
    public <T extends IObject> Set<Image> getImages(@NotNull
    Class<T> rootNodeType, @NotNull
    @Validate(Long.class)
    Set<Long> rootNodeIds, Parameters options);

    /**
     * Retrieve images by options.
     * 
     * @param options
     *            Parameters as above. No notion of <code>leaves</code>.
     *            <code>experimenter|group</code> apply at the Image level and
     *            <b>must be present</b>. OPTIONS: - startTime and/or endTime
     *            should be Timestamp.valueOf("YYYY-MM-DD hh:mm:ss.ms").
     *             <code>acquisition data</code> is only relevant for images.
     * @return A set of images.
     */
    public Set<Image> getImagesByOptions(Parameters options);

    /**
     * Retrieve a user's images.
     * 
     * @param options
     *            Parameters as above. No notion of <code>leaves</code>.
     *            <code>experimenter|group</code> apply at the Image level and
     *            <b>must be present</b>.
     * @return A set of images.
     */
    public Set<Image> getUserImages(Parameters options);

    /**
     * Counts the number of members in a collection for a given object. For
     * example, if you wanted to retrieve the number of Images contained in a
     * Dataset you would pass TODO.
     * 
     * @param type
     *            The fully-qualified classname of the object to be tested
     * @param property
     *            Name of the property on that class, omitting getters and
     *            setters.
     * @param ids
     *            Set of Longs, the ids of the objects to test
     * @param options
     *            Parameters. Unused.
     * @return A map from id integer to count integer
     */
    public Map getCollectionCount(@NotNull String type, 
    		@NotNull String property, 
    		@NotNull @Validate(Long.class) Set<Long> ids, Parameters options);

    /**
     * Given a list of IDs of certain entity types, calculates which filesets are split such that
     * a non-empty proper subset of their images are referenced, directly or indirectly, as being
     * included. The return value lists both the fileset IDs and the image IDs in ascending order,
     * the image ID lists separated by if they were included.
     * Warning: following discussion in trac ticket 11019 the return type may be changed.
     * @param included the entities included
     * @param options parameters, presently ignored
     * @return the partially included filesets
     */
    public Map<Long, Map<Boolean, List<Long>>> getImagesBySplitFilesets(Map<Class<? extends IObject>, List<Long>> included,
            Parameters options);

    /**
     * Retrieves a collection with all members initialized ("loaded"). This is
     * useful when a collection has been nulled in a previous query.
     * 
     * @param dataObject
     *            Can be "unloaded".
     * @param collectionName
     *            <code>public final static String</code> from the
     *            IObject.class
     * @param options
     *            Parameters. Unused.
     * @return An initialized collection.
     */
    public Collection retrieveCollection(@NotNull IObject dataObject, 
    		@NotNull String collectionName, Parameters options);

    // ~ WRITE API
    // =========================================================================

    /**
     * Creates the specified data object.
     * <p>
     * A placeholder parent object is created if the data object is to be
     * put in a collection.
     * </p>
     * <p>
     * For example, if the object is a <code>Dataset</code>, we first create
     * a <code>Project</code> as parent then we set the Dataset parent as
     * follows: <code>
     *      //pseudo-code TODO
     *      Project p = new Project(id,false);
     *      dataset.addProject(p);
     * </code>
     * then for each parent relationship a DataObject {@link ILink#link} is
     * created.
     * 
     * @param object
     *            IObject. Supported: Project, Dataset,
     *            Annotation, Group, Experimenter. Not null.
     * @options Parameters as above.
     * @return the created object
     */
    public <T extends IObject> T createDataObject(@NotNull
    T object, Parameters options);

    /**
     * convenience method to save network calls. Loops over the array of
     * IObjects calling createDataObject.
     * 
     * @param dataObjects
     *            Array of Omero <code>IObjects</code>
     * @param options
     *            Parameters as above.
     * 
     * @see createDataObject
     */
    public IObject[] createDataObjects(@NotNull IObject[] dataObjects, 
    		Parameters options);

    /**
     * Removes links between OmeroDataObjects e.g Project-Dataset, Dataset-Image
     * Note that the objects themselves aren't deleted, only the Link objects.
     * 
     * @param dataObjectLinks
     *            Not null.
     * @param options
     *            Parameters as above.
     */
    public void unlink(@NotNull ILink[] dataOjectLinks, Parameters options);

    /**
     * Convenience method for creating links. Functionality also availeble
     * from {@link #createDataObject(IObject, Parameters)}
     * 
     * @param dataObjectLinks
     *            Array of links to be created.
     * @param options
     *            Parameters as above.
     * @return the created links
     */
    public ILink[] link(@NotNull ILink[] dataObjectLinks, Parameters options);

    /**
     * Updates a data object.
     * <p>
     * To link or unlink objects to the specified object, we should call the
     * methods link or unlink. TODO Or do we use for example
     * dataset.setProjects(set of projects) to add. Tink has to be set as
     * follows dataset->project and project->dataset.
     * 
     * Alternatively, you can make sure that the collection is <b>exactly</b>
     * how it should be in the database. If you can't guarantee this, it's best
     * to send all your collections back as <code>null</code>
     * 
     * @param dataObject
     *            Pojos-based IObject. Supported: supported: Project, Dataset,
     *            Annotation, Group, Experimenter.
     * @param options
     *            Parameters as above.
     * @return created data object
     */
    public <T extends IObject> T updateDataObject(@NotNull
    T dataObject, Parameters options);

    /**
     * convenience method to save network calls. Loops over the array of
     * IObjects calling updateDataObject.
     * 
     * @param dataObjects
     * @param options
     *            Parameters as above.
     * @return created data objects.
     * @see #updateDataObject(IObject, Parameters)
     */
    public IObject[] updateDataObjects(@NotNull IObject[] dataObjects, 
    		Parameters options);

}
