/*
 * ome.services.query.Query
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

// Application-internal dependencies
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;
import ome.parameters.QueryParameter;
import ome.util.builders.PojoOptions;

/**
 * base Query type. 
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class Query<T> implements HibernateCallback
{
    private static Log log = LogFactory.getLog(Query.class);
    
    // For Criteria
    public final static FetchMode FETCH = FetchMode.JOIN;
    public final static int LEFT_JOIN = Criteria.LEFT_JOIN;
    public final static int INNER_JOIN = Criteria.INNER_JOIN;

    /**
     * container of {@link QueryParameterDef} instances. Typically created 
     * statically in Query subclasses and passed to the constructor in 
     * {@link Query}
     */
    protected Definitions defs;
    
    /** container of {@link QueryParameter} instances. These must at least cover
     * all the {@link QueryParameterDef}s defined for this {@link Query} but can
     * define more. Other special fields of {@link Parameters}, such as a 
     * {@link ome.parameters.Filter} instance can also be used by {@link Query}
     * instances.
     */
    protected Parameters params;

    private Query() { /* have to have the Parameters */ }
    
    /** 
     * main constructor used by subclasses. Both arguments must be provided. 
     * @param definitions Not null.
     * @param parameters Not null.
     */
    public Query(Definitions definitions, Parameters parameters)
    {
        this.defs = definitions;
        this.params = parameters;
        checkParameters();
    }
    
    public boolean check(String name){
        return defs.containsKey(name);
    }
    
    public QueryParameter get(String name){
        return params.get(name);
    }
    
    public Object value(String name){
        return params.get(name).value;
    }
    
    protected void checkParameters(){
        
        if (defs == null)
            throw new IllegalStateException(
                    "Query parameter definitions not set.");
        
        if (params == null) 
            throw new IllegalArgumentException(
                    "Null arrays "+
                    "are not valid for definitions.");
        
        if (! params.keySet().containsAll( defs.keySet() ) )
        {
            Set diff = new HashSet( defs.keySet() );
            diff.removeAll( params.keySet() );
            throw new IllegalArgumentException(
                    "Required parameters missing from query: "+ diff 
                    );
        }
            
        for (String name : defs.keySet())
        {
            QueryParameter parameter = params.get( name );
            QueryParameterDef def = defs.get( name );
            
            def.errorIfInvalid( parameter );
                
        }
        
    }
    
    public Object doInHibernate(Session session)
        throws HibernateException, SQLException
    {
        try {
            enableFilters(session);
            return runQuery(session);
        } finally {
            disableFilters(session);
        }
    }
    
    protected abstract T 
    runQuery(Session session) throws HibernateException, SQLException;
    
    protected Set<String> newlyEnabledFilters = new HashSet<String>();

    protected void enableFilters(Session session){
        
    }

    protected void ownerFilter(Session session, String...filters)
    {
    
        if (check(OPTIONS) )
        {
            PojoOptions po = new PojoOptions( (Map)value( OPTIONS ) );
            if ( po.isExperimenter() ) // TODO || is Group();
            {
                for (String filter : filters)
                {
                    if (session.getEnabledFilter( filter ) != null) 
                        newlyEnabledFilters.add(  filter );
                
                    session.enableFilter( filter )
                        .setParameter( OWNER_ID, po.getExperimenter() );
                }
            
            }
        }
    }
    
    protected void disableFilters(Session session)
    {
        for (String enabledFilter : newlyEnabledFilters)
        {
            session.disableFilter(enabledFilter);    
        }
        
    }    
}

