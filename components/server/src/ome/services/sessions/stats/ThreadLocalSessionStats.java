/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;


/**
 * Delegates to a {@link SessionStats} which is acquired on every method 
 * invocation. This object doesn't itself contain a {@link ThreadLocal} but
 * relies on the {@link ThreadLocal} instances in {@link CurrentDetails}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class ThreadLocalSessionStats extends DelegatingSessionStats {

    private final CurrentDetails cd;
    private final SessionManager sm;
    
    public ThreadLocalSessionStats(CurrentDetails cd, SessionManager sm) {
        this.cd = cd;
        this.sm = sm;
    }
    
    protected SessionStats stats() {
        return sm.getSessionStats(cd.getCurrentEventContext().getCurrentSessionUuid());
    }

}
