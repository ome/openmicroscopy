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
import ome.util.builders.PojoOptions;

/*

 cg |  cg  | cg_owner | cg_group | c |  c   | c_owner | c_group | img | img  | i_owner | i_group | iann | iann_owner | iann_group 
----+------+----------+----------+---+------+---------+---------+-----+------+---------+---------+------+------------+------------
 CG | 9091 |        0 |        0 | C | 7071 |       0 |       0 | Img | 5051 |       0 |       0 | 3031 |          0 |          0
 CG | 9092 |        0 |        0 | C | 7071 |       0 |       0 | Img | 5051 |       0 |       0 | 3031 |          0 |          0
 CG | 9091 |        0 |        0 | C | 7072 |       0 |       0 | Img | 5051 |       0 |       0 | 3031 |          0 |          0
 CG | 9092 |        0 |        0 | C | 7072 |       0 |       0 | Img | 5051 |       0 |       0 | 3031 |          0 |          0
 CG | 9091 |        0 |        0 | C | 7071 |       0 |       0 | Img | 5052 |       0 |       0 | 3332 |      10000 |          0
 CG | 9092 |        0 |        0 | C | 7071 |       0 |       0 | Img | 5052 |       0 |       0 | 3332 |      10000 |          0
 CG | 9091 |        0 |        0 | C | 7072 |       0 |       0 | Img | 5052 |       0 |       0 | 3332 |      10000 |          0
 CG | 9092 |        0 |        0 | C | 7072 |       0 |       0 | Img | 5052 |       0 |       0 | 3332 |      10000 |          0
 CG | 9991 |    10000 |        0 | C | 7771 |   10000 |       0 | Img | 5551 |   10000 |       0 | 3033 |          0 |          0
 CG | 9992 |    10000 |        0 | C | 7771 |   10000 |       0 | Img | 5551 |   10000 |       0 | 3033 |          0 |          0
 CG | 9991 |    10000 |        0 | C | 7772 |   10000 |       0 | Img | 5551 |   10000 |       0 | 3033 |          0 |          0
 CG | 9992 |    10000 |        0 | C | 7772 |   10000 |       0 | Img | 5551 |   10000 |       0 | 3033 |          0 |          0
 CG | 9991 |    10000 |        0 | C | 7771 |   10000 |       0 | Img | 5552 |   10000 |       0 | 3334 |      10000 |          0
 CG | 9992 |    10000 |        0 | C | 7771 |   10000 |       0 | Img | 5552 |   10000 |       0 | 3334 |      10000 |          0
 CG | 9991 |    10000 |        0 | C | 7772 |   10000 |       0 | Img | 5552 |   10000 |       0 | 3334 |      10000 |          0
 CG | 9992 |    10000 |        0 | C | 7772 |   10000 |       0 | Img | 5552 |   10000 |       0 | 3334 |      10000 |          0

TODO FIXME NEW Data with only one in each group (not all images in all categories in cg e.g.)
 */ 

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
        QueryParameter declassification = PojosQP.algorithm(IPojos.DECLASSIFICATION);
        run_tests( declassification, 0, 4, 4, 0, 4);
    }
    
    public void test_classification_nme() throws Exception
    {
        QueryParameter classificationNME = PojosQP.algorithm(IPojos.CLASSIFICATION_NME);
        run_tests( classificationNME, 16, 12, 12, 8, 4); // TODO need more
    }

    public void test_classification_me() throws Exception
    {
        QueryParameter classificationME = PojosQP.algorithm(IPojos.CLASSIFICATION_ME);
        run_tests( classificationME, 16, 12, 12, 0, 12); // TODO need more
    }
    
    private void run_tests(QueryParameter algorithm, int...sizes)
    {
        QueryParameter noOptions = QP.Map("options", null);
        PojoOptions po = new PojoOptions().exp(10000L);
        QueryParameter notRootOptions = QP.Map("options", po.map());

        // No categories for image.
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5050L)),
                algorithm,noOptions);
           
        list = (List) iQuery.execute(q);
        assertListSize( sizes[0] );
        
        // Well-defined categories (root).
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5051L)),
                algorithm,noOptions);
           
        list = (List) iQuery.execute(q);
        assertListSize( sizes[1] );

        // Well-defined categories (user).
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5551L)),
                algorithm,noOptions);
           
        list = (List) iQuery.execute(q);
        assertListSize( sizes[2] );
        
        // Filtering out root on root's objects.
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5051L)), // Belongs to root.
                algorithm,notRootOptions); // Not root.
           
        list = (List) iQuery.execute(q);
        assertListSize( sizes[3] );

        // Filtering out root on user's objects.
        q= new PojosCGCPathsQueryDefinition(
                PojosQP.ids(Arrays.asList(5551L)), // Belongs to user.
                algorithm,notRootOptions); // Not root.
           
        list = (List) iQuery.execute(q);
        assertListSize(  sizes[4] );

       
    }

    
    
    private void assertListSize(int size)
    {
        assertTrue(String.format("List.size() != %d but was %s",size,list.size()),
                list.size() == size);
    }
    
}
