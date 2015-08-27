/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Correct username and credentials provided, but credentials expired.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.ISession#changeExpiredCredentials(String, String, String)
 */
public class ExpiredCredentialException extends SessionException {

    private static final long serialVersionUID = -4513364892739872987L;

    public ExpiredCredentialException(String msg) {
        super(msg);
    }

}
