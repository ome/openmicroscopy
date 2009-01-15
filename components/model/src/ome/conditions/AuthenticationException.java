/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Invalid username and/or credential provided.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class AuthenticationException extends SessionException {

    private static final long serialVersionUID = -4513364892739872987L;

    public AuthenticationException(String msg) {
        super(msg);
    }

}
