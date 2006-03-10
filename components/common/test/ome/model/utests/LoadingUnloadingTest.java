package ome.model.utests;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;

import junit.framework.TestCase;


public class LoadingUnloadingTest extends TestCase
{

    Project p;
    Dataset d;
    Image i;
    Pixels pix;
    
    protected void setUp() throws Exception
    {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }
    
    public void test_unloaded_allow_only_ID_accessors() throws Exception
    {
    
        p.unload();
        p.getId();
        p.setId( null );
        try_and_fail( p.fields() );        
        
    }

    private void try_and_fail( Set strings )
    {
        for (Iterator it = strings.iterator(); it.hasNext();)
        {
            String field = (String) it.next();
            try {
                p.retrieve( field );
                if ( ! Project.ID.equals( field ))
                    fail("Should have thrown an exception on:"+field);
            } catch (IllegalStateException stateExc) {
                if ( Project.ID.equals( field ))
                    fail("Should NOT throw an exception on id");
            }
            
        }
    }
    
}