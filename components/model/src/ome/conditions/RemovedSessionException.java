/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

import javax.ejb.ApplicationException;

/**
 * Client attempted to access a {@link Session} which has either
 * {@link SessionTimeoutException expired} or been closed manually.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@ApplicationException
public class RemovedSessionException extends SessionException {

    private static final long serialVersionUID = -4513364892739872987L;

    public RemovedSessionException(String msg) {
        super(msg);
    }

}
