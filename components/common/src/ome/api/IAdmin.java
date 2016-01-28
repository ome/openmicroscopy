/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.List;
import java.util.Map;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.conditions.AuthenticationException;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.EventContext;
import ome.system.Roles;

/**
 * Administration interface providing access to admin-only functionality as well
 * as JMX-based server access and selected user functions. Most methods require
 * membership in privileged {@link ExperimenterGroup groups}.
 * 
 * Methods which return {@link ome.model.meta.Experimenter} or
 * {@link ome.model.meta.ExperimenterGroup} instances fetch and load all related
 * instances of {@link ome.model.meta.ExperimenterGroup} or
 * {@link ome.model.meta.Experimenter}, respectively.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @since OME3.0
 */
public interface IAdmin extends ServiceInterface {

    /**
     * Returns true if the currently logged in user can modify the given
     * {@link IObject}. This uses the same logic that would be applied during
     * a Hibernate flush to the database.
     */
    boolean canUpdate(IObject obj);
    
    // ~ Getting users and groups
    // =========================================================================

    /**
     * fetch an {@link Experimenter} and all related
     * {@link ExperimenterGroup groups}.
     * 
     * @param id
     *            id of the Experimenter
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if id does not exist.
     */
    Experimenter getExperimenter(long id);

    /**
     * look up an {@link Experimenter} and all related
     * {@link ExperimenterGroup groups} by name.
     * 
     * @param omeName
     *            Name of the Experimenter
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if omeName does not exist.
     */
    Experimenter lookupExperimenter(@NotNull
    String omeName);

    /**
     * Looks up all {@link Experimenter experimenters} present and all related
     * {@link ExperimenterGroup groups}.
     * 
     * @return all Experimenters. Never null.
     */
    List<Experimenter> lookupExperimenters();

    /**
     * Looks up all id of {@link Experimenter experimenters} who uses LDAP
     * authentication (has set dn on password table).
     * 
     * @return list of Experimenters. Never null.
     */
    List<Map<String, Object>> lookupLdapAuthExperimenters();

    /**
     * Looks up {@link Experimenter experimenters} who uses LDAP authentication
     * (has set dn on password table).
     * 
     * @return Experimenter. Never null.
     */
    String lookupLdapAuthExperimenter(long id);

    /**
     * fetch an {@link ExperimenterGroup} and all contained
     * {@link Experimenter users}.
     * 
     * @param id
     *            id of the ExperimenterGroup
     * @return an ExperimenterGroup. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if id does not exist.
     */
    ExperimenterGroup getGroup(long id);

    /**
     * look up an {@link ExperimenterGroup} and all contained
     * {@link Experimenter users} by name.
     * 
     * @param groupName
     *            Name of the ExperimenterGroup
     * @return an ExperimenterGroup. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if groupName does not exist.
     */
    ExperimenterGroup lookupGroup(@NotNull
    String groupName);

    /**
     * Looks up all {@link ExperimenterGroup groups} present and all related
     * {@link Experimenter experimenters}. The experimenters' groups are also
     * loaded.
     * 
     * @return all Groups. Never null.
     */
    List<ExperimenterGroup> lookupGroups();

    /**
     * fetch all {@link Experimenter users} contained in this group. The
     * returned users will have all fields filled in and all collections
     * unloaded.
     * 
     * @param groupId
     *            id of the ExperimenterGroup
     * @return non-null array of all {@link Experimenter users} in this group.
     */
    Experimenter[] containedExperimenters(long groupId);

    /**
     * fetch all {@link ExperimenterGroup groups} of which the given user is a
     * member. The returned groups will have all fields filled in and all
     * collections unloaded.
     * 
     * @param experimenterId
     *            id of the Experimenter. Not null.
     * @return non-null array of all {@link ExperimenterGroup groups} for this
     *         user.
     */
    ExperimenterGroup[] containedGroups(long experimenterId);

    /**
     * retrieve the default {@link ExperimenterGroup group} for the given user
     * id.
     * 
     * @param experimenterId
     *            of the Experimenter. Not null.
     * @return non-null {@link ExperimenterGroup}. If no default group is
     *         found, an exception will be thrown.
     */
    ExperimenterGroup getDefaultGroup(long experimenterId);


