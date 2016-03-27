/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IADMIN_ICE
#define OMERO_API_IADMIN_ICE

#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Administration interface providing access to admin-only
         * functionality as well as JMX-based server access and selected user
         * functions. Most methods require membership in privileged
         * {@link omero.model.ExperimenterGroup groups}.
         *
         * Methods which return {@link omero.model.Experimenter} or
         * {@link omero.model.ExperimenterGroup} instances fetch and load all
         * related instances of {@link omero.model.ExperimenterGroup} or
         * {@link omero.model.Experimenter}, respectively.
         *
         **/
        ["ami", "amd"] interface IAdmin extends ServiceInterface
            {

                // Getters
                /**
                 * Returns true if the currently logged in user can modify the
                 * given {@link omero.model.IObject}. This uses the same logic
                 * that would be applied during a Hibernate flush to the
                 * database.
                 */
                idempotent bool canUpdate(omero::model::IObject obj) throws ServerError;

                /**
                 * Fetches an {@link omero.model.Experimenter} and all related
                 * {@link omero.model.ExperimenterGroup}.
                 *
                 * @param id id of the Experimenter
                 * @return an Experimenter. Never null.
                 * @throws ApiUsageException if id does not exist.
                 */

                idempotent omero::model::Experimenter getExperimenter(long id) throws ServerError;
                /**
                 * Looks up an {@link omero.model.Experimenter} and all related
                 * {@link omero.model.ExperimenterGroup} by name.
                 *
                 * @param omeName Name of the Experimenter
                 * @return an Experimenter. Never null.
                 * @throws ApiUsageException if omeName does not exist.
                 */

                idempotent omero::model::Experimenter lookupExperimenter(string name) throws ServerError;
                /**
                 * Looks up all {@link omero.model.Experimenter} experimenters
                 * present and all related
                 * {@link omero.model.ExperimenterGroup} groups.
                 *
                 * @return all Experimenters. Never null.
                 */
                idempotent ExperimenterList lookupExperimenters() throws ServerError;
                /**
                 * Fetches an {@link omero.model.ExperimenterGroup} and all
                 * contained {@link omero.model.Experimenter} users.
                 *
                 * @param id id of the ExperimenterGroup
                 * @return an ExperimenterGroup. Never null.
                 * @throws ApiUsageException if id does not exist.
                 */
                idempotent omero::model::ExperimenterGroup getGroup(long id) throws ServerError;
                /**
                 * Looks up an {@link omero.model.ExperimenterGroup} and all
                 * contained {@link omero.model.Experimenter} users by name.
                 *
                 * @param groupName Name of the ExperimenterGroup
                 * @return an ExperimenterGroup. Never null.
                 * @throws ApiUsageException if groupName does not exist.
                 */
                idempotent omero::model::ExperimenterGroup lookupGroup(string name) throws ServerError ;

                /**
                 * Looks up all {@link omero.model.ExperimenterGroup} groups
                 * present and all related
                 * {@link omero.model.Experimenter} experimenters. The
                 * experimenter's groups are also loaded.
                 *
                 * @return all Groups. Never null.
                 */
                idempotent ExperimenterGroupList lookupGroups() throws ServerError;
                /**
                 * Fetches all {@link omero.model.Experimenter} users
                 * contained in this group. The returned users will have all
                 * fields filled in and all collections unloaded.
                 *
                 * @param groupId id of the ExperimenterGroup
                 * @return non-null array of all
                 * {@link omero.model.Experimenter} users in this group.
                 */
                idempotent ExperimenterList containedExperimenters(long groupId) throws ServerError;

                /**
                 * Fetches all {@link omero.model.ExperimenterGroup} groups of
                 * which the given user is a member. The returned groups will
                 * have all fields filled in and all collections unloaded.
                 *
                 * @param experimenterId id of the Experimenter. Not null.
                 * @return non-null array of all
                 * {@link omero.model.ExperimenterGroup} groups for this user.
                 */
                idempotent ExperimenterGroupList containedGroups(long experimenterId) throws ServerError;

                /**
                 * Retrieves the default {@link omero.model.ExperimenterGroup}
                 * group for the given user id.
                 *
                 * @param experimenterId of the Experimenter. Not null.
                 * @return non-null {@link omero.model.ExperimenterGroup}. If
                 *         no default group is found, an exception will be
                 *         thrown.
                 */
                idempotent omero::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;

                /**
                 * Looks up {@link omero.model.Experimenter} experimenters who
                 * use LDAP authentication  (has set dn on password table).
                 *
                 * @return Experimenter. Never null.
                 */
                idempotent string lookupLdapAuthExperimenter(long id) throws ServerError;

                /**
                 * Looks up all ids of {@link omero.model.Experimenter}
                 * experimenters who use LDAP authentication (has set dn on
                 * password table).
                 *
                 * @return list of experimenters. Never null.
                 */
                idempotent RList lookupLdapAuthExperimenters() throws ServerError;

                /**
                 * Finds the ids for all groups for which the given
                 * {@link omero.model.Experimenter} is a member.
                 *
                 * @param e Non-null, managed (i.e. with id)
                 * @see omero.model.Details#getOwner
                 */
                idempotent LongList getMemberOfGroupIds(omero::model::Experimenter exp) throws ServerError;

                /**
                 * Finds the ids for all groups for which the given
                 * {@link omero.model.Experimenter} is owner/leader.
                 *
                 * @param e Non-null, managed (i.e. with id)
                 * @see omero.model.Details#getOwner
                 */
                idempotent LongList getLeaderOfGroupIds(omero::model::Experimenter exp) throws ServerError;

                // Mutators

                /**
                 * Allows a user to update his/her own information. This is
                 * limited to the fields on Experimenter, all other fields
                 * (groups, etc.) are ignored. The experimenter argument need
                 * not have the proper id nor the proper omeName (which is
                 * immutable). To change the users default group (which is the
                 * only other customizable option), use
                 * {@link #setDefaultGroup}
                 *
                 * @see #setDefaultGroup
                 * @param experimenter A data transfer object. Only the fields:
                 *        firstName, middleName, lastName, email, and
                 *        institution are checked. Not null.
                 */
                void updateSelf(omero::model::Experimenter experimenter) throws ServerError;

                /**
                 * Uploads a photo for the user which will be displayed on
                 * his/her profile.
                 * This photo will be saved as an
                 * {@link omero.model.OriginalFile} object with the given
                 * format, and attached to the user's
                 * {@link omero.model.Experimenter} object via an
                 * {@link omero.model.FileAnnotation} with
                 * the namespace:
                 *  "openmicroscopy.org/omero/experimenter/photo"
                 * (NSEXPERIMENTERPHOTO).
                 * If such an {@link omero.model.OriginalFile} instance
                 * already exists, it will be overwritten. If more than one
                 * photo is present, the oldest version will be modified (i.e.
                 * the highest updateEvent id).
                 *
                 * Note: as outlined in ticket:1794, this photo will be placed
                 * in the "user" group and therefore will be visible to
                 * everyone on the system.
                 *
                 * @param filename Not null. String name which will be used.
                 * @param format Not null. Format.value string. 'image/jpeg'
                 *        and 'image/png' are common values.
                 * @param data Not null. Data from the image. This will be
                 *        written to disk.
                 * @return the id of the overwritten or newly created user
                 *         photo OriginalFile object.
                 */
                long uploadMyUserPhoto(string filename, string format, Ice::ByteSeq data) throws ServerError;

                /**
                 * Retrieves the {@link omero.model.OriginalFile} object
                 * attached to this user as specified by
                 * {@link #uploadMyUserPhoto}.
                 * The return value is order by the most recently modified
                 * file first.
                 *
                 * @return file objects. Possibly empty.
                 */
                idempotent OriginalFileList getMyUserPhotos() throws ServerError;

                /**
                 * Updates an experimenter if admin or owner of group. Only
                 * string fields on the object are taken into account.
                 * The root and guest experimenters may not be renamed.
                 *
                 * Before a SecurityViolation would be thrown, however, this
                 * method will pass to {@link #updateSelf} <em>if</em> the
                 * current user matches the given experimenter.
                 *
                 * @param experimenter the Experimenter to update.
                 */
                void updateExperimenter(omero::model::Experimenter experimenter) throws ServerError;

                /**
                 * Updates an experimenter if admin or owner of group.
                 * Only string fields on the object are taken into account.
                 * The root and guest experimenters may not be renamed.
                 *
                 * @param experimenter the Experimenter to update.
                 * @param password Not-null. Must pass validation in the
                 *        security sub-system.
                 */
                void updateExperimenterWithPassword(omero::model::Experimenter experimenter,
                                                    omero::RString password) throws ServerError;

                /**
                 * Updates an experimenter group if admin or owner of group.
                 * Only string fields on the object are taken into account.
                 * The root, system and guest groups may not be renamed,
                 * nor may the user's current group.
                 *
                 * @param group the ExperimenterGroup to update.
                 */
                void updateGroup(omero::model::ExperimenterGroup group) throws ServerError;

                /**
                 * Creates and returns a new user. This user will be created
                 * with the default group specified.
                 *
                 * @param newUser a new {@link omero.model.Experimenter}
                 *        instance
                 * @param group group name of the default group for this user
                 * @return id of the newly created
                 * {@link omero.model.Experimenter}
                 */
                long createUser(omero::model::Experimenter experimenter, string group) throws ServerError;

                /**
                 * Creates and returns a new system user. This user will be
                 * created with the "System" (administration) group as default
                 * and will also be in the "user" group.
                 *
                 * @param newSystemUser a new {@link omero.model.Experimenter}
                 *        instance
                 * @return id of the newly created
                 *         {@link omero.model.Experimenter}
                 */
                long createSystemUser(omero::model::Experimenter experimenter) throws ServerError;

                /**
                 * Creates and returns a new user in the given groups.
                 *
                 * @param experimenter A new {@link omero.model.Experimenter}
                 *        instance. Not null.
                 * @param defaultGroup Instance of
                 *        {@link omero.model.ExperimenterGroup}. Not null.
                 * @param otherGroups Array of
                 *        {@link omero.model.ExperimenterGroup} instances. Can
                 *        be null.
                 * @return id of the newly created
                 *         {@link omero.model.Experimenter} Not null.
                 */
                long createExperimenter(omero::model::Experimenter user,
                                        omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;

                /**
                 * Creates and returns a new user in the given groups with
                 * password.
                 *
                 * @param experimenter A new {@link omero.model.Experimenter}
                 *        instance. Not null.
                 * @param password Not-null. Must pass validation in the
                 *        security sub-system.
                 * @param defaultGroup Instance of
                 *        {@link omero.model.ExperimenterGroup}. Not null.
                 * @param otherGroups Array of
                 *        {@link omero.model.ExperimenterGroup} instances. Can
                 *        be null.
                 * @return id of the newly created
                 *         {@link omero.model.Experimenter} Not null.
                 * @throws SecurityViolation if the new password is too weak.
                 */
                long createExperimenterWithPassword(omero::model::Experimenter user, omero::RString password,
                                                    omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;

                /**
                 * Creates and returns a new group. The
                 * {@link omero.model.Details#setPermissions} method should be
                 * called on the instance which is passed. The given
                 * {@link omero.model.Permissions} will become the default for
                 * all objects created while logged into this group, possibly
                 * modified by the user's umask settings.
                 * If no permissions is set, the default will be
                 * {@link omero.model.Permissions#USER_PRIVATE},
                 * i.e. a group in which no user can see the other group
                 * member's data.
                 *
                 * @param group  a new
                 * {@link omero.model.ExperimenterGroup} instance. Not null.
                 * @return id of the newly created {@link ExperimenterGroup}
                 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/1434">ticket:1434"</a>
                 */
                long createGroup(omero::model::ExperimenterGroup group) throws ServerError;

                /**
                 * Adds a user to the given groups.
                 *
                 * @param user A currently managed entity. Not null.
                 * @param groups Groups to which the user will be added. Not
                 *        null.
                 */
                idempotent void addGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;

                /**
                 * Removes an experimenter from the given groups.
                 * <ul>
                 * <li>The root experimenter is required to be in both the
                 * user and system groups.</li>
                 * <li>An experimenter may not remove themself from the user
                 * or system group.</li>
                 * <li>An experimenter may not be a member of only the user
                 * group, some other group is also required as the default
                 * group.</li>
                 * <li>An experimenter must remain a member of some group.</li>
                 * </ul>
                 *
                 * @param user A currently managed entity. Not null.
                 * @param groups Groups from which the user will be removed.
                 *        Not null.
                 */
                idempotent void removeGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;

                /**
                 * Sets the default group for a given user.
                 *
                 * @param user A currently managed
                 *        {@link omero.model.Experimenter}. Not null.
                 * @param group The group which should be set as default group
                 *        for this user. Not null.
                 */
                idempotent void setDefaultGroup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;

                /**
                 * Adds the user to the owner list for this group.
                 *
                 * Since <a href="https://trac.openmicroscopy.org/ome/ticket/1434">Beta 4.2</a>
                 * multiple users can be the "owner" of a group.
                 *
                 * @param group A currently managed
                 *        {@link omero.model.ExperimenterGroup}. Not null.
                 * @param owner A currently managed
                 *        {@link omero.model.Experimenter}. Not null.
                 */
                idempotent void setGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;

                /**
                 * Removes the user from the owner list for this group.
                 *
                 * Since <a href="https://trac.openmicroscopy.org/ome/ticket/1434">Beta 4.2</a>
                 * multiple users can be the "owner" of a group.
                 *
                 * @param group A currently managed
                 *        {@link omero.model.ExperimenterGroup}. Not null.
                 * @param owner A currently managed
                 *        {@link omero.model.Experimenter}. Not null.
                 */
                idempotent void unsetGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;

                /**
                 * Adds the given users to the owner list for this group.
                 *
                 * @param group A currently managed
                 *        {@link omero.model.ExperimenterGroup}. Not null.
                 * @param owners A set of currently managed
                 *        {@link omero.model.Experimenter}s. Not null.
                 */
                idempotent void addGroupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;

                /**
                 * removes the given users from the owner list for this group.
                 *
                 * @param group A currently managed
                 *        {@link omero.model.ExperimenterGroup}. Not
                 * @param owners A set of currently managed
                 *        {@link omero.model.Experimenter}s. Not null.
                 */
                idempotent void removeGroupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;

                /**
                 * Removes a user by removing the password information for
                 * that user as well as all
                 * {@link omero.model.GroupExperimenterMap} instances.
                 *
                 * @param user Experimenter to be deleted. Not null.
                 */
                idempotent void deleteExperimenter(omero::model::Experimenter user) throws ServerError;

                /**
                 * Removes a group by first removing all users in the group,
                 * and then deleting the actual
                 * {@link omero.model.ExperimenterGroup} instance.
                 *
                 * @param group {@link omero.model.ExperimenterGroup} to be
                 *        deleted. Not null.
                 */
                idempotent void deleteGroup(omero::model::ExperimenterGroup group) throws ServerError;

                ["deprecated:changeOwner() is deprecated. use omero::cmd::Chown2() instead."]
                idempotent void changeOwner(omero::model::IObject obj, string omeName) throws ServerError;

                ["deprecated:changeGroup() is deprecated. use omero::cmd::Chgrp2() instead."]
                idempotent void changeGroup(omero::model::IObject obj, string omeName) throws ServerError;

                ["deprecated:changePermissions() is deprecated. use omero::cmd::Chmod2() instead."]
                idempotent void changePermissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;

                /**
                 * Moves the given objects into the "user" group to make them
                 * visible and linkable from all security contexts.
                 *
                 * @param iObjects
                 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/1794">ticket 1794</a>
                 */
                idempotent void moveToCommonSpace(IObjectList objects) throws ServerError;

                // UAuth

                /**
                 * Changes the password for the current user.
                 * <p>
                 * <em>Warning:</em>This method requires the user to be
                 * authenticated with a password and not with a one-time
                 * session id. To avoid this problem, use
                 * {@link #changePasswordWithOldPassword}.
                 * </p>
                 *
                 * @param newPassword Possibly null to allow logging in with
                 *        no password.
                 * @throws SecurityViolation if the user is not authenticated
                 *         with a password.
                 * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/911">ticket:911</a>
                 * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/3201">ticket:3201</a>
                 */
                idempotent void changePassword(omero::RString newPassword) throws ServerError;

                idempotent void changePasswordWithOldPassword(omero::RString oldPassword, omero::RString newPassword) throws ServerError;

                /**
                 * Changes the password for the a given user.
                 *
                 * @param newPassword Not-null. Might must pass validation in
                 *        the security sub-system.
                 * @throws SecurityViolation if the new password is too weak.
                 */
                idempotent void changeUserPassword(string omeName, omero::RString newPassword) throws ServerError;

                /**
                 * Uses JMX to refresh the login cache <em>if supported</em>.
                 * Some backends may not provide refreshing. This may be
                 * called internally during some other administrative tasks.
                 * The exact implementation of this depends on the application
                 * server and the authentication/authorization backend.
                 */
                idempotent void synchronizeLoginCache() throws ServerError;

                /**
                 * Used after an {@link omero.ExpiredCredentialException}
                 * instance is thrown.
                 */
                void changeExpiredCredentials(string name, string oldCred, string newCred) throws ServerError;

                ["deprecated:reportForgottenPassword() is deprecated. use omero::cmd::ResetPasswordRequest() instead."]
                void reportForgottenPassword(string name, string email) throws ServerError;

                // Security Context
                /**
                 * Returns the active {@link omero.sys.Roles} in use by the
                 * server.
                 *
                 * @return Non-null, immutable {@link omero.sys.Roles}
                 *         instance.
                 */
                idempotent omero::sys::Roles getSecurityRoles() throws ServerError;

                /**
                 * Returns an implementation of {@link omero.sys.EventContext}
                 * loaded with the security for the current user and thread.
                 * If called remotely, not all values of
                 * {@link omero.sys.EventContext} will be sensible.
                 *
                 * @return Non-null, immutable {@link omero.sys.EventContext}
                 *         instance
                 */
                idempotent omero::sys::EventContext getEventContext() throws ServerError;
            };

    };
};

#endif
