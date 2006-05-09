package ome.client.utests.session;

import org.testng.annotations.*;
import ome.client.Storage;
import ome.model.IObject;
import ome.model.containers.Project;

import junit.framework.TestCase;

public class KeyTest extends TestCase
{

  @Test
    public void test_nullConstraints() throws Exception
    {
        try
        {
            new Storage.Key(null, new Long(1));
            fail("Should have thrown an IllegalArgumentExc.");
        }
        catch ( IllegalArgumentException e )
        {}

        try
        {
            new Storage.Key(Project.class, null);
            fail("Should have thrown an IllegalArgumentExc.");
        }
        catch ( IllegalArgumentException e )
        {}
        
        try
        {
            new Storage.Key(null, new Long(1));
            fail("Should have thrown an IllegalArgumentExc.");
        }
        catch ( IllegalArgumentException e )
        {}

        try
        {
            new Storage.Key((IObject) null);
            fail("Should have thrown an IllegalArgumentExc.");
        }
        catch ( IllegalArgumentException e )
        {}
        
    }
    
  @Test
    public void test_equality() throws Exception
    {
        Storage.Key k1 = new Storage.Key(Project.class, new Long(1));
        Storage.Key k2 = new Storage.Key(Project.class, new Long(1));
        assertEquals(k1,k2);
        assertTrue(k1.hashCode()==k2.hashCode());
    }
    
}
