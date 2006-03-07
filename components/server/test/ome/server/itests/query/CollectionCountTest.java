package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.CollectionCountQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.QueryParameter;
import ome.util.IdBlock;
import ome.util.builders.PojoOptions;

public class CollectionCountTest extends AbstractInternalContextTest
{
   
    private static Log log = LogFactory.getLog(CollectionCountTest.class);
    
    CollectionCountQueryDefinition q;
    List list;

    
    protected void creation_fails(QueryParameter...parameters){
        try {
            q= new CollectionCountQueryDefinition( // TODO if use lookup, more generic
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {
            log.info ( "Expected: "+e.getMessage() );
        }
    }
    
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( );
        
        creation_fails(
                PojosQP.ids( null ), // Null
                PojosQP.String( "field", null )
                );
        
        creation_fails(
                PojosQP.ids( new ArrayList() ), // Empty
                PojosQP.String( "field", null )
                );
    }
    
    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new CollectionCountQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.String("field", Project.DATASETLINKS));
           
        list = (List) iQuery.execute(q);

    }
    
}
