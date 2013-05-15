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
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

/**
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public class DeleteStep extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(DeleteStep.class);

    final private OmeroContext ctx;

    public DeleteStep(ExtendedMetadata em, OmeroContext ctx, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        super(em, idx, stack, spec, entry, ids);
        this.ctx = ctx;
    }

    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
            throws GraphException {

        // Phase 1: top-levels
        if (stack.size() <= 1) {
            // If this is a top-level annotation delete then the first thing we
            // do is delete all the links to it.
            if (spec instanceof AnnotationGraphSpec) {
                deleteAnnotationLinks((AnnotationGraphSpec) spec, session,
                        Arrays.<Long> asList(id));
            }
        }

        final QueryBuilder nullOp = optionalNullBuilder();
        final QueryBuilder qb = spec.deleteQuery(ec, table, opts);

        // Phase 2: NULL
        optionallyNullField(session, nullOp, id);

        // Phase 3: validation (duplicates constraint violation logic)
        graphValidation(session);

        // Phase 4: primary action
        StopWatch swStep = new Slf4JStopWatch();
        qb.param("id", id);
        Query q = qb.query(session);
        int count = q.executeUpdate();

        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
        swStep.lap("omero.deletestep." + table + "." + id);

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

    @SuppressWarnings("unchecked")
    protected List<Long> findImproperIncomingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select source.%s.id from %s source where source.%s.id = ?",
                lock[1], lock[0], lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        List<Long> rv = q.list();

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s", rv.size(), str, id));
        }

        sw.stop("omero.delete.step." + lock[0] + "." + lock[1]);

        return rv;
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
