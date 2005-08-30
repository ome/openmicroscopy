/*
 * ome.testing
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

package ome.testing;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import ome.api.HierarchyBrowsing;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Project;
import ome.util.Utils;


/** abstract helper class for developing Spring-based integration tests.
 * There are duplicate methods here (* and *NoReturn) to be useable by both JUnit and Grinder.
 * These tests can just as easily be called on the client as the server proxies. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public abstract class AbstractOmeroHierarchyBrowserIntegrationTest 
	extends AbstractDependencyInjectionSpringContextTests implements HierarchyBrowsingTests {

    private static Log log = LogFactory
            .getLog(AbstractOmeroHierarchyBrowserIntegrationTest.class);
    
    /** These two values will need to be set by each concrete test */
    HierarchyBrowsing hb;
    OMEData data;

    public OMEData getData() {
        return data;
    }
    public void setData(OMEData data) {
        this.data = data;
    }
    public HierarchyBrowsing getHb() {
        return hb;
    }
    public void setHb(HierarchyBrowsing hb) {
        this.hb = hb;
    }
    
    public AbstractOmeroHierarchyBrowserIntegrationTest(String name){
        super();
        this.setName(name);
    }
    
    public AbstractOmeroHierarchyBrowserIntegrationTest(OMEData data){
        super();
        this.setName("AbstractOmeroHierarchyBrowserIntegrationTest with Data");
        this.data = data;
    }
    
    public AbstractOmeroHierarchyBrowserIntegrationTest(String name, OMEData data){
        super();
        this.data = data;
        this.setName(name);
    }
    
    public HierarchyBrowsingTests init(){
		this.applicationContext = getContext(getConfigLocations());
		this.applicationContext.getBeanFactory().autowireBeanProperties(
		this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, isDependencyCheck());
		return this;
    }
    
    public ApplicationContext getAppContext(){
        return this.applicationContext;
    }
    
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIHierarchyProjectNoReturn()
	 */
    public void testLoadPDIHierarchyProjectNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadPDIHierarchyProject();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIHierarchyProject()
	 */
    public Object testLoadPDIHierarchyProject() {
        return hb.loadPDIHierarchy(Project.class, data.prjId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIAnnotatedHierarchyProjectNoReturn()
	 */
    public void testLoadPDIAnnotatedHierarchyProjectNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadPDIAnnotatedHierarchyProject();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIAnnotatedHierarchyProject()
	 */
    public Object testLoadPDIAnnotatedHierarchyProject() {
        return hb.loadPDIAnnotatedHierarchy(Project.class, data.prjId, data.userId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIHierarchyDatasetNoReturn()
	 */
    public void testLoadPDIHierarchyDatasetNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadPDIHierarchyDataset();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIHierarchyDataset()
	 */
    public Object testLoadPDIHierarchyDataset() {
        return hb.loadPDIHierarchy(Dataset.class, data.dsId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIAnnotatedHierarchyDatasetNoReturn()
	 */
    public void testLoadPDIAnnotatedHierarchyDatasetNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadPDIAnnotatedHierarchyDataset();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadPDIAnnotatedHierarchyDataset()
	 */
    public Object testLoadPDIAnnotatedHierarchyDataset() {
        return hb.loadPDIAnnotatedHierarchy(Dataset.class, data.dsId, data.userId);
    }    
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIHierarchyCategoryGroupNoReturn()
	 */
    public void testLoadCGCIHierarchyCategoryGroupNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadCGCIHierarchyCategoryGroup();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIHierarchyCategoryGroup()
	 */
    public Object testLoadCGCIHierarchyCategoryGroup() {
        return hb.loadCGCIHierarchy(CategoryGroup.class,data.cgId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIAnnotatedHierarchyCategoryGroupNoReturn()
	 */
    public void testLoadCGCIAnnotatedHierarchyCategoryGroupNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadCGCIAnnotatedHierarchyCategoryGroup();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIAnnotatedHierarchyCategoryGroup()
	 */
    public Object testLoadCGCIAnnotatedHierarchyCategoryGroup() {
        return hb.loadCGCIAnnotatedHierarchy(CategoryGroup.class,data.cgId,data.userId);
    }    
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIHierarchyCategoryNoReturn()
	 */
    public void testLoadCGCIHierarchyCategoryNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadCGCIHierarchyCategory();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIHierarchyCategory()
	 */
    public Object testLoadCGCIHierarchyCategory() {
        return hb.loadCGCIHierarchy(Category.class,data.cId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIAnnotatedHierarchyCategoryNoReturn()
	 */
    public void testLoadCGCIAnnotatedHierarchyCategoryNoReturn() throws Exception {
        super.setUp();
        Object obj = testLoadCGCIAnnotatedHierarchyCategory();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testLoadCGCIAnnotatedHierarchyCategory()
	 */
    public Object testLoadCGCIAnnotatedHierarchyCategory() {
        return hb.loadCGCIAnnotatedHierarchy(Category.class,data.cId,data.userId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCIHierarchiesNoReturn()
	 */
    public void testFindCGCIHierarchiesNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindCGCIHierarchies(); 
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCIHierarchies()
	 */
    public Object testFindCGCIHierarchies() {
        return hb.findCGCIHierarchies(data.imgsCGCI);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCPathsContainedNoReturn()
	 */
    public void testFindCGCPathsContainedNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindCGCPathsContained(); 
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCPathsContained()
	 */
    public Object testFindCGCPathsContained() {
        return hb.findCGCPaths(data.imgsCGCI,true);
    }  
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCPathsNotContainedNoReturn()
	 */
    public void testFindCGCPathsNotContainedNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindCGCPathsNotContained(); 
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindCGCPathsNotContained()
	 */
    public Object testFindCGCPathsNotContained() {
        return hb.findCGCPaths(data.imgsCGCI,false);
    }    
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindPDIHierarchiesNoReturn()
	 */
    public void testFindPDIHierarchiesNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindPDIHierarchies();
    }    
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindPDIHierarchies()
	 */
    public Object testFindPDIHierarchies() {
        return hb.findPDIHierarchies(data.imgsPDI);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindImageAnnotationsSetNoReturn()
	 */
    public void testFindImageAnnotationsSetNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindImageAnnotationsSet();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindImageAnnotationsSet()
	 */
    public Object testFindImageAnnotationsSet() {
        return hb.findImageAnnotations(data.imgsAnn1);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindImageAnnotationsSetForExperimenterNoReturn()
	 */
    public void testFindImageAnnotationsSetForExperimenterNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindImageAnnotationsSetForExperimenter();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindImageAnnotationsSetForExperimenter()
	 */
    public Object testFindImageAnnotationsSetForExperimenter() {
        return hb.findImageAnnotationsForExperimenter(data.imgsAnn2, data.userId);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindDatasetAnnotationsSetNoReturn()
	 */
    public void testFindDatasetAnnotationsSetNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindDatasetAnnotationsSet();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindDatasetAnnotationsSet()
	 */
    public Object testFindDatasetAnnotationsSet() {
        return hb.findDatasetAnnotations(data.dsAnn1);
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindDatasetAnnotationsSetForExperimenterNoReturn()
	 */
    public void testFindDatasetAnnotationsSetForExperimenterNoReturn() throws Exception {
        super.setUp();
        Object obj = testFindDatasetAnnotationsSetForExperimenter();
    }
    /* (non-Javadoc)
	 * @see ome.testing.HierarchyBrowsingTests#testFindDatasetAnnotationsSetForExperimenter()
	 */
    public Object testFindDatasetAnnotationsSetForExperimenter() {
        return hb.findDatasetAnnotationsForExperimenter(data.dsAnn2, data.userId);
    }
    /***********************************/
    
    public void testContainerCallWithWrongParameters(){
        try {
            getHb().loadPDIHierarchy(Object.class,1);
            fail("loadPDIHierarchy(class,int) didn't choke on bad class.");
        } catch (IllegalArgumentException iae){
            log.info("Caught expected exception: "+iae.getMessage());
        }

        try {
            getHb().loadCGCIHierarchy(Object.class,1);
            fail("loadCGCIHierarchy(class,int) didn't choke on bad class.");
        } catch (IllegalArgumentException iae){
            log.info("Caught expected exception: "+iae.getMessage());
        }
             
       }
    
    String nullObj = "This should get us nothing.";
    String emptyColl = "This collection should be empty.";
    String nonNull = "We should get something back";

    /** Each method should return a null or an empty set as appropriate */
    public void testNulls(){
        Set test = new HashSet();
        test.add(new Integer(-1)); // Non-existence set of ids
        int nonExp = -1; // Non-existence experimenter ID
        //
        assertTrue(emptyColl,getHb().findDatasetAnnotations(test).size()==0);
        assertTrue(emptyColl,getHb().findDatasetAnnotations(new HashSet()).size()==0);
        //
        assertTrue(emptyColl,getHb().findDatasetAnnotationsForExperimenter(test,nonExp).size()==0);
        assertTrue(emptyColl,getHb().findDatasetAnnotationsForExperimenter(new HashSet(),nonExp).size()==0);
        //
        assertTrue(emptyColl,getHb().findImageAnnotations(test).size()==0);
        assertTrue(emptyColl,getHb().findImageAnnotations(new HashSet()).size()==0);
        //
        assertTrue(emptyColl,getHb().findImageAnnotationsForExperimenter(test,nonExp).size()==0);
        assertTrue(emptyColl,getHb().findImageAnnotationsForExperimenter(new HashSet(),nonExp).size()==0);
        //
        assertTrue(emptyColl,getHb().findPDIHierarchies(test).size()==0);
        assertTrue(emptyColl,getHb().findPDIHierarchies(new HashSet()).size()==0);
        //
        assertTrue(emptyColl,getHb().findCGCIHierarchies(test).size()==0);
        assertTrue(emptyColl,getHb().findCGCIHierarchies(new HashSet()).size()==0);
        //
        assertNull(nullObj,getHb().loadCGCIHierarchy(CategoryGroup.class, 0));
        assertNull(nullObj,getHb().loadCGCIHierarchy(Category.class, 0));
        assertNull(nullObj,getHb().loadCGCIAnnotatedHierarchy(CategoryGroup.class, 0,nonExp));
        assertNull(nullObj,getHb().loadCGCIAnnotatedHierarchy(Category.class, 0,nonExp));
        //
        assertNull(nullObj,getHb().loadPDIHierarchy(Project.class, 0));
        assertNull(nullObj,getHb().loadPDIHierarchy(Dataset.class, 0));
        assertNull(nullObj,getHb().loadPDIAnnotatedHierarchy(Project.class, 0,nonExp));
        assertNull(nullObj,getHb().loadPDIAnnotatedHierarchy(Dataset.class, 0,nonExp));
        //
        assertTrue(emptyColl,getHb().findCGCPaths(test,true).size()==0);
        assertTrue(emptyColl,getHb().findCGCPaths(new HashSet(),true).size()==0);
        //TODO The Logic here is reversed! assertTrue(emptyColl,getHb().findCGCPaths(test,false).size()==0);
        assertTrue(emptyColl,getHb().findCGCPaths(new HashSet(),false).size()==0);

    }
    
    public void testContainedImages(){
        // Something
        Set result = (Set) testFindPDIHierarchies();
        assertTrue(nonNull, result != null && result.size() != 0);
        // Not too much
        Set test = Utils.getImagesinPDI(result);
        assertTrue("There should only be as many images "+test.size()+" as in the data.imagesPDI ("+data.imgsPDI.size()+").", test.size() == this.getData().imgsPDI.size());
    }
  

}
