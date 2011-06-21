/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.eventlogs;

import ome.model.IObject;
import ome.model.meta.EventLog;

/**
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */

public class AllEventsLogLoader extends EventLogLoader {

    long previous = 0;
    long max = -1;
    private boolean more = true;

    @Override
    protected EventLog query() {
        if (max < 0) {
            final IObject lastLog = lastEventLog();
            max = lastLog.getId();
        }

        EventLog el = nextEventLog(previous);

        if (el == null) {
            previous = Long.MAX_VALUE;
        } else {
            previous = el.getId();
        }

        if (previous >= max) {
            more = false;
        }
        return el;
    }

    @Override
    public long more() {
        if (max < 0) {
            return 1; // Not initialized.
        }
        long diff = max - previous;
        return diff < 0 ? 0 : diff;
    }
}
