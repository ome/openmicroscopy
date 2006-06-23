/*
 * ome.services.query.QuerySource
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
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
import ome.parameters.Parameters;

/**
 * contract for any source of {@link ome.services.query.Query queries}. 
 * Instances should be registered with the 
 * {@link ome.services.query.QueryFactory} in the Spring configuration.
 * 
 * QuerySources are reponsible for mapping the given query ID to a Query instance
 * (possibly dependent on the {@link ome.parameters.Parameters}). The order 
 * of sources provided to {@link ome.services.query.QueryFactory} is very 
 * important. 
 * 
 * QuerySources can use any mechanism available to perform the lookup, e.g. 
 * a database backend, flat-files, the set of Hibernate named-queries, or 
 * concrete classes (see {@link ome.services.query.ClassQuerySource}).
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class QuerySource
{
    
    /**
     * map the queryID argument to some Query instance (including null). This
     * mapping can be dependent on the provided {@link Parameters} 
     * @param <T> the generic type of the return Query. This is usually provided
     *   indirectly through the type assignment, e.g. "Query<List> q = ... "
     * @param queryID abstract identifier for the sought query. 
     * @param parameters named parameters for lookup and actual bindings.
     * @return A possible null Query for later execution.
     */
    public abstract <T> Query<T> lookup(String queryID, Parameters parameters);
}
