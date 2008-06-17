/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ome.api.IQuery;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.tools.hibernate.QueryBuilder;

/**
 * Data access object for the {@link FullTextIndexer} which provides an
 * {@link Iterator} interface for {@link EventLog} instances to be properly
 * indexed. Also supports the concept of batches. After {@link #batchSize}
 * queries,
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class EventLogLoader implements Iterator<EventLog>,
        Iterable<EventLog> {

    /**
     * Currently 100.
     */
    public final static int DEFAULT_BATCH_SIZE = 100;

    protected int batchSize = DEFAULT_BATCH_SIZE;

    /**
     * Set the number of {@link EventLog} instances will be loaded in a single
     * run. If not set, {@link #DEFAULT_BATCH_SIZE} will be used.
     * 
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * The number of objects which have been returned via {@link #next()}. If
     * {@link #count} is -1, then {@link #hasNext()} will temporarily return
     * null. This signals the end of a batch. A call to {@link #more()} will
     * test whether or not other batches are available.
     */
    private int count = 0;

    /**
     * {@link List} of {@link EventLog} instances which will be consumed before
     * making use of the {@link #query()} method. Used to implement the default
     * {@link #rollback(EventLog)} mechanism.
     */
    protected final List<EventLog> backlog = Collections
            .synchronizedList(new ArrayList<EventLog>());

    private EventLog log;

    /**
     * Array of class types which will get excluded from indexing.
     */
    protected List<String> excludes = Collections.emptyList();

    /**
     * Spring injector
     */
    public void setExcludes(String[] excludes) {
        this.excludes = Collections.unmodifiableList(Arrays.asList(excludes));
    }

    protected IQuery queryService;

    /**
     * Spring injector
     */
    public void setQueryService(IQuery queryService) {
        this.queryService = queryService;
    }

    /**
     * Tests for available objects. If {@link #count} is 0, calls
     * {@link #query()} to load a new {@link #log}. Otherwise, just tests that
     * field for null.
     */
    public boolean hasNext() {
        if (count == -1) {
            count = 0;
            return false;
        }
        if (backlog.size() > 0) {
            return true;
        }
        if (log == null) {
            log = query();
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

        // Here we increment the number of times that this method has
        // successfully been entered. If we've reached batchSize, then set
        // to -1 to signal to {@link #hasNext()} that a batch is over.
        count++;
        if (count == batchSize) {
            count = -1;
        }

        synchronized (backlog) {
            if (backlog.size() > 0) {
                return backlog.remove(0);
            }
        }

        // already loaded by call to hasNext() above
        EventLog rv = log;
        log = null;
        return rv;

    }

    public final void remove() {
        throw new UnsupportedOperationException("Cannot remove EventLogs");
    }

    public void rollback(EventLog log) {
        backlog.add(log);
    }

    protected abstract EventLog query();

    public Iterator<EventLog> iterator() {
        return this;
    }

    /**
     * Should return an estimate of how many more {@link EventLog} instances are
     * available for processing. Some implementations may attempt to take extra
     * measures if the number is too large. Use 1 for a constant rather than
     * {@link Long#MAX_VALUE}. Use 0 to stop execution.
     */
    public abstract long more();

    /**
     * Returns the {@link EventLog} with the next id after the given argument or
     * null if none exists. This method will only return "true" {@link EventLog}
     * instances, with a valid id. The {@link #excludes} list is used to filter
     * out unwanted {@link EventLog} isntances.
     */
    public final EventLog nextEventLog(long id) {
        List<String> copy = excludes; // Instead of synchronizing
        QueryBuilder qb = new QueryBuilder();
        qb.select("el");
        qb.from("EventLog", "el");
        qb.where();
        qb.and("el.id > " + id);
        if (copy != null) {
            for (String exclude : copy) {
                qb.and("el.entityType != '" + exclude + "'");
            }
        }
        qb.order("id", true);
        String query = qb.queryString();

        return queryService.findByQuery(query, new Parameters(new Filter()
                .page(0, 1)));
    }

    public final EventLog lastEventLog() {
        return queryService.findByQuery(
                "select el from EventLog el order by id desc", new Parameters(
                        new Filter().page(0, 1)));
    }
}