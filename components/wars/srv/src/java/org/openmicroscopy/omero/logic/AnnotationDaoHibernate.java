/*
 * Created on Jun 5, 2005
 */
package org.openmicroscopy.omero.logic;

import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.IntegerType;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/** provides data access for Annotation objects.
 * 
 * @author josh
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
