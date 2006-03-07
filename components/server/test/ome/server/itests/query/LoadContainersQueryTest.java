package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ome.api.IPojos;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.Query;
import ome.services.query.QueryParameter;
import ome.util.CBlock;
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

    public void test_retrieve_projects() throws Exception
    {
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids( Arrays.asList( 9991L, 9992L  ) ),
                PojosQP.options( null ),
                PojosQP.Class( QP.CLASS, Project.class ));
           
        list = (List) iQuery.execute(q);
        
        assertTrue( "Didn't find our two projects", list.size() == 2 );
        
        for (Project prj : (List<Project>) list)
        {
            List datasetIds = prj.collectFromDatasetLinks( new IdBlock() );
            assertTrue( "And our datasets weren't there", 
                    datasetIds.containsAll( Arrays.asList( 7771L, 7772L )));
        }
        
    }

    public void test_retrieve_projects_with_leaves() throws Exception
    {
        PojoOptions po = new PojoOptions().leaves();
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids( Arrays.asList( 9991L, 9992L  ) ),
                PojosQP.options( po.map() ),
                PojosQP.Class( QP.CLASS, Project.class ));
           
        list = (List) iQuery.execute(q);

        for (Project prj : (List<Project>) list)
        {
            for (Dataset ds : (List<Dataset>) prj.collectFromDatasetLinks(null))
            {
                List imagesIds = ds.collectFromImageLinks( new IdBlock() );
                assertTrue( "Missing images", 
                        imagesIds.containsAll( Arrays.asList( 5551L, 5552L )));
            }
            
        }
        
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
}
