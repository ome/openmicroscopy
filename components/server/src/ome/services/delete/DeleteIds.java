/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.api.IDelete;
import ome.model.IObject;
import ome.services.messages.EventLogMessage;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Wrapper around a map from {@link DeleteSpec} to lists of ids which will be
 * collected in a preliminary phase of delete as well as those which are
 * actually deleted.
 * 
 * This is necessary since intermediate deletes, may disconnect the graph,
 * causing later deletes to fail if they were solely based on the id of the root
 * element.
 * 
 * The {@link DeleteIds} instance can only be initialized with a graph of
 * initialized {@DeleteSpec}s.
 * 
 * To handle SOFT requirements, each new attempt to delete either a node or a
 * leaf in the subgraph is surrounded by a savepoint. Ids added during a
 * savepoint (or a sub-savepoint) or only valid until release is called, at
 * which time they are merged into the final view.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 * @see ticket:3032
 */
public class DeleteIds {

    private final static Log log = LogFactory.getLog(DeleteIds.class);

    private final Map<DeleteSpec, List<List<Long>>> ids = new HashMap<DeleteSpec, List<List<Long>>>();

    /**
     * List of Maps of db table names to the ids actually deleted from that
     * table. The first entry of the list are the actual results. All later
     * elements are temporary views from some savepoint.
     */
    private final LinkedList<Map<String, Set<Long>>> actualIds = new LinkedList<Map<String, Set<Long>>>();

    /**
     * Map from table name to the {@link IObject} class which will be deleted
     * for raising the {@link EventLogMessage}.
     */
    private final Map<String, Class<IObject>> classes = new HashMap<String, Class<IObject>>();

    private final OmeroContext ctx;

    private final Session session;

    /**
     * Total count of objects found.
     */
    private long foundCount = 0;

    public DeleteIds(OmeroContext ctx, Session session, DeleteSpec spec)
            throws DeleteException {
        this.ctx = ctx;
        this.session = session;

        add();
        List<String[]> paths = new ArrayList<String[]>();
        StopWatch sw = new CommonsLogStopWatch();
        load(spec, paths);
        paths = Collections.unmodifiableList(paths);

        Iterator<DeleteSpec> it = spec.walk();
        while (it.hasNext()) {
            DeleteSpec subSpec = it.next();
            List<List<Long>> ids = subSpec.backupIds(session, paths);
            for (List<Long> list : ids) {
                foundCount += list.size();
            }
            this.ids.put(subSpec, ids);
        }
        sw.stop("omero.delete.ids");
    }

    private void load(DeleteSpec spec, List<String[]> paths) {
        for (DeleteEntry entry : spec.entries()) {
            DeleteSpec subSpec = entry.getSubSpec();
            if (subSpec == null) {
                String[] p = entry.path(spec.getSuperSpec());
                paths.add(p);
            } else {
                load(subSpec, paths);
            }
        }
    }

    /**
     * Return the total number of ids loaded into this instance.
     */
    public long getTotalFoundCount() {
        return foundCount;
    }

    /**
     * Return the total number of ids which were deleted. This is calculated by
     * taking the only the completed savepoints into account.
     */
    public long getTotalDeletedCount() {
        int count = 0;
        for (Map.Entry<String, Set<Long>> entry : actualIds.getFirst()
                .entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    /**
     * Return the list of ids which have been detected for deletion. These may
     * or may not eventually be deleted due to options and database constraints.
     * 
     * @see #getDeletedsIds(String)
     */
    public List<Long> getFoundIds(DeleteSpec deleteSpec, int step) {
        return ids.get(deleteSpec).get(step);
    }

    /**
     * Get the set of ids which were actually deleted. See
     * {@link #addAll(String, Class, List)}
     */
    public Set<Long> getDeletedsIds(String table) {
        Set<Long> set = lookup(table);
        if (set == null) {
            return new HashSet<Long>();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * Add the actually deleted ids to the current savepoint.
     *
     * It is critical that these ids are actually deleted and that any failure
     * for them to be removed will cause the entire transaction to fail (in
     * which case these ids will be ignored).
     * 
     * @throws DeleteException
     *             thrown if the {@link EventLogMessage} raised fails.
     */
    public void addDeletedIds(String table, Class<IObject> k, long id)
            throws DeleteException {

        classes.put(table, k);
        Set<Long> set = lookup(table);
        set.add(id);

    }

    //
    // Transactions
    //

    private void call(Session session, String call, String savepoint) {
        try {
            session.connection().prepareCall(call + savepoint).execute();
        } catch (Exception e) {
            RuntimeException re = new RuntimeException("Failed to '" + call
                    + savepoint + "'");
            re.initCause(e);
            throw re;
        }
    }

    public String savepoint() throws DeleteException {
        add();
        String savepoint = UUID.randomUUID().toString();
        savepoint = savepoint.replaceAll("-", "");
        call(session, "SAVEPOINT DEL", savepoint);
        log.debug(String.format("Enter savepoint %s: new depth=%s",
                savepoint, actualIds.size()));
        return savepoint;
    }

    public void release(String savepoint) throws DeleteException {

        if (actualIds.size() == 0) {
            throw new DeleteException("Release at depth 0!");
        }

        // Update the next map up with the current values
        int count = 0;
        Map<String, Set<Long>> ids = actualIds.removeLast();
        for (Map.Entry<String, Set<Long>> entry : ids.entrySet()) {
            String key = entry.getKey();
            Map<String, Set<Long>> last = actualIds.getLast();
            Set<Long> old = last.get(key);
            Set<Long> neu = entry.getValue();
            count += neu.size();
            if (old == null) {
                last.put(key, neu);
            } else {
                old.addAll(neu);
            }
        }

        // If this is the last map, i.e. the truly deleted ones, then
        // raise the EventLogMessage
        if (actualIds.size() == 0) {
            for (Map.Entry<String, Set<Long>> entry : ids.entrySet()) {
                String key = entry.getKey();
                Class<IObject> k = classes.get(key);

                EventLogMessage elm = new EventLogMessage(this, "DELETE", k,
                        new ArrayList<Long>(entry.getValue()));

                try {
                    ctx.publishMessage(elm);
                } catch (Throwable t) {
                    DeleteException de = new DeleteException(
                            "EventLogMessage failed.");
                    de.initCause(t);
                    throw de;
                }

            }

        }

        call(session, "RELEASE SAVEPOINT DEL", savepoint);

        log.debug(String.format("Released savepoint %s with %s ids: new depth=%s",
                savepoint, count, actualIds.size()));

    }

    public void rollback(String savepoint) throws DeleteException {
        if (actualIds.size() == 0) {
            throw new DeleteException("Release at depth 0!");
        }

        int count = 0;
        Map<String, Set<Long>> ids = actualIds.removeLast();
        for (String key : ids.keySet()) {
            Set<Long> old = ids.get(key);
            count += old.size();
        }

        call(session, "ROLLBACK TO SAVEPOINT DEL", savepoint);

        log.debug(String.format("Rolled back savepoint %s with %s ids: new depth=%s",
                savepoint, count, actualIds.size()));

    }

    //
    //
    // Helpers

    /**
     * Lookup and initialize if necessary a {@link Set<Long>} for the given
     * table.
     * 
     * @param table
     * @return
     */
    private Set<Long> lookup(String table) {
        Set<Long> set = actualIds.getLast().get(table);
        if (set == null) {
            set = new HashSet<Long>();
            actualIds.getLast().put(table, set);
        }
        return set;
    }

    private void add() {
        actualIds.add(new HashMap<String, Set<Long>>());
    }

}
