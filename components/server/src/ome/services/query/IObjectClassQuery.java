/*
 * ome.services.query.IObjectClassQuery
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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.parameters.Parameters;
import ome.parameters.QueryParameter;

/**
 * simple query subclass which uses the {@link ome.parameters.Parameters#CLASS}
 * parameter value to create a {@link org.hibernate.Criteria} and then adds
 * {@link org.hibernate.criterion.Expression} instances based on all other
 * parameter names.
 * <p>
 * For example:
 * </p>
 * <code>
 *   Parameters p = new Parameters().addClass( Image.class )
 *   .addString( "name", "LT-3059");
 * </code>
 * <p>
 * produces a query of the form "select i from Image i where name = 'LT-3059'"
 * </p>
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class IObjectClassQuery extends Query {

    static String CLASS = Parameters.CLASS;

    static Definitions defs = new Definitions(new QueryParameterDef(CLASS,
            Class.class, false));

    public IObjectClassQuery(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria((Class) value(CLASS));
        for (QueryParameter qp : params.queryParameters()) {
            if (!qp.name.equals(CLASS)) {
                c.add(Restrictions.eq(qp.name, qp.value)); // TODO checks for
                                                            // type.
            }
        }
        setCriteria(c);
    }
}
