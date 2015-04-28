/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ILDAP_ICE
#define OMERO_API_ILDAP_ICE

#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        ["ami", "amd"] interface ILdap extends ServiceInterface
            {
                idempotent ExperimenterList searchAll() throws ServerError;
                idempotent StringSet searchDnInGroups(string attr, string value) throws ServerError;
                idempotent ExperimenterList searchByAttribute(string dn, string attribute, string value) throws ServerError;
                idempotent ExperimenterList searchByAttributes(string dn, StringSet attributes, StringSet values) throws ServerError;
                idempotent omero::model::Experimenter searchByDN(string userdn) throws ServerError;
                idempotent string findDN(string username) throws ServerError;
                idempotent string findGroupDN(string groupname) throws ServerError;
                idempotent omero::model::Experimenter findExperimenter(string username) throws ServerError;
                idempotent omero::model::ExperimenterGroup findGroup(string groupname) throws ServerError;

                ["deprecated:setDN() is deprecated. Set the LDAP flag on model objects instead."]
                idempotent void setDN(omero::RLong experimenterID, string dn) throws ServerError;

                idempotent bool getSetting() throws ServerError;
                idempotent ExperimenterList discover() throws ServerError;
                idempotent ExperimenterGroupList discoverGroups() throws ServerError;
                omero::model::Experimenter createUser(string username) throws ServerError;
            };

    };
};

#endif
