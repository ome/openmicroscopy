/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import ome.model.IObject;
import ome.services.graphs.AnnotationGraphSpec;
import ome.services.graphs.GraphConstraintException;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphOpts;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.services.graphs.GraphStep;
import ome.services.messages.EventLogMessage;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

/**
 * Single action produced by {@link ChgrpStepFactory}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChgrpStep extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(ChgrpStep.class);

    final private OmeroContext ctx;

    final private long userGroup;

    final private long grp;

    public ChgrpStep(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long grp) {
        super(em, idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.grp = grp;
        this.userGroup = roles.getUserGroupId();
    }

    @Override
    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
            throws GraphException {

        logPhase("Processing");

        // Phase 1: top-levels
        if (stack.size() <= 1) {
            // If this is a top-level annotation delete then the first thing we
            // do is delete all the links to it.
            if (spec instanceof AnnotationGraphSpec) {
                deleteAnnotationLinks((AnnotationGraphSpec) spec, session,
                        Arrays.<Long> asList(id));
            }
        }

        // Phase 2: NULL
        optionallyNullField(session, id);

        // Phase 3: Actual chgrp.
        final QueryBuilder qb = spec.chgrpQuery(ec, table, opts);
        qb.param("id", id);
        qb.param("grp", grp);
        Query q = qb.query(session);
        int count = q.executeUpdate();

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, grp=%s",
                    count, qb.queryString(), id, grp));
        }

        if (stack.size() <= 1) {
            if (count == 0) {
                throw new GraphException("No top-level item found: " +
                    String.format("%s (id=%s, grp=%s)", qb.queryString(), id, grp));
            }
        }

        graphValidation(session);


        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
    }

    @SuppressWarnings("unchecked")
    protected List<Long> findImproperIncomingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select distinct source.id from %s source where source.%s.id = ? and not " +
                "(source.details.group.id = ? OR source.details.group.id = ?)",
                lock[0], lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        q.setLong(1, grp);
        q.setLong(2, userGroup);
        List<Long> rv = q.list();

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, grp=%s, userGroup=%s",
                    rv.size(), str, id, grp, userGroup));
        }

        sw.stop("omero.chgrp.step." + lock[0] + "." + lock[1]);

        return rv;
    }

    @Override
    public void validate(GraphState graphState, Session session, SqlAction sql,
            GraphOpts opts) throws GraphException {

        logPhase("Validating");

        // ticket:6422 - validation of graph, phase 2
        // =====================================================================
        final String[][] locks = em.getLockCandidateChecks(iObjectType, true);

        final HashMultimap<String, Long> constraints = HashMultimap.create();
        final List<String> total = new ArrayList<String>();
        for (String[] lock : locks) {
            List<Long> bad = findImproperOutgoingLinks(session, lock);
            if (bad != null && bad.size() > 0) {
                String msg = String.format("%s:%s improperly links to %s.%s: %s",
                        iObjectType.getSimpleName(), id, lock[0], lock[1],
                        bad.size());
                total.add(msg);
                constraints.putAll(lock[0], bad);
            }
        }
        if (total.size() > 0) {
            if (opts.isForce()) {
                QueryBuilder qb = new QueryBuilder();
                qb.delete(table);
                qb.where();
                qb.and("id = :id");
                qb.param("id", id);
                qb.query(session).executeUpdate();
                publish(new EventLogMessage(this, "DELETE", iObjectType,
                        Collections.singletonList(id)));
            } else {
                throw new GraphConstraintException(String.format("%s:%s improperly links to %s objects",
                    iObjectType.getSimpleName(), id, total.size()), constraints);
            }
        }

        logPhase("Validated");
    }

    private List<Long> findImproperOutgoingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select distinct source.%s.id from %s target, %s source " +
                "where target.id = source.%s.id and source.id = ? " +
                "and not (target.details.group.id = ? " +
                "  or target.details.group.id = ?)",
                lock[1], lock[0], iObjectType.getName(), lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        q.setLong(1, grp);
        q.setLong(2, userGroup);
        List<Long> rv = q.list();

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, grp=%s, userGroup=%s",
                    rv.size(), str, id, grp, userGroup));
        }

        sw.stop("omero.chgrp.validation." + lock[0] + "." + lock[1]);

        return rv;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {

    }

    protected void publish(EventLogMessage elm) throws GraphException {
        try {
            ctx.publishMessage(elm);
        } catch (Throwable t) {
            GraphException de = new GraphException("EventLogMessage failed.");
            de.initCause(t);
            throw de;
        }
    }

}
