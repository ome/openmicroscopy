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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;

/**
 * Static methods for dealing with password hashes and the "password" table.
 * Used primarily by {@link ome.logic.AdminImpl}
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SecuritySystem
 * @see ome.logic.AdminImpl
 * @since 3.0-Beta1
 */
public abstract class PasswordUtil {

    public static void changeUserPasswordById(SimpleJdbcTemplate jdbc, Long id,
            String password) {
        int results = jdbc.update("update password set hash = ? "
                + "where experimenter_id = ? ", preparePassword(password), id);
        if (results < 1) {
            results = jdbc.update("insert into password values (?,?) ", id,
                    preparePassword(password));
        }
    }

    public static String getUserPasswordHash(SimpleJdbcTemplate jdbc, Long id) {
        String stored;
        try {
            stored = jdbc.queryForObject("select hash "
                    + "from password where experimenter_id = ? ", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            stored = null; // This means there's not one.
        }
        return stored;
    }

    public static Long userId(SimpleJdbcTemplate jdbc, String name) {
        Long id;
        try {
            id = jdbc.queryForObject(
                    "select id from experimenter where omeName = ?", Long.class,
                    name);
        } catch (EmptyResultDataAccessException e) {
            id = null; // This means there's not one.
        }
        return id;
    }

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
