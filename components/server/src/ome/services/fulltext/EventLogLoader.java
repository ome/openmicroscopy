/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
public abstract class EventLogLoader implements Iterator<EventLog>,
        Iterable<EventLog> {

    private final static int DEFAULT_BATCH_SIZE = 10;

    private final static Parameters P = new Parameters();
    static {
        P.setFilter(new Filter().page(0, 1));
    }

    private final int batchSize = DEFAULT_BATCH_SIZE;

    /**
     * The number of objects which have been returned via {@link #next()}
     */
    private int count = 0;

    /**
     * Whether or not {@link #hasNext()} should initialize itself.
     */
    private boolean doInit = true;

    private EventLog log;

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

    /**
     * Tests for available objects. If {@link #count} is 0, calls
     * {@link #query()} to load a new {@link #log}. Otherwise, just tests that
     * field for null.
     */
    public boolean hasNext() {
        if (doInit) {
            log = query();
            doInit = false;
        }
        return log != null;
    }

    /**
     * Returns the current {@link #log} instance which may be loaded by a call
     * to {@link #hasNext()} if necessary. If {@link #hasNext()} returns false,
     * a {@link NoSuchElementException} will be thrown.
     */
    public EventLog next() {

        // Consumer should have checked with hasNext
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        EventLog rv = log;

        if (count == batchSize) {
            count = 0;
            doInit = true;
            log = null;
        } else {
            count++;
            log = query();
        }
        return rv;
    }

    public final void remove() {
        throw new UnsupportedOperationException("Cannot remove EventLogs");
    }

    protected abstract EventLog query();

    public Iterator<EventLog> iterator() {
        return this;
    }

    /**
     * Always returns true. The default implementation is to tell the
     * {@link FullTextIndexer} to always retry in a while loop. Other
     * implementations may want to break the execution.
     * 
     * @return true
     */
    public boolean more() {
        return true;
    }
}