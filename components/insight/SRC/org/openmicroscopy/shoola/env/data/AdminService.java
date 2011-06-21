/*
 * org.openmicroscopy.shoola.env.data.AdminService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Provides methods to handle groups and users.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface AdminService
{

	/** Identifies the <code>User</code> group. */
	public static final String	USER_GROUP = GroupData.USER;
	
	/** Identifies the <code>Default</code> group. */
	public static final String	DEFAULT_GROUP = GroupData.DEFAULT;
	
	
	/** Identifies the used space on the file system. */
	public static final int 	USED = 100;

	/** Identifies the free space on the file system. */
	public static final int 	FREE = 101;
	
	/**
	 * Changes the password of the user currently logged in.
	 * 
	 * @param oldPassword	The password used to log in.
	 * @param newPassword	The new password.
	 * @return <code>true</code> if successfully modified,
	 * 			<code>false</code> otherwise.
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
	 * @param group The group the user is member of.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp, GroupData
			group)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Updates the specified group.
	 * 
	 * @param group The group to update.
	 * @param permissions The desired permissions level or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public GroupData updateGroup(GroupData group, int permissions)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Changes the current group of the specified user.
	 * 
	 * @param exp 		The experimenter to handle.
	 * @param groupID 	The identifier group the user is member of.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public void changeExperimenterGroup(ExperimenterData exp, long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates and returns the experimenters.
	 * 
	 * @param object The object hosting information about the experimenters
	 * to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ExperimenterData> createExperimenters(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates and returns the new group
	 * 
	 * @param object The object hosting information about the group to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public GroupData createGroup(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the address of the server the user is currently connected to.
	 * 
	 * @return See above.
	 */
	public String getServerName();
	
	/**
	 * Returns the version of the server if available.
	 * 
	 * @return See above.
	 */
	public String getServerVersion();
	
	/**
	 * Returns the name used to log in.
	 * 
	 * @return See above.
	 */
	public String getLoggingName();

	/**
	 * Loads the group specified by the passed identifier or all available
	 * groups if <code>-1</code>.
	 * 
	 * @param groupID The group identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<GroupData> loadGroups(long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the experimenters contained in the specified group.
	 * 
	 * @param groupID The group identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ExperimenterData> loadExperimenters(long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Deletes the specified experimenters. Returns the experimenters 
	 * that could not be deleted.
	 * 
	 * @param experimenters The experimenters to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ExperimenterData> deleteExperimenters(
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Deletes the specified groups. Returns the groups 
	 * that could not be deleted.
	 * 
	 * @param groups The groups to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<GroupData> deleteGroups(List<GroupData> groups)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the permissions associated to the specified group.
	 * 
	 * @param group The group to handle. If <code>null</code>, 
	 * 				returns the permissions of the default group.
	 * @return See above.
	 */
	public int getPermissionLevel(GroupData group);
	
	/**
	 * Returns the permissions associated to the current group.
	 * 
	 * @return See above.
	 */
	public int getPermissionLevel();
	
	/**
	 * Copies the passed experimenters to the specified group.
	 * 
	 * @param group The group to add the experimenters to.
	 * @param experimenters The experimenters to add.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ExperimenterData> copyExperimenters(GroupData group, 
			Collection experimenters)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Cuts and paste the specified experimenters.
	 * 
	 * @param toPaste   The nodes to paste.
	 * @param toCut     The nodes to cut.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ExperimenterData> cutAndPasteExperimenters(Map toPaste, 
			Map toRemove)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Counts the number of experimenters within the specified groups.
	 * Returns a map whose keys are the group identifiers and the values the 
	 * number of experimenters in the group.
	 * 
	 * @param ids The group identifiers.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Map<Long, Long> countExperimenters(List<Long> ids)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Updates the specified experimenters. Returns a map whose key are the 
	 * experimenter that cannot be updated and whose values are the exception.
	 * 
	 * 
	 * @param group The default group.
	 * @param experimenters The experimenters to update.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Map<ExperimenterData, Exception> updateExperimenters(GroupData group,
			Map<ExperimenterData, UserCredentials> experimenters)
			throws DSOutOfServiceException, DSAccessException;

	/**
	 * Resets the password of the specified experimenters.
	 * 
	 * @param object The object to handle.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public List<ExperimenterData> resetExperimentersPassword(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Activates or not the specified experimenters.
	 * 
	 * @param object The object to handle.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public List<ExperimenterData> activateExperimenters(AdminObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	
	/**
	 * Reloads the groups and experimenters for a group owners.
	 * 
	 * @param exp The owner of a group.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public List<GroupData> reloadPIGroups(ExperimenterData exp)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the <code>Group</code> if the name of the group already exists,
	 * <code>null</code> otherwise.
	 * 
	 * @param name The name of the group.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public GroupData lookupGroup(String name)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the <code>Experimenter</code> if the name of the experimenter 
	 * already exists, <code>null</code> otherwise.
	 * 
	 * @param name The name of the experimenter.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public ExperimenterData lookupExperimenter(String name)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Uploads the specified photo for the passed user. Returns the uploaded
	 * photo.
	 * 
	 * @param f The file to upload.
	 * @param format The format of the file.
	 * @param experimenter The experimenter to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Object uploadUserPhoto(File f, String format, 
			ExperimenterData experimenter)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the disk space.
	 * 
	 * @param f The file to upload.
	 * @param format The format of the file.
	 * @param experimenter The experimenter to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public DiskQuota getQuota(Class type, long id)
		throws DSOutOfServiceException, DSAccessException;

}
