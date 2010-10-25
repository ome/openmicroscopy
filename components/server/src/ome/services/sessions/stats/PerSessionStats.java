/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import ome.security.basic.CurrentDetails;

/**
 * Delegates to a {@link SessionStats} which is acquired on every method
 * invocation. This object doesn't itself contain a {@link ThreadLocal} but
 * relies on the {@link ThreadLocal} instances in {@link CurrentDetails}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class PerSessionStats extends DelegatingStats {

    private final CurrentDetails cd;

    public PerSessionStats(CurrentDetails cd) {
        this.cd = cd;
    }

    protected SessionStats[] stats() {
        return new SessionStats[] { cd.getStats() };
    }

}
