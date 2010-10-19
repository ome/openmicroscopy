/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import ome.api.IDelete;
import ome.model.IObject;
import ome.security.basic.CurrentDetails;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Tree-structure containing all scheduled deletes which closely resembles the
 * tree structure of the {@link DeleteSpec} itself. All ids of the intended
 * deletes will be collected in a preliminary phase. This is necessary since
 * intermediate deletes, may disconnect the graph, causing later deletes to fail
 * if they were solely based on the id of the root element.
 *
 * The {@link DeleteState} instance can only be initialized with a graph of
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
 * @see ticket:3031
 * @see ticket:3032
 */
public class DeleteState {

    static class Tables {

        /**
         * Original data as returned by
         * {@link DeleteSpec#queryBackupIds(Session, int, DeleteEntry, QueryBuilder)}
         * References to these rows will be stored in {@link #pointers}.
         */
        final Map<DeleteEntry, List<List<Long>>> tables = new HashMap<DeleteEntry, List<List<Long>>>();

        /**
         * Pointers to the data in {@link #tables} which can be used to gather
         * together {@link #columnSets(DeleteEntry)} for further processing. If
         * {@link #tables} contained:
         *
         * <pre>
         * {
         *   entry#1:
         *      [
         *          [0, 1],
         *          [0, 10],
         *          [0, 15],
         *          [1, 20],
         *          [1, 25],
         *          [1, 35]
         *      ]
         * }
         * </pre>
         *
         * then {@link #pointers} would contain:
         *
         * <pre>
         * {
         *  entry#1:
         *      [
         *          [0,1,2],
         *          [3,4,5]
         *      ]
         *  }
         * </pre>
         *
         * A column-set consists of paths which only vary in their last element.
         */
        final Map<DeleteEntry, List<List<Integer>>> pointers = new HashMap<DeleteEntry, List<List<Integer>>>();

        public void add(DeleteEntry entry, List<List<Long>> results) {

            tables.put(entry, results);
            pointers.put(entry, new LinkedList<List<Integer>>());

            Set<Integer> exclude = new HashSet<Integer>();
            for (int r = 0; r < results.size(); r++) {

                if (exclude.contains(r)) {
                    continue; // Already handled.
                }

                // Take the current line as the basis
                // for comparison. Worst case, it will
                // form a column-set of length 1.
                List<Integer> pointer = new LinkedList<Integer>();
                pointer.add(r);

                List<Long> current = results.get(r);
                CHECK: for (int q = r + 1; q < results.size(); q++) {
                    if (exclude.contains(q)) {
                        continue; // Already handled.
                    }
                    List<Long> check = results.get(q);
                    for (int w = 0; w < Math.max(1, current.size() - 1); w++) {
                        if (!current.get(w).equals(check.get(w))) {
                            continue CHECK;
                        }
                    }
                    pointer.add(q);
                    exclude.add(q);
                }
                pointers.get(entry).add(pointer);
            }
        }

        public Iterator<List<List<Long>>> columnSets(final DeleteEntry entry,
                final List<Long> match) {

            final List<List<Integer>> ptrs = pointers.get(entry);
            final Iterator<List<Integer>> p = ptrs == null ? null : ptrs.iterator();
            final List<List<Long>> r = tables.get(entry);

            return new Iterator<List<List<Long>>>() {

                List<List<Long>> next = null;

                void load() {

                    LOOP: while (p != null && next == null && p.hasNext()) {
                        List<List<Long>> rv = new LinkedList<List<Long>>();
                        List<Integer> pointer = p.next();
                        for (Integer idx : pointer) {
                            List<Long> cols = r.get(idx);
                            if (match != null) {
                                for (int w = 0; w < match.size() - 1; w++) {
                                    if (!match.get(w).equals(cols.get(w))) {
                                        continue LOOP;
                                    }
                                }
                            }
                            rv.add(cols);
                        }
                        next = rv;
                    }
                }

                public boolean hasNext() {
                    load();
                    return next != null;
                }

                public List<List<Long>> next() {
                    load();
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return next;
                    } finally {
                        next = null;
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };

        }
    }

    private final static Log log = LogFactory.getLog(DeleteState.class);

    private final static String INVALIDATED = "INVALIDATED:";

    /**
     * List of each individual {@link DeleteStep} which this instance will
     * perform.
     */
    private final List<DeleteStep> steps = new ArrayList<DeleteStep>();

