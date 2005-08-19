/*
 * org.openmicroscopy.omero.server.itests.LeftOuterJoinTest
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
import java.util.Iterator;
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
import org.openmicroscopy.omero.OMEModel;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.DaoCleanUpHibernate;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.logic.ReturnLogger;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.util.Utils;

import sun.security.krb5.internal.i;

/** 
 * tests for a HQL join bug.
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class LeftOuterJoinTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(LeftOuterJoinTest.class);
    HierarchyBrowsing hb;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	org.openmicroscopy.omero.logic.Utils.setUserAuth();
        hb = (HierarchyBrowsing) applicationContext.getBean("hierarchyBrowsingService");
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations();
    }

    public void testNewCleaningMethods(){
        Object o = hb.loadPDIHierarchy(Project.class,63);
        Utils.structureSize(o);
    }
    
    public void testImageThumbnailExplodsOnHessianSerialization() {
        Set imgIds = new HashSet();
        imgIds.add(new Integer(1191));
        imgIds.add(new Integer(4665));
        imgIds.add(new Integer(1304));
        imgIds.add(new Integer(4977));
        imgIds.add(new Integer(3540));
        imgIds.add(new Integer(2064));
        Set result = hb.findPDIHierarchies(imgIds);
        Set test = Utils.getImagesinPDI(result);
        assertTrue("Images in should eq. images out",imgIds.size()==test.size());
    }

    public void testDuplicateImages() {
        OMEData data = (OMEData) applicationContext.getBean("data");
        Set result = hb.findPDIHierarchies(data.imgsPDI);
        Set test = Utils.getImagesinPDI(result);
        assertTrue("Images in should eq. images out",data.imgsPDI.size()==test.size());
        
        Set noDupesPlease = new HashSet(); 
        for (Iterator i = test.iterator(); i.hasNext();) {
            Image img = (Image) i.next();
            if (noDupesPlease.contains(img.getImageId())) 
                fail("But also the IDs should be unique!");
          	noDupesPlease.add(img.getImageId());
        }
        
    }

    public void testWhereAreTheImageAnnsLoadCGCI(){
    	Category c =(Category) hb.loadCGCIAnnotatedHierarchy(Category.class,250,1);
    	Set<Classification> clas = c.getClassifications();
    	boolean annsThere = false;
    	for (Classification cla : clas){
    		Image img = cla.getImage();
    		Set<ImageAnnotation> anns = img.getImageAnnotations();
    		log.info("Annotations for image "+img.getImageId()+":"+anns);
    		if (anns !=null && anns.size() > 0) annsThere = true;
    	}
    	assertTrue("There should be an annotation",annsThere);
    }
    
    public void testAndNowAFrigginStackOverFLow() throws Throwable{
    	Set<OMEModel> set = hb.findPDIAnnotatedHierarchies(TestUtils.getSetFromInt(new int[]{2,3}),1);
    	log.info(set);
    	// Already applied below:
//    	DaoCleanUpHibernate clean = (DaoCleanUpHibernate)this.applicationContext.getBean("daoCleanUp");
//    	ReturnLogger log = (ReturnLogger) this.applicationContext.getBean("logging");
//    	clean.clean(set);
//    	log.log(set);
    	
    }
    
}
