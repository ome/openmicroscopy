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
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IDelete;
import ome.model.IObject;
import ome.services.messages.EventLogMessage;
import ome.system.OmeroContext;

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
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteIds {

    private final Map<DeleteSpec, List<List<Long>>> ids = new HashMap<DeleteSpec, List<List<Long>>>();

    /**
     * Map of db table names to the ids actually deleted from that table.
     */
    private final Map<String, Set<Long>> actualIds = new HashMap<String, Set<Long>>();

    private final OmeroContext ctx;

    /**
     * Total count of objects found.
     */
    private long foundCount = 0;

    /**
     * Total count of objects deleted.
     */
    private long deletedCount = 0;

    public DeleteIds(OmeroContext ctx, Session session, DeleteSpec spec)
            throws DeleteException {
        this.ctx = ctx;

        StopWatch sw = new CommonsLogStopWatch();
        List<String[]> paths = new ArrayList<String[]>();
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
     * Return the total number of ids which were deleted.
     */
    public long getTotalDeletedCount() {
        return deletedCount;
    }
    
    /**
     * Return the list of ids which have been detected for deletion. These may
     * or may not eventually be deleted due to options and database
     * constraints.
     * 
     * @see #getDeletedsIds(String)
     */
    public List<Long> getFoundIds(DeleteSpec deleteSpec, int step) {
        return ids.get(deleteSpec).get(step);
    }

    /**
     * Get the set of ids which were actually deleted. See {@link #addAll(String, Class, List)}
     */
    public Set<Long> getDeletedsIds(String table) {
        Set<Long> set = actualIds.get(table);
        if (set == null) {
            return new HashSet<Long>();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * Add the actually deleted ids. It is critical that these ids are actually
     * deleted and that any failure for them to be removed will cause the entire
     * transaction to fail (in which case these ids will be ignored).
     * 
     * @throws DeleteException
     *             thrown if the {@link EventLogMessage} raised fails.
     */
    public void addDeletedIds(String table, Class<IObject> k, List<Long> ids)
            throws DeleteException {
        Set<Long> set = lookup(table);
        set.addAll(ids);
        deletedCount += ids.size();

        EventLogMessage elm = new EventLogMessage(this, "DELETE", k, ids);
        try {
            ctx.publishMessage(elm);
        } catch (Throwable t) {
            DeleteException de = new DeleteException("EventLogMessage failed.");
            de.initCause(t);
            throw de;
        }

    }

    /**
     * Lookup and initialize if necessary a {@link Set<Long>} for the given
     * table.
     * 
     * @param table
     * @return
     */
    private Set<Long> lookup(String table) {
        Set<Long> set = actualIds.get(table);
        if (set == null) {
            set = new HashSet<Long>();
            actualIds.put(table, set);
        }
        return set;
    }

}
