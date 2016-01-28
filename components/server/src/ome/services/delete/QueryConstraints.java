/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.delete;

import java.util.List;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.security.AdminAction;

/**
 * {@link AdminAction} which queries all {@link Dataset datasets} and
 * retrieve {@link Image images} linked to the datasets.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.IDelete
 */
public class QueryConstraints implements AdminAction {

    public final static String dsAllQuery = "select ds from Dataset ds join ds.imageLinks il "
            + " join il.child as img where img.id = :id";
    public final static String dsNotOwnQuery = "select ds from Dataset ds join ds.imageLinks il "
            + " join il.child as img where img.id = :id "
            + " and ds.details.owner.id != :owner"; // TODO what about links?

    final LocalAdmin iAdmin;
    final LocalQuery iQuery;
    final long id;
    final boolean force;
    final UnloadedCollector rv;
    final Parameters p;

    public QueryConstraints(LocalAdmin iAdmin, LocalQuery iQuery, long id,
            boolean force) {
        this.iAdmin = iAdmin;
        this.iQuery = iQuery;
        this.id = id;
        this.force = force;
        p = new Parameters().addId(id);
        rv = new UnloadedCollector(iQuery, iAdmin, false);
    }

    public void runAsAdmin() {

        String dsQuery;
        if (force) {
            dsQuery = dsNotOwnQuery;
            p.addLong("owner", iAdmin.getEventContext().getCurrentUserId());
        } else {
            dsQuery = dsAllQuery;
        }
        rv.addAll(iQuery.findAllByQuery(dsQuery, p));

        // TODO What about categories of other users?

    }

    public List<IObject> getResults() {
        return rv.list;
    }

}
