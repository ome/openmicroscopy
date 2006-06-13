package ome.server.utests;

import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.transform.ResultTransformer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.services.query.Hierarchy;
import ome.services.query.Query;

public class HierarchyTest extends MockObjectTestCase
{

    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    @Override
    protected void setUp() throws Exception
    {
        mockCriteria = mock( Criteria.class );
    }

    @Configuration(afterTestMethod = true)
    @Override
    protected void tearDown() throws Exception
    {
        mockCriteria.reset( );
    }
    
    Mock mockCriteria;

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsProject()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "datasetLinks.parent.projectLinks.parent" );

        Hierarchy.fetchParents( (Criteria) mockCriteria.proxy( ),
                                Project.class,
                                2 );

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsDataset()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "datasetLinks.parent" );

        Hierarchy.fetchParents( (Criteria) mockCriteria.proxy( ),
                                Dataset.class,
                                1 );

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsCategoryGroup()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "categoryLinks.parent.categoryGroupLinks.parent" );

        Hierarchy.fetchParents( (Criteria) mockCriteria.proxy( ),
                                CategoryGroup.class,
                                2 );

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchParentsCategory()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "categoryLinks.parent" );

        Hierarchy.fetchParents( (Criteria) mockCriteria.proxy( ),
                                Category.class,
                                1 );

    }

    // TODO need with depth less than MAX

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenProject()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "datasetLinks.child.imageLinks.child" );

        Hierarchy.fetchChildren( (Criteria) mockCriteria.proxy( ),
                                 Project.class,
                                 2 );
    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenDataset()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "imageLinks.child" );

        Hierarchy.fetchChildren( (Criteria) mockCriteria.proxy( ),
                                 Dataset.class,
                                 1 );
    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenCategoryGroup()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "categoryLinks.child.imageLinks.child" );

        Hierarchy.fetchChildren( (Criteria) mockCriteria.proxy( ),
                                 CategoryGroup.class,
                                 2 );
    }

    /*
     * Test method for 'ome.services.query.Hierarchy.fetchChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testFetchChildrenCategory()
    {
        addCreateCriteriaToMock( mockCriteria,
                                 "imageLinks.child" );

        Hierarchy.fetchChildren( (Criteria) mockCriteria.proxy( ),
                                 Category.class,
                                 1 );
    }

    // TODO join is not currently used.

    /*
     * Test method for 'ome.services.query.Hierarchy.joinParents(Criteria,
     * Class, int)'
     */
    @Test
    public void testJoinParents()
    {

    }

    /*
     * Test method for 'ome.services.query.Hierarchy.joinChildren(Criteria,
     * Class, int)'
     */
    @Test
    public void testJoinChildren()
    {

    }

    Object   _this = new Object(), 
        i_1 = new Object( ), i_2 = new Object( ), 
        l_1 = new Object( ), l_2 = new Object( );

    Object[] o   = new Object[]{_this, i_1, i_2, l_1, l_2};
    String[] s   = new String[]{"this", "genitem_1", "genitem_2", "genlink_1", "genlink_2"};

    /*
     * Test method for 'ome.services.query.Hierarchy.getChildTransformer(Class)'
     */
    @Test
    public void testGetChildTransformerProject()
    {
        ResultTransformer rt = Hierarchy.getChildTransformer( Project.class );
        Map<String, Object> m = (Map) rt.transformTuple( o, s );
        
        assertTrue( m.containsKey( Project.class.getName( ) ) );
        assertEquals( _this,
                      m.get( Project.class.getName( ) ) );
        assertTrue( m.containsKey( Dataset.class.getName( ) ) );
        assertEquals( i_1,
                      m.get( Dataset.class.getName( ) ) );
    }

    /*
     * Test method for 'ome.services.query.Hierarchy.getChildTransformer(Class)'
     */
    @Test
    public void testGetChildTransformerCategoryGroup()
    {
        ResultTransformer rt = Hierarchy.getChildTransformer( CategoryGroup.class );
        Map<String, Object> m = (Map) rt.transformTuple( o, s );
        assertTrue( m.containsKey( CategoryGroup.class.getName( ) ) );
        assertEquals( _this,
                      m.get( CategoryGroup.class.getName( ) ) );
        assertTrue( m.containsKey( Category.class.getName( ) ) );
        assertEquals( i_1,
                      m.get( Category.class.getName( ) ) );

    }

    /*
     * Test method for
     * 'ome.services.query.Hierarchy.getParentTransformer(Class)'
     */
    @Test
    public void testGetParentTransformer()
    {

    }

    // ~ Helpers
    // =========================================================================

    private void addCreateCriteriaToMock(Mock mock,
                                         String dotSeparatedPath)
    {
        String[] paths = dotSeparatedPath.split( "[.]" );
        for ( String path : paths )
        {
            mock.expects( once( ) ).method( "createCriteria" ).with( stringContains( path ),
                                                                     ANYTHING,
                                                                     eq( Criteria.LEFT_JOIN ) ).will( returnValue( null ) );

        }
    }

}
