/* ome.api.local.LocalAdmin
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

import java.util.List;

import ome.annotations.Hidden;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local (internal) extensions for administration
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: 1322 $ $Date: 2007-03-06 11:49:47 +0000 (Tue, 06 Mar 2007) $)
 *          </small>
 * @since OMERO3.0
 */
public interface LocalLdap extends ome.api.ILdap {

	/**
	 * Gets base from the LdapContextSource
	 * 
	 * @return String
	 */
	String getBase();
	
	/**
	 * Gets user from LDAP for checking him by requirements and setting his
	 * details on DB
	 * 
	 * @return {@link ome.system.ServiceFactory}
	 */
	boolean createUserFromLdap(String username, @Hidden String password);
	
	/**
	 * Valids specyfied requirements for base (groups, attributes)
	 * 
	 * @return boolean
	 */
	boolean validateRequiroments(String base);
	
	/**
	 * Valids password for base. Base is user's DN. When context was created
	 * successful specyfied requrements are valid.
	 * 
	 * @return boolean
	 */
	boolean validatePassword(String base, @Hidden String password);
	
	/**
	 * Checks that user's group list contains require groups. If one of user's
	 * groups is on require groups' list will return true.
	 * 
	 * @return boolean
	 */
	boolean isInGroups(List groups, List usergroups);
	
}
