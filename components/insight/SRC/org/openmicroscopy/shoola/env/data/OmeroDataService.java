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


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import pojos.DataObject;
import pojos.ExperimenterData;

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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set loadContainerHierarchy(Class rootNodeType, List rootNodeIDs,
			boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 * 
	 * @param rootNodeType  The top-most type which will be searched for 
	 *                      Can be <code>Project</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param userID		The Id of the selected user.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
	public Set findContainerHierarchy(Class rootNodeType, List leavesIDs,
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * 
	 * @param nodeType	The type of container. Can either be Project, 
	 * 					Dataset,  or Image.
	 * @param nodeIDs   Set of node ids..
	 * @param userID	The Id of the root.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set getImages(Class nodeType, List nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images imported by the specified user.
	 * 
	 * @param userID The id of the user.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Set getExperimenterImages(long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Counts the number of items in a collection for a given object.
	 * Returns a map which key is the passed rootNodeID and the value is 
	 * the number of items contained in this object.
	 * 
	 * @param rootNodeType 	The type of container. Can either be Dataset.
	 * @param property		One of the properties defined by this class.
	 * @param rootNodeIDs	Set of root node IDs.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Map getCollectionCount(Class rootNodeType, String property,
			List rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates a new <code>DataObject</code> and links it to the specified 
	 * parent. The parent will be <code>null</code> if the
	 * <code>DataObject</code> to create is either a <code>Project</code>.
	 * 
	 * @param newObject The <code>DataObject</code> to create.
	 *                  Mustn't be <code>null</code>.
	 * @param parent    The parent of the <code>DataObject</code> or  Can be 
	 *                  <code>null</code> if no parent specified.
	 * @param children	The nodes to add to the newly created 
	 * 					<code>DataObject</code>.
	 * @return          The newly created <code>DataObject</code>
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public DataObject createDataObject(DataObject newObject, DataObject parent, 
										Collection children)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Updates the specified <code>DataObject</code>.
	 * 
	 * @param object    The <code>DataObject</code> to update.
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public DataObject updateDataObject(DataObject object)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Adds the given objects to the specified node.
	 * 
	 * @param parent    The <code>DataObject</code> to update. Either a 
	 *                  <code>ProjectData</code> or <code>DatasetData</code>.
	 * @param children  The collection of objects to add.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public void addExistingObjects(DataObject parent, Collection children)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Cuts and paste the specified nodes.
	 * 
	 * @param toPaste   The nodes to paste.
	 * @param toCut     The nodes to cut.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List getChannelsMetadata(long pixelsID)
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Map getArchivedFiles(String location, long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Changes the password of the user currently logged in.
	 * 
	 * @param oldPassword	The password used to log in.
	 * @param newPassword	The new password.
	 * @return 	<code>Boolean.TRUE</code> if successfully modified,
	 * 			<code>Boolean.FALSE</code> otherwise.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
	 * @throws DSAccessException If an error occurred while trying to 
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
	 * including nested sub-directories. Returns <code>-1</code> 
	 * otherwise.
	 * 
	 * @param index One of the following constants: {@link #USED} or 
	 * 				{@link #FREE}.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public long getSpace(int index, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the images after a given date.
	 * 
	 * @param lowerTime		The timestamp identifying the start of the period.
	 * @param time			The timestamp identifying the end of the period.
	 * @param userID		The Id of the user.
	 * @param asDataObject 	Pass <code>true</code> to convert the object into
	 * 						the corresponding <code>DataObject</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getImagesPeriod(Timestamp lowerTime, Timestamp time, 
			long userID, boolean asDataObject)
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List getImagesAllPeriodCount(Timestamp lowerTime, Timestamp time, 
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Retrieves the objects specified by the context of the search
	 * and returns an object hosting various elements used for the display.
	 * 	
	 * @param context The context of the search.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object advancedSearchFor(SearchDataContext context)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Finds the objects containing the object identifying by the specified
	 * type and id e.g. find the datasets containing a given image.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection findContainerPaths(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the collection of original files corresponding to the passed
	 * pixels set.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getOriginalFiles(long pixelsID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the wells for the specified plate and acquisition.
	 * 
	 * @param plateID The ID of the plate.
	 * @param acquisitionID The ID of the acquisition.
	 * @param userID The id of the user.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Collection loadPlateWells(long plateID, long acquisitionID,
			long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Deletes the collection of objects. Returns a collection of objects
	 * that couldn't be deleted.
	 * 
	 * @param objects	The collection of objects to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public Collection<DeletableObject> delete(
			Collection<DeletableObject> objects)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the version of the server if available.
	 * 
	 * @return See above.
	 */
	public String getServerVersion();
	
}
