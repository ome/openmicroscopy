/*
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ApiUsageException;
import ome.parameters.Parameters;

/**
 * query locator which is configured by Spring. A QueryFactory instance is
 * created in the ome/services/services.xml Spring config and is injected with
 * multiple {@link ome.services.query.QuerySource "query sources"}. The lookup
 * proceeds through the available query sources calling
 * {@link ome.services.query.QuerySource#lookup(String, Parameters) querySource.lookup()},
 * and returns the first non-null result. If no result is found, an exception is
 * thrown.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class QueryFactory {

    private static Logger log = LoggerFactory.getLogger(QueryFactory.class);

    /**
     * sources available for lookups. This array will never be null.
     */
    protected QuerySource[] sources;

    private QueryFactory() {
    }; // We need the sources

    /**
     * main constructor which takes a non-null array of query sources as its
     * only argument. This array is copied, so modifications will not be
     * noticed.
     * 
     * @param querySources
     *            Array of query sources. Not null.
     */
    public QueryFactory(QuerySource... querySources) {
        if (querySources == null || querySources.length == 0) {
            throw new ApiUsageException(
                    "QuerySource[] argument to QueryFactory constructor "
                            + "may not be null or empty.");
        }
        int size = querySources.length;
        this.sources = new QuerySource[size];
        System.arraycopy(querySources, 0, this.sources, 0, size);
    }

    /**
     * 
     * @param <T>
     * @param queryID
     * @param params
     * @return See above.
     */
    public <T> Query<T> lookup(String queryID, Parameters params) {
        Query<T> q = null;

        for (QuerySource source : sources) {
            q = source.lookup(queryID, params);
            if (q != null) {
                break;
            }
        }

        if (q == null) {
            throw new QueryException("No query found for queryID=" + queryID);
        }

        return q;

    }

}
