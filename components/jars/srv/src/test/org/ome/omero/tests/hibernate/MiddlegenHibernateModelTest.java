/*
 * Created on Apr 21, 2005
*/
package org.ome.tests.hibernate;

import org.hibernate.SessionFactory;
import org.ome.hibernate.model.Image;
import org.ome.tests.srv.SpringTestHarness;
import org.springframework.orm.hibernate3.HibernateTemplate;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class MiddlegenHibernateModelTest extends TestCase {

    SessionFactory factory = (SessionFactory) SpringTestHarness.ctx
    .getBean("sessionFactory");

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MiddlegenHibernateModelTest.class);
    }
    
    public void test1(){
        HibernateTemplate ht = new HibernateTemplate(factory);
        Image i = new Image();
        i.setName("josh");
        ht.save(i);
    }

}
