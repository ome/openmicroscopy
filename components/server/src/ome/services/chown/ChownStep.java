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
import ome.services.graphs.GraphStep.Callback;
import ome.services.messages.EventLogMessage;
import ome.services.sharing.ShareBean;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.tools.spring.InternalServiceFactory;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

/**
 * Single action produced by {@link ChownStepFactory}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChownStep extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(ChownStep.class);

    final private OmeroContext ctx;

    final private long userGroup;

    final private long usr;

    final private ShareBean share;

    public ChownStep(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long usr) {
        super(em, idx, stack, spec, entry, ids);
        this.ctx = ctx;
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
                List<Long> bad = findImproperIncomingLinks(session, lock);
                if (bad != null && bad.size() > 0) {
                    log.warn(String.format("%s:%s improperly linked by %s.%s: %s",
                            iObjectType.getSimpleName(), id, lock[0], lock[1],
                            bad.size()));
                    total += bad.size();
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

    @SuppressWarnings("unchecked")
    protected List<Long> findImproperIncomingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select count(*) from %s source where source.%s.id = ? and not " +
                "(source.details.group.id = ? OR source.details.group.id = ?)",
                lock[0], lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        q.setLong(1, usr);
        q.setLong(2, userGroup);
        List<Long> rv = q.list();

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, usr=%s, userGroup=%s",
                    rv.size(), str, id, usr, userGroup));
        }

        sw.stop("omero.chown.step." + lock[0] + "." + lock[1]);

        return rv;
    }


    public void validate(Session session, GraphOpts opts)
    throws GraphException {

        // ticket:6422 - validation of graph, phase 2
        // =====================================================================
        final String[][] locks = em.getLockCandidateChecks(iObjectType, true);

        List<String> total = new ArrayList<String>();
        for (String[] lock : locks) {
            Long bad = findImproperOutgoingLinks(session, lock);
            if (bad != null && bad > 0) {
                String msg = String.format("%s:%s improperly links to %s.%s: %s",
                        iObjectType.getSimpleName(), id, lock[0], lock[1],
                        bad);
                total.add(msg);
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
            } else {
                throw new GraphException(String.format("%s:%s improperly links to %s objects",
                    iObjectType.getSimpleName(), id, total.size()));
            }
        }

    }

    private Long findImproperOutgoingLinks(Session session, String[] lock) {
        StopWatch sw = new Slf4JStopWatch();
        String str = String.format(
                "select count(*) from %s target, %s source " +
                "where target.id = source.%s.id and source.id = ? " +
                "and not (target.details.group.id = ? " +
                "  or target.details.group.id = ?)",
                lock[0], iObjectType.getName(), lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        // FIXME: q.setLong(1, grp);
        q.setLong(2, userGroup);
        Long rv = (Long) q.list().get(0);


        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, grp=%s, userGroup=%s",
                    rv, str, id, usr, userGroup));
        }

        sw.stop("omero.chown.validation." + lock[0] + "." + lock[1]);

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
