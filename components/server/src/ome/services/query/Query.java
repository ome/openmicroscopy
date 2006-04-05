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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.builders.PojoOptions;

// Application-internal dependencies

/**
 * source of all our queries.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class Query implements HibernateCallback
{
    private static Log log = LogFactory.getLog(Query.class);
    
    public final static String OWNER_ID = "ownerId"; // TODO from Fitlers I/F

    // For Criteria
    public final static FetchMode FETCH = FetchMode.JOIN;
    public final static int LEFT_JOIN = Criteria.LEFT_JOIN;
    public final static int INNER_JOIN = Criteria.INNER_JOIN;
    
    // FOR DEFINITIONS
    protected Definitions defs;
    
    protected Map<String, QueryParameter> qps 
        = new HashMap<String, QueryParameter>();

    private Query() { /* have to have the Parameters */ }
    public Query(Definitions definitions, QueryParameter... parameters)
    {
        this.defs = definitions;
        if ( parameters != null)
            for (QueryParameter parameter : parameters)
            {
                qps.put( parameter.name, parameter );
            }
        checkParameters();
    }
    
    public boolean check(String name){
        return defs.containsKey(name);
    }
    
    public QueryParameter get(String name){
        return qps.get(name);
    }
    
    public Object value(String name){
        return qps.get(name).value;
    }
    
    protected void checkParameters(){
        
        if (defs == null)
            throw new IllegalStateException(
                    "Query parameter definitions not set.");
        
        if (qps == null) 
            throw new IllegalArgumentException(
                    "Null arrays "+
                    "are not valid for definitions.");
        
        if (! qps.keySet().containsAll( defs.keySet() ) )
        {
            Set diff = new HashSet( defs.keySet() );
            diff.removeAll( qps.keySet() );
            throw new IllegalArgumentException(
                    "Required parameters missing from query: "+ diff 
                    );
        }
            
        for (String name : defs.keySet())
        {
            QueryParameter parameter = qps.get( name );
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
    
    protected abstract Object 
    runQuery(Session session) throws HibernateException, SQLException;
    
    protected Set<String> newlyEnabledFilters = new HashSet<String>();

    protected void enableFilters(Session session){
        
    }

    protected void ownerFilter(Session session, String...filters)
    {
    
        if (check(QP.OPTIONS) )
        {
            PojoOptions po = new PojoOptions( (Map)value( QP.OPTIONS ) );
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

class Definitions {
    
    public Definitions(QueryParameterDef...parameterDefs)
    {
        if ( parameterDefs != null)
            for (QueryParameterDef def : parameterDefs)
            {
                defs.put( def.name, def );
            }
    }
    
    Map<String,QueryParameterDef> defs = new HashMap<String, QueryParameterDef>();

    public boolean containsKey(Object key)
    {
        return defs.containsKey(key);
    }

    public boolean isEmpty()
    {
        return defs.isEmpty();
    }

    public Set<String> keySet()
    {
        return defs.keySet();
    }

    public QueryParameterDef put(String key, QueryParameterDef value)
    {
        return defs.put(key, value);
    }

    public int size()
    {
        return defs.size();
    }

    public QueryParameterDef get(Object key)
    {
        return defs.get(key);
    }
   
   
}