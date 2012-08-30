/*
 * ome.api.ILdap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports
import java.util.List;
import javax.naming.directory.Attributes;

// Application-internal dependencies
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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1552 $ $Date:
 *          2007-05-23 09:43:33 +0100 (Wed, 23 May 2007) $) </small>
 * @since OME3.0
 */
public interface ILdap extends ServiceInterface {

	// ~ Getting users from Ldap
	// =========================================================================

	/**
	 * Searchs all {@link ome.model.meta.Experimenter} list on LDAP for
	 * attribute objectClass = person.
	 * 
	 * @return all Experimenter list.
	 */
	List<Experimenter> searchAll();

	/**
	 * Searchs Distinguished Name - {@link java.lang.String} in groups
	 * 
	 * @param attr -
	 *            String name of memeber attribute. Never null or empty.
	 * @param value -
	 *            user's DN which should be set on value for attribute. Never
	 *            null or empty.
	 * @return List of groups which contains DN.
	 */
	List<String> searchDnInGroups(@NotNull
	String attr, @NotNull
	String value);

	/**
	 * Searchs all {@link ome.model.meta.Experimenter} in LDAP for specyfied
	 * attribute
	 * 
	 * @param dn -
	 *            Distinguished Name - {@link java.lang.String} base for
	 *            search. Never null, should be
	 *            {@link net.sf.ldaptemplate.support.DistinguishedName#EMPTY_PATH}.
	 * @param attr -
	 *            String name of attribute. Never null or empty.
	 * @param value -
	 *            String expected value of attribute. Never null or empty.
	 * @return List of Experimenters.
	 */
	List<Experimenter> searchByAttribute(@NotNull
	String dn, @NotNull
	String attribute, @NotNull
	String value);

	/**
	 * Searchs all {@link ome.model.meta.Experimenter} in LDAP for specyfied
	 * attributes. Attributes should be specyfied in String [] and their values
	 * should be set in equivalets String [].
	 * 
	 * @param dn -
	 *            {@link net.sf.ldaptemplate.support.DistinguishedName} base for
	 *            search. Never null, should be
	 *            {@link net.sf.ldaptemplate.support.DistinguishedName#EMPTY_PATH}.
	 * @param attr -
	 *            String [] name of attribute. Never null or empty.
	 * @param value -
	 *            String [] expected value of attribute. Never null or empty.
	 * @return List of Experimenters.
	 */
	List<Experimenter> searchByAttributes(@NotNull
	String dn, @NotNull
	String[] attributes, @NotNull
	String[] values);

	/**
	 * Searchs one {@link ome.model.meta.Experimenter} in LDAP for specyfied
	 * Distinguished Name - {@link java.lang.String}
	 * 
	 * @param userdn
	 *            unique Distinguished Name - {@link java.lang.String}
	 *            of user, Never null or empty.
	 * @return an Experimenter.
	 */
	Experimenter searchByDN(@NotNull
	String userdn);

	/**
	 * Searches for Distinguished Names in LDAP which match the passed
	 * user or group name. If list of cn's contains more then one DN will
	 * return exception.
	 *
	 * By default this method searches for user names. However if the value
	 * is prefixed with either "user:" or "group:" then that string will
	 * be stripped off and will determine the search method to use.
	 * 
	 * @param userOrGroupName
	 *            Name of the Experimenter or ExperimenterGroup.
	 * @return an String Distinguished Name. Never null.
	 * @throws ome.conditions.ApiUsageException
	 *             if more or less one 'cn' under the specified base.
	 */
	String findDN(@NotNull
	String userOrGroupName);

    /**
     * Searchs Experimenter by unique Distinguished Name - {@link java.lang.String} in
     * LDAP for Common Name equals username. Common Name should be unique under
     * the specified base. If list of cn's contains more then one DN will return
     * exception.
     * 
     * @param username
     *            Name of the Experimenter equals CommonName.
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException
     *             if more then one 'cn' under the specified base.
     */
    Experimenter findExperimenter(@NotNull String username);
    
	/**
         * If "dn" is "true", then LDAP will be enabled for the given user.
         * If "dn" is "false", then LDAP will be disabled. All other values
         * will throw an {@link ome.conditions.ApiUsageException} since
         * If "dn" is "group:true", then LDAP will be enabled for the given group.
         * If "dn" is "group:false", then LDAP will be disabled for the given group.
         *
         * All other values this method is essentially deprecated after 4.4.2.
	 */
	void setDN(@NotNull
	Long userOrGroupID, @NotNull
	String dn);

	// ~ Getting Ldap paramiters for searching
	// =========================================================================
	
	/**
	 * Gets config value from properties.
	 * 
	 * @return boolean
	 */
	boolean getSetting();
}
