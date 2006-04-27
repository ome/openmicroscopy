package ome.server.itests.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.api.IPojos;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.PojosQP;
import ome.services.query.QP;
import ome.services.query.QueryParameter;
import ome.services.query.QueryParameterDef;
import ome.util.builders.PojoOptions;

public class FindAnnotationsQueryTest extends AbstractInternalContextTest
{
    PojosFindAnnotationsQueryDefinition q;
    List                                list;
    Set                                 ids;

    protected void creation_fails(QueryParameter... parameters)
    {
        try
        {
            // new IdsQueryParameterDef(),
            // new OptionsQueryParameterDef(),
            // new QueryParameterDef(QP.CLASS,Class.class,false),
            // new QueryParameterDef("annotatorIds",Collection.class,true));
            q = new PojosFindAnnotationsQueryDefinition( parameters );
            fail( "Should have failed!" );
        }
        catch ( IllegalArgumentException e )
        {
        }
    }

    public void test_illegal_arguments() throws Exception
    {

        creation_fails( PojosQP.ids( null ), // Null
                        PojosQP.options( null ),
                        PojosQP.Class( QP.CLASS,
                                       Image.class ),
                        PojosQP.Set( "annotatorIds",
                                     Collections.emptySet( ) ) );

        creation_fails( PojosQP.ids( Collections.emptySet( ) ), // Empty!
                        PojosQP.options( null ),
                        PojosQP.Class( QP.CLASS,
                                       Image.class ),
                        PojosQP.Set( "annotatorIds",
                                     Collections.emptySet( ) ) );
        creation_fails( PojosQP.ids( Arrays.asList( 1L ) ),
                        PojosQP.Class( QP.CLASS, // Null here.
                                       null ),
                        PojosQP.options( null ),
                        PojosQP.Set( "annotatorIds",
                                     Collections.emptySet( ) ) );
        creation_fails( PojosQP.ids( Arrays.asList( 1 ) ), // Integer not Long
                        PojosQP.options( null ),
                        PojosQP.Class( QP.CLASS,
                                       Image.class ),
                        PojosQP.Set( "annotatorIds",
                                     Collections.emptySet( ) ) );
    }

    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q = new PojosFindAnnotationsQueryDefinition( 
                                                     PojosQP.ids( Arrays.asList( doesntExist ) ),
                                                     PojosQP.Class(QP.CLASS, Image.class),
                                                     PojosQP.options(null),
                                                     PojosQP.Set("annotatorIds",Collections.EMPTY_SET)
                                                     );

        list = (List) iQuery.execute( q );
    }

    public void test_images_exist() throws Exception
    {
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
        q = new PojosFindAnnotationsQueryDefinition( 
                                                    PojosQP.ids( ids ),
                                                    PojosQP.Class(QP.CLASS, Image.class),
                                                    PojosQP.options(null),
                                                    PojosQP.Set("annotatorIds",Collections.EMPTY_SET)
                                                    );
        Collection<ImageAnnotation> results = (Collection) iQuery.execute(q);
        for ( ImageAnnotation annotation : results )
        {
            assertTrue(ids.contains(annotation.getImage().getId()));
        }
    }
    
    public void test_dataset_exist() throws Exception
    {
        ids = new HashSet(data.getMax("Dataset.Annotated.ids",2));
        q = new PojosFindAnnotationsQueryDefinition( 
                                                    PojosQP.ids( ids ),
                                                    PojosQP.Class(QP.CLASS, Dataset.class),
                                                    PojosQP.options(null),
                                                    PojosQP.Set("annotatorIds",Collections.EMPTY_SET)
                                                    );
        Collection<DatasetAnnotation> results = (Collection) iQuery.execute(q);
        for ( DatasetAnnotation annotation : results )
        {
            assertTrue(ids.contains(annotation.getDataset().getId()));
        }
    }
    
    

}
