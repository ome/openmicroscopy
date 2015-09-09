/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.redirect;

import java.util.Set;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionControlPrx;
import Glacier2.SessionPrx;
import Ice.Current;

/**
 * {@link Redirector} implementation to effectively disable redirects.
 * 
 *@since Beta-4.0-RC2
 */
public class NullRedirector implements Redirector {

    /**
     * Always returns null
     */
    public SessionPrx getProxyOrNull(Context context, String userId,
            SessionControlPrx control, Current current)
            throws CannotCreateSessionException {
        return null;
    }

    /**
     * Does nothing.
     */
    public void chooseNextRedirect(Context context, Set<String> nodeUuids) {

    }

    /**
     * Does nothing.
     */
    public void handleRingShutdown(Context context, String uuid) {

    }

}