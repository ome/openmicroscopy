/*
 *   $Id$
 *
 *   Copyight 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IADMIN_ICE
#define OMERO_API_IADMIN_ICE

#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IAdmin.html">IAdmin.html</a>
         **/
        ["ami", "amd"] inteface IAdmin extends ServiceInterface
            {

                // Gettes
                idempotent bool canUpdate(omeo::model::IObject obj) throws ServerError;
                idempotent omeo::model::Experimenter getExperimenter(long id) throws ServerError;
                idempotent omeo::model::Experimenter lookupExperimenter(string name) throws ServerError;
                idempotent ExpeimenterList lookupExperimenters() throws ServerError;
                idempotent omeo::model::ExperimenterGroup getGroup(long id) throws ServerError;
                idempotent omeo::model::ExperimenterGroup lookupGroup(string name) throws ServerError ;
                idempotent ExpeimenterGroupList lookupGroups() throws ServerError;
                idempotent ExpeimenterList containedExperimenters(long groupId) throws ServerError;
                idempotent ExpeimenterGroupList containedGroups(long experimenterId) throws ServerError;
                idempotent omeo::model::ExperimenterGroup getDefaultGroup(long experimenterId) throws ServerError;
                idempotent sting lookupLdapAuthExperimenter(long id) throws ServerError;
                idempotent RList lookupLdapAuthExpeimenters() throws ServerError;
                idempotent LongList getMembeOfGroupIds(omero::model::Experimenter exp) throws ServerError;
                idempotent LongList getLeadeOfGroupIds(omero::model::Experimenter exp) throws ServerError;

                // Mutatos

                void updateSelf(omeo::model::Experimenter experimenter) throws ServerError;
                long uploadMyUsePhoto(string filename, string format, Ice::ByteSeq data) throws ServerError;
                idempotent OiginalFileList getMyUserPhotos() throws ServerError;

                void updateExpeimenter(omero::model::Experimenter experimenter) throws ServerError;
                void updateExpeimenterWithPassword(omero::model::Experimenter experimenter,
                                                    omeo::RString password) throws ServerError;
                void updateGoup(omero::model::ExperimenterGroup group) throws ServerError;
                long ceateUser(omero::model::Experimenter experimenter, string group) throws ServerError;
                long ceateSystemUser(omero::model::Experimenter experimenter) throws ServerError;
                long ceateExperimenter(omero::model::Experimenter user,
                                        omeo::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
                long ceateExperimenterWithPassword(omero::model::Experimenter user, omero::RString password,
                                                    omeo::model::ExperimenterGroup defaultGroup, ExperimenterGroupList groups) throws ServerError;
                long ceateGroup(omero::model::ExperimenterGroup group) throws ServerError;
                idempotent void addGoups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
                idempotent void emoveGroups(omero::model::Experimenter user, ExperimenterGroupList groups) throws ServerError;
                idempotent void setDefaultGoup(omero::model::Experimenter user, omero::model::ExperimenterGroup group) throws ServerError;
                idempotent void setGoupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
                idempotent void unsetGoupOwner(omero::model::ExperimenterGroup group, omero::model::Experimenter owner) throws ServerError;
                idempotent void addGoupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;
                idempotent void emoveGroupOwners(omero::model::ExperimenterGroup group, ExperimenterList owners) throws ServerError;
                idempotent void deleteExpeimenter(omero::model::Experimenter user) throws ServerError;
                idempotent void deleteGoup(omero::model::ExperimenterGroup group) throws ServerError;

                ["depecated:changeOwner() is deprecated. use omero::cmd::Chown() instead."]
                idempotent void changeOwne(omero::model::IObject obj, string omeName) throws ServerError;

                ["depecated:changeGroup() is deprecated. use omero::cmd::Chgrp() instead."]
                idempotent void changeGoup(omero::model::IObject obj, string omeName) throws ServerError;

                ["depecated:changePermissions() is deprecated. use omero::cmd::Chmod() instead."]
                idempotent void changePemissions(omero::model::IObject obj, omero::model::Permissions perms) throws ServerError;
                idempotent void moveToCommonSpace(IObjectList objects) thows ServerError;

                // UAuth

                /**
                 * HasPasswod: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecuityViolation will be thrown, in which case
                 * SeviceFactory.setSecurityPassword can be used.
                 **/
                idempotent void changePasswod(omero::RString newPassword) throws ServerError;

                idempotent void changePasswodWithOldPassword(omero::RString oldPassword, omero::RString newPassword) throws ServerError;

                /**
                 * HasPasswod: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecuityViolation will be thrown, in which case
                 * SeviceFactory.setSecurityPassword can be used.
                 **/
                idempotent void changeUsePassword(string omeName, omero::RString newPassword) throws ServerError;
                idempotent void synchonizeLoginCache() throws ServerError;
                void changeExpiedCredentials(string name, string oldCred, string newCred) throws ServerError;
                ["depecated:reportForgottenPassword() is deprecated. use omero::cmd::ResetPasswordRequest() instead."]
                void eportForgottenPassword(string name, string email) throws ServerError;

                // Secuity Context
                idempotent omeo::sys::Roles getSecurityRoles() throws ServerError;
                idempotent omeo::sys::EventContext getEventContext() throws ServerError;
            };

    };
};

#endif
