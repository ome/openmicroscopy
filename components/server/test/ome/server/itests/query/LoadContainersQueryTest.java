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

    List level2objects = Arrays.asList( 9991L, 9992L  );
    List level1objects = Arrays.asList( 7771L, 7772L  );
    List level0objects = Arrays.asList( 5551L, 5552L  );
    
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

    // =========================================================================
    // =========================================================================
    // ~ UNFILTERED
    // =========================================================================
    // =========================================================================

    
    public void test_retrieve_levels() throws Exception
    {
       
        runLevel2( Project.class );
        check_pd_ids( level1objects );

        runLevel2WithLeaves( Project.class );
        check_pdi_ids( level1objects, level0objects );
        
        runLevel2( CategoryGroup.class );
        check_cgc_ids( level1objects );

        runLevel2WithLeaves( CategoryGroup.class );
        check_cgci_ids( level1objects, level0objects );

        runLevel1( Dataset.class );

        runLevel1WithLeaves( Dataset.class );
        check_di_ids( level0objects );
        
        runLevel1( Category.class );

        runLevel1WithLeaves( Category.class );
        check_ci_ids( level0objects );

        
    }

    // =========================================================================
    // =========================================================================
    // ~ FILTERING
    // =========================================================================
    // =========================================================================

    
    PojoOptions po10000 = new PojoOptions().exp( 10000L );
    QueryParameter filterForUser = PojosQP.options( po10000.map() );
    QueryParameter noFilter = PojosQP.options( null );

    public void test_owner_filter() throws Exception 
    {
        QueryParameter ids;

        
        // Belongs to user.
        ids = PojosQP.ids(Arrays.asList( 9990L ));
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

        
        // Doesn't belong to user.
        ids = PojosQP.ids(Arrays.asList( 9090L ));
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.Class( QP.CLASS, Project.class ),
                ids, noFilter);
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );
        
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.Class( QP.CLASS, Project.class ),
                ids, filterForUser );
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() == 0 );

       
        // Null ids.
        run_null_filter_check_size(Project.class, 3);
        run_null_filter_check_size(CategoryGroup.class, 8);
        run_null_filter_check_size(Dataset.class, 3);
        run_null_filter_check_size(Category.class, 10);
    }

    // ~ Helpers
    // =========================================================================

    private void runLevel2( Class klass )
    {
        runLevel( klass, level2objects, new PojoOptions() );
    }

    private void runLevel2WithLeaves( Class klass )
    {
        runLevel( klass, level2objects, new PojoOptions().leaves() );
    }

    private void runLevel1( Class klass )
    {
        runLevel( klass, level1objects, new PojoOptions() );
    }

    private void runLevel1WithLeaves( Class klass )
    {
        runLevel( klass, level1objects, new PojoOptions().leaves() );
    }
    
    private void runLevel( Class klass, List ids, PojoOptions po )
    {
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids( ids ),
                PojosQP.options( po.map() ),
                PojosQP.Class( QP.CLASS, klass ));
           
        list = (List) iQuery.execute(q);
        
        assertTrue( "Didn't find any results expected results!", 
                list.size() == ids.size() );
    }

    private void check_pd_ids(List ids)
    {
        for (Project prj : (List<Project>) list)
        {
            List datasetIds = prj.collectFromDatasetLinks( new IdBlock() );
            assertTrue( "And our datasets weren't there", 
                    datasetIds.containsAll( ids ));
        }
    }

    private void check_pdi_ids(List ids1, List ids2)
    {
        check_pd_ids( ids1 );
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

    private void check_di_ids(List ids)
    {
        for (Dataset ds: (List<Dataset>) list)
        {
            List imgIds = ds.collectFromImageLinks( new IdBlock() );
            assertTrue( "And our images weren't there", 
                    imgIds.containsAll( ids ));
        }
    }

    private void check_cgc_ids(List ids)
    {
        for (CategoryGroup cg: (List<CategoryGroup>) list)
        {
            List catIds = cg.collectFromCategoryLinks( new IdBlock() );
            assertTrue( "And our categories weren't there", 
                    catIds.containsAll( ids ));
        }
    }

    private void check_cgci_ids(List ids1, List ids2)
    {
        check_cgc_ids( ids1 );
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
    
    private void check_ci_ids(List ids)
    {
        for (Category cat: (List<Category>) list)
        {
            List imgIds = cat.collectFromImageLinks( new IdBlock() );
            assertTrue( "And our images weren't there", 
                    imgIds.containsAll( ids ));
        }
    }
    
    private void run_null_filter_check_size(Class klass, int size)
    {
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.Class( QP.CLASS, klass ),
                PojosQP.ids( null ), filterForUser );
           
        list = (List) iQuery.execute(q);
        assertTrue( String.format(
                "Didn't find all our objects of type %s, %d < %d ", 
                klass.getName(), list.size(),size),
                list.size() >= size );
    }
    
}
