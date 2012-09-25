/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chown;

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
import ome.services.sharing.ShareBean;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.tools.spring.InternalServiceFactory;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Single action produced by {@link ChownStepFactory}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 */
public class ChownStep extends GraphStep {

    final private static Log log = LogFactory.getLog(ChownStep.class);

    final private OmeroContext ctx;

    final private ExtendedMetadata em;

    final private long userGroup;

    final private long usr;

    final private ShareBean share;

    public ChownStep(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long usr) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.em = em;
        this.usr = usr;
        this.userGroup = roles.getUserGroupId();
        this.share = (ShareBean) new InternalServiceFactory(ctx).getShareService();
    }

    @SuppressWarnings("unchecked")
    @Override
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

        final QueryBuilder qb = spec.chmodQuery(ec, table, opts);
        qb.param("id", id);
        qb.param("usr", usr);
        Query q = qb.query(session);
        int count = q.executeUpdate();

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
        while (true) {

            final String[][] locks = em.getLockChecks(x);

            for (String[] lock : locks) {
                Long bad = findImproperIncomingLinks(session, lock);
                if (bad != null && bad > 0) {
                    log.warn(String.format("%s:%s improperly linked by %s.%s: %s",
                            iObjectType.getSimpleName(), id, lock[0], lock[1],
                            bad));
                    total += bad;
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
            throw new GraphException(String.format("%s:%s improperly linked by %s objects",
                iObjectType.getSimpleName(), id, total));
        }


        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
    }

    private Long findImproperIncomingLinks(Session session, String[] lock) {
        CommonsLogStopWatch sw = new CommonsLogStopWatch();
        String str = String.format(
                "select count(*) from %s source where source.%s.id = ? and not " +
                "(source.details.group.id = ? OR source.details.group.id = ?)",
                lock[0], lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        q.setLong(1, usr);
        q.setLong(2, userGroup);
        Long rv = (Long) q.list().get(0);

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, usr=%s, userGroup=%s",
                    rv, str, id, usr, userGroup));
        }

        sw.stop("omero.chown.step." + lock[0] + "." + lock[1]);

        return rv;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "CHOWN", k,
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
