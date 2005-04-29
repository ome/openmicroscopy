/*
 * Created on Apr 28, 2005
*/
package org.ome.omero.tests;

import org.ome.omero.interfaces.HierarchyBrowsing;
import org.ome.omero.model.Dataset;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class HierarchyBrowsingTest extends TestCase {

    HierarchyBrowsing hb = (HierarchyBrowsing) SpringTestHarness.ctx.getBean("hierarchyBrowsingService");
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HierarchyBrowsingTest.class);
    }

    public void testLoadPDIHierarchy() {
        hb.loadPDIHierarchy(Dataset.class, 1);
    }

    public void testLoadCGCIHierarchy() {
    }

    public void testFindPDIHierarchies() {
    }

    public void testFindCGCIHierarchies() {
    }

    /*
     * Class under test for Map findImageAnnotations(Set)
     */
    public void testFindImageAnnotationsSet() {
    }

    /*
     * Class under test for Map findImageAnnotations(Set, int)
     */
    public void testFindImageAnnotationsSetint() {
    }

    /*
     * Class under test for Map findDatasetAnnotations(Set)
     */
    public void testFindDatasetAnnotationsSet() {
    }

    /*
     * Class under test for Map findDatasetAnnotations(Set, int)
     */
    public void testFindDatasetAnnotationsSetint() {
    }

}
