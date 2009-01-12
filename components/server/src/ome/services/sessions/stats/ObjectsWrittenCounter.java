/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import ome.services.messages.stats.ObjectsWrittenStatsMessage;
import ome.util.messages.InternalMessage;


/**
 * Counter for all the objects read in a session.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class ObjectsWrittenCounter extends LongCounter {

    public ObjectsWrittenCounter(int interval) {
        super(interval);
    }

    protected InternalMessage message() {
        return new ObjectsWrittenStatsMessage(this, count);
    }


}
