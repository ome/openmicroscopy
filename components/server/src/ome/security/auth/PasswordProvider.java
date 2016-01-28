/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;

import ome.security.SecuritySystem;

/**
 * Authentication interface responsible for checking and changing passwords. In
 * addition, a {@link PasswordProvider implementation} may claim to know nothing
 * for a particular user name. See {@link #checkPassword(String, String, boolean)} for
 * more information.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */
public interface PasswordProvider {

    /**
     * Returns true if this provider considers itself responsible for the given
     * user name. In general, if this method returns false, then checkPassword
     * will return null or false for all possible passwords. However, some
     * providers (like the LDAP provider) may create a user to synchronize with
     * some backend during a call to {@link #checkPassword(String, String, boolean)}.
     * {@link #hasPassword(String)} will not do this. This is typically only of
     * importance during {@link #changePassword(String, String)} since a
     * provider which is not responsible for a password should not attempt to
     * change it, and before a provider has not created a user, it is also not
     * responsible.
     */
    boolean hasPassword(String user);

    /**
     * Authenticates the give user given the password token. May return a null
     * {@link Boolean} in order to signal that this provider is not responsible
     * for the given user and can make no decision. Concrete implementations may
     * decide to return false for all unknown users. If readOnly is false, then
     * some implementations may choose to create new users.
     */
    Boolean checkPassword(String user, String password, boolean readOnly);

    /**
     * Attempts to change the password for the given user. May throw a
     * {@link PasswordChangeException}, for example if the provider uses a
     * read-only medium.
     */
    void changePassword(String user, String password)
            throws PasswordChangeException;

}
