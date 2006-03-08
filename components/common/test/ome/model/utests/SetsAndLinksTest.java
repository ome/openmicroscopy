package ome.model.utests;

import java.util.List;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;

import junit.framework.TestCase;


public class SetsAndLinksTest extends TestCase
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
    
    public void test_linking() throws Exception
    {
        p.linkDataset( d );
        
        assertTrue( p.linkedDatasetList().size() == 1);
        assertTrue( p.linkedDatasetIterator().next().equals( d ));
        
    }
    
    public void test_unlinking() throws Exception
    {
        p.linkDataset( d );
        p.unlinkDataset( d );
        assertTrue( p.linkedDatasetList().size() == 0 );

        p.linkDataset( d );
        p.clearDatasetLinks();
        assertTrue( p.linkedDatasetList().size() == 0 );
        
    }
    
    public void test_retrieving() throws Exception
    {
        p.linkDataset( d );
        List l = p.eachLinkedDataset( null );
        assertTrue( l.size() == 1 );
        assertTrue( l.get(0).equals( d ));
    }
}
