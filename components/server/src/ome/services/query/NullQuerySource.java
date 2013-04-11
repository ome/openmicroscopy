/*
 * ome.services.query.QuerySource
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.parameters.Parameters;

// Application-internal dependencies

/**
 * always returns null for any id. Useful for testing.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class NullQuerySource extends QuerySource {

    private static Logger log = LoggerFactory.getLogger(NullQuerySource.class);

    @Override
    public Query lookup(String queryID, Parameters parameters) {
        return null;
    }

}
