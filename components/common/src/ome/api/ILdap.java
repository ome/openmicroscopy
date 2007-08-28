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
	 * Searchs unique Distinguished Name - {@link java.lang.String} in
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
	String findDN(@NotNull
	String username);

	/**
	 * Searchs all {@link ome.model.meta.Experimenter} in LDAP for objectClass =
	 * person
	 * 
	 * @param omeName
	 *            Name of the Experimenter
	 * @return an Experimenter. Never null.
	 * @throws ome.conditions.ApiUsageException
	 *             if omeName does not exist.
	 */
	void setDN(@NotNull
	Long experimenterID, @NotNull
	String dn);

	// ~ Getting Ldap paramiters for searching
	// =========================================================================

	/**
	 * Searchs all Groups in LDAP 
	 * 
	 * @return an ExperimenterGroups.
	 */
	List<ExperimenterGroup> searchGroups();

	/**
	 * Searchs all {@link javax.naming.directory.Attributes} in LDAP
	 * 
	 * @return {@link javax.naming.directory.Attributes}
	 */
	Attributes searchAttributes();

	//  ~ Getters and Setters Ldap rewuiroments from properties file
	// =========================================================================

	/**
	 * Gets specyfied requirements from properties.
	 * 
	 * @return List<String>
	 */
	List<String> getReqGroups();
	
	/**
	 * Gets specyfied attributes from properties.
	 * 
	 * @return String []
	 */
	String[] getReqAttributes();
	
	/**
	 * Gets specified values for attributes from properties.
	 * 
	 * @return String []
	 */
	String[] getReqValues();
	
	/**
	 * Sets specyfied requirements from properties.
	 * 
	 * @return List<String>
	 */
	void setReqGroups(List<String> groups);
	
	/**
	 * Sets specyfied attributes from properties.
	 * 
	 * @return String []
	 */
	void setReqAttributes(String [] attrs);
	
	/**
	 * Sets specified values for attributes from properties.
	 * 
	 * @return String []
	 */
	void setReqValues(String [] vals);
	
	/**
	 * Gets config value from properties.
	 * 
	 * @return boolean
	 */
	boolean getSetting();
}
