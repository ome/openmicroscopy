/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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

package ome.services.graphs;

import java.util.List;

import ome.system.EventContext;
import ome.tools.hibernate.QueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractHierarchyGraphSpec} specialized for processing light sources.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.4
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class RenderingDefGraphSpec extends BaseGraphSpec {

    private final static Logger log = LoggerFactory
        .getLogger(RenderingDefGraphSpec.class);

    //
    // Initialization-time values
    //

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public RenderingDefGraphSpec(List<String> entries) {
        super(entries);
    }

    /**
     * Uses direct SQL in order to workaround any security filters
     * which may be in place since rdefs are considered outside of
     * the security system.
     */
    @Override
    public QueryBuilder deleteQuery(EventContext ec, String table, GraphOpts opts) {
        // Copied from BaseGraphSpec
        final QueryBuilder qb = new QueryBuilder(true); // SQL QUERY #9496
        qb.delete(table);
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(ec, qb, true);
        }
        return qb;
    }

}
