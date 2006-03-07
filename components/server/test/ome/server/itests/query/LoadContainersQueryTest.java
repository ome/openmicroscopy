package ome.server.itests.query;

import java.util.Arrays;
import java.util.List;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.QueryParameter;
import ome.util.IdBlock;
import ome.util.builders.PojoOptions;

public class LoadContainersQueryTest extends AbstractInternalContextTest
{
    PojosLoadHierarchyQueryDefinition q;
    List list;

    protected void creation_fails(QueryParameter...parameters){
        try {
            q= new PojosLoadHierarchyQueryDefinition( // TODO if use lookup, more generic
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {}
    }
    
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( );
        
        creation_fails(
                PojosQP.ids(null), // Null
                PojosQP.options(null),
                PojosQP.Class(QP.CLASS, null)
                );
        
        creation_fails(
                PojosQP.ids( Arrays.asList( 1 )), // Not long
                PojosQP.options( null ),
                PojosQP.Class(QP.CLASS, Project.class )
                );

        /* TODO currently handled by IPojos
        creation_fails(
                PojosQP.ids( new ArrayList() ), // Empty
                PojosQP.options( null ),
                PojosQP.Class(QP.CLASS, Project.class )
                );
        
        PojoOptions po = new PojoOptions().allExps();
        creation_fails(
                PojosQP.ids( null ), 
                PojosQP.options( po.map() ), // Has to have experimenter
                PojosQP.Class(QP.CLASS, Project.class )
                );
        */
        
    }

    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.options(null),
                PojosQP.Class( QP.CLASS, Project.class ));
           
        list = (List) iQuery.execute(q);

        PojoOptions po = new PojoOptions().exp( doesntExist );
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids( null ),
                PojosQP.options( po.map() ),
                PojosQP.Class( QP.CLASS, Project.class ));
           
        list = (List) iQuery.execute(q);
        
    }

    public void test_retrieve_level_2() throws Exception
    {
        List level1Ids = Arrays.asList( 7771L, 7772L );
        List level2Ids = Arrays.asList( 5551L, 5552L );
        
        runLevel2( Project.class );
        checkDatasetIds( level1Ids );

        runLevel2WithLeaves( Project.class );
        checkDatasetImageIds( level1Ids, level2Ids );
        
        runLevel2( CategoryGroup.class );
        checkCategoryIds( level1Ids );

        runLevel2WithLeaves( CategoryGroup.class );
        checkCategoryImageIds( level1Ids, level2Ids );
        
    }

    public void test_owner_filter() throws Exception 
    {
        PojoOptions po = new PojoOptions().exp( 10000L );
        QueryParameter filterForUser = PojosQP.options( po.map() );
        QueryParameter noFilter = PojosQP.options( null );
        
        QueryParameter ids = PojosQP.ids(Arrays.asList( 9990L ));
        
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.Class( QP.CLASS, Project.class ),
                ids, noFilter);
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );
        
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.Class( QP.CLASS, Project.class ),
                ids, filterForUser );
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );
        
    }

    // ~ Helpers
    // =========================================================================

    private void runLevel2( Class klass )
    {
        runLevel2( klass, new PojoOptions() );
    }

    private void runLevel2WithLeaves( Class klass )
    {
        runLevel2( klass, new PojoOptions().leaves() );
    }

    private void runLevel2( Class klass, PojoOptions po )
    {
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids( Arrays.asList( 9991L, 9992L  ) ),
                PojosQP.options( po.map() ),
                PojosQP.Class( QP.CLASS, klass ));
           
        list = (List) iQuery.execute(q);
        
        assertTrue( "Didn't find our two projects", list.size() == 2 );
    }

    private void checkDatasetIds(List ids)
    {
        for (Project prj : (List<Project>) list)
        {
            List datasetIds = prj.collectFromDatasetLinks( new IdBlock() );
            assertTrue( "And our datasets weren't there", 
                    datasetIds.containsAll( ids ));
        }
    }

    private void checkDatasetImageIds(List ids1, List ids2)
    {
        checkDatasetIds( ids1 );
        for (Project prj : (List<Project>) list)
        {
            for (Dataset ds : (List<Dataset>) prj.collectFromDatasetLinks(null))
            {
                List imagesIds = ds.collectFromImageLinks( new IdBlock() );
                assertTrue( "Missing images", 
                        imagesIds.containsAll( ids2 ));
            }
            
        }
    }
    
    private void checkCategoryIds(List ids)
    {
        for (CategoryGroup cg: (List<CategoryGroup>) list)
        {
            List catIds = cg.collectFromCategoryLinks( new IdBlock() );
            assertTrue( "And our categories weren't there", 
                    catIds.containsAll( ids ));
        }
    }

    private void checkCategoryImageIds(List ids1, List ids2)
    {
        checkCategoryIds( ids1 );
        for (CategoryGroup cg: (List<CategoryGroup>) list)
        {
            for (Category cat: (List<Category>) cg.collectFromCategoryLinks(null))
            {
                List imagesIds = cat.collectFromImageLinks( new IdBlock() );
                assertTrue( "Missing images", 
                        imagesIds.containsAll( ids2 ));
            }
            
        }
    }
    
}
