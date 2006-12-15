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

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;

/**
 * something failed in looking up a query
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class QueryException extends ApiUsageException
{
    public QueryException(String message)
    {
        super(message);
    }
    
    // TODO should extend FixAndRetryException
}
