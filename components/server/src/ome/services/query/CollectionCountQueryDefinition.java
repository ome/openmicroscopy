/*
 * ome.services.query.CollectionCountQueryDefinition
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ome.parameters.Parameters;
import ome.tools.lsid.LsidUtils;

/**
 * counts the number of members in a collection. This query is used by the
 * {@link ome.api.IContainer IContainer} interface (possibly among others) to add
 * information to outgoing results.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class CollectionCountQueryDefinition extends Query {

    static Definitions defs = new Definitions(new IdsQueryParameterDef(),
            new QueryParameterDef("field", String.class, false));

    public CollectionCountQueryDefinition(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        String s_field = (String) value("field"); // TODO Generics??? if not
        // in arrays!
        String s_target = LsidUtils.parseType(s_field);
        String s_collection = LsidUtils.parseField(s_field);
        String s_query = String.format(
                "select target.id, count(collection) from %s target "
                        + "join target.%s collection "
                        + (check("ids") ? "where target.id in (:ids)" : "")
                        + "group by target.id", s_target, s_collection);

        org.hibernate.Query q = session.createQuery(s_query);
        if (check("ids")) {
            q.setParameterList("ids", (Collection) value("ids"));
        }
        setQuery(q);

    }

    // TODO filters...?
}
