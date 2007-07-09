/*
 * ome.security.LdapUtil
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

// Third-party libraries
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import net.sf.ldaptemplate.support.DistinguishedName;

// Application-internal dependencies
import ome.api.IAdmin;
import ome.api.ILdap;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalLdap;
import ome.model.meta.Experimenter;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;

/**
 * Static methods for dealing with LDAP (searching user in LDAP) and the
 * "password" table. Used primarily by {@link ome.security.JBossLoginModule}
 * 
 * @author Aleksandra Tarkowska, A.Tarkowska at dundee.ac.uk
 * @see SecuritySystem
 * @see ome.logic.LdapImpl
 * @since 3.0-Beta3
 */
public class LdapUtil {

	// ~ LDAP context
	// =========================================================================

	/**
	 * Creates the initial context with no connection request controls
	 * 
	 * @return {@link javax.naming.ldap.LdapContext}
	 */
	public static LdapContext createContext(String username, String password)
			throws NamingException {

		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<String, String>(5, 0.75f);
		LdapContext ctx = null;
		try {
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL,
					"ldap://lsd-test.lifesci.dundee.ac.uk:389");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			if (username != "" && username != null) {
				env.put(Context.SECURITY_PRINCIPAL, username);
				if (password != null)
					env.put(Context.SECURITY_CREDENTIALS, password);
			}
			// Create the initial context with no connection request controls
			ctx = new InitialLdapContext(env, null);

		} catch (AuthenticationException authEx) {
			throw new AuthenticationException("Authentication falilure! "
					+ authEx.toString());
		} catch (NamingException e) {
			throw new NamingException(e.toString());
		}
		return ctx;
	}

	// ~ Ldap utils
	// =========================================================================

	/**
	 * Valids password for base. Base is user's DN. When context was created
	 * successful specyfied requrements are valid.
	 * 
	 * @return boolean
	 */
	public static boolean validatePassword(String base, String password)
			throws NamingException {
		LdapContext ctx = createContext(base, password);
		if (ctx != null) {
			// Check requiroments
			return validateRequiroments(base);
		}
		return false;
	}

	/**
	 * Valids specyfied requirements for base (groups, attributes)
	 * 
	 * @return boolean
	 */
	protected static boolean validateRequiroments(String base) {

		boolean result = false;

		ServiceFactory factory = createServiceFactory();
		ILdap il = (LocalLdap) factory.getLdapService();

		// list of groups
		List<String> groups = new ArrayList<String>();
		groups.add("jGroup");
		groups.add("jrsLab");

		// List of attributes
		List<String> Tattrs = new ArrayList<String>();
		Tattrs.add("gidNumber");
		Tattrs.add("objectClass");
		String[] attrs = new String[Tattrs.size()];
		for (int i = 0; i < Tattrs.size(); i++) {
			attrs[i] = Tattrs.get(i);
		}

		// List of values for attributes
		List<String> Tvals = new ArrayList<String>();
		Tvals.add("1614");
		Tvals.add("person");
		String[] vals = new String[Tvals.size()];
		for (int i = 0; i < Tvals.size(); i++) {
			vals[i] = Tvals.get(i);
		}

		// if groups
		if (groups.size() > 0) {
			List usergroups = il.searchDnInGroups("member", base);
			result = isInGroups(groups, usergroups);
		}
		// if attributes
		if (result) {
			// cut DN
			DistinguishedName dn = new DistinguishedName(base);
			dn.removeFirst();
			dn.removeFirst();

			if (attrs.length > 0) {
				List<Experimenter> l = il.searchByAttributes(dn, attrs, vals);
				if (l.size() <= 0)
					result = false;
				else
					result = true;
			}
		}
		return result;
	}

	/**
	 * Checks that user's group list contains require groups. If one of user's
	 * groups is on require groups' list will return true.
	 * 
	 * @return boolean
	 */
	protected static boolean isInGroups(List groups, List usergroups) {
		// user is not in groups
		if (usergroups.size() <= 0)
			return false;
		boolean flag = false;
		// checks containing
		for (int i = 0; i < usergroups.size(); i++) {
			if (groups.contains(usergroups.get(i)))
				flag = true;
		}
		return flag;
	}

	// ~ Non user creating ServiceFactory - root access
	// =========================================================================

	/**
	 * Gets user from LDAP for checking him by requirements and setting his
	 * details on DB
	 * 
	 * @return {@link ome.system.ServiceFactory}
	 */
	public static boolean getUserFromLdap(String username, String password)
			throws NamingException {

		// Create context for root
		ServiceFactory factory = createServiceFactory();
		IAdmin ia = (LocalAdmin) factory.getAdminService();
		ILdap il = (LocalLdap) factory.getLdapService();

		// Find user by DN
		DistinguishedName dn = new DistinguishedName();
		try {
			 dn = il.findDN(username);
			if (dn == null)
				return false;
		} catch (Exception e) {
			return false;
		}
		
		String sufix = "ou=lifesci,o=dundee";

		// DistinguishedName converted toString includes spaces
		if (!validateRequiroments(dn.toString().replace(" ", "")+"," + sufix))
			return false;

		// Valid user's password
		boolean access = validatePassword(dn.toString() + "," + sufix, password);		
		if (access) {
			// If validation is successful search his details by DN
			Experimenter exp = il.searchByDN(dn);
			// Create new user in DB
			long id = ia.createExperimenter(exp, ia.lookupGroup("default"), ia
					.lookupGroup("user"));
			
			// Set user's DN in PASSWORD table (add sufix on the beginning)
			dn.addAll(0, new DistinguishedName(sufix));
			il.setDN(id, dn);
		}
		return access;
	}

	/**
	 * Creates ServiceFactory for using
	 * 
	 * @return {@link ome.system.ServiceFactory}
	 */
	protected static ServiceFactory createServiceFactory() {
		// Create context for root
		OmeroContext applicationContext = (OmeroContext) OmeroContext
				.getManagedServerContext();
		ServiceFactory factory = new ServiceFactory(
				(OmeroContext) applicationContext);
		SecuritySystem securitySystem = (SecuritySystem) applicationContext
				.getBean("securitySystem");
		Roles roles = securitySystem.getSecurityRoles();
		securitySystem.login(new Principal(roles.getRootName(), roles
				.getSystemGroupName(), "Test"));
		return factory;
	}

	public static void setDNById(SimpleJdbcOperations jdbc, Long id, String dn) {
		int results = jdbc
				.update(
						"update password set dn = ? where experimenter_id = ? ",
						dn, id);
		if (results < 1) {
			results = jdbc.update("insert into password values (?,?,?) ", id,
					null, dn);
		}
	}

}
