/*
 * ome.server.utests.HierarchyBrowsingUnitTest
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
package ome.server.utests;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

//Application-internal dependencies
import ome.dao.AnnotationDao;
import ome.dao.ContainerDao;
import ome.logic.HierarchyBrowsingImpl;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.Project;

/**
 * tests each public method of HierachyBrowsing using jmock
 * facilities for the needed Daos.
 * 
 * The load* methods have no logic and so aren't tested.
 * 
 * @DEV.TODO add DEV.SEARCH to build. (e.g. no hibernate or spring here)
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
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
        containerDao.expects(once()).method("findPDIHierarchies").with(same(ids),same(-1),same(false)).will(returnValue(result));
        Set set = manager.findPDIHierarchies(ids	);
        List list = new ArrayList(set);
        assertTrue("There should only be one project in the result.",list.size()==1);
        assertTrue("And that one project must be this one.",list.get(0)==prj);
        containerDao.verify();
        containerDao.reset();
    }
    
    public void testfindCGCIHiearchies(){

        /*
         * CGCI Hierachies 
         */
        
        // Arguments
        Integer id = new Integer(1);
        Set ids = new HashSet();
        ids.add(id);
        
        // Result
        Image img = new Image();
        img.setImageId(id);
        img.setClassifications(new HashSet());
        Classification cla1 = new Classification();
        img.getClassifications().add(cla1);
        Category c1 = new Category();
        CategoryGroup cg1 = new CategoryGroup();
        cla1.setCategory(c1);
        c1.setCategoryGroup(cg1);
        List result = new ArrayList();
        result.add(img);

        // Run
        containerDao.expects(once()).method("findCGCIHierarchies").with(same(ids),same(-1),same(false)).will(returnValue(result));
        Set set = manager.findCGCIHierarchies(ids);
        List list = new ArrayList(set);
        assertTrue("There should only be one category group in the result not "+list.size(),list.size()==1);
        assertTrue("And that one c. group should be this one not "+list.get(0),list.get(0)==cg1);
        containerDao.verify();
        containerDao.reset();

    }
    
}
