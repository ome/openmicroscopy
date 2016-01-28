/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.eventlogs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.EventLog;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.system.metrics.Counter;
import ome.system.metrics.Metrics;
import ome.system.metrics.NullMetrics;
import ome.system.metrics.Timer;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * {@link PersistentEventLogLoader} implementation which loads many rows at the
 * same time, culling out duplicates in order to speed up indexing. Many of the
 * methods used by either {@link PersistentEventLogLoader} or its super-classes
 * are not intended for use here. Instead, the {@link Iterator} interface is
 * most critical. As with other implementations, {@link #hasNext()} is used for
 * loading data if necessary, while {@link #next()} simply returns an object. In
 * some cases, nulls may be returned, which consumers must contend with.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.3
 */
public class EventLogQueue extends PersistentEventLogLoader {

    /**
     * Basic states which an {@link Entry} can be in.
     *
     * @since 5.0.3
     */
    enum State {

        /**
         * Newly created and still unprocessed.
         */
        OPEN,

        /**
         * Marked on the <em>previous</em> {@link Entry} when
         * {@link EventLogQueue#next} is about to return.
         */
        PASS,

        /**
         * Marked when {@link EventLogQueue#onApplicationEvent(ApplicationEvent)}
         * is called with a {@link EventLogFailure}.
         */
        FAIL;
    }

    /**
     * Simple container for the data returned from
     * {@link SqlAction#getEventLogPartitions(java.util.Collection, java.util.Collection, long, long)}
     * . State is mutable so that subsequent data for the same IObject can be
     * merged into a single representation.
     *
     * @since 5.0.3
     */
    private static class Entry implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Id of {@link EventLog} for this entry. If less than 0, then this
         * represents a backlog item.
         * @see EventLog#getId()
         */
        long eventLog;

        /**
         * Class name for this {@link EventLog}.
         * @see EventLog#getEntityType()
         */
        final String objType;

        /**
         * Id of object for this {@link EventLog}.
         * @see EventLog#getEntityId()
         */
        final long objId;

        /**
         * Action for this {@link EventLog}.
         * @see EventLog#getAction()
         */
        String action;

        /**
         * Number of rows that were skipped by the {@link SqlAction}
         * windowing function.
         */
        int skipped;

        State state = State.OPEN;

        Entry(long log, String type, long id, String action, int skipped) {
            this.eventLog = log;
            this.objType = type;
            this.objId = id;
            this.action = action;
            this.skipped = skipped;
        }

        public void update(long eventLogId, String action, int skipped) {
            this.eventLog = eventLogId;
            this.action = action;
            this.skipped = skipped;
        }

        public void pass() {
            state = State.PASS;
        }

        public void fail() {
            state = State.FAIL;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Entry[");
            sb.append(eventLog);
            sb.append("]");
            sb.append("<");
            sb.append(objType);
            sb.append(":");
            sb.append(objId);
            sb.append("=");
            sb.append(action);
            sb.append(">");
            return sb.toString();
        }
    }

    /**
     * Wrapper to combine {@link EventLog} and {@link Entry} instances for
     * consumption by the FullTextIndexer.
     *
     * @since 5.0.3
     */
    //Unfortunately this {@link EventLog} subclass needlessly creates a
    //       Details object.
    private static class WrappedEventLog extends EventLog {

        private static final long serialVersionUID = 1L;

        private final Entry entry;

        private final Timer.Context timer;

        WrappedEventLog(Entry e, Timer.Context timer) {
            this.timer = timer;
            this.entry = e;
            setId(e.eventLog);
            setAction(e.action);
            setEntityType(e.objType);
            setEntityId(e.objId);
        }
    }

    /**
     * Collection of collections which must be kept in sync during additions
     * and removals. Three queues are available from which {@link Entry}
     * instances will be "popped": {@link #priorityQ}, {@link #regularQ},
     * and {@link #failureQ}. At the same time, an index is maintained per
     * each {@link EventLog#getEntityType() entityType} so that later log
     * items are not repeated.
     *
     * @since 5.0.3
     */
    private static class Data implements Serializable {

        private static final Logger log = LoggerFactory.getLogger(Data.class);

        private static final long serialVersionUID = 1L;

        /**
         * Intended to hide access to much of the {@link Map} interface in order
         * to simplify keeping the various collections in sync.
         *
         * @since 5.0.3
         */
        private class Entries implements Serializable {

            private static final long serialVersionUID = 1L;

            private final Map<Long, Entry> entries;

            public Entries(Map<Long, Entry> entries) {
                this.entries = entries;
            }

            public Entry get(Long objId) {
                return this.entries.get(objId);
            }

            private void addRegular(Entry entry) {
                entries.put(entry.objId, entry);
                regularQ.add(entry);
                regularCount.inc();
            }

            public void addPriority(Entry entry) {
                entries.put(entry.objId, entry);
                priorityQ.add(entry);
                priorityCount.inc();
            }
        }

        /**
         * A weak-valued map of all the entries to be found elsewhere in the
         * queues.
         */
        final private Entries[] entriesArray;

        /**
         * Priority queue, essentially the backlog from other implementations,
         * which should be handled first.
         */
        final private LinkedList<Entry> priorityQ = new LinkedList<Entry>();

        /**
         * Ordered list of {@link Entry} items which should be processed next if
         * there is nothing in the priority queue.
         */
        final private LinkedList<Entry> regularQ = new LinkedList<Entry>();

        /**
         * List of failed items. They may be retried when no other processing is
         * necessary.
         */
        final private LinkedList<WrappedEventLog> failureQ = new LinkedList<WrappedEventLog>();

        /**
         * @see EventLogQueue#types
         */
        final private List<String> types;

        final transient private Counter priorityCount, regularCount, failureCount;

        public Data(Counter priority, Counter regular, Counter failure,
                List<String> types) {
            this.priorityCount = priority;
            this.regularCount = regular;
            this.failureCount = failure;
            this.types = types;
            this.entriesArray = new Entries[types.size()];
            for (int i = 0; i < types.size(); i++) {
                this.entriesArray[i] = new Entries(new HashMap<Long, Entry>());
            }
        }

        /**
         * Return the {@link Entry} map which matches the given type.
         */
        protected Entries entries(String type) {
            int idx = types.indexOf(type);
            if (idx >= 0) {
                return entriesArray[idx];
            }
            return null;
        }

        public boolean hasNext() {
            if (!priorityQ.isEmpty()) {
                return true;
            }
            if (!regularQ.isEmpty()) {
                return true;
            }
            return false;
        }

        public Entry next() {
            String which = null;
            Entry entry = null;
            if (!priorityQ.isEmpty()) {
                entry = priorityQ.remove(0);
                priorityCount.dec();
                which = "priority";
            } else if (!regularQ.isEmpty()) {
                entry = regularQ.remove(0);
                regularCount.dec();
                which = "regular";
            } else {
                throw new NoSuchElementException();
            }
            entries(entry.objType).entries.remove(entry.objId);
//            log.debug("Returning {}. Remaining: priority={}, regular={}",
//                    entry, priorityCount.getCount(), regularCount.getCount());
            return entry;
        }

        public void fail(EventLogFailure failure) {
            WrappedEventLog wrapped = (WrappedEventLog) failure.log;
            failureQ.add(wrapped);
            wrapped.entry.fail();
            failureCount.inc();
            // Note: this will stay in the hash map to prevent future access.
        }

    }

    /**
     * Default maximum for the number of rows that will be loaded in a single
     * call to
     * {@link SqlAction#getEventLogPartitions(java.util.Collection, java.util.Collection, long, long)}
     * . Currently 1 million.
     */
    final static public int DEFAULT_MAX = 1000 * 1000;

    /**
     * Array of entity types which will be used in
     * {@link SqlAction#getEventLogPartitions(String[], String[], long, long)}
     * in the clause "where entityType in (:types)"
     */
    final private List<String> types;

    /**
     * Array of actions which will be used in
     * {@link SqlAction#getEventLogPartitions(String[], String[], long, long)}
     * in the clause "where action in (:actions)"
     */
    final private List<String> actions;

    final private Data data;

    /**
     * Maximum number of entries (LIMIT) that should be returned from
     * {@link SqlAction#getEventLogPartitions(String[], String[], long, long)}.
     */
    final private int max;

    /**
     * Length of time for loading rows from the database.
     */
    final private Timer lookupTime;

    /**
     * Length of time for processing one {@link WrappedEventLog}.
     */
    final private Timer processTime;

    final private Counter priorityCount, regularCount, failureCount;

    final private Counter nextCount;

    private int batchCount;

    /**
     * Last {@link Entry} which was returned by the {@link #next()} method. If a
     * {@link EventLogFailure} is received, then this should be marked as such.
     * If {@link #next()} is called again without a failure, it can be assumed
     * that the processing was successful.
     */
    private WrappedEventLog lastReturned;

    public EventLogQueue() {
        this(new NullMetrics(),
                DEFAULT_MAX, new String[]{Project.class.getName(),
                Dataset.class.getName(), Screen.class.getName(),
                Plate.class.getName(), Image.class.getName()}, new String[]{
                "INSERT", "UPDATE", "REINDEX", "DELETE"});
    }

    public EventLogQueue(Metrics metrics, int max,
            String[] types, String[] actions) {

        this.lookupTime = metrics.timer(this, "lookupTime");
        this.processTime = metrics.timer(this, "processTime");
        this.nextCount = metrics.counter(this, "nextCount");
        this.priorityCount = metrics.counter(this, "priorityCount");
        this.regularCount = metrics.counter(this, "regularCount");
        this.failureCount = metrics.counter(this, "failureCount");

        // Rough testing shows each entry in the queue to take up about
        // 100 bytes of storage. If the max would use "too much memory",
        // then scale it down by 10%. E.g. the default would use ~100MB,
        // if this is more than 25% of memory, scale down.
        long memory = Runtime.getRuntime().maxMemory();
        long queueBytes = max * 100;
        if (queueBytes >  (.25 * memory)) {
            this.max = max/10;
            log.warn("max_partition_size set to more than 25% of "
                    + "total heap size. Reducing by 1/10th to {}", this.max);
        } else {
            this.max = max;
        }
        this.types = Arrays.asList(types);
        this.actions = Arrays.asList(actions);
        this.data = new Data(priorityCount, regularCount, failureCount,
                this.types);
    }

    //
    // HELPERS
    //

    protected List<Object[]> lookup() {
        final Timer.Context ctx = lookupTime.time();
        try {
            final long current = getCurrentId();
            List<Object[]> rv = sql.getEventLogPartitions(types, actions,
                    current, max);
            log.debug(String.format("objects found searching " +
                    "from %s (max: %s): %s",
                    current, max, rv.size()));
            return rv;
        } finally {
            ctx.stop();
        }
    }

    protected int load(List<Object[]> rows) {
        int loaded = 0;
        for (Object[] row : rows) {
            if (row == null || row.length != 5 || !(row[0] instanceof Long)
                    || !(row[1] instanceof String) || !(row[2] instanceof Long)
                    || !(row[3] instanceof String)
                    || !(row[4] instanceof Integer)) {
                log.error("Invalid row data: " + Arrays.toString(row));
                continue;
            }
            if (load((Long) row[0], (String) row[1], (Long) row[2],
                    (String) row[3], (Integer) row[4])) {
                loaded++;
            }
        }
        return loaded;
    }

    /**
     * Guarantees that the given arguments are available somewhere in the queue
     * returning true if they were newly added.
     */
    protected boolean load(Long eventLogId, String type,
            Long objId, String action,
            Integer skipped) {
        boolean added = false;
        final Data.Entries entries = data.entries(type);
        Entry entry = entries.get(objId);
        if (entry == null) {
            entry = new Entry(eventLogId, type, objId, action, skipped);
            entries.addRegular(entry);
            added = true;
        } else {
            entry.update(eventLogId, action,  skipped);
        }
        return added;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof EventLogFailure) {
            EventLogFailure failure = (EventLogFailure) arg0;
            if (failure.wasSource(this)) {
                if (lastReturned != failure.log) {
                    log.error("lastReturned is not failure item!");
                }
                lastReturned.timer.stop(); // In case of fail
                lastReturned = null; // Prevent success later
                data.fail(failure);
            }
        } else {
            super.onApplicationEvent(arg0);
        }
    }

    /**
     * Handles cleanup of the previously offered {@link Entry} and prepares the
     * new {@link Entry} for processing.
     */
    private EventLog offer(Entry entry) {
        // First handle the previously returned
        if (this.lastReturned != null) {
            this.lastReturned.timer.stop(); // In case of success
            Entry last = this.lastReturned.entry;
            last.pass();
            if (last.eventLog >= 0) {
                setCurrentId(last.eventLog);
            }
            log.debug(String.format("Successfully handled %s. Skipped: %s",
                    last, last.skipped));
            this.lastReturned = null;
        }

        if (entry.state != State.OPEN) {
            return null;
        }
        this.lastReturned = new WrappedEventLog(entry, processTime.time());
        return this.lastReturned;
    }

    //
    // EventLogLoader overrides
    //

    /**
     * Checks if either any {@link ome.services.eventlogs.EventLogQueue.Entry} instances are available or tries
     * to load them if not. Conditions which will lead this to return false
     * include: "stop" being set, the batch size being met, the current
     * id in the database being equivalent to the newest event log.
     */
    public boolean hasNext() {

        if (isStopSet()) {
            return false;
        }

        batchCount++;
        if (batchCount > batchSize) {
            batchCount = 0;
            return false;
        }

        if (data.hasNext()) {
            return true;
        }
        return load(lookup()) > 0;
    }

    /**
     * Return a wrapped version of {@link ome.services.eventlogs.EventLogQueue.Data#next()} which could possibly be
     * null.
     */
    public EventLog next() {
        nextCount.inc();
        return offer(data.next());
    }

    //
    // PersistentEventLogLoader
    //

    @Override
    protected EventLog query() {
        throw new UnsupportedOperationException();
    }

    /**
     * In general, {@link EventLogQueue} will intend to use much larger batch
     * sizes, and so further loops should likely not be attempted.
     */
    @Override
    public long more() {
        return 0;
    }

    /**
     * Do nothing.
     */
    @Override
    public void initialize() {
        // no-op
    }

    /**
     * Only schedule a new backlog event if there is no currently registered
     * event of that type.
     */
    @Override
    public boolean addEventLog(Class<? extends IObject> cls, long id) {
        final boolean debug = log.isDebugEnabled();
        final String type = cls.getName();
        final Data.Entries entries = data.entries(type);
        if (entries == null) {
            if (debug) {
                log.debug("Type not available for backlog:" + type);
            }
            return false;
        }

        Entry entry = entries.get(id);
        if (entry != null) {
            if (debug) {
                log.debug("Entry already scheduled:" + entry);
            }
            entry.skipped++;
           return false;
        } else {
            entry = new Entry(-1, type, id, "REINDEX", 0);
            entries.addPriority(entry);
            if (debug) {
                log.debug("New backlog entry:" + entry);
            }
            return true;
        }
    }
}
