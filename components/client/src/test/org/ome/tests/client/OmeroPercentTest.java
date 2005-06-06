/*
 * Created on Feb 27, 2005
 */
package org.ome.tests.client;

import org.ome.omero.client.ServiceFactory;
import org.ome.omero.interfaces.HierarchyBrowsing;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

import junit.framework.TestCase;

/** this class is not fully a JUnit test case, but rather
 * is intended for use with Grinder.
 * @author josh
 */
public class OmeroPercentTest extends TestCase {

    ServiceFactory services = new ServiceFactory();
    HierarchyBrowsing hb = services.getHierarchyBrowsingService();
    PerformanceData data;
    
    public OmeroPercentTest(String name){
        super(name);
        data = new PerformanceData(); // Completely random
    }
    
    public OmeroPercentTest(PerformanceData data){
        super("OmeroPercentTest with Data");
        this.data = data;
    }
    
    public void testAll(){
        Object result = testLoadPDIHierarchyProject();
        System.out.println(Utils.structureSize(result));
        testLoadPDIHierarchyDataset();
        testLoadCGCIHierarchyCategoryGroup() ;
        testLoadCGCIHierarchyCategory() ;
        testFindCGCIHierarchies() ;
        testFindPDIHierarchies() ;
        testFindImageAnnotationsSet() ;
        testFindImageAnnotationsSetForExperimenter() ;
        testFindDatasetAnnotationsSet() ;
        testFindDatasetAnnotationsSetForExperimenter();
    }
    
    public Object testLoadPDIHierarchyProject() {
        return hb.loadPDIHierarchy(ProjectData.class, data.prjId);
    }

    public Object testLoadPDIHierarchyDataset() {
        return hb.loadPDIHierarchy(DatasetData.class, data.dsId);
    }

    public Object testLoadCGCIHierarchyCategoryGroup() {
        return hb.loadCGCIHierarchy(CategoryGroupData.class,data.cgId);
    }
    
    public Object testLoadCGCIHierarchyCategory() {
        return hb.loadCGCIHierarchy(CategoryData.class,data.cId);
    }

    public Object testFindCGCIHierarchies() {
        return hb.findCGCIHierarchies(data.imgsCGCI);
    }
    
    public Object testFindPDIHierarchies() {
        return hb.findPDIHierarchies(data.imgsPDI);
    }

    public Object testFindImageAnnotationsSet() {
        return hb.findImageAnnotations(data.imgsAnn1);
    }

    public Object testFindImageAnnotationsSetForExperimenter() {
        return hb.findImageAnnotationsForExperimenter(data.imgsAnn2, data.userId);
    }

    public Object testFindDatasetAnnotationsSet() {
        return hb.findDatasetAnnotations(data.dsAnn1);
    }

    public Object testFindDatasetAnnotationsSetForExperimenter() {
        return hb.findDatasetAnnotationsForExperimenter(data.dsAnn2, data.userId);
    }
    
}
