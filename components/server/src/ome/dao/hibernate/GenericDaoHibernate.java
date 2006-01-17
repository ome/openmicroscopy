/*
 * ome.logic.GenericDaoHibernate
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

package ome.dao.hibernate;

//Java imports
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;

//Application-internal dependencies
import ome.api.OMEModel;
import ome.dao.GenericDao;

/** uses Hibernate to fulfill basic/generic needs.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * 
 */
public class GenericDaoHibernate extends HibernateDaoSupport implements GenericDao {

    public boolean checkType(String type) {
        ClassMetadata meta = getHibernateTemplate().getSessionFactory().getClassMetadata(type);
        return meta == null ? false : true;
    }
    
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
    
	public Object getUniqueByExample(final Object example) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(example.getClass());
                c.add(Example.create(example));
                return c.uniqueResult();
                
            }
        });
	}

	public List getListByExample(final Object example) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(example.getClass());
                c.add(Example.create(example));
                return c.list();
                
            }
        });
	}

	
	public Object getUniqueByFieldILike(final Class klazz, final String field, final String value) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                c.add(Expression.ilike(field,value,MatchMode.ANYWHERE));
                return c.uniqueResult();
                
            }
        });
	}

	public List getListByFieldILike(final Class klazz, final String field, final String value) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                c.add(Expression.ilike(field,value,MatchMode.ANYWHERE));
                return c.list();
                
            }
        });
	}

	public Object getUniqueByFieldEq(final Class klazz, final String field, final Object value) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                c.add(Expression.eq(field,value));
                return c.uniqueResult();
                
            }
        });
	}

	public List getListByFieldEq(final Class klazz, final String field, final Object value) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                c.add(Expression.eq(field,value));
                return c.list();
                
            }
        });
	}	
	
	/*
	 * @see ome.logic.GenericDao#getById(java.lang.Class, int)
	 * @DEV.TODO weirdness here; learn more about CGLIB initialization.
	 */
	public Object getById(final Class klazz, final int id) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                OMEModel o = (OMEModel) session.load(klazz,id);
                Hibernate.initialize(o);
                return o;
                
                
            }
        });
	}
	
	public void persist(final Object[] objects) {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	for (Object o : objects){
            		session.saveOrUpdate(o);
            	}
            	
            	return null;
                
            }
        });
	}
	
	@Deprecated
	public Object queryUnique(final String query, final Object[] params) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	Query q = session.createQuery(query);
            	fillParams(q,params);
                return q.uniqueResult();
                
            }
        });
	}
	
	@Deprecated
	public List queryList(final String query, final Object[] params) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	Query q = session.createQuery(query);
            	fillParams(q,params);
                return q.list();
                
            }
        });
	}

	@Deprecated
	public Object queryUniqueMap(final String query, final Map params) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	Query q = session.createQuery(query);
            	fillParamsMap(q,params);
                return q.uniqueResult();
                
            }
        });
	}
	
	@Deprecated
	public List queryListMap(final String query, final Map params) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	Query q = session.createQuery(query);
            	fillParamsMap(q,params);
                return q.list();
                
            }
        });
	}
	
	public Object getUniqueByMap(final Class klazz, final Map constraints) {
        return (Object) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                	c.add(Expression.allEq(constraints));
                return c.uniqueResult();
                
            }
        });
	}

	public List getListByMap(final Class klazz, final Map constraints) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
               	c.add(Expression.allEq(constraints));
                return c.list();
                
            }
        });
	}
	
	private void fillParams(Query q, final Object[] params) {
		if (null!=params){
			for (int i = 0; i < params.length; i++) {
				q.setParameter(i,params[i]);
			}
		}
	}

	private void fillParamsMap(Query q, final Map params) {
		if (null!=params){
            Set availableParameters = new HashSet(Arrays.asList(q.getNamedParameters()));
			for (Object o : params.keySet()) {
				String s = (String) o;
				if (s.endsWith("_list")){ 
					// Perhaps two arguments. params / paramLists FIXME above too.
					// TODO only take the existing parameters
					q.setParameterList(s,(Collection)params.get(o));
				} else {
                    if (availableParameters.contains(s))
                        q.setParameter(s,params.get(o));
				}
			}
		}
	}

}
				