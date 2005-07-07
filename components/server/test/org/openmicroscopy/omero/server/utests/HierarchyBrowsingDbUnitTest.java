/*
 * org.openmicroscopy.omero.server.utests.HierarchyBrowsingDbUnitTest
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
package org.openmicroscopy.omero.server.utests;

//Java imports
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;


//Third-party libraries
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractSpringContextTests;

//Application-internal dependencies
import org.openmicroscopy.omero.logic.AnnotationDao;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Image;

/**
 * @author josh
 * @DEV.TODO move to itests if addCacheableFile doesn't speed things up.
 * @DEV.TODO test "valid=false" sections of queries
 */
public class HierarchyBrowsingDbUnitTest extends AbstractSpringContextTests {

    static IDatabaseConnection c = null;
    ApplicationContext ctx;
    DataSource ds = null;
    ContainerDao cdao = null;
    AnnotationDao adao = null;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HierarchyBrowsingDbUnitTest.class);
    }

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { 
                "WEB-INF/dao.xml",
                "WEB-INF/test/config-test.xml", 
                "WEB-INF/test/data-test.xml" };
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        ctx = getContext(getConfigLocations());

        if (null == cdao || null == adao || null == ds) {
            cdao = (ContainerDao) ctx.getBean("containerDao");
            adao = (AnnotationDao) ctx.getBean("annotationDao");
            ds = (DataSource) ctx.getBean("dataSource");
        }
        
        if (null==c) {
            try {
                c = new DatabaseConnection(ds.getConnection());
                DatabaseOperation.CLEAN_INSERT.execute(c,getData());
            } catch (Exception e){
                c = null;
                throw e;
            }
        }

    }
    
    public IDataSet getData() throws Exception {
        URL file = this.getClass().getClassLoader().getResource("db-export.xml");
        return new XmlDataSet(new FileInputStream(file.getFile()));
    }

    public void testFindPDIHierarchies(){
        Set set = getSetFromInt(new int[]{1,5,6,7,8,9,0});
        List tmp = cdao.findPDIHierarchies(set,-1,false);
        Set result = new HashSet(tmp);
        assertTrue("Should have found all the images but Zero but found "+result.size(), result.size()+1==set.size());
        for (Iterator i = result.iterator(); i.hasNext();) {
            Image img = (Image) i.next();
            Set ds = img.getDatasets();
            assertTrue("Fully initialized datasets",Hibernate.isInitialized(ds));
            for (Iterator j = ds.iterator(); j.hasNext();) {
                Dataset d = (Dataset) j.next();
                assertTrue("Fully initialized projects",Hibernate.isInitialized(d.getProjects()));
            }
        }        
    }
    
    public void testFindDSAnn() {
        // Use filter sets to DRY
        Set set = new HashSet();
        set.add(new Integer(120));
        int experimenter = 286033;
        testDsResult(adao.findDataListAnnotationForExperimenter(set,experimenter));
        testDsResult(adao.findDataListAnnotations(set));
    }
 
    void testDsResult(List result){
        assertTrue("This should be the only known DS annotation",result.size()==1);
        DatasetAnnotation ann = (DatasetAnnotation) result.get(0);
        assertTrue("Attribute id is also known.", ann.getAttributeId().intValue()==320101);
        assertTrue("Mex should be fetched.",Hibernate.isInitialized(ann.getModuleExecution()));
    }
    
    public void testFindImageAnn(){
        Set set = new HashSet();
        //select a.image_id, m.experimenter_id from module_executions m, image_annotations a where a.module_execution_id = m.module_execution_id;
        //images{1,27, 313,36,42, 7,41, 357, 296,11,26,28,39,31, 485, 558,25,24, 340,4391,4446,4507, 8, 1,22};
        //users
        set.add(new Integer(1));
        set.add(new Integer(27));
        set.add(new Integer(313));
        int experimenter = 1;
        List r1 = adao.findImageAnnotations(set);
        List r2 = adao.findImageAnnotationsForExperimenter(set,experimenter);
        assertTrue("The user filtered list should be smaller", r2.size() < r1.size());
    }
    
    Set getSetFromInt(int[] ids){
        Set set = new HashSet();
        for (int i = 0; i < ids.length; i++) {
            set.add(new Integer(ids[i]));
        }
        return set;
    }
}
