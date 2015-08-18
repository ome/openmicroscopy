/*
 * org.openmicroscopy.shoola.env.data.OmeroDataService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


//Third-party libraries







//Application-internal dependencies
import omero.api.StatefulServiceInterfacePrx;

import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.SearchParameters;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;

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

	/**
	 * Identifies the count property.
	 */
	public static final String IMAGES_PROPERTY = "images";

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 *
	 * @param ctx The security context.
	 * @param rootNodeType The top-most type which will be searched for
	 *                     Can be <code>Project</code>,
	 *                     <code>Dataset</code>.
	 *                     Mustn't be <code>null</code>.
	 * @param rootNodeIDs A set of the IDs of top-most containers.
	 *                    Passed <code>null</code> to retrieve all top-most
	 *                    nodes e.g. all user's projects.
	 * @param withLeaves Passed <code>true</code> to retrieve the images,
	 *                   <code>false</code> otherwise.
	 * @param userID The identifier of the selected user.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Set loadContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, List rootNodeIDs, boolean withLeaves,
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 *
	 * @param ctx The security context.
	 * @param rootNodeType The top-most type which will be searched for
	 *                     Can be <code>Project</code>.
	 *                     Mustn't be <code>null</code>.
	 * @param userID The Id of the selected user.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Set loadTopContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, long userID)
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
	 * its containers or an <code>Image</code>.
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
	 * @param ctx The security context.
	 * @param rootNodeType  Top-most type which will be searched for
	 *                      Can be <code>Project</code>.
	 *                      Mustn't be <code>null</code>.
	 * @param leavesIDs     Set of ids of the Images that sit at the bottom of
	 *                      the trees. Mustn't be <code>null</code>.
	 * @param userID		The Id of the user.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Set findContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, List leavesIDs, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images contained in containers specified by the
	 * node type.
	 *
	 * @param ctx The security context.
	 * @param nodeType The type of container. Can either be Project,
	 * 					Dataset, or Image.
	 * @param nodeIDs Set of node ids..
	 * @param userID The Id of the root.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Set getImages(SecurityContext ctx, Class nodeType, List nodeIDs,
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images imported by the specified user.
	 *
	 * @param ctx The security context.
	 * @param userID The id of the user.
	 * @param orphan Indicates to load the images not in any container or all
	 * the images.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Set getExperimenterImages(SecurityContext ctx, long userID, boolean
			orphan)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Counts the number of items in a collection for a given object.
	 * Returns a map which key is the passed rootNodeID and the value is
	 * the number of items contained in this object.
	 *
	 * @param ctx The security context.
	 * @param rootNodeType The type of container. Can either be Dataset.
	 * @param property One of the properties defined by this class.
	 * @param rootNodeIDs Set of root node IDs.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Map getCollectionCount(SecurityContext ctx,
			Class rootNodeType, String property, List rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates a new <code>DataObject</code> and links it to the specified
	 * parent. The parent will be <code>null</code> if the
	 * <code>DataObject</code> to create is either a <code>Project</code>.
	 *
	 * @param ctx The security context.
	 * @param newObject The <code>DataObject</code> to create.
	 *                  Mustn't be <code>null</code>.
	 * @param parent The parent of the <code>DataObject</code> or  Can be
	 *               <code>null</code> if no parent specified.
	 * @param children The nodes to add to the newly created
	 *                 <code>DataObject</code>.
	 * @return The newly created <code>DataObject</code>
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public DataObject createDataObject(SecurityContext ctx,
			DataObject newObject, DataObject parent, Collection children)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Updates the specified <code>DataObject</code>.
	 *
	 * @param ctx The security context.
	 * @param object The <code>DataObject</code> to update.
	 * @return The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public DataObject updateDataObject(SecurityContext ctx, DataObject object)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Adds the given objects to the specified node.
	 *
	 * @param ctx The security context.
	 * @param parent The <code>DataObject</code> to update. Either a
	 *               <code>ProjectData</code> or <code>DatasetData</code>.
	 * @param children  The collection of objects to add.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public void addExistingObjects(SecurityContext ctx, DataObject parent,
			Collection children)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Cuts and paste the specified nodes.
	 *
	 * @param ctx The security context.
	 * @param toPaste   The nodes to paste.
	 * @param toCut     The nodes to cut.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public void cutAndPaste(SecurityContext ctx, Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves and saves locally the archived files.
	 *
	 * @param ctx The security context.
	 * @param location The location where to save the files.
	 * @param imageID The ID of the image.
	 * @param keepOriginalPath Pass <code>true</code> to preserve the original 
	 *         path structure
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Map<Boolean, Object> getArchivedImage(SecurityContext ctx,
			File location, long imageID, boolean keepOriginalPath)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images after a given date.
	 *
	 * @param ctx The security context.
	 * @param lowerTime The timestamp identifying the start of the period.
	 * @param time The timestamp identifying the end of the period.
	 * @param userID The Id of the user.
	 * @param asDataObject Pass <code>true</code> to convert the object into
	 *                     the corresponding <code>DataObject</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getImagesPeriod(SecurityContext ctx, Timestamp lowerTime,
			Timestamp time, long userID, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the number of images imported during a given period of time.
	 *
	 * @param ctx The security context.
	 * @param lowerTime The timestamp identifying the start of the period.
	 * @param time The timestamp identifying the end of the period.
	 * @param userID The Id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	public List getImagesAllPeriodCount(SecurityContext ctx,
			Timestamp lowerTime, Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the objects specified by the context of the search
	 * and returns an object hosting various elements used for the display.
	 *
	 * @param ctx The security context.
	 * @param context The context of the search.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	public SearchResultCollection search(SecurityContext ctx,
	        SearchParameters context)
		throws DSOutOfServiceException, DSAccessException;
        
	/**
	 * Finds the objects containing the object identifying by the specified
	 * type and id e.g. find the datasets containing a given image.
	 *
	 * @param ctx The security context.
	 * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who added attachments to the object
     *               or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection findContainerPaths(SecurityContext ctx, Class type,
			long id, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the collection of original files corresponding to the passed
	 * pixels set.
	 *
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getOriginalFiles(SecurityContext ctx, long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the wells for the specified plate and acquisition.
	 *
	 * @param ctx The security context.
	 * @param plateID The ID of the plate.
	 * @param acquisitionID The ID of the acquisition.
	 * @param userID The id of the user.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Collection loadPlateWells(SecurityContext ctx, long plateID,
			long acquisitionID, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Deletes the collection of objects. The objects should all be of the
	 * same types. Returns a handle to monitor the status of the deletion
	 *
	 * @param ctx The security context.
	 * @param objects The collection of objects to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 * @throws ProcessException If an error occurred while starting the process.
	 */
	public RequestCallback delete(SecurityContext ctx,
			Collection<DeletableObject> objects)
		throws DSOutOfServiceException, DSAccessException, ProcessException;

	/**
	 * Returns the view of the server repositories.
	 *
	 * @param ctx The security context.
	 * @param userID The ID of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public FSFileSystemView getFSRepositories(SecurityContext ctx, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Transfers the collection of objects. The objects should all be of the
	 * same types. Returns a handle to monitor the status of the transfer.
	 *
	 * @param ctx The security context.
	 * @param target The context of the target.
	 * @param targetNode The elements to transfer the data to.
	 * @param objects The collection of objects to transfer.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 * @throws ProcessException If an error occurred while starting the process.
	 */
	public RequestCallback transfer(SecurityContext ctx,
			SecurityContext target, List<DataObject> targetNode,
		List<DataObject> objects)
		throws DSOutOfServiceException, DSAccessException, ProcessException;

	/**
	 * Loads to the plate hosting the specified images.
	 * Returns a map whose keys are the image's id and the values are the
	 * corresponding plate.
	 *
	 * @param ctx The security context.
	 * @param ids The collection of image's identifiers.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Map<Long, PlateData> loadPlateFromImage(SecurityContext ctx,
		Collection<Long> ids)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Closes the specified service.
	 *
	 * @param ctx The security context
	 * @param svc The service to handle.
	 */
	void closeService(SecurityContext ctx, StatefulServiceInterfacePrx svc);

	/**
	 * Given a list of IDs of a given type. Determines the filesets that will be
	 * split. Returns the a Map with fileset's ids as keys and the
	 * values if the map:
	 * Key = <code>True</code> value: List of image's ids that are contained.
	 * Key = <code>False</code> value: List of image's ids that are missing
	 * so the delete or change group cannot happen.
	 *
	 * @param ctx The security context, necessary to determine the service.
	 * @param rootType The top-most type which will be searched
	 *                  Mustn't be <code>null</code>.
	 * @param rootIDs A set of the IDs of objects.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public Map<Long, Map<Boolean, List<ImageData>>> getImagesBySplitFilesets(
			SecurityContext ctx, Class<?> rootType, List<Long> rootIDs)
		throws DSOutOfServiceException, DSAccessException;

	/**
	* Finds all Datasets the given image ids are linked to
	* @param ctx The security context, necessary to determine the service.
	* @param imgIds The ids of the images
	* @return See above.
	* @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMERO service.
	*/
	public Map<Long, List<DatasetData>> findDatasetsByImageId(SecurityContext ctx, List<Long> imgIds) throws DSOutOfServiceException, DSAccessException;
}
