package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.api.IPojos;
import ome.model.containers.Project;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.Query;
import ome.services.query.QueryParameter;
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
        
    }

    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new PojosLoadHierarchyQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.options(null),
                PojosQP.Class( QP.CLASS, Project.class ));
           
        list = (List) iQuery.execute(q);
 
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
