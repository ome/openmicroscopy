/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Base session related exception. May be thrown be any service method. All
 * services other than {@code ISession} will most likely throw
 * {@link SessionTimeoutException} or {@link RemovedSessionException}, and the
 * {@code ome.api.ISession} service itself: {@link AuthenticationException} and
 * {@link ExpiredCredentialException}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class SessionException extends RootException {

    private static final long serialVersionUID = -4513364892739872987L;

    public SessionException(String msg) {
        super(msg);
    }

}
