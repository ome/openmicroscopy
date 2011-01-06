/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.security.SecuritySystem;
import ome.util.SqlAction;
import ome.util.Utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static methods for dealing with password hashes and the "password" table.
 * Used primarily by {@link ome.logic.AdminImpl}
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @see SecuritySystem
 * @see ome.logic.AdminImpl
 * @since 3.0-Beta1
 */
public class PasswordUtil {

    private final static Log log = LogFactory.getLog(PasswordUtil.class);

    private final SqlAction sql;

    public PasswordUtil(SqlAction sql) {
        this.sql = sql;
    }

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
        System.out.println(new PasswordUtil(null).preparePassword(args[0]));
    }

    public String generateRandomPasswd() {
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        char[] chars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U',
                'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
                'j', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z' };
        for (int i = 0; i < 10; i++) {
            buffer.append(chars[random.nextInt(chars.length)]);
        }
        return buffer.toString();
    }

    public String getDnById(Long id) {
        return sql.dnForUser(id);
    }

    public void changeUserPasswordById(Long id, String password) {
        if (! sql.setUserPassword(id, preparePassword(password))) {
            throw new InternalException("0 results for password insert.");
        }
    }

    public String getUserPasswordHash(Long id) {
        return sql.getPasswordHash(id);
    }

    public Long userId(String name) {
        return sql.getUserId(name);
    }

    public List<String> userGroups(String name) {
        return sql.getUserGroups(name);
    }

    public String preparePassword(String newPassword) {
        // This allows setting passwords to "null" - locked account.
        return newPassword == null ? null
        // This allows empty passwords to be considered "open-access"
                : newPassword.trim().length() == 0 ? newPassword
                // Regular MD5 digest.
                        : passwordDigest(newPassword);
    }

    /**
     * Creates an MD5 hash of the given clear text and base64 encodes it.
     *
     * @DEV.TODO This should almost certainly be configurable as to encoding,
     *           algorithm, character encoding, and possibly even the
     *           implementation in general.
     */
    public String passwordDigest(String clearText) {

        if (clearText == null) {
            throw new ApiUsageException("Value for digesting may not be null");
        }

        byte[] bytes = null;
        try {
            bytes = clearText.getBytes("ISO-8859-1"); // FIXME
        } catch (UnsupportedEncodingException uee) {
            log.warn("Unsupported charset ISO-8859-1. Using default");
            bytes = clearText.getBytes();
        }

        String hashedText = null;
        try {
            bytes = Utils.calculateMessageDigest(bytes);
            bytes = Base64.encodeBase64(bytes);
            hashedText = new String(bytes);
        } catch (Exception e) {
            log.error("Could not hash password", e);
        }

        if (hashedText == null) {
            throw new InternalException("Failed to obtain digest.");
        }
        return hashedText;
    }

}
