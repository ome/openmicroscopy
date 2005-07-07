/*
 * org.openmicroscopy.omero.logic.ContainerDao
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
import java.util.List;
import java.util.Set;

//Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.IntegerType;
import org.openmicroscopy.omero.OMEModel;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

//Application-internal dependencies



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
                	q.setInteger("exp",arg2);
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
                	q.setInteger("exp",arg1);
                }
                
                return q.list();
            }

        });
    }
    
	private String getClassName(final Class arg0) {
		String klass = arg0.getName().substring(
		        arg0.getPackage().getName().length() + 1);
		return klass;
	}

}
