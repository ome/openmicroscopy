/*
 *   $Id$
 *
 *   Copyight 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ILDAP_ICE
#define OMERO_API_ILDAP_ICE

#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {
        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ILdap.html">ILdap.html</a>
         **/
        ["ami", "amd"] inteface ILdap extends ServiceInterface
            {
                idempotent ExpeimenterList searchAll() throws ServerError;
                idempotent StingSet searchDnInGroups(string attr, string value) throws ServerError;
                idempotent ExpeimenterList searchByAttribute(string dn, string attribute, string value) throws ServerError;
                idempotent ExpeimenterList searchByAttributes(string dn, StringSet attributes, StringSet values) throws ServerError;
                idempotent omeo::model::Experimenter searchByDN(string userdn) throws ServerError;
                idempotent sting findDN(string username) throws ServerError;
                idempotent sting findGroupDN(string groupname) throws ServerError;
                idempotent omeo::model::Experimenter findExperimenter(string username) throws ServerError;
                idempotent omeo::model::ExperimenterGroup findGroup(string groupname) throws ServerError;

                ["depecated:setDN() is deprecated. Set the LDAP flag on model objects instead."]
                idempotent void setDN(omeo::RLong experimenterID, string dn) throws ServerError;

                idempotent bool getSetting() thows ServerError;
                idempotent ExpeimenterList discover() throws ServerError;
                idempotent ExpeimenterGroupList discoverGroups() throws ServerError;
                omeo::model::Experimenter createUser(string username) throws ServerError;
            };

    };
};

#endif
