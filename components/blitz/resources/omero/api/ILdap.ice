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
        /**
         * Administration interface providing access to admin-only
         * functionality as well as JMX-based server access and selected user
         * functions. Most methods require membership in privileged
         * {@link omero.model.ExperimenterGroup} groups.
         *
         * Methods which return {@link omero.model.Experimenter} or
         * {@link omero.model.ExperimenterGroup} instances fetch and load all
         * related instances of {@link omero.model.ExperimenterGroup} or
         * {@link omero.model.Experimenter}, respectively.
         */
        ["ami", "amd"] interface ILdap extends ServiceInterface
            {
                /**
                 * Searches all {@link omero.model.Experimenter} list on LDAP
                 * for attribute objectClass = person.
                 *
                 * @return all Experimenter list.
                 */
                idempotent ExperimenterList searchAll() throws ServerError;

                /**
                 * Searches Distinguished Name in groups.
                 *
                 * @param attr   Name of member attribute. Never null or empty.
                 * @param value  User's DN which should be set on value for
                 *               attribute. Never null or empty.
                 * @return       List of groups which contains DN.
                 */
                idempotent StringSet searchDnInGroups(string attr, string value) throws ServerError;

                /**
                 * Searches all {@link omero.model.Experimenter} in LDAP for
                 * specified attribute.
                 *
                 * @param dn        Distinguished Name base for search. Never
                 *                  null.
                 * @param attribute Name of attribute. Never null or empty.
                 * @param value     Expected value of attribute. Never null or
                 *                  empty.
                 * @return          List of Experimenters.
                 */
                idempotent ExperimenterList searchByAttribute(string dn, string attribute, string value) throws ServerError;

                /**
                 * Searches all {@link omero.model.Experimenter} in LDAP for
                 * specified attributes. Attributes should be specified in
                 * StringSet and their values should be set in equivalent
                 * StringSet.
                 *
                 * @param dn         Distinguished Name base for search. Never
                 *                   null.
                 * @param attributes Name of attribute. Never null or empty.
                 * @param values     Expected value of attribute. Never null
                 *                   or empty.
                 * @return           List of Experimenters.
                 */
                idempotent ExperimenterList searchByAttributes(string dn, StringSet attributes, StringSet values) throws ServerError;

                /**
                 * Searches one {@link omero.model.Experimenter} in LDAP for
                 * specified Distinguished Name.
                 *
                 * @param userdn
                 *            unique Distinguished Name - string of user,
                 *            Never null or empty.
                 * @return an Experimenter.
                 */
                idempotent omero::model::Experimenter searchByDN(string userdn) throws ServerError;

                /**
                 * Searches unique Distinguished Name - string in LDAP for
                 * Common Name equals username. Common Name should be unique
                 * under the specified base. If list of cn's contains more
                 * then one DN will return exception.
                 *
                 * @param username
                 *            Name of the Experimenter equals CommonName.
                 * @return a Distinguished Name. Never null.
                 * @throws ApiUsageException
                 *             if more then one 'cn' under the specified base.
                 */
                idempotent string findDN(string username) throws ServerError;

                /**
                 * Looks up the DN for a group.
                 *
                 * @return a Distinguished Name. Never null.
                 * @throws ApiUsageException
                 *             if more then one 'cn' under the specified base.
                 */
                idempotent string findGroupDN(string groupname) throws ServerError;

                /**
                 * Searches Experimenter by unique Distinguished Name -
                 * string in LDAP for Common Name equals username. Common
                 * Name should be unique under the specified base. If list of
                 * cn's contains more then one DN will return exception.
                 *
                 * @param username
                 *            Name of the Experimenter equals CommonName.
                 * @return an Experimenter. Never null.
                 * @throws ApiUsageException
                 *             if more then one 'cn' under the specified base.
                 */
                idempotent omero::model::Experimenter findExperimenter(string username) throws ServerError;

                /**
                 * Looks up a specific {@link omero.model.ExperimenterGroup}
                 * in LDAP using the provided group name. It is expected that
                 * the group name will be unique in the searched LDAP base
                 * tree. If more than one group with the specified name has
                 * been found, an exception will be thrown.
                 *
                 * @param groupname
                 * @return an ExperimenterGroup. Never <code>null</null>.
                 * @throws ApiUsageException
                 *             if more then one group name matches under the
                 *             specified base.
                 */
                idempotent omero::model::ExperimenterGroup findGroup(string groupname) throws ServerError;

                ["deprecated:setDN() is deprecated. Set the LDAP flag on model objects instead."]
                idempotent void setDN(omero::RLong experimenterID, string dn) throws ServerError;

                /**
                 * Gets config value from properties.
                 *
                 * @return boolean
                 */
                idempotent bool getSetting() throws ServerError;

                /**
                 * Discovers and lists {@link omero.model.Experimenter}s who
                 * are present in the remote LDAP server and in the local DB
                 * but have the <code>ldap</code> property set to
                 * <code>false</code>.
                 *
                 * @return list of Experimenters.
                 */
                idempotent ExperimenterList discover() throws ServerError;

                /**
                * Discovers and lists {@link omero.model.ExperimenterGroup}s
                * which are present in the remote LDAP server and in the local
                * DB but have the <code>ldap</code> property set to
                * <code>false</code>.
                *
                * @return list of ExperimenterGroups.
                */
                idempotent ExperimenterGroupList discoverGroups() throws ServerError;

                /**
                 * Creates an {@link omero.model.Experimenter} entry in the
                 * OMERO DB based on the supplied LDAP username.
                 * @param username
                 * @return created Experimenter or null
                 */
                omero::model::Experimenter createUser(string username) throws ServerError;
            };

    };
};

#endif
