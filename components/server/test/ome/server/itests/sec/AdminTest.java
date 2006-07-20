package ome.server.itests.sec;

import java.util.UUID;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;

public class AdminTest extends AbstractManagedContextTest
{

	// ~ IAdmin.createUser
	// =========================================================================
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testUserAccountCreationWithNull() throws Exception {
		iAdmin.createUser(null);
	}
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testUserAccountCreationWithEmpty() throws Exception {
		Experimenter e = new Experimenter();
		iAdmin.createUser(e);
	}
	
	@Test
	public void testUserAccountCreation() throws Exception {
		Experimenter e = new Experimenter();
		e.setEmail("blah");
		e.setFirstName("foo");
		e.setLastName("bar");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.createUser(e);
		assertNotNull(e.getEmail());
		assertNotNull(e.getOmeName());
		assertNotNull(e.getFirstName());
		assertNotNull(e.getLastName());
		assertTrue(e.sizeOfGroupExperimenterMap() == 1 );
	}
	
	// ~ IAdmin.createSystemUser
	// =========================================================================
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testSysUserAccountCreationWithNull() throws Exception {
		iAdmin.createUser(null);
	}
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testSysUserAccountCreationWithEmpty() throws Exception {
		Experimenter e = new Experimenter();
		iAdmin.createSystemUser(e);
	}
	
	@Test
	public void testSysUserAccountCreation() throws Exception {
		Experimenter e = new Experimenter();
		e.setEmail("blah");
		e.setFirstName("foo");
		e.setLastName("bar");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.createSystemUser(e);
		assertNotNull(e.getEmail());
		assertNotNull(e.getOmeName());
		assertNotNull(e.getFirstName());
		assertNotNull(e.getLastName());
		assertTrue(e.sizeOfGroupExperimenterMap() == 2);
	}

	// ~ IAdmin.createExperimenter
	// =========================================================================
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testExperimenterAccountCreationWithAllNulls() throws Exception {
		iAdmin.createExperimenter(null,null,null);
	}
	
	@Test
	@ExpectedExceptions( ApiUsageException.class )
	public void testExperimenterAccountCreationWithEmpty() throws Exception {
		Experimenter e = new Experimenter();
		iAdmin.createExperimenter(e,null,null);
	}
	
	@Test
	public void testExperimenterAccountCreation() throws Exception {
		Experimenter e = new Experimenter();
		e.setEmail("blah");
		e.setFirstName("foo");
		e.setLastName("bar");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.createExperimenter(e, new ExperimenterGroup(0L,false), null);
		assertNotNull(e.getEmail());
		assertNotNull(e.getOmeName());
		assertNotNull(e.getFirstName());
		assertNotNull(e.getLastName());
		assertTrue(e.sizeOfGroupExperimenterMap() == 1);
	}
	
	// ~ Groups
	// =========================================================================
	@Test
	public void testUserCanOnlySetDetailsOnOwnObject() throws Exception 
	{
		fail("implement"); // also verify for 
	}
	
	@Test
	public void testUserCanOnlySetDetailsToOwnGroup() throws Exception 
	{
		fail("implement"); // also verify iupdate 
	}
	
	// ~ chgrp
	// =========================================================================
	@Test
	public void testUserUsesChgrpThroughAdmin() throws Exception {
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("chgrp");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.createUser(e);
	
		// and a new group
		ExperimenterGroup g = new ExperimenterGroup();
		g.setName(UUID.randomUUID().toString());
		g = iAdmin.createGroup(g);
		
		// and user to group
		iAdmin.addGroups(e, g);
		
		// login
		loginUser(e.getOmeName());
		
		// create a new image
		Image i = new Image();
		i.setName(UUID.randomUUID().toString());
		i = factory.getUpdateService().saveAndReturnObject(i);
		
		// it should be in some other group
		Long group = i.getDetails().getGroup().getId();
		assertFalse( group.equals(g.getId()));
		
		// now let's try to change that group
		factory.getAdminService().changeGroup(i, g.getName());
		Image copy = factory.getQueryService().get(Image.class, i.getId());
		Long test = copy.getDetails().getGroup().getId();
		
		assertFalse( test.equals( group     ));
		assertTrue(  test.equals( g.getId() ));
		
		
	}
	
	@Test
	public void testCanOnlyChgrpOnOwnObject() throws Exception {
		
	}
	
	@Test
	public void testCanOnlyChgrpToOwnGroups() throws Exception {
		
	}

}
