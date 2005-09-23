/*
 * ome.logic.ContainerDao
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.IntegerType;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

//Application-internal dependencies
import ome.api.OMEModel;
import ome.dao.ContainerDao;


/** uses Hibernate to fulfill hierarchy needs.
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
public class ContainerDaoHibernate extends HibernateDaoSupport implements ContainerDao {

	private static final String ANNOTATED = "_Annotated";
	private static Log log = LogFactory.getLog(ContainerDaoHibernate.class);
	
    public OMEModel loadHierarchy(final Class arg0, final int arg1, final int arg2, final boolean arg3) {
        return (OMEModel) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	StringBuilder query = new StringBuilder("loadHierarchy_by_");
                query.append(getClassName(arg0));
                
                if (arg3){
                	query.append(ANNOTATED);
                }

                Query q = session.getNamedQuery(query.toString());
                q.setLong("id", arg1);
                
                if (arg3){
                	q.setInteger("expId",arg2);
                }
                
                return q.uniqueResult();
            }
        });

    }

    public List findPDIHierarchies(final Set arg0, final int arg1, final boolean arg2) {
    	return findHierarchies("findPDI",arg0,arg1,arg2);
    }
    public List findCGCIHierarchies(final Set arg0, final int arg1, final boolean arg2) {
    	return findHierarchies("findCGCI",arg0,arg1,arg2);
    }
    
    protected List findHierarchies(final String queryName, final Set arg0, final int arg1, final boolean arg2) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	StringBuilder query = new StringBuilder(queryName);

            	if (arg2){
            		query.append(ANNOTATED);
            	}
            	
                Query q = session.getNamedQuery(query.toString());
                q.setParameterList("img_list", arg0, new IntegerType());
                
                if (arg2){
                	q.setInteger("expId",arg1);
                }
                
                return q.list();
            }

        });
    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findCGCPaths(java.util.Set, boolean)
     * @DEV.TODO Review the query for categories without categoryGroups 
     * @DEV.WARNING Categories without CategoryGroups may not appear as expected.
     */
    public List findCGCPaths(final Set imgIds, final boolean contained) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

            	StringBuilder query = new StringBuilder("findCGCPaths");
                if (!contained){
                	query.append("_Not");
                }
                query.append("_Contained");

                Query q = session.getNamedQuery(query.toString());
                q.setParameterList("img_list", imgIds, new IntegerType());
                
                return q.list();
            }
        });

    }

    
    
    /* =================================
     * Helper Functions TODO Util class for all Daos
     * =================================*/
    
	private String getClassName(final Class arg0) {
		
		if (arg0 ==null)
			throw new IllegalArgumentException("Class argument cannot be null.");
		
		String klass = arg0.getName();
		int last = klass.lastIndexOf(".");
		if (last == -1)	
			return klass;
		
		return klass.substring(last+1);
	}

	private Object getUniqe(Query q) {
		List l = q.list();
		Set set = new HashSet(l);
		
		if (set.size()>1){
			throw new DataRetrievalFailureException("Multiple datasets with same id returned.");
		}
		
		return l.get(0);
	}

}
