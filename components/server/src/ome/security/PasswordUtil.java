/*
 * ome.security.PasswordUtil
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.security;

// Java imports

// Third-party libraries
import org.jboss.security.Util;

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;

/**
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see SecuritySystem
 * @since 3.0-M4
 */
public abstract class PasswordUtil {

	public static String preparePassword(String newPassword) {
		// This allows setting passwords to "null" - locked account.
		return newPassword == null ? null
		// This allows empty passwords to be considered "open-access"
				: newPassword.trim().length() == 0 ? newPassword
				// Regular MD5 digest.
						: passwordDigest(newPassword);
	}

	public static String passwordDigest(String clearText) {
		if (clearText == null) {
			throw new ApiUsageException("Value for digesting may not be null");
		}

		// These constants are also defined in app/resources/jboss-login.xml
		// and this method is called from {@link JBossLoginModule}
		String hashedText = Util.createPasswordHash("MD5", "base64",
				"ISO-8859-1", null, clearText, null);

		if (hashedText == null) {
			throw new InternalException("Failed to obtain digest.");
		}
		return hashedText;
	}

}
