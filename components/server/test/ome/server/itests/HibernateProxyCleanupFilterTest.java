/*
 * ome.server.itests.HibernateProxyCleanupFilter
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
package ome.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries
import ome.dao.hibernate.ProxyCleanupFilter;
import ome.util.Filterable;

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
import org.openmicroscopy.omero.OMEModel;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.DaoCleanUpHibernate;
import org.openmicroscopy.omero.logic.GenericDao;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.logic.ReturnLogger;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.server.itests.ConfigHelper;
import org.openmicroscopy.omero.server.itests.TestUtils;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.util.Utils;

import sun.security.krb5.internal.i;

/** 
 * testing dao cleanup. comparison of DaoUtils and Filterer
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class HibernateProxyCleanupFilterTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(HibernateProxyCleanupFilterTest.class);
    ContainerDao cd;
    GenericDao gd;
    DaoCleanUpHibernate cu;
    ProxyCleanupFilter cf;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	org.openmicroscopy.omero.logic.Utils.setUserAuth();
        cd = (ContainerDao) applicationContext.getBean("containerDao");
        gd = (GenericDao) applicationContext.getBean("genericDao");
        cu = (DaoCleanUpHibernate) applicationContext.getBean("daoCleanUp");
        cf = new ProxyCleanupFilter();
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return ConfigHelper.getDaoConfigLocations();
    }
    
    void blowUp(Object o, boolean expectingThrow){
    	try {
    		Utils.structureSize(o);
    		if (expectingThrow) 
    			fail("We need an exception here");
    	} catch (Exception e){
    		if (!expectingThrow)
    			throw new RuntimeException(e);
    	}
    }
        
    public void testNewCleaningMethods(){
        Object o;
        long start;

        start=System.currentTimeMillis();
        o = gd.getById(Project.class, 63);
        o = cf.filter(null,(Project)o);
        System.out.println("Filtering:"+ (System.currentTimeMillis()-start));
        blowUp(o,false);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");     
        start=System.currentTimeMillis();
        o = gd.getById(Project.class, 64);
        System.out.println("No filtering:"+ (System.currentTimeMillis()-start));
        blowUp(o,false);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        start=System.currentTimeMillis();
        o = cd.loadHierarchy(Project.class,63,1,false);
        System.out.println("No filtering:"+ (System.currentTimeMillis()-start));
        blowUp(o,true);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        start=System.currentTimeMillis();
        start=System.currentTimeMillis();
        o = cd.loadHierarchy(Project.class,64,1,false);
        o = cf.filter(null,(Filterable) o);
        System.out.println("Filtering:"+ (System.currentTimeMillis()-start));
        blowUp(o,false);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        o = cd.loadHierarchy(Project.class,63,1,false);
        o = cu.clean(o);
        System.out.println("DaoCleanup:"+ (System.currentTimeMillis()-start));
        blowUp(o,false);
       
    }
    
}
