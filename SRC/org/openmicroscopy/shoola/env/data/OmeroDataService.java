/*
 * org.openmicroscopy.shoola.env.data.OmeroDataService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;


//Java imports
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.util.SearchResult;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;

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
  
	/** Identifies the used space on the file system. */
	public static final int USED = 100;

	/** Identifies the free space on the file system. */
	public static final int FREE = 101;

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
	 *                      Can be <code>Project</code>,
	 *                      <code>CategoryGroup</code>, <code>Category</code>
	 *                      <code>Dataset</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param rootNodeIDs   A set of the IDs of top-most containers. 
	 *                      Passed <code>null</code> to retrieve all top-most
	 *                      nodes e.g. all user's projects.
	 * @param withLeaves   	Passed <code>true</code> to retrieve the images,
	 *                      <code>false</code> otherwise.
	 * @param userID		The Id of the selected user.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
			boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 * 
	 * @param rootNodeType  The top-most type which will be searched for 
	 *                      Can be <code>Project</code> or
	 *                      <code>CategoryGroup</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param userID		The Id of the selected user.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID)
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
	 * @param userID		The Id of the user.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs,
			long userID)
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
	 * @param forUser		Pass <code>true</code> to retrieve the annotations
	 * 						for the current user, <code>false</code>
	 * 						otherwise.
	 * @return A map whose key is rootNodeID and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs, 
			boolean forUser)
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
	 * don't end with the specified leaves, note that in that case because of 
	 * the mutually exclusive constraint the categories which don't contain a
	 * specified leaf but which is itself contained in a group which already
	 * has a category ending with the specified leaf is excluded.
	 * </p>
	 * <p>  
	 * This is <u>more</u> restrictive than may be imagined. The goal is to 
	 * find CGC paths to which an Image <B>MAY</b> be attached.
	 * </p>
	 * 
	 * @param imgIDs        Set of ids of the images that sit at the bottom of 
	 *                      the CGC trees. Mustn't be <code>null</code>.
	 * @param algorithm     The search algorithm for finding paths. One of the 
	 *                      following constants: {@link #DECLASSIFICATION},
	 *                      {@link #CLASSIFICATION_ME},
	 *                      {@link #CLASSIFICATION_NME}.
	 * @param userID   		The Id of the user.                  
	 * @return A <code>Set</code> of hierarchy trees with all root nodes 
	 * that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set findCGCPaths(Set imgIDs, int algorithm, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * 
	 * @param nodeType	The type of container. Can either be Project, 
	 * 					Dataset, CategoryGroup, Category or Image.
	 * @param nodeIDs   Set of node ids..
	 * @param userID	The Id of the root.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set getImages(Class nodeType, Set nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images imported by the specified user.
	 * 
	 * @param userID The id of the user.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set getExperimenterImages(long userID)
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
	 * retrieve data from OMERO service. 
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
	 * retrieve data from OMERO service. 
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
	 * @param data              Collection of annotation data to remove. 
	 *                          Mustn't be <code>null</code>.
	 * @return See above.                  
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public DataObject removeAnnotationFrom(DataObject annotatedObject, 
			List data)
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
	 * retrieve data from OMERO service. 
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
	 * retrieve data from OMERO service. 
	 */
	public DataObject createDataObject(DataObject newObject, DataObject parent)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Unlinks the specified <code>DataObject</code> and the parent object.
	 * 
	 * @param children  The children to remove. Mustn't be <code>null</code>.
	 * @param parent    The child's parent. The parent is <code>null</code> if 
	 *                  the child is a top node container i.e. 
	 *                  <code>Project</code> or <code>Categorygroup</code>.
	 * @return          The updated parent.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set removeDataObjects(Set children, DataObject parent)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Updates the specified <code>DataObject</code>.
	 * 
	 * @param object    The <code>DataObject</code> to update.
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public DataObject updateDataObject(DataObject object)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Adds the images to the specified categories.
	 * 
	 * @param images        The images to classify.
	 * @param categories    The categories to add the images to.
	 * @return The classified images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	public Set classify(Set<ImageData> images, Set<CategoryData> categories)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Removes the images from the specified categories.
	 * 
	 * @param images        The images to declassify
	 * @param categories    The categories to remove the images from.
	 * @return The declassified images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set declassify(Set<ImageData> images, Set<CategoryData> categories)
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
	 * @param userID    The Id of the user.
	 * @return  A set of existing nodes.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set loadExistingObjects(Class nodeType, Set nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Adds the given objects to the specified node.
	 * 
	 * @param parent    The <code>DataObject</code> to update. Either a 
	 *                  <code>ProjectData</code> or <code>DatasetData</code>.
	 * @param children  The collection of objects to add.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public void addExistingObjects(DataObject parent, Set children)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Cuts and paste the specified nodes.
	 * 
	 * @param toPaste   The nodes to paste.
	 * @param toCut     The nodes to cut.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public void cutAndPaste(Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the channel metadata for the specified pixels sets.
	 * 
	 * @param pixelsID  The id of pixels set.
	 * @return A list of metadata.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List getChannelsMetadata(long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates the specified annotation for the specified
	 * <code>DataObject</code>s. The updated <code>DataObject</code> are
	 * then returned.
	 * 
	 * @param toAnnotate	The objects to handle. Mustn't be <code>null</code>.
	 * @param data          The annotation to create. 
	 *                      Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List createAnnotationFor(Set toAnnotate, AnnotationData data)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Updates the specified annotation. The updated <code>DataObject</code>s 
	 * are then returned.
	 * 
	 * @param toUpdate	The objects to handle. Mustn't be <code>null</code>.
	 *                          
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List updateAnnotationFor(Map toUpdate)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a map whose keys are the <code>GroupData</code> objetcs
	 * and the values are the <code>ExperimenterData</code>s contained
	 * in the group.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Map<GroupData, Set> getAvailableGroups()
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the oprhans datasets or categories.
	 * 
	 * @param nodeType	The type of the rootNodes. It can either be
	 *                  <code>Dataset</code> or <code>Image</code>.
	 *                  Mustn't be <code>null</code>. 
	 * @param b			Pass <code>true</code> to retrieve the images,
	 * 					<code>false</code> otherwise.
	 * @param userID	The Id of the root.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set getOrphanContainers(Class nodeType, boolean b, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves and saves the archived files.
	 * Returns a map whose keys are the number of archived files retrieves
	 * and the values are the number of files actually saved.
	 * 
	 * @param location	The location where to save the files.
	 * @param pixelsID	The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Map getArchivedFiles(String location, long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Classifies the images contained in the specified folders.
	 * 
	 * @param containers	The folders containing the images to classify.
	 * 						Mustn't be <code>null</code>.
	 * @param categories	Collection of <code>CategoryData</code>.
	 * @return The classified images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set classifyChildren(Set containers, Set categories)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * 
	 * @param ids
	 * @param rootType
	 * @param tags
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Set tagImagesIncontainers(Set<Long> ids, Class rootType, 
			Set<CategoryData> tags)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the images contained in the passed folders i.e. 
	 * datasets categories.
	 * 
	 * @param folders	The folders containing the images to annotate.
	 * 					Mustn't be <code>null</code>.
	 * @param data		The annotation. Mustn't be <code>null</code>.
	 * @return The annotated images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List annotateChildren(Set folders, AnnotationData data)
		throws DSOutOfServiceException, DSAccessException;		

	/**
	 * Changes the password of the user currently logged in.
	 * 
	 * @param oldPassword	The password used to log in.
	 * @param newPassword	The new password.
	 * @return 	<code>Boolean.TRUE</code> if successfully modified,
	 * 			<code>Boolean.FALSE</code> otherwise.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Boolean changePassword(String oldPassword, String newPassword)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Updates the specified experimenter.
	 * 
	 * @param exp	The experimenter to update.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the address of the server the user is currently connected to.
	 * 
	 * @return See above.
	 */
	public String getServerName();
	
	/**
	 * Returns the name used to log in.
	 * 
	 * @return See above.
	 */
	public String getLoggingName();

	/**
	 * Returns the free or available space (in Kilobytes) if the passed
	 * parameter is <code>FREE</code>, returns the used space (in Kilobytes) 
	 * if the passed parameter is <code>USED</code> on the file system
	 * including nested subdirectories. Returns <code>-1</code> 
	 * otherwise.
	 * 
	 * @param index One of the following constants: {@link #USED} or 
	 * 				{@link #FREE}.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public long getSpace(int index)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images after a given date.
	 * 
	 * @param lowerTime	The timestamp identifying the start of the period.
	 * @param time		The timestamp identifying the end of the period.
	 * @param userID	The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Set getImagesPeriod(Timestamp lowerTime, Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the number of images imported during a given period of time.
	 * 
	 * @param lowerTime	The timestamp identifying the start of the period.
	 * @param time		The timestamp identifying the end of the period.
	 * @param userID	The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List getImagesPeriodIObject(Timestamp lowerTime, Timestamp time, 
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the number of images imported during a given period of time.
	 * 
	 * @param lowerTime	The timestamp identifying the start of the period.
	 * @param time		The timestamp identifying the end of the period.
	 * @param userID	The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List getImagesAllPeriodCount(Timestamp lowerTime, Timestamp time, 
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/** 
	 * Finds the categories containing the image.
	 * 
	 * @param imageID	The id of the image.
	 * @param leaves	Passed <code>true</code> to retrieve the images
	 * 					<code>false</code> otherwise.	
	 * @param userID	The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Set findCategoryPaths(long imageID, boolean leaves, long userID)
		throws DSOutOfServiceException, DSAccessException; 

	/** 
	 * Finds the categories containing the images.
	 * 
	 * @param imagesID	The id of the images.
	 * @param leaves	Passed <code>true</code> to retrieve the images
	 * 					<code>false</code> otherwise.	
	 * @param userID	The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Set findCategoryPaths(Set<Long> imagesID, boolean leaves, 
			long userID)
		throws DSOutOfServiceException, DSAccessException; 

	/**
	 * Retrieves the objects specified by the context of the search
	 * and returns an object hosting various elements used for the display.
	 * 	
	 * @param scope			The scope of the search.
	 * @param values		The terms to find.
	 * @param users			The users' name.
	 * @param start			The start of the time interval.
	 * @param end			The end of the time interval.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.	
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public SearchResult advancedSearchFor(List<Class> scope, List<String> values, 
			List<ExperimenterData> users, Timestamp start, Timestamp end,
			String separator, boolean caseSensitive)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the objects specified by the context of the search
	 * and returns an object hosting various elements used for the display.
	 * 	
	 * @param context The context of the search.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public SearchResult advancedSearchFor(SearchDataContext context)
		throws DSOutOfServiceException, DSAccessException;
	
	public Collection loadTags(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	public Collection loadAttachments(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	public Collection loadUrls(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	public Collection loadRatings(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	public Collection findContainerPaths(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
}
