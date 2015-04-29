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
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.1/api/ome/api/IAdmin.html">IAdmin.html</a>
         **/
        ["ami", "amd"] interface IAdmin extends ServiceInterface
            {

                // Getters
                idempotent bool canUpdate(omero::model::IObject obj) throws ServerError;
                idempotent omero::model::Experimenter getExperimenter(long id) throws ServerError;
                idempotent omero::model::Experimenter lookupExperimenter(string name) throws ServerError;
                idempotent ExperimenterList lookupExperimenters() throws ServerError;
                idempotent omero::model::ExperimenterGroup getGroup(long id) throws ServerError;
                idempotent omero::model::ExperimenterGroup lookupGroup(string name) throws ServerError ;
                idempotent ExperimenterGroupList lookupGroups() throws ServerError;
                idempotent ExperimenterList containedExperimenters(long groupId) throws ServerError;
                idempotent ExperimenterGroupList containedGroups(long experimenterId) throws ServerError;
                idempotent omero::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;
                idempotent string lookupLdapAuthExperimenter(long id) throws ServerError;
                idempotent RList lookupLdapAuthExperimenters() throws ServerError;
                idempotent LongList getMemberOfGroupIds(omero::model::Experimenter exp) throws ServerError;
                idempotent LongList getLeaderOfGroupIds(omero::model::Experimenter exp) throws ServerError;

                // Mutators

                void updateSelf(omero::model::Experimenter experimenter) throws ServerError;
                long uploadMyUserPhoto(string filename, string format, Ice::ByteSeq data) throws ServerError;
                idempotent OriginalFileList getMyUserPhotos() throws ServerError;

                void updateExperimenter(omero::model::Experimenter experimenter) throws ServerError;
                void updateExperimenterWithPassword(omero::model::Experimenter experimenter,
                                                    omero::RString password) throws ServerError;
                void updateGroup(omero::model::ExperimenterGroup group) throws ServerError;
                long createUser(omero::model::Experimenter experimenter, string group) throws ServerError;
                long createSystemUser(omero::model::Experimenter experimenter) throws ServerError;
                long createExperimenter(omero::model::Experimenter user,
                                        omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
                long createExperimenterWithPassword(omero::model::Experimenter user, omero::RString password,
                                                    omero::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
                long createGroup(omero::model::ExperimenterGroup group) throws ServerError;
                idempotent void addGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
                idempotent void removeGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
                idempotent void setDefaultGroup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;
                idempotent void setGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
                idempotent void unsetGroupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
                idempotent void addGroupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;
                idempotent void removeGroupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;
                idempotent void deleteExperimenter(omero::model::Experimenter user) throws ServerError;
                idempotent void deleteGroup(omero::model::ExperimenterGroup group) throws ServerError;

                ["deprecated:changeOwner() is deprecated. use omero::cmd::Chown2() instead."]
                idempotent void changeOwner(omero::model::IObject obj, string omeName) throws ServerError;

                ["deprecated:changeGroup() is deprecated. use omero::cmd::Chgrp2() instead."]
                idempotent void changeGroup(omero::model::IObject obj, string omeName) throws ServerError;

                ["deprecated:changePermissions() is deprecated. use omero::cmd::Chmod() instead."]
                idempotent void changePermissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;
                idempotent void moveToCommonSpace(IObjectList objects) throws ServerError;

                // UAuth

                /**
                 * HasPassword: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecurityViolation will be thrown, in which case
                 * ServiceFactory.setSecurityPassword can be used.
                 **/
                idempotent void changePassword(omero::RString newPassword) throws ServerError;

                idempotent void changePasswordWithOldPassword(omero::RString oldPassword, omero::RString newPassword) throws ServerError;

                /**
                 * HasPassword: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecurityViolation will be thrown, in which case
                 * ServiceFactory.setSecurityPassword can be used.
                 **/
                idempotent void changeUserPassword(string omeName, omero::RString newPassword) throws ServerError;
                idempotent void synchronizeLoginCache() throws ServerError;
                void changeExpiredCredentials(string name, string oldCred, string newCred) throws ServerError;
                ["deprecated:reportForgottenPassword() is deprecated. use omero::cmd::ResetPasswordRequest() instead."]
                void reportForgottenPassword(string name, string email) throws ServerError;

                // Security Context
                idempotent omero::sys::Roles getSecurityRoles() throws ServerError;
                idempotent omero::sys::EventContext getEventContext() throws ServerError;
            };

    };
};

#endif
