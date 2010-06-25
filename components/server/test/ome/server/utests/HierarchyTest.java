/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Map;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.services.query.Hierarchy;

import org.hibernate.Criteria;
import org.hibernate.transform.ResultTransformer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HierarchyTest extends MockObjectTestCase {

    // ~ Testng Adapter
    // =========================================================================
    @BeforeMethod
    @Override
    protected void setUp() throws Exception {
        mockCriteria = mock(Criteria.class);
    }

    @AfterMethod
    @Override
    protected void tearDown() throws Exception {
        mockCriteria.reset();
    }

    Mock mockCriteria;

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsProject() {
        addCreateCriteriaToMock(mockCriteria,
                "datasetLinks.parent.projectLinks.parent");

        Hierarchy.fetchParents((Criteria) mockCriteria.proxy(), Project.class,
                2);

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsDataset() {
        addCreateCriteriaToMock(mockCriteria, "datasetLinks.parent");

        Hierarchy.fetchParents((Criteria) mockCriteria.proxy(), Dataset.class,
                1);

    }

    // TODO need with depth less than MAX

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenProject() {
        addCreateCriteriaToMock(mockCriteria,
                "datasetLinks.child.imageLinks.child");

        Hierarchy.fetchChildren((Criteria) mockCriteria.proxy(), Project.class,
                2);
    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenDataset() {
        addCreateCriteriaToMock(mockCriteria, "imageLinks.child");

        Hierarchy.fetchChildren((Criteria) mockCriteria.proxy(), Dataset.class,
                1);
    }

    // TODO join is not currently used.

    /*
     * Test method for 'ome.services.query.Hierarchy.joinParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testJoinParents() {

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.joinChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testJoinChildren() {

    }

    Object _this = new Object(), i_1 = new Object(), i_2 = new Object(),
            l_1 = new Object(), l_2 = new Object();

    Object[] o = new Object[] { _this, i_1, i_2, l_1, l_2 };

    String[] s = new String[] { "this", "genitem_1", "genitem_2", "genlink_1",
            "genlink_2" };

    /*
     * Test method for 'ome.services.query.Hierarchy.getChildTransformer(Class)'
     */
    @Test
    public void testGetChildTransformerProject() {
        ResultTransformer rt = Hierarchy.getChildTransformer(Project.class);
        Map<String, Object> m = (Map) rt.transformTuple(o, s);

        assertTrue(m.containsKey(Project.class.getName()));
        assertEquals(_this, m.get(Project.class.getName()));
        assertTrue(m.containsKey(Dataset.class.getName()));
        assertEquals(i_1, m.get(Dataset.class.getName()));
    }

    /*
     * Test method for
     * 'ome.services.query.Hierarchy.getParentTransformer(Class)'
     */
    @Test
    public void testGetParentTransformer() {

    }

    // ~ Helpers
    // =========================================================================

    private void addCreateCriteriaToMock(Mock mock, String dotSeparatedPath) {
        String[] paths = dotSeparatedPath.split("[.]");
        for (String path : paths) {
            mock.expects(once()).method("createCriteria").with(
                    stringContains(path), ANYTHING, eq(Criteria.LEFT_JOIN))
                    .will(returnValue(null));

        }
    }

}
