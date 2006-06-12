package ome.system.utests;

import org.testng.annotations.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;

import junit.framework.TestCase;


public class LoginTest extends TestCase
{

    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_null_user() throws Exception
    {
        new Login(null, "");
    }
  
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_null_password() throws Exception
    {
        new Login("", null);
    }
  
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_null_user_ext() throws Exception
    {
        new Login(null, "",null,null);
    }
  
    @Test
    @ExpectedExceptions( ApiUsageException.class )
    public void test_null_password_ext() throws Exception
    {
        new Login("", null,null,null);
    }
    
    @Test
    public void test_asProperties() throws Exception
    {
        Login l = new Login("a", "b");
        Properties p = l.asProperties();
        assertNotNull( p.getProperty(Login.OMERO_USER));
        assertNotNull( p.getProperty(Login.OMERO_PASS));
        assertNull( p.getProperty(Login.OMERO_GROUP));
        assertNull( p.getProperty(Login.OMERO_EVENT));

        assertEquals( p.getProperty(Login.OMERO_USER),"a");
        assertEquals( p.getProperty(Login.OMERO_PASS),"b");
    }

    @Test
    public void test_asProperties_extNulls() throws Exception
    {
        Login l = new Login("a", "b",null,null);
        Properties p = l.asProperties();
        assertNotNull( p.getProperty(Login.OMERO_USER));
        assertNotNull( p.getProperty(Login.OMERO_PASS));
        assertNull( p.getProperty(Login.OMERO_GROUP));
        assertNull( p.getProperty(Login.OMERO_EVENT));

        assertEquals( p.getProperty(Login.OMERO_USER),"a");
        assertEquals( p.getProperty(Login.OMERO_PASS),"b");
    }
    
    @Test
    public void test_asProperties_ext() throws Exception
    {
        Login l = new Login("a", "b", "c", "d" );
        Properties p = l.asProperties();
        assertNotNull( p.getProperty(Login.OMERO_USER));
        assertNotNull( p.getProperty(Login.OMERO_PASS));
        assertNotNull( p.getProperty(Login.OMERO_GROUP));
        assertNotNull( p.getProperty(Login.OMERO_EVENT));

        assertEquals( p.getProperty(Login.OMERO_USER),"a");
        assertEquals( p.getProperty(Login.OMERO_PASS),"b");
        assertEquals( p.getProperty(Login.OMERO_GROUP),"c");
        assertEquals( p.getProperty(Login.OMERO_EVENT),"d");
    }

    @Test
    public void test_getters() throws Exception
    {
        Login l = new Login("a", "b");
        
        assertNotNull( l.getName() );
        assertEquals( l.getName(), "a" );
        
        assertNotNull( l.getPassword() );
        assertEquals( l.getPassword(), "b" );
        
        assertNull( l.getGroup() );
        assertNull( l.getEvent() );
        
    }

    @Test
    public void test_getters_extNulls() throws Exception
    {
        Login l = new Login("a", "b", null, null);
        
        assertNotNull( l.getName() );
        assertEquals( l.getName(), "a" );
        
        assertNotNull( l.getPassword() );
        assertEquals( l.getPassword(), "b" );
        
        assertNull( l.getGroup() );
        assertNull( l.getEvent() );
    }

    @Test
    public void test_getters_ext() throws Exception
    {
        Login l = new Login("a", "b", "c", "d" );
        
        assertNotNull( l.getName() );
        assertEquals( l.getName(), "a" );
        
        assertNotNull( l.getPassword() );
        assertEquals( l.getPassword(), "b" );
        
        assertNotNull( l.getGroup() );
        assertEquals( l.getGroup(), "c" );

        assertNotNull( l.getEvent() );
        assertEquals( l.getEvent(), "d" );

    }

  
}
