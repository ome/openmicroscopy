/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

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
import ome.system.OmeroContext;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Single action produced by {@link ChgrpStepFactory}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 */
public class ChgrpStep extends GraphStep {

    final private static Log log = LogFactory.getLog(ChgrpStep.class);

    final private OmeroContext ctx;

    final private long grp;

    public ChgrpStep(OmeroContext ctx, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids, long grp) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
        this.grp = grp;
    }

    @Override
    public void action(Callback cb, Session session, GraphOpts opts)
            throws GraphException {

        final QueryBuilder qb = queryBuilder(opts);
        qb.param("id", id);
        qb.param("grp", grp);
        Query q = qb.query(session);
        int count = q.executeUpdate();
        if (count > 0) {
            cb.addGraphIds(this);
        }
        logResults(count);
    }

    private QueryBuilder queryBuilder(GraphOpts opts) {
        final QueryBuilder qb = new QueryBuilder();
        qb.update(table);
        qb.append("set group_id = :grp ");
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
