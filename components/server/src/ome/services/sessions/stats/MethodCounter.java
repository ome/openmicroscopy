/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import ome.services.messages.stats.ObjectsReadStatsMessage;
import ome.util.messages.InternalMessage;


/**
 * Counter for active methods for a given session.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class MethodCounter extends LongCounter {

    public MethodCounter(int interval) {
        super(interval);
    }

    protected InternalMessage message() {
        return new ObjectsReadStatsMessage(this, count);
    }


}
