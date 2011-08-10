/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.services.graphs.AnnotationGraphSpec;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphOpts;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public class DeleteStep extends GraphStep {

    final private static Log log = LogFactory.getLog(DeleteStep.class);

    final private OmeroContext ctx;

    public DeleteStep(OmeroContext ctx, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
    }

    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
            throws GraphException {

        // Phase 1: top-levels
        if (stack.size() <= 1) {
            StopWatch swTop = new CommonsLogStopWatch();
            runTopLevel(spec, session, Arrays.<Long> asList(id));
            swTop.stop("omero.deletestep.top." + id);
        }

        final QueryBuilder nullOp = optionalNullBuilder();
        final QueryBuilder qb = queryBuilder(opts);

        // Phase 2: NULL
        optionallyNullField(session, nullOp, id);

        // Phase 3: primary action
        StopWatch swStep = new CommonsLogStopWatch();
        qb.param("id", id);
        Query q = qb.query(session);
        int count = q.executeUpdate();
        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
        swStep.lap("omero.deletestep." + table + "." + id);

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
    private void runTopLevel(GraphSpec spec, Session session, List<Long> ids) {
        // If this is a top-level annotation delete then the first thing we
        // do is delete all the links to it.
        if (!(spec instanceof AnnotationGraphSpec)) {
            return;
        }

        AnnotationGraphSpec aspec = (AnnotationGraphSpec) spec;
        String sspec = aspec.getSuperSpec();
        if (sspec == null || sspec.length() == 0) {
            if (ids != null && ids.size() > 0) {
                QueryBuilder qb = new QueryBuilder();
                qb.delete("ome.model.IAnnotationLink"); // FIXME
                qb.where();
                qb.and("child.id in (:ids)");
                qb.paramList("ids", ids);
                // ticket:2962
                EventContext ec = spec.getCurrentDetails().getCurrentEventContext();
                GraphStep.permissionsClause(ec, qb);

                Query q = qb.query(session);
                int count = q.executeUpdate();
                log.info("Graphed " + count + " annotation links");
            }
        }
    }

    private QueryBuilder optionalNullBuilder() {
        QueryBuilder nullOp = null;
        if (entry.isNull()) { // WORKAROUND see #2776, #2966
            // If this is a null operation, we don't want to modify the source
            // row,
            // but modify a second row pointing at the source row via a FK.
            //
            // NB: below we also prevent this from
            // being raised as an event. TODO: refactor out to Op
            //
            nullOp = new QueryBuilder();
            nullOp.update(table);
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

    private QueryBuilder queryBuilder(GraphOpts opts) {
        final QueryBuilder qb = new QueryBuilder();
        qb.delete(table);
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(ec, qb);
        }
        return qb;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "DELETE", k,
                new ArrayList<Long>(ids));

        try {
            ctx.publishMessage(elm);
        } catch (Throwable t) {
            GraphException de = new GraphException("EventLogMessage failed.");
            de.initCause(t);
            throw de;
        }

    }
}
