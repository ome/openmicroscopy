/*
 * ome.security.PasswordUtil
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports

// Third-party libraries
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jboss.security.Util;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

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

    /**
     * Main method which takes exactly one argument, passes it to
     * {@link #preparePassword(String)} and prints the results on
     * {@link System#out}. This is used by the build system to define the
     * "@ROOTPASS@" placeholder in data.sql.
     */
    public static void main(String args[]) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("PasswordUtil.main takes 1 arg.");
        }
        System.out.println(preparePassword(args[0]));
    }

    public static String generateRandomPasswd() {
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        char[] chars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
                'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c',
                'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        for ( int i = 0; i < 10; i++ ) {
            buffer.append(chars[random.nextInt(chars.length)]);
        }
        return buffer.toString();
    }
    
    public static String getDnById(SimpleJdbcOperations jdbc, Long id) {
        String expire;
        try {
            expire = jdbc.queryForObject("select dn from password " +
                "where experimenter_id = ? ", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            expire = null; // This means there's not one.
        }
        return expire;
    }
    
    public static void changeUserPasswordById(SimpleJdbcOperations jdbc, Long id,
            String password) {
        int results = jdbc.update("update password set hash = ? "
                + "where experimenter_id = ? ", preparePassword(password), id);
        if (results < 1) {
            results = jdbc.update("insert into password values (?,?) ", id,
                    preparePassword(password));
        }
    }

    public static String getUserPasswordHash(SimpleJdbcOperations jdbc, Long id) {
        String stored;
        try {
            stored = jdbc.queryForObject("select hash "
                    + "from password where experimenter_id = ? ", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            stored = null; // This means there's not one.
        }
        return stored;
    }

    public static Long userId(SimpleJdbcOperations jdbc, String name) {
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

    public static List<String> userGroups(SimpleJdbcOperations jdbc, String name) {
        List<String> roles;
        try {
            roles = jdbc.query(
                    "select g.name from experimentergroup g, " +
                    "groupexperimentermap m, experimenter e " +
                    "where omeName = ? and " +
                    "e.id = m.child and " +
                    "m.parent = g.id",
                    new ParameterizedRowMapper<String>(){
                      public String mapRow(ResultSet rs, int rowNum)
                      throws SQLException {
                            return rs.getString(1);
                        }
                    },
                    name);
        } catch (EmptyResultDataAccessException e) {
            roles = null; // This means there's not one.
        }
        return roles == null ? new ArrayList<String>() : roles;
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
