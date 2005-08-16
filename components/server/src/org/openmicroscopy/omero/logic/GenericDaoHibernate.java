/*
 * org.openmicroscopy.omero.logic.GenericDaoHibernate
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

package org.openmicroscopy.omero.logic;

//Java imports

//Third-party libraries

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.openmicroscopy.omero.BaseModelUtils;
import org.openmicroscopy.omero.OMEModel;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

//Application-internal dependencies
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
//TODO can use generics here!
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

	
	public Object getByName(final Class klazz, final String name) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Criteria c = session.createCriteria(klazz);
                c.add(Expression.ilike("name",name,MatchMode.ANYWHERE));
                return c.uniqueResult();
                
            }
        });
	}
	
	/*
	 * @see org.openmicroscopy.omero.logic.GenericDao#getById(java.lang.Class, int)
	 * @DEV.TODO weirdness here; learn more about CGLIB initialization.
	 */
	public Object getById(final Class klazz, final int id) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                OMEModel o = (OMEModel) session.load(klazz,id);
                BaseModelUtils u = o.getUtils();
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

}
