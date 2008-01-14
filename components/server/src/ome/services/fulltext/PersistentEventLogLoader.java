/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.model.meta.EventLog;

/**
 * P@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Reseting that saved value would restart indexing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class PersistentEventLogLoader extends EventLogLoader {

    /**
     * Non-iterator method which increments the next {@link EventLog} which will
     * be returned. Unlike other iterators, {@link PersistentEventLogLoader}
     * will continually return the same instance until it is successful.
     */
    @Override
    public void done() {
        // null
    }

    @Override
    protected EventLog query() {
        return null;
    }

    /**
     * Always returns true. The default implementation is to tell the
     * {@link FullTextIndexer} to always retry in a while loop. Other
     * implementations may want to break the execution.
     * 
     * @return true
     */
    @Override
    public boolean more() {
        return true;
    }
}