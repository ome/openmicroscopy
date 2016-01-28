/*
 * ome.services.query.QueryException
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

import ome.conditions.ApiUsageException;

/**
 * something failed in looking up a query
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class QueryException extends ApiUsageException {
    /**
     * 
     */
    private static final long serialVersionUID = -6576647291843374605L;

    public QueryException(String message) {
        super(message);
    }

    // TODO should extend FixAndRetryException
}
