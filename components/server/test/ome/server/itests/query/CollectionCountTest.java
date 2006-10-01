package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.model.containers.Project;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.CollectionCountQueryDefinition;

public class CollectionCountTest extends AbstractManagedContextTest
{
   
    private static Log log = LogFactory.getLog(CollectionCountTest.class);
    
    CollectionCountQueryDefinition q;
    List list;

    
    protected void creation_fails(Parameters parameters){
        try {
            q= new CollectionCountQueryDefinition( // TODO if use lookup, more generic
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {
        } catch (ApiUsageException e) {
        }
    }
    
    @Test
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( null );
        
        creation_fails(
                new Parameters()
                    .addIds(null) // Null
                    .addString("field",null));
        
        creation_fails(
                new Parameters()
                .addIds(new ArrayList()) // Empty
                .addString("field",null));
    }
    
    @Test
    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new CollectionCountQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList(doesntExist))
                .addString("field", Project.DATASETLINKS));
           
        list = (List) iQuery.execute(q);

    }
    
}
