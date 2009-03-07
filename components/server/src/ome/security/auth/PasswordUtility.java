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
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */

public interface PasswordUtility {

    String encodePassword(String password);

    Boolean comparePasswords(String trusted, String provided);
}
