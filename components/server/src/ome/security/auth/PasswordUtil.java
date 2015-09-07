/*
 * Copyright (C) 2006-2014 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.auth;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.security.SecuritySystem;
import ome.system.Roles;
import ome.util.SqlAction;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static enum METHOD {

       CLEAR(false, false),
       LEGACY(true, false),
       ALL(true, true);

       private final boolean hash;
       private final boolean salt;

       METHOD(boolean hash, boolean salt) {
           this.hash = hash;
           this.salt = salt;
       }
    }

    /**
     * The default encoding for converting plain text passwords to byte arrays
     * (UTF-8)
     */
    public final static String DEFAULT_ENCODING = "UTF-8";

    private final static Logger log = LoggerFactory.getLogger(PasswordUtil.class);

    private final SqlAction sql;

    private final Roles roles;

    private final boolean passwordRequired;

    private final Charset encoding;

    public PasswordUtil(SqlAction sql) {
        this(sql, new Roles(), true);
    }

    public PasswordUtil(SqlAction sql, boolean passwordRequired) {
        this(sql, new Roles(), passwordRequired);
    }

    public PasswordUtil(SqlAction sql, Charset encoding) {
        this(sql, true, encoding);
    }

    public PasswordUtil(SqlAction sql, boolean passwordRequired, Charset encoding) {
        this(sql, new Roles(), passwordRequired, encoding);
    }

    public PasswordUtil(SqlAction sql, Roles roles, boolean passwordRequired) {
        this(sql, roles, passwordRequired, Charset.forName(DEFAULT_ENCODING));
    }

    public PasswordUtil(SqlAction sql, Roles roles, boolean passwordRequired,
            Charset encoding) {
        this.sql = sql;
        this.roles = roles;
        this.passwordRequired = passwordRequired;
        this.encoding = encoding;
    }

    /**
     * Main method which takes exactly one argument, passes it to
     * {@link #preparePassword(String)} and prints the results on
     * {@link System#out}. This is used by the build system to define the
     * "@ROOTPASS@" placeholder in data.sql.
     * @param args the command-line arguments
     */
    public static void main(String args[]) {
        if (args == null || args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("PasswordUtil password [user-id]");
        }
        PasswordUtil util = new PasswordUtil(null);
        String pw = args[0];
        if (args.length == 1) {
            System.out.println(util.preparePassword(pw));
        } else {
            Long userId = Long.valueOf(args[1]);
            System.out.println(util.prepareSaltedPassword(userId, pw));
        }
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

    public boolean getDnById(Long id) {
        return sql.isLdapExperimenter(id);
    }

    /**
     * Calls {@link #changeUserPasswordById(Long, String, METHOD)} with
     * "false" as the value of the salt argument in order to provide backwards
     * compatibility.
     * @param id the user ID
     * @param password the password
     */
    public void changeUserPasswordById(Long id, String password) {
        changeUserPasswordById(id, password, METHOD.LEGACY);
    }

    /**
     * Calls either {@link #preparePassword(String)} or
     * {@link #prepareSaltedPassword(Long, String)} and passes the resulting
     * value to {@link SqlAction#setUserPassword(Long, String)}.
     * An {@link InternalException} is thrown if the modification is not
     * successful, which should only occur if the user has been deleted.
     * @param id the user ID
     * @param password the password
     * @param meth how to encode the password
     */
    public void changeUserPasswordById(Long id, String password, METHOD meth) {
        String prepared = password;
        if (meth.hash){
            prepared = preparePassword(id, password, meth.salt);
        }
        if (! sql.setUserPassword(id, prepared)) {
            throw new InternalException("0 results for password insert.");
        }
        sql.clearPermissionsBit("experimenter", id, 16);
    }

    public String getUserPasswordHash(Long id) {
        return sql.getPasswordHash(id);
    }

    /**
     * Get the user's ID
     * @param name the user's name
     * @return their ID, or {@code null} if they cannot be found
     */
    public Long userId(String name) {
        return sql.getUserId(name);
    }

    /**
     * Get the user's name
     * @param id the user's ID
     * @return their name, or {@code null} if they cannot be found
     */
    public String userName(long id) {
        return sql.getUsername(id);
    }

    public List<String> userGroups(String name) {
        return sql.getUserGroups(name);
    }

    public String preparePassword(String newPassword) {
        return preparePassword(null, newPassword, false);
    }

    public String prepareSaltedPassword(Long userId, String newPassword) {
        return preparePassword(userId, newPassword, true);
    }

    protected String preparePassword(Long userId, String newPassword, boolean salt) {
        // This allows setting passwords to "null" - locked account.
        // Also checks if empty passwords are to be considered "open-access"
        return newPassword == null
                || (newPassword.trim().isEmpty() && isPasswordRequired(userId)) ? null
                : newPassword.trim().isEmpty() ? newPassword
                // Regular MD5 digest.
                        : passwordDigest(userId, newPassword, salt);
    }

    /**
     * Creates an MD5 hash of the given clear text and base64 encodes it.
     * @param clearText the cleartext of the password
     * @return the password hash
     */
    // TODO This should almost certainly be configurable as to encoding,
    // algorithm, and possibly even the implementation in general.
    public String passwordDigest(String clearText) {
        return passwordDigest(null, clearText, false);
    }

    /**
     * Creates an MD5 hash of the given clear text and base64 encodes it.
     * If the provided userId argument is not null, then it will be used
     * as a salt value for the password.
     * @param userId the user's ID, may be {@code null}
     * @param clearText the cleartext of the password
     * @return the password hash
     */
    public String saltedPasswordDigest(Long userId, String clearText) {
        return passwordDigest(userId, clearText, true);
    }

    protected String passwordDigest(Long userId, String clearText, boolean salt) {

        if (clearText == null) {
            throw new ApiUsageException("Value for digesting may not be null");
        }

        byte[] bytes = clearText.getBytes(encoding);

        // If salting is activated, prepend the salt.
        if (userId != null && salt) {
            byte[] saltedBytes = ByteBuffer.allocate(8).putLong(userId).array();
            byte[] newValue = new byte[saltedBytes.length+bytes.length];
            System.arraycopy(saltedBytes, 0, newValue, 0, saltedBytes.length);
            System.arraycopy(bytes, 0, newValue, saltedBytes.length, bytes.length);
            bytes = newValue;
        }

        String hashedText = null;
        ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
        try {
            bytes = cpf.getProvider(ChecksumType.MD5).putBytes(bytes)
                    .checksumAsBytes();
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

    /**
     * Returns a boolean based on the supplied user ID and system property
     * setting. If password requirement is switched off or the user is
     * a guest user, then this returns <code>false</code>. In all other cases
     * this returns <code>true</code>.
     *
     * @param id The user ID.
     * @return boolean <code>true</code> or <code>false</code>
     */
    public boolean isPasswordRequired(Long id) {
        if (id == null) {
            return passwordRequired;
        } else {
            return !id.equals(roles.getGuestId()) && passwordRequired;
        }
    }

}
