/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Server could not acquire necessary lock. Wait and try again.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3
 */
public class LockTimeout extends ConcurrencyException {

    /**
     *
     */
    private static final long serialVersionUID = 5920393509820381811L;

    /**
     * Informational field on number of seconds that the lock was tried for.
     */
    public final int seconds;

    public LockTimeout(String msg, long backOffInMilliseconds, int timeoutInSeconds) {
        super(msg, backOffInMilliseconds);
        this.seconds = timeoutInSeconds;
    }

}
