/*
 * ome.security.LdapUtil
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * Static methods for dealing with LDAP (DN) and the "password" table. Used
 * primarily by {@link ome.security.JBossLoginModule}
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

	public static List<Map<String, Object>> lookupLdapAuthExperimenters(
			SimpleJdbcOperations jdbc) {
		return jdbc
				.queryForList(
						"select dn, experimenter_id from password where dn is not null ",
						null);
	}
}
