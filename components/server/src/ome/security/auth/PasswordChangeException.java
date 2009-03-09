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
 * Caught exception thrown from
 * {@link PasswordProvider#changePassword(String, String)} to allow read-only
 * implementations to refuse an action.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */

public class PasswordChangeException extends Exception {

    public PasswordChangeException(String msg) {
        super(msg);
    }

}
