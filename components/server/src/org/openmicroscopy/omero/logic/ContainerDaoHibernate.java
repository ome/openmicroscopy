/*
 * org.openmicroscopy.omero.logic.ContainerDao
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

//Application-internal dependencies
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;


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

    public Object loadHierarchy(final Class arg0, final int arg1) {
        return getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                String klass = arg0.getName().substring(
                        arg0.getPackage().getName().length() + 1);

                Query q = session.getNamedQuery("loadHierarchy_by_" + klass);
                q.setLong("id", arg1);
                return q.uniqueResult();
            }
        });

    }

    public List findPDIHierarchies(final Set arg0) {

        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findPDI");
                q.setParameterList("img_list", arg0, new IntegerType());
                return q.list();
            }

        });
    }
    
    public List findCGCIHierarchies(final Set arg0) {

        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findCGCI");
                q.setParameterList("img_list", arg0, new IntegerType());
                return q.list();
            }

        });
    }

    /** load necessary because of the whackyness of CategoryGroup 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a CategoryGroup
     */
    public CategoryGroup loadCG(Integer id){
        return (CategoryGroup) getHibernateTemplate().load(CategoryGroup.class, id);
        
    }
    
    /** load necessary because of the whackyness of Category 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a Category
     */
    public Category loadC(Integer id){
        return (Category) getHibernateTemplate().load(Category.class, id);
    }

}