    /**
     * Finds the ids for all groups for which the given {@link Experimenter} is
     * owner/leader.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<Long> getLeaderOfGroupIds(Experimenter e);

    /**
     * Finds the ids for all groups for which the given {@link Experimenter} is
     * a member.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<Long> getMemberOfGroupIds(Experimenter e);

    // ~ Updating users and groups
    // =========================================================================

    /**
     * Allows a user to update his/her own information. This is limited to the
     * fields on Experimenter, all other fields (groups, etc.) are ignored. The
     * experimenter argument need not have the proper id nor the proper omeName
     * (which is immutable). To change the users default group (which is the
     * only other customizable option), use
     * {@link #setDefaultGroup(Experimenter, ExperimenterGroup)}
     * 
     * @see #setDefaultGroup(Experimenter, ExperimenterGroup)
     * @param experimenter
     *            A data transfer object. Only the fields: firstName,
     *            middleName, lastName, email, and institution are checked. Not
     *            null.
     */
    void updateSelf(@NotNull
    Experimenter experimenter);

    /**
     * Uploads a photo for the user which will be displayed on his/her profile.
     * This photo will be saved as an {@link ome.model.core.OriginalFile} object
     * with the given format, and attached to the user's {@link Experimenter}
     * object via an {@link ome.model.annotations.FileAnnotation} with
     * the namespace: "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO).
     * If such an {@link ome.model.core.OriginalFile} instance already exists,
     * it will be overwritten. If more than one photo is present, the oldest
     * version will be modified (i.e. the highest updateEvent id).
     *
     * Note: as outlined in ticket:1794, this photo will be placed in the "user"
     * group and therefore will be visible to everyone on the system.
     *
     * @param filename Not null. String name which will be used.
     * @param format Not null. Format.value string. 'image/jpeg' and 'image/png' are common values.
     * @param data Not null. Data from the image. This will be written to disk.
     * @return the id of the overwritten or newly created user photo OriginalFile object.
     */
    long uploadMyUserPhoto(String filename, String format, byte[] data);

    /**
     * Retrieve the {@link ome.model.core.OriginalFile} object attached to this
     * user as specified by {@link #uploadMyUserPhoto(String, String, byte[])}.
     * The return value is order by the most recently modified file first.
     *
     * @return file objects. Possibly empty.
     */
    List<OriginalFile> getMyUserPhotos();

    /**
     * Updates an experimenter if admin or owner of group. Only string fields on
     * the object are taken into account.
     * The root and guest experimenters may not be renamed.
     *
     * Before a SecurityViolation would be thrown, however, this method will
     * pass to {@link #updateSelf(Experimenter)} <em>if</em> the current user
     * matches the given experimenter.
     * 
     * @param experimenter
     *            the Experimenter to update.
     */
    void updateExperimenter(@NotNull
    Experimenter experimenter);

    /**
     * Updates an experimenter if admin or owner of group.
     * Only string fields on the object are taken into account.
     * The root and guest experimenters may not be renamed.
     * 
     * @param experimenter
     *            the Experimenter to update.
     * @param password
     *            Not-null. Must pass validation in the security sub-system.           
     */
    void updateExperimenterWithPassword(@NotNull
    Experimenter experimenter, @Hidden
    String password);
    
    /**
     * Updates an experimenter group if admin or owner of group.
     * Only string fields on the object are taken into account.
     * The root, system and guest groups may not be renamed,
     * nor may the user's current group.
     * 
     * @param group
     *            the ExperimenterGroup to update.
     */
    void updateGroup(@NotNull
    ExperimenterGroup group);

    // ~ Creating users in groups
    // =========================================================================

    /**
     * create and return a new user. This user will be created with the default
     * group specified.
     * 
     * @param newUser
     *            a new {@link Experimenter} instance
     * @param group group name of the default group for this user
     * @return id of the newly created {@link Experimenter}
     */
    long createUser(@NotNull
    Experimenter newUser, @NotNull
    String group);

    /**
     * create and return a new system user. This user will be created with the
     * "System" (administration) group as default and will also be in the "user"
     * group.
     * 
     * @param newSystemUser  a new {@link Experimenter} instance
     * @return id of the newly created {@link Experimenter}
     */
    long createSystemUser(@NotNull
    Experimenter newSystemUser);

    /**
     * create and return a new user in the given groups.
     * 
     * @param experimenter
     *            A new {@link Experimenter} instance. Not null.
     * @param defaultGroup
     *            Instance of {@link ExperimenterGroup}. Not null.
     * @param otherGroups
     *            Array of {@link ExperimenterGroup} instances. Can be null.
     * @return id of the newly created {@link Experimenter} Not null.
     */
    long createExperimenter(@NotNull
    Experimenter experimenter, @NotNull
    ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups);

