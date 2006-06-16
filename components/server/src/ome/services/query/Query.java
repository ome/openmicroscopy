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
 * base Query type to facilitate the creation of ORM queries. This class
 * attempts to enforce a strict usage pattern. First, subclasses must define
 * a {@link ome.services.query.Definitions} instance, which can optionally 
 * (and perhaps preferrably) be static, which must be passed into the 
 * super constructor.
 * <p>
 *  Then the 
 * </p>
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class Query<T> implements HibernateCallback
{
    private static Log log = LogFactory.getLog(Query.class);
    
    // For Criteria
    /** 
     * imported constant for ease of use
     * @see FetchMode#JOIN 
     */
    protected final static FetchMode FETCH = FetchMode.JOIN;
    
    /** 
     * imported constant for ease of use
     * @see Criteria#LEFT_JOIN 
     */
    protected final static int LEFT_JOIN = Criteria.LEFT_JOIN;
    
    /** 
     * imported constant for ease of use
     * @see Criteria#INNER_JOIN 
     */
    protected final static int INNER_JOIN = Criteria.INNER_JOIN;

    /**
     * container of {@link QueryParameterDef} instances. Typically created 
     * statically in Query subclasses and passed to the  
     * {@link Query#Query(Definitions, Parameters)} constructor 
     */
    protected final Definitions defs;
    
    /** 
     * container of {@link QueryParameter} instances. These must at least cover
     * all the {@link QueryParameterDef}s defined for this {@link Query} but can
     * define more. Other special fields of {@link Parameters}, such as a 
     * {@link ome.parameters.Filter} instance can also be used by {@link Query}
     * instances.
     */
    protected final Parameters params;

    /**
     * one of two possible outcomes of the {@link #buildQuery(Session)} method.
     * If not null on {@link #doInHibernate(Session) execution}, this instance
     * will be used.
     * 
     * This field is private so that we can guarantee that subclasses 
     * build one and only one representation.
     * 
     * @see #_criteria
     */
    private org.hibernate.Query _query;

    /**
     * one of two possible outcomes of the {@link #buildQuery(Session)} method.
     * If not null on {@link #doInHibernate(Session) execution}, this instance
     * will be used.
     * 
     * This field is private so that we can guarantee that subclasses 
     * build one and only one representation.
     * 
     * @see #_query
     */
    private org.hibernate.Criteria _criteria;
    
    /* have to have the Parameters */
    private Query() {  
        this.defs = null;
        this.params = null;
        checkParameters();
    }
    
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

    /**
     * check the {@link Parameters} instance against the {@link Definitions}
     * instance for this Query. Can be extended by subclasses, but 
     * <code>super.checkParameters()</code> should most likely be called.
     */
    protected void checkParameters(){
        
        if (defs == null)
            throw new IllegalStateException(
                    "Query parameter definitions not set.");
        
        if (params == null) 
            throw new IllegalArgumentException(
                    "Null arrays "+
                    "are not valid for definitions.");
        
        Set<String> missing = new HashSet<String>();
        for ( String name : defs.keySet() )
        {
            if ( ! defs.get( name ).optional 
                    && ! params.keySet().contains( name ))
            {
                missing.add( name );
            }
        }
        
        if ( missing.size() > 0 )
        {
            throw new IllegalArgumentException(
                    "Required parameters missing from query: "+ missing 
                    );
        }
            
        for (String name : defs.keySet())
        {
            QueryParameter parameter = params.get( name );
            QueryParameterDef def = defs.get( name );
            
            def.errorIfInvalid( parameter );
                
        }
        
    }

    // ~ Lookups
    // =========================================================================

    /**
     * check that there is a {@link Definitions definition} for this 
     * {@link Query} with the provided argument as its 
     * {@link QueryParameterDef#name name}. 
     */
    public boolean check(String name){
        return defs.containsKey(name);
    }
    
    /**
     * get the {@link QueryParameter} for this name argument.
     */
    public QueryParameter get(String name){
        return params.get(name);
    }

    /**
     * get the Object value for this name argument. Returns null even if no
     * QueryParameter is associated with the name (no exception).
     */
    public Object value(String name){
        QueryParameter p = params.get(name);
        return p == null ? null : p.value;
    }
    

    // ~ Execution
    // =========================================================================

    /**
     * template method defined by {@link HibernateTemplate}. This does not
     * need to be overriden by subclasses, but rather 
     * {@link #buildQuery(Session)}. This ensures that the filters are set 
     * properly, that {@link #buildQuery(Session)} does its job, and that 
     * everything is cleaned up properly afterwards.
     * 
     *   It also enforces contracts established by {@link Parameters} and 
     *   {@link ome.parameters.Filter}
     */
    public Object doInHibernate(Session session)
        throws HibernateException, SQLException
    {
        try {
            enableFilters(session);
            buildQuery(session);
            
            if ( _query == null && _criteria == null )
            {
                throw new IllegalStateException(
                        "buildQuery did not properly define a Query or " +
                        "Criteria\n by calling setQuery() or setCriteria()."
                );
            }

            boolean unique = params.getFilter().isUnique();
            
            if ( _query != null )
            {
                _query.setFirstResult( params.getFilter().firstResult() );
                _query.setMaxResults( params.getFilter().maxResults() );
                return unique ? 
                        _query.uniqueResult() :
                            _query.list();
            } else {
                _criteria.setFirstResult( params.getFilter().firstResult() );
                _criteria.setMaxResults( params.getFilter().maxResults() );
                return unique ? 
                        _criteria.uniqueResult() : 
                            _criteria.list();
            }
            
        } finally {
            disableFilters(session);
        }
    }
    
    /**
     * main point of entry for subclasses. This method must build either a 
     * {@link org.hibernate.Criteria} or a {@link org.hibernate.Query} instance
     * and make it available via {@link #setCriteria(org.hibernate.Criteria)} 
     * or {@link #setQuery(org.hibernate.Query)}
     */
    protected abstract void buildQuery(Session session) 
    throws HibernateException, SQLException;

    /**
     * provide this Query instance with a {@link org.hibernate.Query} to be
     * used for retrieving data. {@link #setCriteria(org.hibernate.Criteria)}
     * should not also be called with a non-null value. 
     */
    protected void setQuery( org.hibernate.Query query )
    {
        if ( _criteria != null )
        {
            throw new IllegalStateException(
                    "This Query already has a Criteria set:"+_criteria
            );
        }
        _query = query;
    }

    /**
     * provide this Query instance with a {@link org.hibernate.Criteria} to be
     * used for retrieving data. {@link #setQuery(org.hibernate.Query)}
     * should not also be called with a non-null value. 
     */
    protected void setCriteria( org.hibernate.Criteria criteria )
    {
        if ( _query != null )
        {
            throw new IllegalStateException(
                    "This Query already has a Query set:"+_query
            );
        }
        _criteria = criteria;
    }
    
    /**
     * the set of filters that is being or has been enabled for this Query. 
     */
    protected Set<String> newlyEnabledFilters = new HashSet<String>();

    /**
     * does nothing by default, but can be overriden by subclasses to enable
     * particular filters.
     */
    protected void enableFilters(Session session){
        
    }

    /**
     * standard filter used by many subclasses which uses the 
     * {@link PojoOptions#isExperimenter()} boolean and the 
     * {@link Parameters#OWNER_ID} constant for defining a filter.
     */
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
    
    /**
     * turns the filters off that are listed in {@link #newlyEnabledFilters}
     */
    protected void disableFilters(Session session)
    {
        for (String enabledFilter : newlyEnabledFilters)
        {
            session.disableFilter(enabledFilter);    
        }
        
    }    
}

