/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IObject;
import ome.services.graphs.GraphConstraintException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import com.google.common.collect.HashMultimap;

/**
 * Post-processing action produced by {@link ChgrpStepFactory},
 * one for each {@link ChgrpStep} in order to check group permission
 * constraints.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 * @see ticket:6422
 */
public class ChgrpValidation extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(ChgrpValidation.class);

    final private OmeroContext ctx;

    final private long userGroup;

    final private long grp;

    final private ShareBean share;

    public ChgrpValidation(OmeroContext ctx, ExtendedMetadata em, Roles roles,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long grp) {
        super(em, idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.grp = grp;
        this.userGroup = roles.getUserGroupId();
        this.share = (ShareBean) new InternalServiceFactory(ctx).getShareService();
    }

    @Override
    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
    throws GraphException {

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

        // No special handling necessary on release.

    }

}
