/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

import ome.services.messages.stats.ObjectsReadStatsMessage;
import ome.util.messages.InternalMessage;


/**
 * Counter for all the objects read in a session.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class ObjectsReadCounter extends LongCounter {

    public ObjectsReadCounter(int interval) {
        super(interval);
    }

    protected InternalMessage message() {
        return new ObjectsReadStatsMessage(this, count);
    }


}
