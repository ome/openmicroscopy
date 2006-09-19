package ome.system.utests;

import org.testng.annotations.*;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Roles;

import junit.framework.TestCase;


public class RolesTest extends TestCase
{
	Roles r = new Roles();
	
    @Test
    public void testRoot() throws Exception
    {
    	assertTrue( r.isRootUser( new Experimenter( 0L, false )));
	}

    @Test
    public void testSystem() throws Exception
    {
    	assertTrue( r.isSystemGroup( new ExperimenterGroup( 0L, false )));
	}

    @Test
    public void testUser() throws Exception
    {
    	assertTrue( r.isUserGroup( new ExperimenterGroup( 1L, false )));
	}

    
}
