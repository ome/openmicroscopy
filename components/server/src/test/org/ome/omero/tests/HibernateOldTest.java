/*
 * Created on Apr 17, 2005
 */
package org.ome.omero.tests;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.ome.hibernate.User;
import org.ome.hibernate.old.Dataset;
import org.ome.hibernate.old.Image;
import org.ome.hibernate.old.Project;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class HibernateOldTest extends TestCase {

    SessionFactory factory = (SessionFactory) SpringTestHarness.ctx
            .getBean("sessionFactory");

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateOldTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }
//
//    public void test1() throws Exception {
//
//        HibernateTemplate ht = new HibernateTemplate(factory);
//        Project p1 = new Project();
//        Project p2 = new Project();
//        Dataset d1 = new Dataset();
//        Dataset d2 = new Dataset();
//        Image i1 = new Image();
//        Image i2 = new Image();
//
//        ht.save(p1);
//        ht.save(p2);
//        ht.save(d1);
//        ht.save(d2);
//        ht.save(i1);
//        ht.save(i2);
//
//        Session session = factory.openSession();
//        Transaction tx = session.beginTransaction();
//
//        p1.getDatasets().add(d1);
//        p1.getDatasets().add(d2);
//        p2.getDatasets().add(d1);
//        p2.getDatasets().add(d2);
//        d1.getProjects().add(p1);
//        d1.getProjects().add(p2);
//        d2.getProjects().add(p1);
//        d2.getProjects().add(p2);
//
//        d1.getImages().add(i1);
//        d1.getImages().add(i2);
//        d2.getImages().add(i1);
//        d2.getImages().add(i2);
//        i1.getDatasets().add(d1);
//        i1.getDatasets().add(d2);
//        i2.getDatasets().add(d1);
//        i2.getDatasets().add(d2);
//
//        session.flush();
//        session.connection().commit();
//        session.close();
//
//        ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//                List result = session.createQuery(
//                        "from Project")
//                        .list();
//                for (Iterator iter = result.iterator(); iter.hasNext();) {
//                    Project p = (Project) iter.next();
//                    assertTrue(p.getDatasets().size() > 0);
//                }
//                return null;
//            }
//        });
//        
//    }

    public void test2 () {
        HibernateTemplate ht = new HibernateTemplate(factory);
        ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                List projects = session.createQuery(
                        "from Project")
                        .list();
                List datasets  = session.createQuery(
                "from Dataset")
                .list();
                for (Iterator iP = projects.iterator(); iP.hasNext();) {
                    Project p = (Project) iP.next();
                    for (Iterator iD = datasets.iterator(); iD.hasNext();) {
                        Dataset d = (Dataset) iD.next();
                        p.getDatasets().add(d);
                        d.getProjects().add(p);
                    }
                }
                return null;
            }
        });
    }
    
}