    /**
     * List of Maps of db table names to the ids actually deleted from that
     * table. The first entry of the list are the actual results. All later
     * elements are temporary views from some savepoint.
     *
     * TODO : refactor into {@link DeleteStep}
     */
    private final LinkedList<Map<String, Set<Long>>> actualIds = new LinkedList<Map<String, Set<Long>>>();

    /**
     * Map from table name to the {@link IObject} class which will be deleted
     * for raising the {@link EventLogMessage}.
     */
    private final Map<String, Class<IObject>> classes = new HashMap<String, Class<IObject>>();

    private final DeleteOpts opts = new DeleteOpts();

    private final OmeroContext ctx;

    private final Session session;

    /**
     * @param ctx
     *            Stored the {@link OmeroContext} instance for raising event
     *            during {@link #release(String)}
     * @param session
     *            non-null, active Hibernate session that will be used to delete
     *            all necessary items as well as lookup items for deletion.
     */
    public DeleteState(OmeroContext ctx, Session session, DeleteSpec spec)
            throws DeleteException {
        this.ctx = ctx;
        this.session = session;
        add(); // Set the actualIds size==1

        final Tables tables = new Tables();
        descend(spec, tables);

        final LinkedList<DeleteStep> stack = new LinkedList<DeleteStep>();
        parse(spec, tables, stack, null);
    }

    //
    // Initialization and id lookup
    //

    /**
     * Walk throw the sub-spec graph actually loading the ids which must be
     * scheduled for delete.
     *
     * @param session
     * @param spec
     * @param paths
     * @throws DeleteException
     */
    private void descend(DeleteSpec spec, Tables tables) throws DeleteException {

        final List<DeleteEntry> entries = spec.entries();

        for (int i = 0; i < entries.size(); i++) {

            final DeleteEntry entry = entries.get(i);
            if (entry.skip()) { // after opts.push()
                if (log.isDebugEnabled()) {
                    log.debug("Skipping " + entry);
                }
                continue;
            }

            final DeleteSpec subSpec = entry.getSubSpec();
            final List<List<Long>> results = spec.queryBackupIds(session, i,
                    entry, null);
            tables.add(entry, results);
            if (subSpec != null) {
                if (results.size() != 0) { // ticket:2823
                    descend(subSpec, tables);
                }
            }
        }
    }

    /**
     * Walk throw the sub-spec graph again, using the results provided to build
     * up a graph of {@link DeleteStep} instances.
     */
    private void parse(DeleteSpec spec, Tables tables,
            LinkedList<DeleteStep> stack, List<Long> match)
            throws DeleteException {

        final List<DeleteEntry> entries = spec.entries();

        for (int i = 0; i < entries.size(); i++) {
            final DeleteEntry entry = entries.get(i);
            final DeleteSpec subSpec = entry.getSubSpec();

            Iterator<List<List<Long>>> it = tables.columnSets(entry, match);
            while (it.hasNext()) {
                List<List<Long>> columnSet = it.next();
                if (columnSet.size() == 0) {
                    continue;
                }

                // For the spec containers, we create a single step
                // per column-set.

                if (subSpec != null) {
                    DeleteStep step = new DeleteStep(steps.size(), stack, spec,
                            entry, null);

                    stack.add(step);
                    parse(subSpec, tables, stack, columnSet.get(0));
                    stack.removeLast();
                    this.steps.add(step);
                } else {

                    // But for the actual entries, we create a step per
                    // individual row.
                    for (List<Long> cols : columnSet) {
                        DeleteStep step = new DeleteStep(steps.size(), stack,
                                spec, entry, cols);
                        this.steps.add(step);
                    }

                }

            }
        }
    }

    //
    // Found and deleted Ids
    //

