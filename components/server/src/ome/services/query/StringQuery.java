/*
 * ome.services.query.StringQuery
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

import ome.conditions.ApiUsageException;
import ome.parameters.Parameters;
import ome.util.SqlAction;

import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * simple HQL query. Parameters are added as named parameters ({@link org.hibernate.Query#setParameter(java.lang.String, java.lang.Object)}.
 * Parameters with a value of type {@link Collection} are added as
 * {@link org.hibernate.Query#setParameterList(java.lang.String, java.util.Collection)}
 * 
 * No parsing is done until execution time.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class StringQuery extends Query {

    /**
     * parameter name for the definition of the HQL string.
     */
    public final static String STRING = "::string::";

    static Definitions defs = new Definitions(new QueryParameterDef(STRING,
            String.class, false));

    private final SqlAction sql;

    /**
     * Default constructor, used primarily for testing.
     * Leaves {@link SqlAction} field null preventing
     * query rewriting.
     */
    public StringQuery(Parameters parameters) {
        this(null, parameters);
    }

    public StringQuery(SqlAction sql, Parameters parameters) {
        super(defs, parameters);
        this.sql = sql;
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {

        String queryString = (String) value(STRING);
        if (sql != null) {
            for (String key : params.keySet()) {
                if (STRING.equals(key)) {
                    continue; // Skip ::string: since its what it is.
                }
                queryString = sql.rewriteHql(queryString, key, value(key));
            }
        }

        org.hibernate.Query query;
        try {
            query = session.createQuery(queryString);
        } catch (Exception e) {
            // Caused by a query parser error in Hibernate.
            throw new QueryException("Illegal query:" + value(STRING) + "\n"
                    + e.getMessage());
        }
        String[] nParams = query.getNamedParameters();
        for (int i = 0; i < nParams.length; i++) {
            String p = nParams[i];
            Object v = value(p);
            if (v == null) {
                throw new ApiUsageException("Null parameters not allowed: " + p);
            }
            if (Collection.class.isAssignableFrom(v.getClass())) {
                query.setParameterList(p, (Collection) v);
            } else {
                query.setParameter(p, v);
            }
        }

        setQuery(query);
    }
}
