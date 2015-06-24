/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.model.IObject;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

/**
 * Single action performed by {@link GraphState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class GraphStep {

    public interface Callback {

        void add();

        void addGraphIds(GraphStep step);

        void savepoint(String savepoint);

        int size();

        void rollback(String savepoint, int count) throws GraphException;

        int collapse(boolean keep);

        void release(String savepoint, int count) throws GraphException;

        Class<IObject> getClass(String key);

        Iterable<Map.Entry<String, Set<Long>>> entrySet();

    }

    private static Logger log = LoggerFactory.getLogger(GraphStep.class);

    /**
     * Used to mark {@link #savepoint} after usage.
     */
    private final static String INVALIDATED = "INVALIDATED_";

    protected final ExtendedMetadata em;

    /**
     * Location of this step in {@link GraphState#steps}.
     */
    public final int idx;

    /**
     * Stack of other {@link GraphStep} instances which show where this step is
     * in the entire graph.
     */
    public final LinkedList<GraphStep> stack;

    /**
     * Final member of {@link #stack} which is the direct ancestor of this
     * step.
     */
    public final GraphStep parent;

    /**
     * {@link GraphSpec} instance which is active for this step.
     */
    public final GraphSpec spec;

    /**
     * {@link GraphEntry} instance which is active for this step.
     */
    public final GraphEntry entry;

    /**
     * Ids of each element in the path to this node. For example, if we are
     * querying /Dataset/DatasetImageLink/Image then this contains: [4, 2, 1]
     * where 4 is the id of the dataset, and 1 is the id of the image.
     */
    private final long[] ids;

    /**
     * The actual id to be processed as opposed to {@link GraphEntry#getId()}
     * which is the id of the root object.
     *
     * @see #ids
     */
    public final long id;

    /**
     * Parsed table name used for the SQL/HQL statements.
     */
    public final String table;

    /**
     * Type of object which is being processed, using during
     * {@link GraphState#release(String)} to send an {@link EventLogMessage}.
     */
    public final Class<IObject> iObjectType;

    /**
     * String representation of the path to this {@link GraphEntry} used for
     * logging.
     */
    public final String pathMsg;

    /**
     * An event context which has been initialized for a particular group, i.e.
     * even we are currently in an omero.group=-1 context, the group of the
     * object in question will be returned by {@link EventContext#getCurrentGroupId()}.
     *
     * See 8723
     */
    public/* final */EventContext ec;

    /**
     * Not final. Set during {@link GraphState#execute(int)}. If anything goes
     * wrong, it and possibly other instances from {@link #stack} will have
     * their savepoints rolled back. Re-used during validation.
     * See {@link #validation()}
     */
    private String savepoint = null;

    /**
     * Re-used during validation.
     * See {@link #validation()}
     */
    private boolean rollbackOnly = false;

    public GraphStep(ExtendedMetadata em, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        this.em = em;
        this.idx = idx;
        this.stack = new LinkedList<GraphStep>(stack);
        if (this.stack.size() > 0) {
            this.parent = this.stack.getLast();
        } else {
            this.parent = null;
        }
        this.spec = spec;
        this.entry = entry;
        this.ids = ids;
        this.id = ids == null ? -1L : ids[ids.length - 1];

        if (entry != null) {
            final String[] path = entry.path(entry.getSuperSpec());
            table = path[path.length - 1];
            pathMsg = StringUtils.join(path, "/");
            iObjectType = spec.getHibernateClass(table);
        } else {
            table = null;
            pathMsg = null;
            iObjectType = null;
        }
    }

    public void setEventContext(EventContext ec) {
        this.ec = ec;
    }

    /**
     * Currently returns the ID array without copying
     * therefore values should not be leaked to code outside
     * of the ome.services.graphs hierarchy. Non-copying is
     * primarily intended to reduce GC overhead.
     */
    public long[] getIds() {
        return this.ids;
    }

    //
    // Main action
    //

    /**
     * Primary action which should perform the main modifications required by
     * the step.
     */
    public abstract void action(Callback cb, Session session, SqlAction sql,
            GraphOpts opts) throws GraphException;



    /**
     * Action performed at the end of the transaction to give all rows the
     * chance to invalidate other actions.
     *
     * @param graphState
     * @param session
     * @param sql
     * @param opts
     */
    public void validate(GraphState graphState, Session session, SqlAction sql,
            GraphOpts opts) throws GraphException {
        // no-op
    }

    protected void logPhase(String phase) {
        log.debug(String.format("%s %s from %s: root=%s", phase, id,
                pathMsg, entry.getId()));
    }

    protected void logResults(final int count) {
        if (count > 0) {
            if (log.isDebugEnabled()) {
                logPhase("Processed");
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Missing object %s from %s: root=%s",
                        id, pathMsg, entry.getId()));
            }
        }
    }

    //
    // Stack
    //

    public void push(GraphOpts opts) throws GraphException {
        for (GraphStep parent : stack) {
            parent.entry.push(opts, parent.ec);
        }
        entry.push(opts, ec);
    }

    public void pop(GraphOpts opts) {
        for (GraphStep parent : stack) {
            parent.entry.pop(opts);
        }
        entry.pop(opts);
    }

    //
    // Transactions
    //

    public void rollbackOnly() {
        rollbackOnly = true;
    }

    public boolean hasSavepoint() {
        return savepoint != null;
    }

    public void validation() {
        savepoint = null;
        rollbackOnly = false;
    }

    public String start(Callback cb) throws GraphException {
        if (savepoint != null && savepoint.startsWith(INVALIDATED)) {
            log.debug("Skipping closed savepoint: " + savepoint);
            return "";
        }

        if (rollbackOnly) {
            if (savepoint != null) {
                rollback(cb);
            }
            log.debug("Skipping due to rollbackOnly: " + this);
            return ""; // EARLY EXIT
        }

        if (ids == null) { // Finalization marker
            if (savepoint != null) {
                release(cb); // We know it's not rollback only.
            }
            return ""; // EARLY EXIT
        }

        return null; // ALL NORMAL!
    }

    public String savepoint(Callback cb) {
        cb.add();
        savepoint = UUID.randomUUID().toString();
        savepoint = savepoint.replaceAll("-", "");
        cb.savepoint(savepoint);
        return savepoint;
    }

    /**
     * Return false if the current action (release or rollback) should be
     * skipped or throw an exception if the current state is invalid. Otherwise,
     * return true.
     *
     * Note:
     * <p>
     * Ok for cb.savepoint to already be invalidated since a second child entry
     * might also fail and attempt a rollback. But if it has been invalidated,
     * then there's no expectation that cb.size() be greater than 0.
     * </p>
     * @param cb
     * @throws GraphException
     */
    private boolean sanityCheck(Callback cb) throws GraphException {
        if (savepoint.startsWith(INVALIDATED)) {
            return false;
        }
        if (cb.size() == 0) {
            throw new GraphException("Action at depth 0!");
        }
        return true;
    }

    public void release(Callback cb) throws GraphException {

        if (!sanityCheck(cb)) {
            return;
        }

        int count = cb.collapse(true);

        // If this is the last map, i.e. the truly processed ones, then
        // raise the EventLogMessage
        if (cb.size() == 1) {
            for (Map.Entry<String, Set<Long>> entry : cb.entrySet()) {
                String key = entry.getKey();
                Class<IObject> k = cb.getClass(key);
                onRelease(k, entry.getValue());
            }
        }
        cb.release(savepoint, count);
        savepoint = INVALIDATED + savepoint;
    }

    public abstract void onRelease(Class<IObject> k, Set<Long> ids) throws GraphException;

    public void rollback(Callback cb) throws GraphException {

        if (!sanityCheck(cb)) {
            return;
        }

        int count = cb.collapse(false);
        rollbackOnly = true;
        cb.rollback(savepoint, count);
        savepoint =  INVALIDATED + savepoint;
    }

    //
    // Validation
    //


    /**
     * Immediately we check that an object moved from GroupA to GroupB
     * is no longer pointed at by any objects in GroupA via foreign key
     * constraints. This is what the DB does for us inherently on delete.
     *
     *
     * NB: After all objects are moved, we need to perform the reverse
     * check, which is that no object in GroupB points at any objects in
     * GroupA, i.e. all necessary objects were moved.
     * @param session
     * @throws GraphConstraintException
     * @see ticket:6442
     */
    protected void graphValidation(Session session) throws GraphConstraintException {

        int total = 0;
        Class<? extends IObject> x = iObjectType;
        final HashMultimap<String, Long> constraints = HashMultimap.create();
        while (true) {

            final String[][] locks = em.getLockChecks(x);

            for (String[] lock : locks) {
                List<Long> bad = findImproperIncomingLinks(session, lock);
                if (CollectionUtils.isNotEmpty(bad)) {
                    log.warn(String.format("%s:%s improperly linked by %s.%s: %s",
                            iObjectType.getSimpleName(), id, lock[0], lock[1],
                            bad.size()));
                    total += bad.size();

                    // TODO: Have both the source and the target IDs as a
                    // workaround even though iObjectType hasn't done anything
                    // "wrong".
                    constraints.putAll(lock[0], bad);
                    constraints.put(iObjectType.getSimpleName(), id);
                }
            }

            Class<?> y = x.getSuperclass();
            if (IObject.class.isAssignableFrom(y)) {
                x = (Class<IObject>) y;
                continue;
            }
            break;
        }

        if (total > 0) {
            throw new GraphConstraintException(String.format("%s:%s improperly linked by %s objects",
                iObjectType.getSimpleName(), id, total), constraints);
        }
    }

    // TODO
    protected List<Long> findImproperIncomingLinks(Session session, String[] lock) {
        return null;
    }

    /*
     * Workaround for refactoring to {@link GraphState}.
     *
     * If more logic is needed by subclasses, then
     * {@link #queryBackupIds(Session, int, GraphEntry, QueryBuilder)}
     * should no longer return list of ids, but rather a "Action" class
     * so that it can inject its own logic as needed, though it would be necessary
     * to give that method its place in the graph to detect "top-ness".
     */
    protected void deleteAnnotationLinks(AnnotationGraphSpec aspec, Session session, List<Long> ids) {
        String sspec = aspec.getSuperSpec();
        if (sspec == null || sspec.length() == 0) {
            if (ids != null && ids.size() > 0) {
                StopWatch swTop = new Slf4JStopWatch();

                QueryBuilder qb = new QueryBuilder();
                qb.delete("ome.model.IAnnotationLink"); // FIXME
                qb.where();
                qb.and("child.id in (:ids)");
                qb.paramList("ids", ids);
                // ticket:2962
                aspec.permissionsClause(ec, qb, false);

                Query q = qb.query(session);
                int count = q.executeUpdate();
                log.info("Deleted " + count + " annotation links");
                swTop.stop("omero.graphstep.deleteannotationlinks." + id);
            }
        }
    }

    protected QueryBuilder optionalNullBuilder() {
        QueryBuilder nullOp = null;
        if (entry.isNull()) { // WORKAROUND see #2776, #2966
            // If this is a null operation, we don't want to modify the source
            // row,
            // but modify a second row pointing at the source row via a FK.
            //
            // NB: below we also prevent this from
            // being raised as an event. TODO: refactor out to Op
            //
            if (!table.contains("Job")) {
                nullOp = new QueryBuilder();
                nullOp.update(table);
                nullOp.append("set relatedTo = null ");
                nullOp.where();
                nullOp.and("relatedTo.id = :id");
            }
        }
        return nullOp;
    }

    protected void optionallyNullField(Session session, Long id) {
        final QueryBuilder nullOp = optionalNullBuilder();
        if (nullOp != null) {
            nullOp.param("id", id);
            Query q = nullOp.query(session);
            int updated = q.executeUpdate();
            if (log.isDebugEnabled()) {// FIXME: logging.
                log.debug("Nulled " + updated + " Pixels.relatedTo fields");
            }
        }
    }

}
