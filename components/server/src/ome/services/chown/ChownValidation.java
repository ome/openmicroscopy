/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chown;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
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
 * Post-processing action produced by {@link ChownStepFactory},
 * one for each {@link ChownStep} in order to check group permission
 * constraints.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 * @see ticket:6422
 */
public class ChownValidation extends GraphStep {

    final private static Log log = LogFactory.getLog(ChownValidation.class);

    final private OmeroContext ctx;

    final private ExtendedMetadata em;

    final private long userGroup;

    final private long grp;

    final private ShareBean share;

    public ChownValidation(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long grp) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.em = em;
        this.grp = grp;
        this.userGroup = roles.getUserGroupId();
        this.share = (ShareBean) new InternalServiceFactory(ctx).getShareService();
    }

    @Override
    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
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
        CommonsLogStopWatch sw = new CommonsLogStopWatch();
        String str = String.format(
                "select count(*) from %s target, %s source " +
                "where target.id = source.%s.id and source.id = ? " +
                "and not (target.details.group.id = ? " +
                "  or target.details.group.id = ?)",
                lock[0], iObjectType.getName(), lock[1]);

        Query q = session.createQuery(str);
        q.setLong(0, id);
        q.setLong(1, grp);
        q.setLong(2, userGroup);
        Long rv = (Long) q.list().get(0);


        if (log.isDebugEnabled()) {
            log.debug(String.format("%s<==%s, id=%s, grp=%s, userGroup=%s",
                    rv, str, id, grp, userGroup));
        }

        sw.stop("omero.chown.validation." + lock[0] + "." + lock[1]);

        return rv;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "CHOWN-VALIDATION", k,
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
