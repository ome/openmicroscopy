/*
 * Created on Jun 7, 2005
*/
package org.openmicroscopy.omero.tests.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.Project;

/**
 * @author josh
 */
public class OmeroHierarchyBrowsingIntegrationTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[]{"org/openmicroscopy/omero/client/spring.xml"}; 
    }
    
    public OmeroHierarchyBrowsingIntegrationTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroHierarchyBrowsingIntegrationTest(OMEData data) {
        super("Omero Integration Test with Data",data);
    }
    
    public void testContainerCallWithWrongParameters(){
        try {
            hb.loadPDIHierarchy(Object.class,1);
            fail("loadPDIHierarchy(class,int) didn't choke on bad class.");
        } catch (IllegalArgumentException iae){
            // We should get here. TODO log
        }

        try {
            hb.loadCGCIHierarchy(Object.class,1);
            fail("loadCGCIHierarchy(class,int) didn't choke on bad class.");
        } catch (IllegalArgumentException iae){
            // We should get here. TODO log
        }
             
       }
    
    String nullObj = "This should get us nothing.";
    String emptyColl = "This collection should be empty.";
    String nonNull = "We should get something back";

    public void testNulls(){
        // Each method should return a null or an empty set as appropriate
        //TODO hb.findCGCIHierarchies(	);
        //TODO generate OMENullData(); and use it here.
        //TODO OMEData toString();
        
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
        assertNull(nullObj,hb.loadCGCIHierarchy(CategoryGroup.class, 0));
        assertNull(nullObj,hb.loadCGCIHierarchy(Category.class, 0));
        //
        assertNull(nullObj,hb.loadPDIHierarchy(Project.class, 0));
        assertNull(nullObj,hb.loadPDIHierarchy(Dataset.class, 0));
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
        assertTrue("There should only be as many images as in the data.imagesPDI", test.size() == this.data.imgsPDI.size());
//      TODO Make sure joins aren't leaving anything out because of empties!
    }

   
}
