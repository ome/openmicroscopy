/*
 * ome.api.ILdap
 *
 *   Copyright 2006 - 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.List;

import ome.annotations.NotNull;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

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
public interface ILdap extends ServiceInterface {

    /**
     * Searches all {@link ome.model.meta.Experimenter} list on LDAP for
     * attribute objectClass = person.
     *
     * @return all Experimenter list.
     */
    List<Experimenter> searchAll();

    /**
     * Searches Distinguished Name - {@link java.lang.String} in groups
     *
     * @param attr
     *            - String name of member attribute. Never null or empty.
     * @param value
     *            - user's DN which should be set on value for attribute. Never
     *            null or empty.
     * @return List of groups which contains DN.
     */
    List<String> searchDnInGroups(@NotNull
    String attr, @NotNull
    String value);

    /**
     * Searches all {@link ome.model.meta.Experimenter} in LDAP for specified
     * attribute
     *
     * @param dn
     *            - Distinguished Name - {@link java.lang.String} base for
     *            search. Never null, should be
     *            {@link net.sf.ldaptemplate.support.DistinguishedName#EMPTY_PATH}
     *            .
     * @param attr
     *            - String name of attribute. Never null or empty.
     * @param value
     *            - String expected value of attribute. Never null or empty.
     * @return List of Experimenters.
     */
    List<Experimenter> searchByAttribute(@NotNull
    String dn, @NotNull
    String attribute, @NotNull
    String value);

    /**
     * Searches all {@link ome.model.meta.Experimenter} in LDAP for specified
     * attributes. Attributes should be specified in String [] and their values
     * should be set in equivalent String [].
     *
     * @param dn
     *            - {@link net.sf.ldaptemplate.support.DistinguishedName} base
     *            for search. Never null, should be
     *            {@link net.sf.ldaptemplate.support.DistinguishedName#EMPTY_PATH}
     *            .
     * @param attr
     *            - String [] name of attribute. Never null or empty.
     * @param value
     *            - String [] expected value of attribute. Never null or empty.
     * @return List of Experimenters.
     */
    List<Experimenter> searchByAttributes(@NotNull
    String dn, @NotNull
    String[] attributes, @NotNull
    String[] values);

    /**
     * Searches one {@link ome.model.meta.Experimenter} in LDAP for specified
     * Distinguished Name - {@link java.lang.String}
     *
     * @param userdn
     *            unique Distinguished Name - {@link java.lang.String} of user,
     *            Never null or empty.
     * @return an Experimenter.
     */
    Experimenter searchByDN(@NotNull
    String userdn);

    /**
     * Searches unique Distinguished Name - {@link java.lang.String} in LDAP for
     * Common Name equals username. Common Name should be unique under the
     * specified base. If list of cn's contains more then one DN will return
     * exception.
     *
     * @param username
     *            Name of the Experimenter equals CommonName.
     * @return an String Distinguished Name. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if more then one 'cn' under the specified base.
     */
    String findDN(@NotNull
    String username);

    /**
     * Looks up the DN for a group.
     *
     * @return an String Distinguished Name. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if more then one 'cn' under the specified base.
     */
    String findGroupDN(@NotNull
    String groupname);

    /**
     * Searches Experimenter by unique Distinguished Name -
     * {@link java.lang.String} in LDAP for Common Name equals username. Common
     * Name should be unique under the specified base. If list of cn's contains
     * more then one DN will return exception.
     *
     * @param username
     *            Name of the Experimenter equals CommonName.
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if more then one 'cn' under the specified base.
     */
    Experimenter findExperimenter(@NotNull
    String username);

    /**
     * Enables or disables LDAP login for the specified
     * {@link ome.model.meta.Experimenter} ID by setting the value of the
     * <code>ldap</code> property.
     *
     * @param experimenterID
     *            ID of the Experimenter.
     * @param isLdap
     *            true if {@link ome.model.meta.Experimenter} has an LDAP
     *            account, false otherwise.
     * @return an Experimenter. Never null.
     */
    void setDN(long experimenterID, boolean isLdap);

    /**
     * Gets config value from properties.
     *
     * @return boolean
     */
    boolean getSetting();

    /**
     * Creates an {@link ome.model.meta.Experimenter} entry in the OMERO DB
     * based on the supplied LDAP username.
     * @param username
     * @return created Experimenter or null
     */
    Experimenter createUser(@NotNull String username);

    /**
     * Discovers and lists {@link ome.model.meta.Experimenter}s who are present
     * in the remote LDAP server and in the local DB but have the
     * <code>ldap</code> property set to <code>false</code>.
     *
     * @return list of Experimenters.
     */
     List<Experimenter> discover();
}
