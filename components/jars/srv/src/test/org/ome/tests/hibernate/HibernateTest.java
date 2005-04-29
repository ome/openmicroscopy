/*
 * Created on Apr 17, 2005
 */
package org.ome.tests.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.ome.hibernate.User;
import org.ome.hibernate.old.Dataset;
import org.ome.hibernate.old.Image;
import org.ome.hibernate.old.Project;
import org.ome.tests.srv.SpringTestHarness;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class HibernateTest extends TestCase {

    SessionFactory factory = (SessionFactory) SpringTestHarness.ctx
            .getBean("sessionFactory");

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test1() {
        User user = new User();
        user.setName("josh");
        HibernateTemplate temp = new HibernateTemplate(factory);
        temp.save(user);
        temp.save(user);
        temp.save(user);
    }

    public void test2() {
        HibernateTemplate temp = new HibernateTemplate(factory);
        temp.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                List result = session.createQuery(
                        "from org.ome.hibernate.User u where u.name='josh'")
                        .list();
                for (Iterator iter = result.iterator(); iter.hasNext();) {
                    User element = (User) iter.next();
                    session.delete(element);
                }
                return null;
            }
        });
    }

  
}
