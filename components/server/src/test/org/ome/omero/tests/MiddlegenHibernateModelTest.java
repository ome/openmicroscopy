/*
 * Created on Apr 21, 2005
*/
package org.ome.omero.tests;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.ome.omero.model.Dataset;
import org.ome.omero.model.Experimenter;
import org.ome.omero.model.Group;
import org.ome.omero.model.Image;
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
    
    public void est1(){
        HibernateTemplate ht = new HibernateTemplate(factory);
        
        Experimenter josh = new Experimenter();
        josh.setOmeName("Josh Moore");
        ht.save(josh);
        
        Image i = new Image();
        i.setName("josh");
        i.setInserted(new Date());
        i.setCreated(new Date());
        i.setExperimenter(josh);
        try {
            ht.save(i);
        } catch (Throwable t){
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }
    
    public void test2() throws HibernateException, SQLException{
            
          Session session = factory.openSession();
          Transaction tx = session.beginTransaction();

          Query q ;
          q = session.createQuery("from Experimenter e where e.omeName = 'Josh Moore'");
          Experimenter josh = (Experimenter) q.uniqueResult();

          q = session.createQuery("from Group g where g.name = 'testgroup'"); 
          Group testGroup = (Group) q.uniqueResult(); 
          
          Image i = new Image();
          i.setName("testimage");
          i.setInserted(new Date());
          i.setCreated(new Date());
          i.setExperimenter(josh);
          i.setDatasets(new HashSet());
          
          Dataset d1 = new Dataset();
          d1.setName("testdataset");
          d1.setLocked(false);
          d1.setExperimenter(josh);
          d1.setGroup(testGroup);
          d1.setImages(new HashSet());
          
          d1.getImages().add(i);
          i.getDatasets().add(d1);
        
          session.save(d1);
          session.save(i);
          
          session.flush();
          session.connection().commit();
          session.close();

    }

}
