/*
 * Created on Feb 27, 2005
 */
package org.openmicroscopy.omero.tests.client;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Project;


/** this class tests the full functionality of Omero from the client throught to the database.
 * There are duplicate methods here (* and *NoReturn) to be useable by both JUnit and Grinder.
 * There are no client unit tests because the client has ZERO logic. These tests could just as well
 * be called on the server instance. 
 * TODO set HierarchyBrowsing in Constructor -- then can test server and client separately!
 * 
 * @author josh
 * @since 1.0
 */
public abstract class AbstractOmeroHierarchyBrowserIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    /** These two values will need to be set by each concrete test */
    HierarchyBrowsing hb;
    OMEData data;
    
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
}
