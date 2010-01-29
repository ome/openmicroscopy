/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.List;
import java.util.Map;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.conditions.AuthenticationException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
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
     * Looks up all {@link ExperimenterGroups groups} present and all related
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
     * Updates an experimenter as admin. All aspects of the passed object are
     * taken into account including omeName, groups, and default group.
     * 
     * @param experimenter
     *            the Experimenter to update.
     */
    void updateExperimenter(@NotNull
    Experimenter experimenter);

    /**
     * Updates an experimenter as admin. All aspects of the passed object are
     * taken into account including omeName, groups, and default group.
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
     * Updates a group. All aspects of the passed object are taken into account
     * including group name and the included users.
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
     * @parm group group name of the default group for this user
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
     * @param newUser
     *            a new {@link Experimenter} instance
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
     * @param newGroup
     *            a new {@link ExperimenterGroup} instance. Not null.
     * @return id of the newly created {@link ExperimenterGroup}
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/1434">ticket:1434"</a>
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
     * removes a user from the given groups.
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
     * sets the owner of a group to be a given user.
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
     * checks an entity for any in-bound references and if none are present,
     * will remove the {@link Flag#LOCKED} status. This method is backend-
     * intensive and should not be used in a tight loop. Returns an array with
     * length equal to the number of instances passed in. A true value means
     * that the object is now unlocked.
     * 
     * @param iObjects
     *            a variable array argument of objects to be unlocked
     * @return an array of equal length to iObjects where a true value asserts
     *         that the instance is now unlocked in the database.
     */
    boolean[] unlock(IObject... iObjects);

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
     */
    void reportForgottenPassword(String name, String email)
            throws AuthenticationException;

    /**
     * Used after an {@link ExpiredCredentialsException} instance is thrown.
     * 
     * Does not require
     */
    void changeExpiredCredentials(String name, String oldCred, String newCred)
            throws AuthenticationException;

    /**
     * change the password for the current user
     * 
     * @param newPassword
     *            Not-null. Must pass validation in the security sub-system.
     * @throws ome.conditions.SecurityViolation
     *             if the new password is too weak.
     */
    void changePassword(@Hidden
    String newPassword);

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
