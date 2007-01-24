/*
 * ome.security.PasswordUtil
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
 * @since 3.0-Beta1
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