    /**
     * Return the total number of ids loaded into this instance.
     */
    public long getTotalFoundCount() {
        return steps.size();
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
    void addDeletedIds(DeleteStep step) throws DeleteException {

        classes.put(step.table, step.iObjectType);
        Set<Long> set = lookup(step.table);
        set.add(step.id);

    }

    //
    // Iteration methods, used for actual deletes
    //

    /**
     *
     * @param step
     *            which step is to be invoked. Running a step multiple times is
     *            not supported.
     *
     * @return Any warnings which were noted during execution.
     * @throws DeleteException
     *             Any errors which were caused during execution. Which
     *             execution states may be encountered is strongly tied to the
     *             definition of the specification and to the options which are
     *             passed in during initialization.
     */
    public String execute(int j) throws DeleteException {

        final DeleteStep step = steps.get(j);

        if (step.savepoint != null && step.savepoint.startsWith(INVALIDATED)) {
            log.debug("Skipping closed savepoint: " + step.savepoint);
            return "";
        }

        if (step.rollbackOnly) {
            if (step.savepoint != null) {
                rollback(step);
            }
            log.debug("Skipping due to rollbackOnly: " + step);
            return ""; // EARLY EXIT
        }

        if (step.ids == null) { // Finalization marker
            if (step.savepoint != null) {
                release(step); // We know it's not rollback only.
            }
            return ""; // EARLY EXIT
        }

        // Add this instance to the opts. Any method which then tries to
        // ask the opts for the current state will have an accurate view.
        step.push(opts);

        try {

            // Lazy initialization of parents.
            // To guarantee that finalization
            // happens (#3125, #3130), a special
            // marker is added and handled above.
            for (DeleteStep parent : step.stack) {
                if (parent.savepoint == null) {
                    savepoint(parent);
                }
            }
            savepoint(step);

            final QueryBuilder nullOp = optionalNullBuilder(step);
            final QueryBuilder qb = queryBuilder(step);

            try {

                // Phase 1: top-levels
                if (step.stack.size() <= 1) {
                    StopWatch swTop = new CommonsLogStopWatch();
                    step.spec.runTopLevel(session,
                            Arrays.<Long> asList(step.id));
                    swTop.stop("omero.delete.top." + step.id);
                }

                // Phase 2: NULL
                optionallyNullField(session, nullOp, step.id);

                // Phase 3: primary delete
                StopWatch swStep = new CommonsLogStopWatch();
                qb.param("id", step.id);
                Query q = qb.query(session);
                int count = q.executeUpdate();
                if (count > 0) {
                    addDeletedIds(step);
                }
                logResults(step, count);
                swStep.lap("omero.delete." + step.table + "." + step.id);

                // Finalize.
                release(step);
                return "";

            } catch (ConstraintViolationException cve) {
                return handleConstraintViolation(step, cve);
            }

        } finally {
            step.pop(opts);
        }
    }

    private void logResults(final DeleteStep step, final int count) {
        if (count > 0) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleted %s from %s: root=%s", step.id,
                        step.pathMsg, step.entry.getId()));
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Missing delete %s from %s: root=%s",
                        step.id, step.pathMsg, step.entry.getId()));
            }
        }
    }

    /**
     * Method called when a {@link ConstraintViolationException} can be thrown.
     * This is both during
     * {@link #delete(Session, CurrentDetails, ExtendedMetadata, String, DeleteState, List, DeleteOpts)}
     * and
     * {@link #execute(Session, CurrentDetails, ExtendedMetadata, DeleteState, List, DeleteOpts)}
     * .
     *
     * @param session
     * @param opts
     * @param type
     * @param rv
     * @param cve
     */
    private String handleConstraintViolation(final DeleteStep step,
            ConstraintViolationException cve) throws DeleteException {

        // First, immediately rollback the current savepoint.
        rollback(step);

        String cause = "ConstraintViolation: " + cve.getConstraintName();
        String msg = String.format("Could not delete softly %s: %s due to %s",
                step.pathMsg, step.id, cause);

        // If this entry is "SOFT" then there's nothing
        // special we need to do.
        if (step.entry.isSoft()) {
            log.debug(msg);
            return cause;
        }

        // Otherwise calculate if there is any "SOFT" setting about this
        // location in the graph, and clean up all of the related entries.
        // As we check down the stack, we can safely call rollback since
        // the only other option is to rollback the entire transaction.
        for (int i = step.stack.size() - 1; i >= 0; i--) {
            DeleteStep parent = step.stack.get(i);
            rollback(parent);
            if (parent.entry.isSoft()) {
                disableRelatedEntries(parent);
                log.debug(String.format("%s. Handled by %s: %s", msg,
                        parent.pathMsg, parent.id));
                return cause;
            }
        }

        log.info(String.format("Failed to delete %s: %s due to %s",
                step.pathMsg, step.id, cause));
        throw cve;

    }

    /**
     * Finds all {@link DeleteStep} instances in {@link #steps} which have the
     * given {@link DeleteStep} argument in their {@link DeleteStep#stack} which
     * amounts to being a descedent. All such instances are set to null in
     * {@link #steps} so that further processing cannot take place on them.
     */
    private void disableRelatedEntries(DeleteStep parent) {
        for (DeleteStep step : steps) {
            if (step == null || step.stack == null) {
                continue;
            } else if (step.stack.contains(parent)) {
                step.rollbackOnly = true;
            }
        }
    }

    private QueryBuilder optionalNullBuilder(final DeleteStep step) {
        QueryBuilder nullOp = null;
        if (step.entry.isNull()) { // WORKAROUND see #2776, #2966
            // If this is a null operation, we don't want to delete the row,
            // but just modify a value. NB: below we also prevent this from
            // being raised as a delete event. TODO: refactor out to Op
            nullOp = new QueryBuilder();
            nullOp.update(step.table);
            nullOp.append("set relatedTo = null ");
            nullOp.where();
            nullOp.and("relatedTo.id = :id");
        }
        return nullOp;
    }

    private void optionallyNullField(Session session,
            final QueryBuilder nullOp, Long id) {
        if (nullOp != null) {
            nullOp.param("id", id);
            Query q = nullOp.query(session);
            int updated = q.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Nulled " + updated + " Pixels.relatedTo fields");
            }
        }
    }

    private QueryBuilder queryBuilder(DeleteStep step) {
        final QueryBuilder qb = new QueryBuilder();
        qb.delete(step.table);
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(step.ec, qb);
        }
        return qb;
    }

    /**
     * Appends a clause to the {@link QueryBuilder} based on the current user.
     *
     * If the user is an admin like root, then nothing is appened, and any
     * delete is permissible. If the user is a leader of the current group, then
     * the object must be in the current group. Otherwise, the object must
     * belong to the current user.
     */
    public static void permissionsClause(EventContext ec, QueryBuilder qb) {
        if (!ec.isCurrentUserAdmin()) {
            if (ec.getLeaderOfGroupsList().contains(ec.getCurrentGroupId())) {
                qb.and("details.group.id = :gid");
                qb.param("gid", ec.getCurrentGroupId());
            } else {
                // This is only a regular user, then the object must belong to
                // him/her
                qb.and("details.owner.id = :oid");
                qb.param("oid", ec.getCurrentUserId());
            }
        }
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

    public String savepoint(DeleteStep step) {
        add();
        step.savepoint = UUID.randomUUID().toString();
        step.savepoint = step.savepoint.replaceAll("-", "");
        call(session, "SAVEPOINT DEL", step.savepoint);
        log.debug(String.format("Enter savepoint %s: new depth=%s",
                step.savepoint,
                actualIds.size()));
        return step.savepoint;
    }

    public void release(DeleteStep step) throws DeleteException {

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

        call(session, "RELEASE SAVEPOINT DEL", step.savepoint);

        log.debug(String.format(
                "Released savepoint %s with %s ids: new depth=%s", step.savepoint,
                count, actualIds.size()));

        step.savepoint = INVALIDATED + step.savepoint;

    }

    public void rollback(DeleteStep step) throws DeleteException {

        if (actualIds.size() == 0) {
            throw new DeleteException("Release at depth 0!");
        }

        step.rollbackOnly = true;
        int count = 0;
        Map<String, Set<Long>> ids = actualIds.removeLast();
        for (String key : ids.keySet()) {
            Set<Long> old = ids.get(key);
            count += old.size();
        }

        call(session, "ROLLBACK TO SAVEPOINT DEL", step.savepoint);

        log.debug(String.format(
                "Rolled back savepoint %s with %s ids: new depth=%s",
                step.savepoint, count, actualIds.size()));

        step.savepoint =  INVALIDATED + step.savepoint;

    }

    //
    // Helpers
    //

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n");
        for (int i = 0; i < steps.size(); i++) {
            DeleteStep step = steps.get(i);
            sb.append(i);
            sb.append(":");
            if (step == null) {
                sb.append("null");
            } else {
                sb.append(step.pathMsg);
                sb.append("==>");
                sb.append(step.id);
                sb.append(" ");
                sb.append("[");
                sb.append(step.stack);
                sb.append("]");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
