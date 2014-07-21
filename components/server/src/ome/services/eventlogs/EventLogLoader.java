/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.eventlogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import ome.api.IQuery;
import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.parameters.Parameters;
import ome.services.fulltext.FullTextIndexer;
import ome.services.messages.ReindexMessage;
import ome.tools.hibernate.QueryBuilder;
import ome.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

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
        Iterable<EventLog>, ApplicationListener {

    protected final Logger log = LoggerFactory.getLogger(getClass());

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

    public int getBatchSize() {
        return this.batchSize;
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
    private final EventBacklog backlog = new EventBacklog();

    /**
     * Marker set when {@link #stop()} is called in order to stop execution
     * after which {@link #hasNext()} will always return false.
     */
    final private AtomicBoolean stop = new AtomicBoolean(false);

    private EventLog eventLog;

    /**
     * Array of class types which will get excluded from indexing.
     */
    protected List<String> excludes = Collections.emptyList();

    /**
     * Query string to be kept in sync with {@link #excludes}.
     * @see #initQueryString()
     */
    protected String query;

    /**
     * Spring injector
     */
    public void setExcludes(String[] excludes) {
        this.excludes = Collections.unmodifiableList(Arrays.asList(excludes));
        initQueryString();
    }

    /**
     * Build a query string based on the current {@link #excludes} {@link List}.
     * The query expects a single :id parameter to be set on execution. The
     * {@link #excludes} list is used to filter out unwanted {@link EventLog}
     * instances.
     */
    private void initQueryString() {
        List<String> copy = excludes; // Instead of synchronizing
        QueryBuilder qb = new QueryBuilder();
        qb.select("el");
        qb.from("EventLog", "el");
        qb.where();
        qb.and("el.id > :id");
        if (copy != null) {
            for (String exclude : copy) {
                qb.and("el.entityType != '" + exclude + "'");
            }
        }
        qb.order("id", true);
        query = qb.queryString();
    }

    protected IQuery queryService;

    /**
     * Spring injector
     */
    public void setQueryService(IQuery queryService) {
        this.queryService = queryService;
    }

    /**
     * Tests for available objects. If {@link #count} is -1, then this batch has
     * ended (set in {@link #next()}) and false will be returned,
     * {@link EventBacklog} will be ready to be switched over to an "adding"
     * state if empty, and{@link #count} is also reset so further calls can
     * finish normally; otherwise {@link #query()} is called to load a new
     * {@link #eventLog}. Otherwise, just tests that field for null.
     */
    public boolean hasNext() {

        if (stop.get()) {
            return false;
        }

        // If we have an event log, we always return true so that we always
        // have a clean slate. (And it's simply being honest)
        if (eventLog != null) {
            return true;
        }

        // If this is the first call in a batch, give the backlog a chance
        // to make this a backlog (removing-only) batch;
        if (count == 0) {
            backlog.flipState();
        }

        // If we've done this enough, then bail out.
        if (count == batchSize) {
            count = 0;
            return false;
        }
        count++;

        // Do what we can to load an event log
        if (backlog.removingOnly()) {
            eventLog = backlog.remove();
        } else {
            eventLog = query();
        }

        boolean endBatch = eventLog == null;
        if (endBatch) {
            count = 0;
        }
        return !endBatch;
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

        // already loaded by call to hasNext() above
        EventLog rv = eventLog;
        eventLog = null;
        return rv;

    }

    public final void remove() {
        throw new UnsupportedOperationException("Cannot remove EventLogs");
    }

    public void rollback(EventLog el) {
        if (excludes.contains(el.getEntityType())) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping rollback of " + el.getEntityType());
            }
        }
        backlog.add(el);
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
     * instances, with a valid id.
     */
    public final EventLog nextEventLog(long id) {
        if (query == null) {
            initQueryString();
        }
        Parameters params = new Parameters().page(0, 1).addId(id);
        return queryService.findByQuery(query, params);
    }

    public final EventLog lastEventLog() {
        return queryService.findByQuery(
                "select el from EventLog el order by id desc",
                new Parameters().page(0, 1));
    }

    // Re-Indexing
    // =========================================================================

    /**
     * Adds an {@link EventLog} for the given {@link Class} and id to the
     * backlog.
     */
    public boolean addEventLog(Class<? extends IObject> cls, long id) {
        if (excludes.contains(cls.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping addition of " + cls.getName());
                return false;
            }
        }
        EventLog el = new EventLog();
        el.setEntityId(id);
        el.setEntityType(cls.getName());
        el.setAction("INSERT");
        return backlog.add(el);
    }

    @SuppressWarnings("unchecked")
    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof ReindexMessage) {
            ReindexMessage<? extends IObject> rm = (ReindexMessage<? extends IObject>) arg0;
            for (IObject obj : rm.objects) {
                Class trueClass = Utils.trueClass(obj.getClass());
                addEventLog(trueClass, obj.getId());
            }
        }
    }

    /**
     * Returns true if the stop flag has been set on this instance.
     */
    public boolean isStopSet() {
        return this.stop.get();
    }

    /**
     * Called by controlling objects (a worker thread) in order
     * to free up the thread.
     */
    public void setStop(boolean stop) {
        if (stop) {
            log.info("Shutting down EventLogLoader");
        } else {
            log.info("Restarting EventLogLoader");
        }
        this.stop.set(stop);
    }
}
