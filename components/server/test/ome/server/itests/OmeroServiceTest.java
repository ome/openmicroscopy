/*
 * ome.server.itests
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

//Third-party libraries
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Image;
import ome.testing.AbstractOmeroHierarchyBrowserIntegrationTest;
import ome.testing.OMEData;
import ome.testing.OMEPerformanceData;
import ome.util.Utils;

/** 
 * tests to the database but without going over the wire. 
 * <code>testHessian</code> also tests the serialization 
 * process. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class OmeroServiceTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {

    private static Log log = LogFactory.getLog(OmeroServiceTest.class);
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations();
    }
    
    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	ome.security.Utils.setUserAuth();
    }
    
    public OmeroServiceTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroServiceTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }
    
    public void testPathCalls(){
    	Set imgs = TestUtils.getSetFromInt(new int[]{2});
    	log.info("Checking paths for image "+imgs);
    	Set<CategoryGroup> con = (Set<CategoryGroup>) this.getHb().findCGCPaths(imgs,true);
    	log.info("Contained:\n"+con+"\n"+cg2path(con));
    	Set<CategoryGroup> non = (Set<CategoryGroup>) this.getHb().findCGCPaths(imgs,false);
    	log.info("Not-Contained:\n"+non+"\n"+cg2path(non));
    	
    	for (CategoryGroup cg : con) {
    		for (CategoryGroup cg2: non){
    			assertTrue(
    					"No contained paths may be contained and not-contained",
    					cg.getAttributeId()!=cg2.getAttributeId()
    				);
    		}
   		}
    }
    
    String cg2path(Set<CategoryGroup> l){
    	StringBuilder sb = new StringBuilder();
    	for (CategoryGroup cg : l){
			int cgId = cg.getAttributeId();
				for (Object o : cg.getCategories()){
				Category c = (Category) o;
				int cId = c.getAttributeId();
				sb.append("/"+cgId+"/"+cId+"\n");
			}
    	}
    	return sb.toString();
    }
    
    public void testHessian(){
        OMEData data = new OMEPerformanceData();
        OMEData old = getData();
        Set imgs = new HashSet();
        imgs.add(new Integer(101));
        imgs.add(new Integer(12));
        imgs.add(new Integer(5393));
        imgs.add(new Integer(4954));
        imgs.add(new Integer(3919));
        imgs.add(new Integer(1273));
        data.imgsCGCI=imgs;
        data.userId=286033;
        Set set = new HashSet();
        set.add(new Integer(120));
        data.dsAnn1=set;
        data.dsAnn2=set;
        setData(data);
        Object o = this.testFindDatasetAnnotationsSetForExperimenter();
        Utils.structureSize(o);
        setData(old);

        log.info(getData());        
        Utils.structureSize(this.testFindCGCIHierarchies());
        Utils.structureSize(this.testFindDatasetAnnotationsSet());
        Utils.structureSize(this.testFindDatasetAnnotationsSetForExperimenter());
        Utils.structureSize(this.testFindImageAnnotationsSet());
        Utils.structureSize(this.testFindImageAnnotationsSetForExperimenter());
        Utils.structureSize(this.testFindPDIHierarchies());
        Utils.structureSize(this.testLoadCGCIHierarchyCategory());
        Utils.structureSize(this.testLoadCGCIHierarchyCategoryGroup());
        Utils.structureSize(this.testLoadPDIHierarchyDataset());
        Utils.structureSize(this.testLoadPDIHierarchyProject());
    }
 
    public void testDuplicateImages() {
        Set result = (Set) this.testFindPDIHierarchies();
        Set test = Utils.getImagesinPDI(result);
        assertTrue("Images in should eq. images out",this.getData().imgsPDI.size()==test.size());
        
        Set noDupesPlease = new HashSet(); 
        for (Iterator i = test.iterator(); i.hasNext();) {
            Image img = (Image) i.next();
            if (noDupesPlease.contains(img.getImageId())) 
                fail("But also the IDs should be unique!");
          	noDupesPlease.add(img.getImageId());
        }
        
    }
    
}
