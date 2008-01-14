/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.Iterator;

import ome.api.IQuery;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;

/**
 * Data access object for the {@link FullTextIndexer} which provides an
 * {@link Iterator} interface for {@link EventLog} instances to be properly
 * indexed. The default implementation keeps tracks of the last {@link EventLog}
 * instance, and always provides the next unindexed instance. Reseting that
 * saved value would restart indexing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class EventLogLoader implements Iterator<EventLog>, Iterable<EventLog> {

    private final static int DEFAULT_BATCH_SIZE = 10;

    private final static Parameters P = new Parameters();
    static {
        P.setFilter(new Filter().page(0, 1));
    }

    private final int batchSize = DEFAULT_BATCH_SIZE;

    private int count = 0;

    protected IQuery queryService;

    public void setQueryService(IQuery queryService) {
        this.queryService = queryService;
    }

    /**
     * Non-iterator method which increments the next {@link EventLog} which will
     * be returned. Unlike other iterators, {@link EventLogLoader} will
     * continually return the same instance until it is successful.
     */
    public void done() {
        // null
    }

    public boolean hasNext() {
        if (count == batchSize) {
            count = 0;
            return false;
        }
        return query() != null;
    }

    public EventLog next() {
        count++;
        return query();
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove EventLogs");
    }

    protected EventLog query() {
        return this.queryService.findByQuery("select el from EventLog el "
                + "where id > :id order by id", new Parameters(P)
                .addId((long) count + 100));

    }

    public Iterator<EventLog> iterator() {
        return this;
    }
}