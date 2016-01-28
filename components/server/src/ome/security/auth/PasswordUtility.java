/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

/**
 * Extension interface to {@link PasswordProvider} for methods which may or may
 * not be available from a provider implementation.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */

public interface PasswordUtility {

    String encodePassword(String password);

    Boolean comparePasswords(String trusted, String provided);
}