    /**
     * create and return a new user in the given groups with password.
     * 
     * @param experimenter
     *            A new {@link Experimenter} instance. Not null.
     * @param password
     *            Not-null. Must pass validation in the security sub-system.
     * @param defaultGroup
     *            Instance of {@link ExperimenterGroup}. Not null.
     * @param otherGroups
     *            Array of {@link ExperimenterGroup} instances. Can be null.
     * @return id of the newly created {@link Experimenter} Not null.
     * @throws ome.conditions.SecurityViolation
     *             if the new password is too weak.
     */
    long createExperimenterWithPassword(@NotNull
    Experimenter experimenter, @Hidden
    String password, @NotNull
    ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups);    
    
    /**
     * create and return a new group. The {@link Details#setPermissions(Permissions)}
     * method should be called on the instance which is passed. The given
     * {@link Permissions} will become the default for all objects created while
     * logged into this group, possibly modified by the user's umask settings.
     * If no permissions is set, the default will be {@link Permissions#USER_PRIVATE},
     * i.e. a group in which no user can see the other group member's data.
     * 
     * @param group  a new {@link ExperimenterGroup} instance. Not null.
     * @return id of the newly created {@link ExperimenterGroup}
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/1434">ticket:1434"</a>
     */
    long createGroup(ExperimenterGroup group);

    /**
     * adds a user to the given groups.
     * 
     * @param user
     *            A currently managed entity. Not null.
     * @param groups
     *            Groups to which the user will be added. Not null.
     */
    void addGroups(@NotNull
    Experimenter user, @NotNull
    ExperimenterGroup... groups);

    /**
     * Removes an experimenter from the given groups.
     * <ul>
     * <li>The root experimenter is required to be in both the user and system groups.</li>
     * <li>An experimenter may not remove themself from the user or system group.</li>
     * <li>An experimenter may not be a member of only the user group,
     * some other group is also required as the default group.</li>
     * <li>An experimenter must remain a member of some group.</li>
     * </ul>
     * 
     * @param user
     *            A currently managed entity. Not null.
     * @param groups
     *            Groups from which the user will be removed. Not null.
     */
    void removeGroups(@NotNull
    Experimenter user, @NotNull
    ExperimenterGroup... groups);

    /**
     * sets the default group for a given user.
     * 
     * @param user
     *            A currently managed {@link Experimenter}. Not null.
     * @param group
     *            The group which should be set as default group for this user.
     *            Not null.
     */
    void setDefaultGroup(@NotNull
    Experimenter user, @NotNull
    ExperimenterGroup group);

    /**
     * adds the user to the owner list for this group.
     *
     * Since Beta4.2 (ticket:1434) multiple users can be the "owner" of a group.
     * 
     * @param group
     *            A currently managed {@link ExperimenterGroup}. Not null.
     * @param owner
     *            A currently managed {@link Experimenter}. Not null.
     */
    void setGroupOwner(@NotNull
    ExperimenterGroup group, @NotNull
    Experimenter owner);

    /**
     * removes the user from the owner list for this group.
     *
     * Since Beta4.2 (ticket:1434) multiple users can be the "owner" of a group.
     *
     * @param group
     *            A currently managed {@link ExperimenterGroup}. Not null.
     * @param owner
     *            A currently managed {@link Experimenter}. Not null.
     */
    void unsetGroupOwner(@NotNull
    ExperimenterGroup group, @NotNull
    Experimenter owner);

    /**
     * adds the given users to the owner list for this group.
     * 
     * @param group
     *            A currently managed {@link ExperimenterGroup}. Not null.
     * @param owner
     *            A set of currently managed {@link Experimenter}s. Not null.
     */
    void addGroupOwners(@NotNull
    ExperimenterGroup group, @NotNull
    Experimenter... owner);

    /**
     * removes the given users from the owner list for this group.
     *
     * @param group
     *            A currently managed {@link ExperimenterGroup}. Not null.
     * @param owner
     *            A set of currently managed {@link Experimenter}s. Not null.
     */
    void removeGroupOwners(@NotNull
    ExperimenterGroup group, @NotNull
    Experimenter... owner);

    /**
     * removes a user by removing the password information for that user as well
     * as all {@link GroupExperimenterMap} instances.
     * 
     * @param user
     *            Experimenter to be deleted. Not null.
     */
    void deleteExperimenter(@NotNull
    Experimenter user);

    /**
     * removes a group by first removing all users in the group, and then
     * deleting the actual {@link ExperimenterGroup} instance.
     * 
     * @param group
     *            {@link ExperimenterGroup} to be deleted. Not null.
     */
    void deleteGroup(@NotNull
    ExperimenterGroup group);

    // ~ Permissions and Ownership
    // =========================================================================

