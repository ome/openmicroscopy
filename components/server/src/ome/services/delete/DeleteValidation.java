/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.services.delete;

import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.services.chgrp.ChgrpStep;
import ome.services.graphs.GraphConstraintException;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphOpts;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

/**
 * Post-processing action produced by {@link DeleteStepFactory},
 * one for each {@link ChgrpStep} in order to check group permission
 * constraints.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 */
public class DeleteValidation extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(DeleteValidation.class);

    private String table;

    private Long id;

    public DeleteValidation(OmeroContext ctx, ExtendedMetadata em,
            int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids,
            String table, Long id) {
        super(em, idx, stack, spec, entry, ids);
        this.table = table;
        this.id = id;
    }

    @Override
    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
    throws GraphException {

        logPhase("Validating");

        QueryBuilder qb = new QueryBuilder();
        qb.select("x.id").from(table, "x");
        qb.where().and("x.id = :id").param("id", id);

        List<?> rv = qb.query(session).list();
        if (rv != null && rv.size() > 0) {
            final HashMultimap<String, Long> constraints = HashMultimap.create();
            constraints.put(table, id);
            throw new GraphConstraintException(
                    String.format("%s:%s still exists after delete!",
                            table, id), constraints);
        }

        logPhase("Validated");
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {

        // No special handling necessary on release.

    }

}
