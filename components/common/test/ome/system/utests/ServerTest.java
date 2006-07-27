package ome.system.utests;

import org.testng.annotations.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.system.Server;

import junit.framework.TestCase;


public class ServerTest extends TestCase
{

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_null_host() throws Exception
    {
        new Server(null);
    }
  
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_bad_port() throws Exception
    {
        new Server("", -100);
    }
    
    @Test
    public void test_asProperties() throws Exception
    {
        Server s = new Server("a");
        Properties p = s.asProperties();
        assertNotNull( p.getProperty(Server.OMERO_HOST));
        assertNotNull( p.getProperty(Server.OMERO_PORT));
        assertEquals( p.getProperty(Server.OMERO_HOST),"a");
        assertEquals( p.getProperty(Server.OMERO_PORT),"1099");
    }

    @Test
    public void test_asProperties_ext() throws Exception
    {
        Server l = new Server("a", 999);
        Properties p = l.asProperties();
        assertNotNull( p.getProperty(Server.OMERO_HOST));
        assertNotNull( p.getProperty(Server.OMERO_PORT));

        assertEquals( p.getProperty(Server.OMERO_HOST),"a");
        assertEquals( p.getProperty(Server.OMERO_PORT),"999");
    }

    @Test
    public void test_getters() throws Exception
    {
        Server s = new Server("a");
        
        assertNotNull( s.getHost() );
        assertEquals( s.getHost(), "a" );
        assertEquals( s.getPort(), 1099 );
        
    }
    
    @Test
    public void test_getters_ext() throws Exception
    {
        Server s = new Server("a", 999);
        
        assertNotNull( s.getHost() );
        assertEquals( s.getHost(), "a" );
        assertEquals( s.getPort(), 999 );
        
    }
  
}
