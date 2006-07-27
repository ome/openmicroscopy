package ome.server.itests.sec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;
import ome.util.IdBlock;

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
		e = iAdmin.getExperimenter(iAdmin.createUser(e));
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
		e = iAdmin.getExperimenter(iAdmin.createSystemUser(e));
		assertNotNull(e.getEmail());
		assertNotNull(e.getOmeName());
		assertNotNull(e.getFirstName());
		assertNotNull(e.getLastName());
		assertTrue(iAdmin.containedGroups(e.getId()).length == 2);
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
		e = iAdmin.getExperimenter(
				iAdmin.createExperimenter(e, 
						new ExperimenterGroup(0L,false), null));
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
		e = e = iAdmin.getExperimenter(iAdmin.createUser(e));
	
		// and a new group
		ExperimenterGroup g = new ExperimenterGroup();
		g.setName(UUID.randomUUID().toString());
		g = iAdmin.getGroup(iAdmin.createGroup(g));
		
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
	public void testUserAdminSettersOnOwnObjects() throws Exception 
	{
		loginRoot();
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("user admin setters");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.getExperimenter(iAdmin.createUser(e));
		
		Image mine = new Image(1L);
		mine.getDetails().setOwner(e);
		
		Image notMine = new Image(1L);
		notMine.getDetails().setOwner(iAdmin.userProxy(0L));

		loginUser(e.getOmeName());
		
		fail("implement");
		
	}

	// ~ IAdmin.setDefaultGroup
	// =========================================================================
	@Test
	public void testSetDefaultGroup() throws Exception 
	{
		loginRoot();
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("user admin setters");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.getExperimenter(iAdmin.createUser(e));
	
		// new test group
		ExperimenterGroup g = new ExperimenterGroup();
		g.setName(UUID.randomUUID().toString());
		g = iAdmin.getGroup( iAdmin.createGroup(g));
		iAdmin.addGroups(e,g);
		
		// check current default group
		ExperimenterGroup def = iAdmin.getDefaultGroup(e.getId());
		assertEquals(def.getId(),(Object)1L);
		
		// now change
		iAdmin.setDefaultGroup(e, g);
		
		// test
		def = iAdmin.getDefaultGroup(e.getId());
		assertEquals(def.getId(),g.getId());
		
	}
	
	// ~ IAdmin.addGroups & .removeGroups
	// =========================================================================
	@Test
	public void testPlusAndMinusGroups() throws Exception {
		loginRoot();
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("user admin setters");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.getExperimenter(iAdmin.createUser(e));
		
		assertTrue(e.linkedExperimenterGroupList().size()==1);
		assertTrue(((ExperimenterGroup)e.linkedExperimenterGroupList().get(0)).getId().equals(1L));
		
		//	two new test groups
		ExperimenterGroup g1 = new ExperimenterGroup();
		g1.setName(UUID.randomUUID().toString());
		g1 = iAdmin.getGroup(iAdmin.createGroup(g1));
		ExperimenterGroup g2 = new ExperimenterGroup();
		g2.setName(UUID.randomUUID().toString());
		g2 = iAdmin.getGroup(iAdmin.createGroup(g2));
		
		iAdmin.addGroups(e,g1,g2);

		// test
		e = iAdmin.lookupExperimenter(e.getOmeName());
		assertTrue(e.linkedExperimenterGroupList().size() == 3);
		
		iAdmin.removeGroups(e, g1);
		e = iAdmin.lookupExperimenter(e.getOmeName());
		assertTrue(e.linkedExperimenterGroupList().size() == 2);
	}
	
	// ~ IAdmin.contained*
	// =========================================================================
	@Test
	public void testContainedUsersAndGroups() throws Exception {
		loginRoot();
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("user admin setters");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.getExperimenter(iAdmin.createUser(e));
		
		//	two new test groups
		ExperimenterGroup g1 = new ExperimenterGroup();
		g1.setName(UUID.randomUUID().toString());
		g1 = iAdmin.getGroup(iAdmin.createGroup(g1));
		ExperimenterGroup g2 = new ExperimenterGroup();
		g2.setName(UUID.randomUUID().toString());
		g2 = iAdmin.getGroup(iAdmin.createGroup(g2));
		
		// add them all together
		iAdmin.addGroups(e, g1, g2);
		
		// test
		Experimenter[] es = iAdmin.containedExperimenters(g1.getId());
		assertTrue(es.length==1);
		assertTrue(es[0].getId().equals(e.getId()));
		
		ExperimenterGroup[] gs = iAdmin.containedGroups(e.getId());
		assertTrue(gs.length==3);
		List<Long> ids = new ArrayList<Long>();
		for (ExperimenterGroup group : gs) {
			ids.add(group.getId());
		}
		assertTrue(ids.contains(1L));
		assertTrue(ids.contains(g1.getId()));
		assertTrue(ids.contains(g2.getId()));
	}

	// ~ IAdmin.lookup* & .get*
	// =========================================================================
	@Test
	public void testLookupAndGet() throws Exception {
		loginRoot();
		// create a new user for the test
		Experimenter e = new Experimenter();
		e.setFirstName("user admin setters");
		e.setLastName("test");
		e.setOmeName(UUID.randomUUID().toString());
		e = iAdmin.getExperimenter(iAdmin.createSystemUser(e));
		
		loginUser(e.getOmeName());
		
		Experimenter test_e = iAdmin.lookupExperimenter(e.getOmeName());
		ExperimenterGroup test_g = iAdmin.getGroup(0L);
		
		assertTrue(test_e.linkedExperimenterGroupList().size() == 2);
		assertTrue(test_g.eachLinkedExperimenter(new IdBlock()).contains(e.getId()));
	}
	
}
