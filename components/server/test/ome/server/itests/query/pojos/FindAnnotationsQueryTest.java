package ome.server.itests.query.pojos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.api.IPojos;
import ome.conditions.ApiUsageException;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractInternalContextTest;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.QueryParameterDef;
import ome.testing.CreatePojosFixture;
import ome.util.builders.PojoOptions;

public class FindAnnotationsQueryTest extends AbstractInternalContextTest
{
    PojosFindAnnotationsQueryDefinition q;
    List                                list;
    Set                                 ids;
    CreatePojosFixture					DATA;
    
    @Configuration( beforeTestClass = true )
    public void makePojos() throws Exception
    {
    	try {
    		setUp();
    		DATA = new CreatePojosFixture( this.serviceFactory );
    		DATA.pdi();
    		DATA.annotations();
    	} finally {
    		tearDown();
    	}
    }
    
    protected void creation_fails(Parameters parameters)
    {
        try
        {
            // new IdsQueryParameterDef(),
            // new OptionsQueryParameterDef(),
            // new QueryParameterDef(QP.CLASS,Class.class,false),
            // new QueryParameterDef("annotatorIds",Collection.class,true));
            q = new PojosFindAnnotationsQueryDefinition( parameters );
            fail( "Should have failed!" );
        } catch ( IllegalArgumentException e ) {
        } catch ( ApiUsageException e ) {
        }
    }

    @Test
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( new Parameters()
                .addIds(null) // Null
                .addOptions(null)
                .addClass(Image.class)
                .addSet("annotatorIds",Collections.emptySet()));

        creation_fails( new Parameters()
                .addIds(Collections.emptySet()) // Empty!
                .addOptions(null)
                .addClass(Image.class)
                .addSet("annotatorIds",Collections.emptySet()));
        
        creation_fails( new Parameters()
                .addIds(Arrays.asList(1l)) 
                .addOptions(null)
                .addClass(null) // Null here
                .addSet("annotatorIds",Collections.emptySet()));
        
        creation_fails( new Parameters()
                .addIds(Arrays.asList(1)) // Integer not Long
                .addOptions(null)
                .addClass(Image.class)
                .addSet("annotatorIds",Collections.emptySet()));
    }

    @Test
    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q = new PojosFindAnnotationsQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( doesntExist )) 
                .addOptions(null)
                .addClass(Image.class)
                .addSet("annotatorIds",Collections.emptySet()));

        list = (List) iQuery.execute( q );
    }

    @Test
    public void test_images_exist() throws Exception
    {
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
        q = new PojosFindAnnotationsQueryDefinition(
                new Parameters()
                .addIds(ids) 
                .addOptions(null)
                .addClass(Image.class)
                .addSet("annotatorIds",Collections.emptySet()));

        Collection<ImageAnnotation> results = (Collection) iQuery.execute(q);
        for ( ImageAnnotation annotation : results )
        {
            assertTrue(ids.contains(annotation.getImage().getId()));
        }
    }
    
    @Test
    public void test_dataset_exist() throws Exception
    {
        ids = new HashSet(data.getMax("Dataset.Annotated.ids",2));
        q = new PojosFindAnnotationsQueryDefinition(
                new Parameters()
                .addIds(ids) 
                .addOptions(null)
                .addClass(Dataset.class)
                .addSet("annotatorIds",Collections.emptySet()));

        Collection<DatasetAnnotation> results = (Collection) iQuery.execute(q);
        for ( DatasetAnnotation annotation : results )
        {
            assertTrue(ids.contains(annotation.getDataset().getId()));
        }
    }
    
    

}
