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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.model.acquisition.Arc;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightSource;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;
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
public class LightSourceGraphSpec extends AbstractHierarchyGraphSpec {

    private final static Logger log = LoggerFactory
        .getLogger(LightSourceGraphSpec.class);

    //
    // Initialization-time values
    //

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public LightSourceGraphSpec(List<String> entries) {
        super(entries);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class getRoot() {
        return LightSource.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Class<? extends LightSource>> getTypes(ExtendedMetadata em) {
        // FIXME this should be calculated.
        Set<Class<? extends LightSource>> rv = new HashSet<Class<? extends LightSource>>();
        rv.add(Arc.class);
        rv.add(Filament.class);
        rv.add(Laser.class);
        rv.add(LightEmittingDiode.class);
        rv.add(LightSource.class);
        return rv;
    }

    @Override
    public QueryBuilder chgrpQuery(EventContext ec, String table, GraphOpts opts) {
        // Copied from BaseGraphSpec
        final QueryBuilder qb = new QueryBuilder(true); // SQL QUERY #9435
        qb.update("lightsource");
        qb.append("set group_id = :grp ");
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(ec, qb, true);
        }
        return qb;
    }

    protected void handleOptions(final int i, final String[] parts) {
        // no-op
    }

    protected void postProcessOptions() {
        // no-op
    }

    @Override
    public boolean overrideKeep() {
        // no-op
        return false;
    }

    protected boolean isOverrideKeep(final int step, final QueryBuilder and,
        final String alias) {
        // no-op
        return false;
    }
}
