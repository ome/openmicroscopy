/*
 * org.openmicroscopy.omero.server.itests
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package org.openmicroscopy.omero.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries
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

//Application-internal dependencies
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.util.Utils;

/** 
 * some  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class OmeroThumbnailsBugTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(OmeroThumbnailsBugTest.class);

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return ConfigHelper.getConfigLocations();//TODO omit data
    }

    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	org.openmicroscopy.omero.logic.Utils.setUserAuth();
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
                Query q = session
                        .createQuery("from Image as i join fetch i.datasets as ds join fetch ds.projects as prj ");
                return q.setMaxResults(1).uniqueResult();
            }
        });
        List result = new ArrayList();
        result.add(img);

        // Run
        containerDao.expects(new InvokeOnceMatcher()).method(
                "findPDIHierarchies").with(new IsSame(ids)).will(
                new ReturnStub(result));
        Set set = hbv.findPDIHierarchies(ids);
        containerDao.verify();
        containerDao.reset();
        Utils.structureSize(set);

    }

//    public void testThumbnailsExplodeCatchExceptions() {
//        for (int i = 0; i < 5000; i++) {
//            Object o;
//            try {
//                Set ids = new HashSet();
//                ids.add(new Integer(i));
//                HierarchyBrowsing hb = (HierarchyBrowsing) applicationContext
//                        .getBean("hierarchyBrowsingService");
//                o = hb.findPDIHierarchies(ids);
//                Utils.structureSize(o);
//                o = hb.findDatasetAnnotations(ids);
//                Utils.structureSize(o);
//                o = hb.findImageAnnotations(ids);
//                Utils.structureSize(o);
//                o = hb.findCGCIHierarchies(ids);
//                Utils.structureSize(o);
//
//            } catch (Throwable t) {
//                log.info("For id "+i+":"+t);
//            }
//        }
//    }

}
