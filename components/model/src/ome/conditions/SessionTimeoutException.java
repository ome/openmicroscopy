/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;


/**
 * The given session has expired and can no longer be used. Clients should
 * assume that after this exception has been thrown, that all further attempts
 * to access the session will thrown {@link RemovedSessionException}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionTimeoutException extends SessionException {

    private static final long serialVersionUID = -4513364892739872987L;

    public final Object sessionContext;

    public SessionTimeoutException(String msg, Object sessionContext) {
        super(msg);
        this.sessionContext = sessionContext;
    }

}
