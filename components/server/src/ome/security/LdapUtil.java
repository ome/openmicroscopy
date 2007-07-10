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
 * Static methods for dealing with LDAP (DN) and the
 * "password" table. Used primarily by {@link ome.security.JBossLoginModule}
 * 
 * @author Aleksandra Tarkowska, A.Tarkowska at dundee.ac.uk
 * @see SecuritySystem
 * @see ome.logic.LdapImpl
 * @since 3.0-Beta3
 */
public class LdapUtil {

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
