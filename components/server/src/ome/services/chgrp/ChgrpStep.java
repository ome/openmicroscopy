/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.model.IObject;
import ome.services.graphs.AnnotationGraphSpec;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphOpts;
import ome.services.graphs.GraphSpec;
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
 */
public class ChgrpStep extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(ChgrpStep.class);

    final private OmeroContext ctx;

    final private ExtendedMetadata em;

    final private long userGroup;

    final private long grp;

    public ChgrpStep(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long grp) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.em = em;
        this.grp = grp;
        this.userGroup = roles.getUserGroupId();
    }

    @SuppressWarnings("unchecked")
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

        // ticket:6422 - validation of graph
        // =====================================================================
        // Immediately we check that an object moved from GroupA to GroupB
        // is no longer pointed at by any objects in GroupA via foreign key
        // constraints. This is what the DB does for us inherently on delete.
        //
        //
        // NB: After all objects are moved, we need to perform the reverse
        // check, which is that no object in GroupB points at any objects in
        // GroupA, i.e. all necessary objects were moved.
        int total = 0;
        Class<? extends IObject> x = iObjectType;
        final Map<String, List<Long>> constraints =
                new HashMap<String, List<Long>>();
        while (true) {

            final String[][] locks = em.getLockChecks(x);

            for (String[] lock : locks) {
                List<Long> bad = findImproperIncomingLinks(session, lock);
                if (bad != null && bad.size() > 0) {
                    log.warn(String.format("%s:%s improperly linked by %s.%s: %s",
                            iObjectType.getSimpleName(), id, lock[0], lock[1],
                            bad.size()));
                    total += bad.size();
                    if (constraints.containsKey(lock[0])) {
                        constraints.get(lock[0]).addAll(bad);
                    } else {
                        constraints.put(lock[0], bad);
                    }
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
            throw new ChgrpGraphException(String.format("%s:%s improperly linked by %s objects",
                iObjectType.getSimpleName(), id, total), constraints);
        }


        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
    }

    @SuppressWarnings("unchecked")
    private List<Long> findImproperIncomingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select source.%s.id from %s source where source.%s.id = ? and not " +
                "(source.details.group.id = ? OR source.details.group.id = ?)",
                lock[1], lock[0], lock[1]);

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
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "CHGRP", k,
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
