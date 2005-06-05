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
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;


/** provides data access for container objects.
 * 
 * @author josh
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
