/*
 * org.openmicroscopy.shoola.env.data.AdminService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
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
 * @since 3.0-Beta4
 */
public interface AdminService
{

    /** Identifies the <code>User</code> group. */
    public static final String USER_GROUP = GroupData.USER;

    /** Identifies the <code>Default</code> group. */
    public static final String DEFAULT_GROUP = GroupData.DEFAULT;


    /** Identifies the used space on the file system. */
    public static final int USED = 100;

    /** Identifies the free space on the file system. */
    public static final int FREE = 101;

    /**
     * Changes the password of the user currently logged in.
     * Returns <code>true</code> if successfully modified,
     * <code>false</code> otherwise.
     * 
     * @param ctx The security context.
     * @param oldPassword The password used to log in.
     * @param newPassword The new password.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service. 
     */
    public Boolean changePassword(SecurityContext ctx, String oldPassword,
            String newPassword)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Updates the specified experimenter.
     * 
     * @param ctx The security context.
     * @param exp The experimenter to update.
     * @param group The group the user is member of.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service. 
     */
    public ExperimenterData updateExperimenter(SecurityContext ctx,
            ExperimenterData exp, GroupData group)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Updates the specified group.
     * 
     * @param ctx The security context.
     * @param group The group to update.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service. 
     */
    public GroupData updateGroup(SecurityContext ctx, GroupData group)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Updates the specified group's permissions.
     * 
     * @param ctx The security context.
     * @param group The group to update.
     * @param permissions The desired permissions level
     * @param adapter The adapter to handle the result
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public void updateGroupPermissions(SecurityContext ctx, GroupData group,
            int permissions, DSCallAdapter adapter)
                    throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Reloads a group from the server
     * @param ctx The security context.
     * @param group The group to load.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public GroupData reloadGroup(SecurityContext ctx, GroupData group)
            throws DSOutOfServiceException, DSAccessException ;
    
    /**
     * Changes the current group of the specified user.
     * 
     * @param ctx The security context.
     * @param exp The experimenter to handle.
     * @param groupID The identifier group the user is member of.
     * @return The updated user.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service.
     */
    public ExperimenterData changeExperimenterGroup(SecurityContext ctx,
            ExperimenterData exp, long groupID)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Creates and returns the experimenters.
     * 
     * @param ctx The security context.
     * @param object The object hosting information about the experimenters
     * to create.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service. 
     */
    public List<ExperimenterData> createExperimenters(SecurityContext ctx,
            AdminObject object)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Creates and returns the new group.
     * 
     * @param ctx The security context.
     * @param object The object hosting information about the group to create.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service. 
     */
    public GroupData createGroup(SecurityContext ctx, AdminObject object)
            throws DSOutOfServiceException, DSAccessException;

    /**
     * Returns the address of the server the user is currently connected to.
     * @return See above.
     */
    public String getServerName();

    /**
     * Returns the version of the server if available.
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
     * Tells whether the communication channel to <i>OMEDS</i> is currently
     * connected.
     * This means that we have established a connection and have successfully
     * logged in.
     * 
     * @return <code>true</code> if connected, <code>false</code> otherwise.
     */
    public boolean isConnected();

    /**
     * Loads the group specified by the passed identifier or all available
     * groups if <code>-1</code>.
     * 
     * @param ctx The security context.
     * @param groupID The group identifier.
     * @return See above.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException        If an error occurred while trying to 
     *                                  retrieve data from OMEDS service.
     */
    public List<GroupData> loadGroups(SecurityContext ctx, long groupID)
            throws DSOutOfServiceException, DSAccessException;

