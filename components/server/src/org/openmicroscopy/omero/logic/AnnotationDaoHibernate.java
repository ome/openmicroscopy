/*
 * org.openmicroscopy.omero.logic.AnnotationDaoHibernate
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
/** uses Hibernate to fulfill annotation needs.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * @DEV.TODO update queries to remove invalid entries. Write tests for that.
 * 
 */
public class AnnotationDaoHibernate extends HibernateDaoSupport implements AnnotationDao {

    public List findImageAnnotations(final Set ids) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findImageAnn");
                q.setParameterList("img_list", ids, new IntegerType());
                return q.list();
            }
        });
    }

    public List findImageAnnotationsForExperimenter(final Set ids, final int exp) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findImageAnnWithID");
                q.setParameterList("img_list", ids, new IntegerType());
                q.setInteger("expId", exp);
                return q.list();
            }
        });

    }

    public List findDataListAnnotations(final Set ids) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findDatasetAnn");
                q.setParameterList("ds_list", ids, new IntegerType());
                return q.list();
            }
        });
    }

    public List findDataListAnnotationForExperimenter(final Set ids, final int exp) {
        return (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findDatasetAnnWithID");
                q.setParameterList("ds_list", ids, new IntegerType());
                q.setInteger("expId", exp);
                return q.list();
            }
        });
    }
}