    /**
     * call
     * {@link ome.model.internal.Details#setOwner(Experimenter) details.setOwner()}
     * on this instance. It is valid for the instance to be
     * {@link IObject#unload() unloaded} (or constructed with an
     * unloading-constructor.)
     * 
     * @param iObject
     *            An entity or an unloaded reference to an entity. Not null.
     * @param omeName
     *            The user name who should gain ownership of this entity. Not
     *            null.
     */
    void changeOwner(@NotNull
    IObject iObject, @NotNull
    String omeName);

    /**
     * call
     * {@link ome.model.internal.Details#setGroup(ExperimenterGroup) details.setGroup()}
     * on this instance. It is valid for the instance to be
     * {@link IObject#unload() unloaded} (or constructed with an
     * unloading-constructor.)
     * 
     * @param iObject
     *            An entity or an unloaded reference to an entity. Not null.
     * @param groupName
     *            The group name who should gain ownership of this entity. Not
     *            null.
     */
    void changeGroup(@NotNull
    IObject iObject, @NotNull
    String groupName);

    /**
     * call
     * {@link ome.model.internal.Details#setPermissions(Permissions) defaults.setPermissions()}
     * on this instance. It is valid for the instance to be
     * {@link IObject#unload() unloaded} (or constructed with an
     * unloading-constructor.)
     * 
     * @param iObject
     *            An entity or an unloaded reference to an entity. Not null.
     * @param perms
     *            The permissions value for this entity. Not null.
     */
    void changePermissions(@NotNull
    IObject iObject, @NotNull
    Permissions perms);

    /**
     * Moves the given objects into the "user" group to make them visible
     * and linkable from all security contexts.
     *
     * @param iObjects
     * @see <a href="https://trac.openmicroscopy.org/ome/ticket/1794">ticket 1794</a>
     */
    void moveToCommonSpace(IObject... iObjects);

    // ~ Authentication and Authorization
    // =========================================================================

    /**
     * Can be used after repeated {@link AuthenticationException} instances are
     * thrown, to request that an email with a temporary password be sent. The
     * given email must match the email for the user listed under the name
     * argument.
     * 
     * Does not require a session to be active.
     * 
     * @param name
     * @param email
     * @throws AuthenticationException
     *             when name and email do not match
     * @deprecated
     */
    @Deprecated
    void reportForgottenPassword(String name, String email)
            throws AuthenticationException;

    /**
     * Used after an {@link ome.conditions.ExpiredCredentialException} instance is thrown.
     * 
     * Does not require
     */
    void changeExpiredCredentials(String name, String oldCred, String newCred)
            throws AuthenticationException;

    /**
     * change the password for the current user.
     * <p>
     * <em>Warning:</em>This method requires the user to be authenticated
     * with a password and not with a one-time session id. To avoid this
     * problem, use {@link #changePasswordWithOldPassword(String, String)}.
     * </p>
     *
     * @param newPassword
     *            Possibly null to allow logging in with no password.
     * @throws ome.conditions.SecurityViolation
     *             if the user is not authenticated with a password.
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/911">ticket:911</a>
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/3201">ticket:3201</a>
     */
    void changePassword(@Hidden
    String newPassword);

    /**
     * change the password for the current user by passing the old password.
     *
     * @param oldPassword
     *            Not-null. Must pass validation in the security sub-system.
     * @param newPassword
     *            Possibly null to allow logging in with no password.
     * @throws ome.conditions.SecurityViolation
     *             if the oldPassword is incorrect.
     */
    void changePasswordWithOldPassword(
        @Hidden @NotNull String oldPassword,
        @Hidden String newPassword);

    /**
     * change the password for the a given user.
     *
     * @param newPassword
     *            Not-null. Might must pass validation in the security
     *            sub-system.
     * @throws ome.conditions.SecurityViolation
     *             if the new password is too weak.
     */
    void changeUserPassword(@NotNull
    String omeName, @Hidden
    String newPassword);

    /**
     * uses JMX to refresh the login cache <em>if supported</em>. Some
     * backends may not provide refreshing. This may be called internally during
     * some other administrative tasks. The exact implementation of this depends
     * on the application server and the authentication/authorization backend.
     */
    void synchronizeLoginCache();

    // ~ Security context
    // =========================================================================

    /**
     * returns the active {@link Roles} in use by the server.
     * 
     * @return Non-null, immutable {@link Roles} instance.
     */
    Roles getSecurityRoles();

    /**
     * returns an implementation of {@link EventContext} loaded with the
     * security for the current user and thread. If called remotely, not all
     * values of {@link EventContext} will be sensible.
     * 
     * @return Non-null, immutable {@link EventContext} instance
     */
    EventContext getEventContext();
}
