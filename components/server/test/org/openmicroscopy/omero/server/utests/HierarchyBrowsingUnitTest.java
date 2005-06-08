package org.openmicroscopy.omero.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import org.openmicroscopy.omero.logic.AnnotationDao;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.HierarchyBrowsingImpl;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;

/**
 * This class tests the generic Manager and BaseManager implementation.
 * @DEV.SEARCH No Hibernate or Spring dependencies
 * @DEV.TODO add DEV.SEARCH to build.
 */
public class HierarchyBrowsingUnitTest extends MockObjectTestCase {
    protected HierarchyBrowsingImpl manager = new HierarchyBrowsingImpl();
    protected Mock annotationDao,containerDao;
    
    protected void setUp() throws Exception {
        super.setUp();
        annotationDao = new Mock(AnnotationDao.class);
        containerDao = new Mock(ContainerDao.class);
        manager.setAnnotationDao((AnnotationDao) annotationDao.proxy());
        manager.setContainerDao((ContainerDao) containerDao.proxy()	);
    }
    
    protected void tearDown() throws Exception {
        manager = null;
        annotationDao = null;
        containerDao = null;
    }

    public void testAnnotationsWithVariousIdSets(){

        /*
         * Image Anns; No Experimenter 
         */
        
        // Arguments
        Integer id = new Integer(1);
        Set ids = new HashSet();
        ids.add(id);
        
        // Return value
        ImageAnnotation an = new ImageAnnotation();
        Image img = new Image();
        img.setImageId(id);
        an.setImage(img);
        List result = new ArrayList();
        result.add(an);

        // Run
        annotationDao.expects(once()).method("findImageAnnotations").with(same(ids)).will(returnValue(result));
        Map map = manager.findImageAnnotations(ids	);
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            Integer key = (Integer) i.next();
            assertTrue("Key should be in our ids set.",ids.contains(key));
        }
        annotationDao.verify();
        annotationDao.reset();

    }

    public void testloadSomeHierarchy(){
        // currently no logic ; testing for bad paramters elsewhere.
    }
    
    public void testfindPDIHierarchies(){
        
        /*
         * PDI Hierachies 
         */
        
        // Arguments
        Integer id = new Integer(1);
        Set ids = new HashSet();
        ids.add(id);
        
        // Result
        Image img = new Image();
        img.setImageId(id);
        img.setDatasets(new HashSet());
        Dataset ds = new Dataset();
        ds.setProjects(new HashSet());
        Project prj = new Project();
        img.getDatasets().add(ds);
        ds.getProjects().add(prj);
        List result = new ArrayList();
        result.add(img);

        // Run
        containerDao.expects(once()).method("findPDIHierarchies").with(same(ids)).will(returnValue(result));
        Set set = manager.findPDIHierarchies(ids	);
        List list = new ArrayList(set);
        assertTrue("There should only be one project in the result.",list.size()==1);
        assertTrue("And that one project must be this one.",list.get(0)==prj);
        containerDao.verify();
        containerDao.reset();
    }
    
}
