/*
 * Created on Jun 13, 2005
 */
package org.openmicroscopy.omero.server.utests;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractSpringContextTests;

import org.openmicroscopy.omero.model.DatasetAnnotation;

/**
 * @author josh
 */
public class HierarchyBrowsingDbUnitTest extends AbstractSpringContextTests {

    ApplicationContext ctx;
    DataSource ds;
    static IDatabaseConnection c = null;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HierarchyBrowsingDbUnitTest.class);
    }

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { 
                "WEB-INF/dao.xml",
                "WEB-INF/test/config-test.xml", 
                "WEB-INF/test/data-test.xml" };
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        ctx = getContext(getConfigLocations());
        
        if (null==ds){
            ds = (DataSource) ctx.getBean("dataSource");
        }
        
        if (null==c) {
            c = new DatabaseConnection(ds.getConnection());
            DatabaseOperation.CLEAN_INSERT.execute(c,getData());
        }

    }
    
    public IDataSet getData() throws Exception {
        return new XmlDataSet(new FileInputStream("sql/db-export.xml"));
    }

    public void testFindDSAnnWithId() {
        // Use filter sets to DRY
        SessionFactory sf = (SessionFactory) ctx.getBean("sessionFactory"); // TODO put in spring
        HibernateTemplate ht = new HibernateTemplate(sf);
        List result = ht.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                List l = new ArrayList();
                l.add(new Integer(120));
                Query q = session.getNamedQuery("findDatasetAnnWithID");
                q.setParameterList("ds_list",l);
                q.setLong("expId",286035);
                return q.list();
            }
        });
        
        assertTrue("This should be the only known DS annotation",result.size()==1);
        DatasetAnnotation ann = (DatasetAnnotation) result.get(0);
        assertTrue("Attribute id is also known.", ann.getAttributeId().intValue()==320101);
        assertTrue("Mex should be fetched.",Hibernate.isInitialized(ann.getModuleExecution()));
        
    }
}
