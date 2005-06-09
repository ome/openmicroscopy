/*
 * org.openmicroscopy.omero.tests
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.omero.tests;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.Project;


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
public abstract class AbstractOmeroHierarchyBrowserIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

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
        this.setName(name);
    }
    
    public AbstractOmeroHierarchyBrowserIntegrationTest(OMEData data){
        this.setName("AbstractOmeroHierarchyBrowserIntegrationTest with Data");
        this.data = data;
    }
    
    public AbstractOmeroHierarchyBrowserIntegrationTest(String name, OMEData data){
        this.data = data;
        this.setName(name);
    }
    
    /***********************************/
    public void testLoadPDIHierarchyProjectNoReturn() {
        Object obj = testLoadPDIHierarchyProject();
    }
    public Object testLoadPDIHierarchyProject() {
        return hb.loadPDIHierarchy(Project.class, data.prjId);
    }
    /***********************************/
    public void testLoadPDIHierarchyDatasetNoReturn() {
        Object obj = testLoadPDIHierarchyDataset();
    }
    public Object testLoadPDIHierarchyDataset() {
        return hb.loadPDIHierarchy(Dataset.class, data.dsId);
    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryGroupNoReturn() {
        Object obj = testLoadCGCIHierarchyCategoryGroup();
    }
    public Object testLoadCGCIHierarchyCategoryGroup() {
        return hb.loadCGCIHierarchy(CategoryGroup.class,data.cgId);
    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryNoReturn() {
        Object obj = testLoadCGCIHierarchyCategory();
    }
    public Object testLoadCGCIHierarchyCategory() {
        return hb.loadCGCIHierarchy(Category.class,data.cId);
    }
    /***********************************/
    public void testFindCGCIHierarchiesNoReturn() {
        Object obj = testFindCGCIHierarchies(); 
    }
    public Object testFindCGCIHierarchies() {
        return hb.findCGCIHierarchies(data.imgsCGCI);
    }
    /***********************************/
    public void testFindPDIHierarchiesNoReturn() {
        Object obj = testFindPDIHierarchies();
    }    
    public Object testFindPDIHierarchies() {
        return hb.findPDIHierarchies(data.imgsPDI);
    }
    /***********************************/
    public void testFindImageAnnotationsSetNoReturn() {
        Object obj = testFindImageAnnotationsSet();
    }
    public Object testFindImageAnnotationsSet() {
        return hb.findImageAnnotations(data.imgsAnn1);
    }
    /***********************************/
    public void testFindImageAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindImageAnnotationsSetForExperimenter();
    }
    public Object testFindImageAnnotationsSetForExperimenter() {
        return hb.findImageAnnotationsForExperimenter(data.imgsAnn2, data.userId);
    }
    /***********************************/
    public void testFindDatasetAnnotationsSetNoReturn() {
        Object obj = testFindDatasetAnnotationsSet();
    }
    public Object testFindDatasetAnnotationsSet() {
        return hb.findDatasetAnnotations(data.dsAnn1);
    }
    /***********************************/
    public void testFindDatasetAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindDatasetAnnotationsSetForExperimenter();
    }
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

    public void testNulls(){
        // Each method should return a null or an empty set as appropriate
        //TODO getHb().findCGCIHierarchies(	);
        //TODO generate OMENullData(); and use it here.
        //TODO OMEData toString();
        
        Set test = new HashSet();
        test.add(new Integer(0)); // Non-existence set of ids
        int nonExp = 0; // Non-existence experimenter ID
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
        assertNull(nullObj,getHb().loadCGCIHierarchy(CategoryGroup.class, 0));
        assertNull(nullObj,getHb().loadCGCIHierarchy(Category.class, 0));
        //
        assertNull(nullObj,getHb().loadPDIHierarchy(Project.class, 0));
        assertNull(nullObj,getHb().loadPDIHierarchy(Dataset.class, 0));
    }
    
    public void testContainedImages(){
        // Something
        Set result = (Set) testFindPDIHierarchies();
        assertTrue(nonNull, result != null && result.size() != 0);
        // Not to much
        Set test = new HashSet();
        Iterator i = result.iterator();
        while (i.hasNext()){
            Object o = i.next();
            if (o instanceof Image) {
                test.add(o);
            } else if (o instanceof Dataset) {
                Dataset dd = (Dataset) o;
                test.addAll(dd.getImages());
            } else if (o instanceof Project) {
                Project pd = (Project) o;
                Iterator p = pd.getDatasets().iterator();
                while (p.hasNext()){
                    Dataset dd = (Dataset) p.next();
                    test.addAll(dd.getImages());
                }
            }
        }
        assertTrue("There should only be as many images as in the data.imagesPDI", test.size() == this.getData().imgsPDI.size());
//      TODO Make sure joins aren't leaving anything out because of empties!
    }
}
