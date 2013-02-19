/*
 * ome.services.query.ClassQuerySource
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.query;

// Java imports
import java.lang.reflect.Constructor;
// Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Application-internal dependencies
import ome.model.IObject;
import ome.parameters.Parameters;

/**
 * creates a query based on the id string by interpreting it as a Class. The
 * class can either be a {@link ome.services.query.Query} implementation or an
 * {@link ome.model.IObject} implementation.
 * 
 * <p>
 * If it is an {@link ome.model.IObject} implementation, the
 * {@link ome.parameters.QueryParameter} instances passed in through
 * {@link Parameters} are interpreted as being field names whose
 * {@link ome.parameters.QueryParameter#value values} should equals the value in
 * the database.
 * </p>
 * 
 * <p>
 * If it is an {@link ome.services.query.Query} implementation, then it is
 * instantiated by passing the {@link ome.parameters.Parameters} into the
 * constructor.
 * </p>
 * 
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 * @see ome.services.query.IObjectClassQuery
 */
public class ClassQuerySource extends QuerySource {

    private static Logger log = LoggerFactory.getLogger(ClassQuerySource.class);

    @Override
    public Query lookup(String queryID, Parameters parameters) {
        Query q = null;
        Class klass = null;
        try {
            klass = Class.forName(queryID);
        } catch (ClassNotFoundException e) {
            // Not an issue.
        }

        // return null immediately
        if (klass == null) {
            return null;
        }

        // it's a query
        else if (Query.class.isAssignableFrom(klass)) {
            try {
                Constructor c = klass.getConstructor(Parameters.class);
                q = (Query) c.newInstance(parameters);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Query could not be instanced.", e.getCause());
                }
                throw new RuntimeException("Error while trying to instantiate:"
                        + queryID, e);
            }
            return q;
        }

        // it's an IObject
        else if (IObject.class.isAssignableFrom(klass)) {
            Parameters p = new Parameters(parameters);
            p.addClass(klass);
            return new IObjectClassQuery(p);
        }

        else {
            return null;
        }
    }

}
