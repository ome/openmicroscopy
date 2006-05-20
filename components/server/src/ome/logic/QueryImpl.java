/*
 * ome.logic.QueryImpl
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

package ome.logic;

//Java imports
import java.sql.SQLException;
import java.util.List;

//Third-party libraries
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.metadata.ClassMetadata;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.api.IQuery;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.dao.Dao;
import ome.services.query.Query;

/**  Provides methods for directly querying object graphs.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * 
 */
@Transactional(readOnly=true)
public class QueryImpl extends AbstractLevel1Service implements LocalQuery {

    private static Log log = LogFactory.getLog(QueryImpl.class);

    @Override
    protected String getName()
    {
        return IQuery.class.getName();
    }
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    public <T extends IObject> Dao<T> getDao()
    {
        return new Dao<T>(this);
    }
    
    @Transactional(readOnly=false)
    public void evict(Object obj){
        getHibernateTemplate().evict(obj);
    }
    
    public void initialize(Object obj){
        Hibernate.initialize(obj);
    }
    
    @Transactional(propagation=Propagation.SUPPORTS)
    public boolean checkType(String type) {
        ClassMetadata meta = getHibernateTemplate().getSessionFactory().getClassMetadata(type);
        return meta == null ? false : true;
    }
    
    @Transactional(propagation=Propagation.SUPPORTS)
    public boolean checkProperty(String type, String property) {
        ClassMetadata meta = getHibernateTemplate().getSessionFactory().getClassMetadata(type);
        String[] names = meta.getPropertyNames();
        for (int i = 0; i < names.length; i++)
        {
            // TODO: possibly with caching and Arrays.sort/search
            if (names[i].equals(property)) return true;
        }
        return false;
    }

    /**
     * @see ome.api.local.LocalQuery#execute(Query)
     */
    @SuppressWarnings("unchecked")
    public <T> T execute(ome.services.query.Query<T> query)
    {
        return (T) getHibernateTemplate().execute(query);
    }
    
    // ~ INTERFACE METHODS
    // =========================================================================

    /**
     * @see ome.api.IQuery#get(java.lang.Class, long)
     * @see org.hibernate.Session#load(java.lang.Class, java.io.Serializable)
     * @DEV.TODO weirdness here; learn more about CGLIB initialization.
     */
    @SuppressWarnings("unchecked")
    public IObject get(@NotNull final Class klass, final long id) 
    throws ValidationException
    {
        return (IObject) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                IObject o = null;
                try 
                {
                    o = (IObject) session.load(klass,id);
                } catch (ObjectNotFoundException onfe)
                {
                    throw new ApiUsageException(String.format(
                            "The requested object (%s,%s) is not available.\n" +
                            "Please use IQuery.find to deteremine existance.\n",
                            klass.getName(),id));
                }
                
                Hibernate.initialize(o);
                return o;
                
                
            }
        });
    }

    /**
     * @see ome.api.IQuery#find(java.lang.Class, long)
     * @see org.hibernate.Session#get(java.lang.Class, java.io.Serializable)
     * @DEV.TODO weirdness here; learn more about CGLIB initialization.
     */
    @SuppressWarnings("unchecked")
    public IObject find(@NotNull final Class klass, final long id)
    {
        if ( klass == null )
            throw new ApiUsageException(
                    "Class argument to find cannot be null."
                    );
        
        return (IObject) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                IObject o = (IObject) session.get(klass,id); 
                Hibernate.initialize(o);
                return o;
                
                
            }
        });
    }

    /**
     * @see ome.api.IQuery#getByClass(java.lang.Class)
     */
    public List findAll(@NotNull final Class klass, final Filter filter)
    {
        if ( filter == null )
            return getHibernateTemplate().loadAll( klass );
        
        return (List) getHibernateTemplate().execute( new HibernateCallback(){
           public Object doInHibernate(Session session) 
           throws HibernateException, SQLException
            {
               Criteria c = session.createCriteria(klass);
               parseFilter( c,filter );
               return c.list();
            } 
        });
    }

    /**
     * @see ome.api.IQuery#findByExample(ome.model.IObject)
     */
    public IObject findByExample(@NotNull final IObject example) throws ApiUsageException
    {
        if ( example == null )
            throw new ApiUsageException(
                    "Example argument to findByExample cannot be null.");
        
        return (IObject) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(example.getClass());
                c.add(Example.create(example));
                return c.uniqueResult();
                
            }
        });
    }

    /**
     * @see ome.api.IQuery#findAllByExample(ome.model.IObject, ome.parameters.Filter)
     */
    public List findAllByExample(final IObject example, final Filter filter)
    {
        if ( example == null )
            throw new ApiUsageException(
                    "Example argument to findAllByExample cannot be null.");
        
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(example.getClass());
                c.add(Example.create(example));
                parseFilter(c,filter);
                return c.list();
                
            }
        });
    }

    /**
     * @see ome.api.IQuery#findByString(java.lang.Class, java.lang.String, java.lang.String)
     */
    public IObject findByString(
    @NotNull final Class klass, 
    @NotNull final String fieldName, 
    final String value) 
    throws ApiUsageException
    {
        return (IObject) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klass);
                c.add(Expression.like(fieldName,value));
                return c.uniqueResult();
                
            }
        });
    }

    /**
     * @see ome.api.IQuery#findAllByString(java.lang.Class, java.lang.String, java.lang.String, boolean, ome.parameters.Filter)
     */
    public List findAllByString(
            @NotNull final Class klass, 
            @NotNull final String fieldName, 
            final String value, 
            final boolean caseSensitive, 
            final Filter filter) 
    throws ApiUsageException
    {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klass);
                parseFilter( c,filter );
                
                if (caseSensitive)
                    c.add(Expression.like(fieldName,value,MatchMode.ANYWHERE));
                else 
                    c.add(Expression.ilike(fieldName,value,MatchMode.ANYWHERE));
                
                return c.list();
                
            }
        });
    }

    /** 
     * @see ome.api.IQuery#findByQuery(java.lang.String, ome.parameters.Parameters)
     */
    public IObject findByQuery(@NotNull String queryName, Parameters params) 
    throws ValidationException
    {
        
        // specify that we should only return a single value if possible
        params.getFilter().unique();
        
        Query<IObject> q = queryFactory.lookup( queryName, params );
        IObject result = null;
        try { 
            result = execute(q);
        } catch (ClassCastException cce) {
            throw new ApiUsageException(
                    "Query named:\n\t"+queryName+"\n" +
                    "has returned an Object of type "+cce.getMessage()+"\n" +
                    "Queries must return IObjects when using findByQuery. \n" +
                    "Please try findAllByQuery for queries which return Lists."
                    );
        }
        return result;
        
    }

    /** 
     * @see ome.api.IQuery#findAllByQuery(java.lang.String, ome.parameters.Parameters)
     */
    public List findAllByQuery(@NotNull String queryName, Parameters params)
    {
        Query<List> q = queryFactory.lookup( queryName, params );
        return execute(q);
    }


    // ~ HELPERS
    // =========================================================================
    
    /** responsible for applying the information provided in a
     * {@link ome.parameters.Filter} to a {@link org.hibernate.Critieria} 
     * instance. 
     */ 
    protected void parseFilter( Criteria c, Filter f)
    {
        if ( f != null )
        {
            c.setFirstResult( f.firstResult() );
            c.setMaxResults( f.maxResults() );
        }
    }

}
				