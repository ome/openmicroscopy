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
 * Wrapper which delegates to a {@link SessionStats} instance acquired during
 * construction. This is mostly useful for stateful services which are created
 * within the context of a single session.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class CurrentSessionStats extends DelegatingStats {

    private final SessionStats[] stats;

    public CurrentSessionStats(CurrentDetails cd, SessionManager sm) {
        stats = new SessionStats[] { sm.getSessionStats(cd
                .getCurrentEventContext().getCurrentSessionUuid()) };
    }

    @Override
    protected SessionStats[] stats() {
        return stats;
    }

}
