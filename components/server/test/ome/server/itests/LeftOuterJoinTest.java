/*
 * ome.server.itests.LeftOuterJoinTest
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import ome.api.OMEModel;
import ome.api.HierarchyBrowsing;
import ome.model.Category;
import ome.model.Classification;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.Project;
import ome.testing.OMEData;
import ome.util.Utils;

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
    	ome.security.Utils.setUserAuth();
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
