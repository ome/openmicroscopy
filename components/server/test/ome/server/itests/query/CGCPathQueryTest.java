package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.api.IPojos;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.Query;
import ome.services.query.QueryParameter;


public class CGCPathQueryTest extends AbstractInternalContextTest
{
    PojosCGCPathsQueryDefinition q;
    List list;

    protected void creation_fails(QueryParameter...parameters){
        try {
            q= new PojosCGCPathsQueryDefinition(
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {}
    }
    
    public void test_illegal_arguments() throws Exception
    {

        creation_fails(
                PojosQP.ids(null), // Null
                QP.String("algorithm","CLASSIFICATION_NME"),
                QP.Map("options",null));
        creation_fails(
                PojosQP.ids(new ArrayList()), // Empty !
                QP.String("algorithm","CLASSIFICATION_NME"),
                QP.Map("options",null));
        creation_fails(
                PojosQP.ids(Arrays.asList(1)),
                QP.String("algorithm",null), // Null here
                QP.Map("options",null));
        creation_fails(
                PojosQP.ids(Arrays.asList(1)), // Integer not Long !
                QP.String("algorithm","DECLASSIFICATION"),
                QP.Map("options",null));
        
    }

    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.algorithm(IPojos.DECLASSIFICATION),
                QP.Map("options",null));
           
        list = (List) iQuery.execute(q);
 
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.algorithm(IPojos.CLASSIFICATION_NME),
                QP.Map("options",null));
           
        list = (List) iQuery.execute(q);
 
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(doesntExist)),
                PojosQP.algorithm(IPojos.CLASSIFICATION_ME),
                QP.Map("options",null));
           
        list = (List) iQuery.execute(q);
 
    }
    
    public void test_declassification() throws Exception 
    {
        
        QueryParameter declass = PojosQP.algorithm(IPojos.DECLASSIFICATION);
        QueryParameter noOptions = QP.Map("options", null);
        
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5051L)),
                declass,noOptions);
           
        list = (List) iQuery.execute(q);
        assertTrue("List.size() != 4 but "+list.size(),list.size() == 4);
        
    }
    
}
