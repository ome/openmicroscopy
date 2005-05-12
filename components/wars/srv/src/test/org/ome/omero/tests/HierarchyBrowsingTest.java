/*
 * Created on Apr 28, 2005
 */
package org.ome.omero.tests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ome.omero.interfaces.HierarchyBrowsing;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class HierarchyBrowsingTest extends TestCase {

    HierarchyBrowsing hb = (HierarchyBrowsing) SpringTestHarness.ctx
            .getBean("hierarchyBrowsingService");

    String nullObj = "This should get us nothing.";
    String emptyColl = "This collection should be empty.";
    String nonNull = "We should get something back";

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HierarchyBrowsingTest.class);
    }

    public void testNulls(){
        // Each method should return a null or an empty set as appropriate
        //TODO hb.findCGCIHierarchies(	);
        
        Set test = new HashSet();
        test.add(new Integer(0)); // Non-existence set of ids
        int nonExp = 0; // Non-existence experimenter ID
        //
        assertTrue(emptyColl,hb.findDatasetAnnotations(test).size()==0);
        assertTrue(emptyColl,hb.findDatasetAnnotations(new HashSet()).size()==0);
        //
        assertTrue(emptyColl,hb.findDatasetAnnotationsForExperimenter(test,nonExp).size()==0);
        assertTrue(emptyColl,hb.findDatasetAnnotationsForExperimenter(new HashSet(),nonExp).size()==0);
        //
        assertTrue(emptyColl,hb.findImageAnnotations(test).size()==0);
        assertTrue(emptyColl,hb.findImageAnnotations(new HashSet()).size()==0);
        //
        assertTrue(emptyColl,hb.findImageAnnotationsForExperimenter(test,nonExp).size()==0);
        assertTrue(emptyColl,hb.findImageAnnotationsForExperimenter(new HashSet(),nonExp).size()==0);
        //
        assertTrue(emptyColl,hb.findPDIHierarchies(test).size()==0);
        assertTrue(emptyColl,hb.findPDIHierarchies(new HashSet()).size()==0);
        //
        assertNull(nullObj,hb.loadCGCIHierarchy(CategoryGroupData.class, 0));
        assertNull(nullObj,hb.loadCGCIHierarchy(CategoryData.class, 0));
        //
        assertNull(nullObj,hb.loadPDIHierarchy(ProjectData.class, 0));
        assertNull(nullObj,hb.loadPDIHierarchy(DatasetData.class, 0));
    }
    
    public void testLoadPDIHierarchy() {
        System.out.println(" ***** HierarchyBrowsingTest.testLoadPDIHierarchy()");
        DataObject dobj = hb.loadPDIHierarchy(DatasetData.class, 10);
        assertTrue(nonNull, dobj != null);

    }

    public void testLoadCGCIHierarchy() {
    }

    public void testFindPDIHierarchies() {
        System.out.println(" ***** HierarchyBrowsingTest.testFindPDIHierarchies()");
        Set ids = new HashSet();
        ids.add(new Integer(19));
        ids.add(new Integer(13));
        ids.add(new Integer(11));
        
        // Something
        Set result = hb.findPDIHierarchies(ids);
        assertTrue(nonNull, result != null && result.size() != 0);
        // Not to much
        Set test = new HashSet();
        Iterator i = result.iterator();
        while (i.hasNext()){
            DataObject o = (DataObject) i.next();
            if (o instanceof ImageData) {
                test.add(o);
            } else if (o instanceof DatasetData) {
                DatasetData dd = (DatasetData) o;
                test.addAll(dd.images);
            } else if (o instanceof ProjectData) {
                ProjectData pd = (ProjectData) o;
                Iterator p = pd.datasets.iterator();
                while (p.hasNext()){
                    DatasetData dd = (DatasetData) p.next();
                    test.addAll(dd.images);
                }
            }
        }
        assertTrue("There should only be 3 images in the return", test.size() == 3);
        //But everything we need
//      TODO Make sure joins aren't leaving anything out because of empties!
    }

    public void testFindCGCIHierarchies() {
        System.out
                .println(" ***** HierarchyBrowsingTest.testFindCGCIHierarchies() *****");
        Set ids = new HashSet();
        ids.add(new Integer(19));
        ids.add(new Integer(13));
        ids.add(new Integer(11));
        
        // Something
        Set result = hb.findCGCIHierarchies(ids);
    }

    /*
     * Class under test for Map findImageAnnotations(Set)
     */
    public void testFindImageAnnotationsSet() {
        System.out
                .println(" ***** HierarchyBrowsingTest.testFindImageAnnotationsSet()");
        Set ids = new HashSet();
        ids.add(new Integer(11));
        ids.add(new Integer(13));
        Map map = hb.findImageAnnotations(ids);
        assertTrue(nonNull, map != null && map.size() != 0);
    }

    /*
     * Class under test for Map findImageAnnotations(Set, int)
     */
    public void testFindImageAnnotationsSetint() {
        System.out.println(" ***** HierarchyBrowsingTest.testFindImageAnnotationsSetint()");
        Set ids = new HashSet();
        ids.add(new Integer(11));
        ids.add(new Integer(13));
        Map map = hb.findImageAnnotationsForExperimenter(ids,5);
        assertTrue(nonNull, map != null && map.size() != 0);
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

    public void testFindNoNullsRecursivelyAndReflectively(){
        
    }
    
}
