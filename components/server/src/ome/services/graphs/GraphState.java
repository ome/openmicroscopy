/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.OverUsageException;
import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.SimpleEventContext;
import ome.util.SqlAction;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.engine.LoadQueryInfluencers;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tree-structure containing all scheduled actions which closely resembles the
 * tree structure of the {@link GraphSpec} itself. All ids of the intended
 * actions will be collected in a preliminary phase. This is necessary since
 * intermediate actions may disconnect the graph, causing later actions to fail
 * if they were solely based on the id of the root element.
 *
 * The {@link GraphState} instance can only be initialized with a graph of
 * initialized {@GraphSpec}s.
 *
 * To handle SOFT requirements, each new attempt to process either a node or a
 * leaf in the subgraph is surrounded by a savepoint. Ids added during a
 * savepoint (or a sub-savepoint) are only valid until release is called, at
 * which time they are merged into the final view.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphState implements GraphStep.Callback {

    private final static Logger log = LoggerFactory.getLogger(GraphState.class);
    /**
     * List of each individual {@link GraphStep} which this instance will
     * perform. These are generated during
     * {@link GraphState#GraphState(GraphStepFactory, SqlAction, Session, GraphSpec)}
     * first by making calls to
     * {@link GraphStepFactory#create(int, List, GraphSpec, GraphEntry, long[])}
     * and then by giving the factory a chance to insert elements via
     * {@link GraphStepFactory#postProcess(List)}.
     *
     *  The instance once set is {@link Collections#unmodifiableList(List) unmodifiable}.
     */
    private final GraphSteps steps;

    /**
     * List of Maps of db table names to the ids actually processed from that
     * table. The first entry of the list is the actual results. All later
     * elements are temporary views from some savepoint.
     *
     * TODO : refactor into {@link GraphStep}
     */
    private final LinkedList<Map<String, Set<Long>>> actualIds = new LinkedList<Map<String, Set<Long>>>();

    /**
     * Map from table name to the {@link IObject} class which will be processed
     * for raising the {@link EventLogMessage}.
     */
    private final Map<String, Class<IObject>> classes = new HashMap<String, Class<IObject>>();

    private final GraphOpts opts = new GraphOpts();

    private final Session session;

    private final SqlAction sql;

    /**
     *
     * @param Base {@link EventContext} instance which will be used to create a
     *           special {@link EventContext} based on the current graph. This second
     *           instance will be passed to each created step via
     *           {@link GraphSpec#setEventContext(EventContext)}.
     *
     * @param ctx
     *            Stored the {@link OmeroContext} instance for raising event
     *            during {@link #release(String)}
     * @param session
     *            non-null, active Hibernate session that will be used to process
     *            all necessary items as well as lookup items for processing.
     */
    public GraphState(EventContext ec, GraphStepFactory factory, SqlAction sql,
        Session session, GraphSpec spec) throws GraphException {

        this.sql = sql;
        this.session = session;

        add(); // Set the actualIds size==1

        final List<GraphStep> steps = new ArrayList<GraphStep>();
        final GraphTables tables = new GraphTables();

        // Making use of an internal Hibernate API. The issue here is that we
        // must temporarily remove all filters (whose names it isn't easy to
        // find) and then replace them *without* starting from the raw
        // definitions, since that requires re-setting the parameters. Longer
        // term, it may be necessary to keep track with this state ourselves,
        // and provide an enable/disable method.
        final LoadQueryInfluencers infl = ((SessionImplementor) session).getLoadQueryInfluencers();
        @SuppressWarnings("unchecked")
        final Map<String, Filter> filters = infl.getEnabledFilters();
        final Map<String, Filter> copy = new HashMap<String, Filter>(filters);
        try {
            filters.clear();
            descend(session, steps, spec, tables);
        } finally {
            filters.putAll(copy);
        }

        final LinkedList<GraphStep> stack = new LinkedList<GraphStep>();
        parse(factory, steps, spec, tables, stack, null);

        // Find the group for the object in question and create an
        // EventContext that will be assigned to each step.
        final ExperimenterGroup g = spec.groupInfo(sql);
        if (g == null) {
            throw new GraphException("No group information found. Does object exist? " + spec);
        }

        final EventContext gec = new SimpleEventContext(ec) {
            @Override
            protected void copy(EventContext ec) {
                super.copy(ec);
                this.cgId = g.getId();
                this.cgName = g.getName();
                setGroupPermissions(g.getDetails().getPermissions());
            }
        };

        // Post-process and lock.
        this.steps = factory.postProcess(steps);
        for (GraphStep step : this.steps) {
            step.setEventContext(gec);
        }
    }

    //
    // Initialization and id lookup
    //

    /**
     * Walk through the sub-spec graph actually loading the ids which must be
     * scheduled for processing. Also responsible for adding the
     * {@link EventContext} to each {@link GraphSpec}.
     *
     * @param spec
     * @param paths
     * @throws GraphException
     */
    private static void descend(Session session, List<GraphStep> steps, GraphSpec spec, GraphTables tables) throws GraphException {

        final List<GraphEntry> entries = spec.entries();

        for (int i = 0; i < entries.size(); i++) {

            final GraphEntry entry = entries.get(i);
            if (entry.skip()) { // after opts.push()
                if (log.isDebugEnabled()) {
                    log.debug("Skipping " + entry);
                }
                continue;
            }

            final GraphSpec subSpec = entry.getSubSpec();

            final long[][] results = spec.queryBackupIds(session, i, entry, null);
            tables.add(entry, results);
            if (subSpec != null) {
                if (results.length != 0) { // ticket:2823
                    descend(session, steps, subSpec, tables);
                }
            }
        }
    }

    /**
     * Walk through the sub-spec graph again, using the results provided to build
     * up a graph of {@link GraphStep} instances.
     */
    private static void parse(GraphStepFactory factory, List<GraphStep> steps, GraphSpec spec, GraphTables tables,
            LinkedList<GraphStep> stack, long[] match)
            throws GraphException {

        final List<GraphEntry> entries = spec.entries();

        for (int i = 0; i < entries.size(); i++) {
            final GraphEntry entry = entries.get(i);
            final GraphSpec subSpec = entry.getSubSpec();

            Iterator<List<long[]>> it = tables.columnSets(entry, match);
            while (it.hasNext()) {
                List<long[]> columnSet = it.next();
                if (columnSet.size() == 0) {
                    continue;
                }

                // For the spec containers, we create a single step
                // per column-set.

                // TODO: This extra graph step is for the superspec is causing
                // the counts in scheduled vs. actual steps to differ!

                if (subSpec != null) {
                    GraphStep step = factory.create(steps.size(), stack, spec,
                            entry, null);

                    stack.add(step);
                    parse(factory, steps, subSpec, tables, stack, columnSet.get(0));
                    stack.removeLast();
                    steps.add(step);
                } else {

                    // But for the actual entries, we create a step per
                    // individual row.
                    for (long[] cols : columnSet) {
                        GraphStep step = factory.create(steps.size(), stack,
                                spec, entry, cols);
                        steps.add(step);
                    }

                }

            }
        }
    }

    //
    // Found and processed Ids
    //

    /**
     * Return the total number of ids loaded into this instance.
     */
    public int getTotalFoundCount() {
        return steps.size();
    }

    public GraphStep getStep(int i) {
        return steps.get(i);
    }

    public GraphOpts getOpts() {
        return opts;
    }

    /**
     * Return the total number of ids which were processed. This is calculated by
     * taking the only the completed savepoints into account.
     */
    public int getTotalProcessedCount() {
        long count = 0;
        for (Map.Entry<String, Set<Long>> entry : actualIds.getFirst()
                .entrySet()) {
            count += entry.getValue().size();
        }
        if (count > Integer.MAX_VALUE) {
            throw new OverUsageException("total processed count: " + count);
        }
        return (int) count;
    }

    /**
     * Get the set of ids which were actually processed. See
     * {@link #addAll(String, Class, List)}
     */
    public Set<Long> getProcessedIds(String table) {
        Set<Long> set = lookup(table);
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * Add the actually processed ids to the current savepoint.
     *
     * It is critical that these ids are actually processed and that any failure
     * for them to be handled will cause the entire transaction to fail (in
     * which case these ids will be ignored).
     *
     * @throws GraphException
     *             thrown if the {@link EventLogMessage} raised fails.
     */
    public void addGraphIds(GraphStep step) {

        classes.put(step.table, step.iObjectType);
        Set<Long> set = lookup(step.table);
        set.add(step.id);

    }

    //
    // Iteration methods, used for actual processing
    //

     /**
      *
      * @param step
      *             which step is to be invoked. Running a step multiple times is
      *             not supported.
      * @return Any warnings which were noted during execution.
      * @throws GraphException
      *             Any errors which were caused during execution. Which
      *             execution states may be encountered is strongly tied to the
      *             definition of the specification and to the options which are
      *             passed in during initialization.
      */
     public String execute(int j) throws GraphException {
         return perform(j, false);
     }

     /**
      * Prepares the next phase ({@link #validate(int)}) by returning how
      * many validation steps should be performed.
      * @return
      */
     public int validation() {
         int total = getTotalFoundCount();
         for (int i = 0; i < total; i++) {
             steps.get(i).validation();
         }
         return total;
     }

     /**
     *
     * @param step
     *             which step is to be invoked. Running a step multiple times is
     *             not supported.
     * @return Any warnings which were noted during execution.
     * @throws GraphException
     *             Any errors which were caused during execution. Which
     *             execution states may be encountered is strongly tied to the
     *             definition of the specification and to the options which are
     *             passed in during initialization.
     */
    public String validate(int j) throws GraphException {
        return perform(j, true);
    }

    /**
     * @param validate
     *       after the proper execution of all steps has taken place,
     *       a validation call is made.
     */
    public String perform(int j, boolean validate) throws GraphException {

        final GraphStep step = steps.get(j);

        if (validate) {
            if (steps.alreadyValidated(step)) {
                return "";
            }
        } else {
            if (steps.alreadySucceeded(step)) {
                return "";
            }
        }

        String msgOrNull = step.start(this);
        if (msgOrNull != null) {
            return msgOrNull; // EARLY EXIT
        }

        // Add this instance to the opts. Any method which then tries to
        // ask the opts for the current state will have an accurate view.
        step.push(opts);

        try {

            // Lazy initialization of parents.
            // To guarantee that finalization
            // happens (#3125, #3130), a special
            // marker is added and handled above.
            for (GraphStep parent : step.stack) {
                if (!parent.hasSavepoint()) {
                    parent.savepoint(this);
                }
            }
            step.savepoint(this);

            try {

                if (!validate) {
                    step.action(this, session, sql, opts);
                } else {
                    step.validate(this, session, sql, opts);
                }

                // Finalize.
                step.release(this);
                steps.succeeded(step);
                return "";

            } catch (ConstraintViolationException cve) {
                String cause = "ConstraintViolation: " + cve.getConstraintName();
                return handleException(step, cve, cause);
            } catch (GraphException ge) {
                String cause = "GraphException: " + ge.message;
                return handleException(step, ge, cause);
            }

        } finally {
            step.pop(opts);
        }
    }

    /**
     * Method called when an exception is be thrown,
     * i.e. during {@link #execute(int)}.
     *
     * @param session
     * @param opts
     * @param type
     * @param rv
     * @param cve
     */
    private String handleException(final GraphStep step,
            Exception e, String cause) throws GraphException {

        // First, immediately rollback the current savepoint.
        step.rollback(this);

        String msg = String.format("Could not process softly %s: %s due to %s",
                step.pathMsg, step.id, cause);

        // If this entry is "SOFT" then there's nothing
        // special we need to do.
        if (step.entry.isSoft() || steps.willBeTriedAgain(step)) {
            log.debug(msg);
            return "Skipping processing of " + step.table + ":" + step.id + "\n";
        }

        // Otherwise calculate if there is any "SOFT" setting about this
        // location in the graph, and clean up all of the related entries.
        // As we check down the stack, we can safely call rollback since
        // the only other option is to rollback the entire transaction.
        for (int i = step.stack.size() - 1; i >= 0; i--) {
            GraphStep parent = step.stack.get(i);
            parent.rollback(this);
            if (parent.entry.isSoft()) {
                disableRelatedEntries(parent);
                log.debug(String.format("%s. Handled by %s: %s", msg,
                        parent.pathMsg, parent.id));
                return cause;
            }
        }

        log.info(String.format("Failed to process %s: %s due to %s",
                step.pathMsg, step.id, cause));

        if (e instanceof ConstraintViolationException) {
            throw (ConstraintViolationException) e;
        } else if (e instanceof GraphException) {
            throw (GraphException) e;
        } else {
            RuntimeException rt = new RuntimeException();
            rt.initCause(e);
            throw rt;
        }
    }

    /**
     * Finds all {@link GraphStep} instances in {@link #steps} which have the
     * given {@link GraphStep} argument in their {@link GraphStep#stack} which
     * amounts to being a descendant. All such instances are set to null in
     * {@link #steps} so that further processing cannot take place on them.
     */
    private void disableRelatedEntries(GraphStep parent) {
        for (GraphStep step : steps) {
            if (step == null || step.stack == null) {
                continue;
            } else if (step.stack.contains(parent)) {
                step.rollbackOnly();
            }
        }
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

    //
    // Callback methods
    //

    public Class<IObject> getClass(String key) {
        return classes.get(key);
    }

    public void add() {
        actualIds.add(new HashMap<String, Set<Long>>());
    }

    public int size() {
        return actualIds.size();
    }

    public Iterable<Map.Entry<String, Set<Long>>> entrySet() {
        return actualIds.getLast().entrySet();
    }

    public int collapse(boolean keep) {
        long count = 0;
        final Map<String, Set<Long>> ids = actualIds.removeLast();
        if (keep) {
            // Update the next map up with the current values
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
        } else {
            for (String key : ids.keySet()) {
                Set<Long> old = ids.get(key);
                count += old.size();
            }
        }
        if (count > Integer.MAX_VALUE) {
            throw new OverUsageException("collapsed count: " + count);
        }
        return (int) count;
    }

    public void savepoint(String savepoint) {

        sql.createSavepoint(savepoint);
        log.debug(String.format("Enter savepoint %s: new depth=%s",
                savepoint,
                actualIds.size()));

    }

    public void release(String savepoint, int count) throws GraphException {

        if (actualIds.size() == 0) {
            throw new GraphException("Release at depth 0!");
        }

        sql.releaseSavepoint(savepoint);

        log.debug(String.format(
                "Released savepoint %s with %s ids: new depth=%s", savepoint,
                count, actualIds.size()));

    }

    public void rollback(String savepoint, int count) throws GraphException {

        if (actualIds.size() == 0) {
            throw new GraphException("Release at depth 0!");
        }

        sql.rollbackSavepoint(savepoint);

        log.debug(String.format(
                "Rolled back savepoint %s with %s ids: new depth=%s",
                savepoint, count, actualIds.size()));

    }

    //
    // Misc
    //

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n");
        if (steps != null) { // null during ctor
            for (int i = 0; i < steps.size(); i++) {
                GraphStep step = steps.get(i);
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
        }
        return sb.toString();
    }

}