    /**
     * Loads the groups the specified experimenter is a member of.
     * 
     * @param ctx The security context.
     * @param id The experimenter identifier.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged
     *                                 in.
     * @throws DSAccessException If an error occurred while trying to
     *                           retrieve data from OMEDS service.
     */
    public List<GroupData> loadGroupsForExperimenter(SecurityContext ctx,
            long id) 
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Loads the experimenters contained in the specified group.
     * 
     * @param ctx The security context.
     * @param groupID The group identifier.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged
     *                                 in.
     * @throws DSAccessException If an error occurred while trying to
     *                           retrieve data from OMEDS service.
     */
    public List<ExperimenterData> loadExperimenters(SecurityContext ctx,
            long groupID)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Deletes the specified experimenters. Returns the experimenters
     * that could not be deleted.
     * 
     * @param ctx The security context.
     * @param experimenters The experimenters to delete.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in.
     * @throws DSAccessException If an error occurred while trying to 
     *                           retrieve data from OMEDS service.
     */
    public List<ExperimenterData> deleteExperimenters(SecurityContext ctx,
            List<ExperimenterData> experimenters)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Deletes the specified groups. Returns the groups 
     * that could not be deleted.
     * 
     * @param ctx The security context.
     * @param groups The groups to delete.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged
     *                                 in.
     * @throws DSAccessException If an error occurred while trying to
     *                           retrieve data from OMEDS service.
     */
    public List<GroupData> deleteGroups(SecurityContext ctx,
            List<GroupData> groups)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Copies the passed experimenters to the specified group.
     * 
     * @param ctx The security context.
     * @param group The group to add the experimenters to.
     * @param experimenters The experimenters to add.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service. 
     */
    public List<ExperimenterData> copyExperimenters(SecurityContext ctx,
            GroupData group, Collection experimenters)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Cuts and paste the specified experimenters.
     * 
     * @param ctx The security context.
     * @param toPaste The nodes to paste.
     * @param toCut The nodes to cut.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service. 
     */
    public List<ExperimenterData> cutAndPasteExperimenters(
            SecurityContext ctx, Map toPaste, Map toRemove)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Counts the number of experimenters within the specified groups.
     * Returns a map whose keys are the group identifiers and the values the 
     * number of experimenters in the group.
     * 
     * @param ctx The security context.
     * @param ids The group identifiers.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service.
     */
    public Map<Long, Long> countExperimenters(SecurityContext ctx,
            List<Long> ids)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Updates the specified experimenters. Returns a map whose key are the 
     * experimenter that cannot be updated and whose values are the exception.
     * 
     * @param ctx The security context.
     * @param group The default group.
     * @param experimenters The experimenters to update.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public Map<ExperimenterData, Exception> updateExperimenters(
            SecurityContext ctx, GroupData group, 
            Map<ExperimenterData, UserCredentials> experimenters)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Resets the password of the specified experimenters.
     * 
     * @param ctx The security context.
     * @param object The object to handle.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public List<ExperimenterData> resetExperimentersPassword(
            SecurityContext ctx, AdminObject object)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Activates or not the specified experimenters.
     * 
     * @param ctx The security context.
     * @param object The object to handle.
     * @return See above
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public List<ExperimenterData> activateExperimenters(SecurityContext ctx,
            AdminObject object)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Reloads the groups and experimenters for a group owners.
     * 
     * @param ctx The security context.
     * @param exp The owner of a group.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public List<GroupData> reloadPIGroups(SecurityContext ctx,
            ExperimenterData exp)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Returns the <code>Group</code> if the name of the group already exists,
     * <code>null</code> otherwise.
     * 
     * @param ctx The security context.
     * @param name The name of the group.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public GroupData lookupGroup(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException;

    /**
     * Returns the <code>Experimenter</code> if the name of the experimenter
     * already exists, <code>null</code> otherwise.
     * 
     * @param ctx The security context.
     * @param name The name of the experimenter.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public ExperimenterData lookupExperimenter(SecurityContext ctx, String name)
            throws DSOutOfServiceException, DSAccessException;

    /**
     * Uploads the specified photo for the passed user. Returns the uploaded
     * photo.
     * 
     * @param ctx The security context.
     * @param f The file to upload.
     * @param format The format of the file.
     * @param experimenter The experimenter to handle.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public Object uploadUserPhoto(SecurityContext ctx, File f, String format,
            ExperimenterData experimenter)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Returns the disk space.
     * 
     * @param ctx The security context.
     * @param type The node
     * @param id The node's id.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to
     * retrieve data from OMERO service.
     */
    public DiskQuota getQuota(SecurityContext ctx, Class type, long id)
            throws DSOutOfServiceException, DSAccessException;

    /**
     * Adds the experimenters to the specified group.
     * 
     * @param ctx The security context.
     * @param group The group to add the experimenters to.
     * @param experimenters The experimenters to add.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occurred while trying to 
     * retrieve data from OMERO service.
     */
    public void addExperimenters(SecurityContext ctx, GroupData group,
            List<ExperimenterData> experimenters)
                    throws DSOutOfServiceException, DSAccessException;

    /**
     * Returns the LDAP details or an empty string.
     *
     * @param ctx The security context.
     * @param userID The id of the user.
     * @return See above.
     * @throws DSOutOfServiceException If the connection can't be established
     *                                  or the credentials are invalid.
     */
    public String lookupLdapAuthExperimenter(SecurityContext ctx, long userID)
       throws DSOutOfServiceException, DSAccessException;

    /**
     * Helper method returning the current user's details.
     * 
     * @return See above.
     */
    public ExperimenterData getUserDetails();

    /**
     * Returns the groups the logged in user is a member of.
     * 
     * @return See above.
     */
    public Collection<GroupData> getAvailableUserGroups();

    /**
     * Returns <code>true</code> if the group is a group required by the
     * security system e.g. system, users, <code>false</code> otherwise.
     *
     * @param groupID The identifier of the group.
     * @return See above.
     */
    public boolean isSecuritySystemGroup(long groupID);

    /**
     * Returns <code>true</code> if the group is a group required by the
     * security system of the specified type e.g. users,
     * <code>false</code> otherwise.
     *
     * @param groupID The identifier of the group.
     * @param key One of the constants defined by <code>GroupData</code>.
     * @return See above.
     */
    public boolean isSecuritySystemGroup(long groupID, String key);
    
    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @return See above.
     */
    public boolean isSystemUser(long userID);

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @param key One of the constants defined by <code>GroupData</code>
     * @return See above.
     */
    public boolean isSystemUser(long userID, String key);

}
