/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.server.itests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jmock.Mock;
import org.jmock.core.constraint.IsSame;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.jmock.core.stub.ReturnStub;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;
import org.openmicroscopy.omero.util.Utils;

/**
 * @author josh
 */
public class OmeroThumbnailsBugTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(OmeroThumbnailsBugTest.class);

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return new String[] { "WEB-INF/dao.xml",
                "WEB-INF/data.xml", "WEB-INF/config-test.xml" };
    }

    public void testImageThumbnailExplodsOnHessianSerialization() {
        HierarchyBrowsingImpl hbv = new HierarchyBrowsingImpl();
        SessionFactory sessions = (SessionFactory) this.applicationContext
                .getBean("sessionFactory");

        // Mock Dao
        Mock containerDao = new Mock(ContainerDao.class);
        hbv.setContainerDao((ContainerDao) containerDao.proxy());

        HibernateTemplate ht = new HibernateTemplate(sessions);

        // Arguments
        Set ids = new HashSet();
        ids.add(new Integer(1));

        // Result
        Image img = (Image) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                Query q = session.createQuery("from Image as i join fetch i.datasets as ds join fetch ds.projects as prj ");
                return q.setMaxResults(1).uniqueResult();
            }
        });
        List result = new ArrayList();
        result.add(img);
        
        // Run
        containerDao.expects(new InvokeOnceMatcher()).method("findPDIHierarchies").with(new IsSame(ids)).will(new ReturnStub(result));
        Set set = hbv.findPDIHierarchies(ids	);
        containerDao.verify();
        containerDao.reset();
        Utils.structureSize(set);

    }
}
