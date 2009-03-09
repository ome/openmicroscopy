/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;

import ome.security.PasswordUtil;
import ome.security.SecuritySystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for most {@link PasswordProvider} implementations, providing
 * configuration for default behaviors. There is no need for a subclass to
 * use this implementation.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */
public abstract class ConfigurablePasswordProvider implements PasswordProvider,
        PasswordUtility {

    final protected Log log = LogFactory.getLog(this.getClass());

    /**
     * Hash implementation to use for encoding passwords to check and changed
     * passwords. Default value: MD5 (For the moment, the only supported value!)
     */
    protected final String hash;

    /**
     * If true, this implementation should return a null on
     * {@link #checkPassword(String, String)} if the user is unknown, otherwise
     * a {@link Boolean#FALSE}. Default value: false
     */
    protected final boolean ignoreUnknown;

    public ConfigurablePasswordProvider() {
        this.hash = "MD5";
        this.ignoreUnknown = false;
    }

    public ConfigurablePasswordProvider(boolean ignoreUnknown) {
        this.hash = "MD5";
        this.ignoreUnknown = ignoreUnknown;
    }

    /**
     * Always returns false, override with specific logic.
     */
    public boolean hasPassword(String user) {
        return false;
    }
    
    /**
     * If {@link #ignoreUnknown} is true, returns null, since the base class
     * knows no users. Otherwise, return {@link Boolean#FALSE} specifying that
     * authentication should fail.
     */
    public Boolean checkPassword(String user, String password) {
        if (ignoreUnknown) {
            return null;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Throws by default.
     */
    public void changePassword(String user, String password)
            throws PasswordChangeException {
        throw new PasswordChangeException(
                "Cannot change password with this implementation: "
                        + getClass().getName());
    }

    /**
     * Encodes the password as it would be encoded for a check by
     * {@link #comparePasswords(String, String)}
     */
    public String encodePassword(String newPassword) {
        return PasswordUtil.preparePassword(newPassword);
    }

    /**
     * Compares the password provided by the user (unhashed) against the given
     * trusted password. In general, if the trusted password is null, return
     * {@link Boolean.FALSE}. If the trusted password is empty (only
     * whitespace), return {@link Boolean.TRUE}. Otherwise return the results of
     * {@link String#equals(Object)}.
     */
    public Boolean comparePasswords(String trusted, String provided) {
        if (trusted == null) {
            return Boolean.FALSE;
        } else if ("".equals(trusted.trim())) {
            return Boolean.TRUE;
        } else {
            return trusted.equals(encodePassword(provided));
        }
    }

}
