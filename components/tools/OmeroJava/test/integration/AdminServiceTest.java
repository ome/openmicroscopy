/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMap;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.sys.ParametersI;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 *  Collections of tests for the <code>IAdmin</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class AdminServiceTest 
	extends AbstractTest
{
	
	/** The password of the user. */
	private String PASSWORD = "password";
	
	/** The password of the user. */
	private String PASSWORD_MODIFIED = "passwordModified";
	
	/**
	 * Tests the <code>lookupGroup</code> method. Retrieves a specified group.
	 * Controls if the following groups exist: <code>system, user</code>
	 * and converts the group into the corresponding POJO.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testLookupGroup() 
    	throws Exception
    {
		IAdminPrx svc = root.getSession().getAdminService();
		ExperimenterGroup group = svc.lookupGroup(USER_GROUP);
		assertNotNull(group);
		assertTrue(group.getName().getValue().equals(USER_GROUP));
		GroupData data = new GroupData(group);
		assertTrue(data.getId() == group.getId().getValue());
		assertTrue(data.getName() == group.getName().getValue());
		group = svc.lookupGroup(SYSTEM_GROUP);
		assertNotNull(group);
		assertTrue(group.getName().getValue().equals(SYSTEM_GROUP));
		//Test the conversion into the corresponding POJO
		data = new GroupData(group);
		assertTrue(data.getId() == group.getId().getValue());
		assertTrue(data.getName() == group.getName().getValue());
    }
	
	/**
	 * Tests the <code>lookupExperimenter</code> method. 
	 * Retrieves an experimenter and converts the group into the corresponding 
	 * POJO.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testLookupExperimenter() 
    	throws Exception
    {
		IAdminPrx svc = root.getSession().getAdminService();
		Experimenter exp = svc.lookupExperimenter("root");
		assertNotNull(exp);
		
		//Test the conversion into the corresponding POJO
		ExperimenterData data = new ExperimenterData(exp);
		assertTrue(data.getId() == exp.getId().getValue());
		assertTrue(data.getUserName() == exp.getOmeName().getValue());
    }
	
	/**
	 * Tests the <code>lookupGroups</code> method. This method should
	 * return 2 or more groups.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testLookupGroups() 
    	throws Exception
    {
		IAdminPrx svc = root.getSession().getAdminService();
		List<ExperimenterGroup> groups = svc.lookupGroups();
		assertNotNull(groups);
		assertTrue(groups.size() >= 2);
    }
	
	/**
	 * Tests the creation of a group with permission <code>rw----</code>.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateGroupRW() 
    	throws Exception
    {
		String uuid = UUID.randomUUID().toString();
		ExperimenterGroup g = new ExperimenterGroupI();
		g.setName(omero.rtypes.rstring(uuid));
		g.getDetails().setPermissions(new PermissionsI("rw----"));
		IAdminPrx svc = root.getSession().getAdminService();
		long id = svc.createGroup(g);
		//Load the group
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNotNull(eg);
		assertTrue(eg.getName().getValue().equals(uuid));
		//test permissions
		Permissions perms = eg.getDetails().getPermissions();
		assertTrue(perms.isUserRead());
		assertTrue(perms.isUserWrite());
		assertFalse(perms.isGroupRead());
		assertFalse(perms.isGroupWrite());
		assertFalse(perms.isWorldRead());
		assertFalse(perms.isWorldWrite());
    }
	
	/**
	 * Tests the creation of a group with permission <code>rwr---</code>.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateGroupRWR() 
    	throws Exception
    {
		String uuid = UUID.randomUUID().toString();
		ExperimenterGroup g = new ExperimenterGroupI();
		g.setName(omero.rtypes.rstring(uuid));
		g.getDetails().setPermissions(new PermissionsI("rwr---"));
		IAdminPrx svc = root.getSession().getAdminService();
		long id = svc.createGroup(g);
		//Load the group
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNotNull(eg);
		assertTrue(eg.getName().getValue().equals(uuid));
		//test permissions
		Permissions perms = eg.getDetails().getPermissions();
		assertTrue(perms.isUserRead());
		assertTrue(perms.isUserWrite());
		assertTrue(perms.isGroupRead());
		assertFalse(perms.isGroupWrite());
		assertFalse(perms.isWorldRead());
		assertFalse(perms.isWorldWrite());
    }
	
	/**
	 * Tests the creation of a group with permission <code>rwrw--</code>.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateGroupRWRW() 
    	throws Exception
    {
		String uuid = UUID.randomUUID().toString();
		ExperimenterGroup g = new ExperimenterGroupI();
		g.setName(omero.rtypes.rstring(uuid));
		g.getDetails().setPermissions(new PermissionsI("rwrw--"));
		IAdminPrx svc = root.getSession().getAdminService();
		long id = svc.createGroup(g);
		//Load the group
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNotNull(eg);
		assertTrue(eg.getName().getValue().equals(uuid));
		//test permissions
		Permissions perms = eg.getDetails().getPermissions();
		assertTrue(perms.isUserRead());
		assertTrue(perms.isUserWrite());
		assertTrue(perms.isGroupRead());
		assertTrue(perms.isGroupWrite());
		assertFalse(perms.isWorldRead());
		assertFalse(perms.isWorldWrite());
    }
	
	/**
	 * Tests the creation of a user w/o setting a password and adds the user
	 * to the newly created group.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateUserWithoutPassword() 
    	throws Exception
    {
		//First create a group.
		String uuid = UUID.randomUUID().toString();
		Experimenter e = new ExperimenterI();
		e.setOmeName(omero.rtypes.rstring(uuid));
		e.setFirstName(omero.rtypes.rstring("user"));
		e.setLastName(omero.rtypes.rstring("user"));
		
		//Create a group and add the experimenter to that group
		ExperimenterGroup g = new ExperimenterGroupI();
		g.setName(omero.rtypes.rstring(uuid));
		g.getDetails().setPermissions(new PermissionsI("rw----"));
		IAdminPrx svc = root.getSession().getAdminService();
		long id = svc.createGroup(g);
		//Load the group
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNotNull(eg);
		long groupId = id;
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		//method tested elsewhere
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg);
		groups.add(userGroup);
		id = svc.createExperimenter(e, eg, groups);
		//Check if we have a user
		p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
		//now check if the user is in correct groups.
		List<Long> ids = new ArrayList<Long>();
		ids.add(groupId);
		ids.add(userGroup.getId().getValue());
		p = new ParametersI();
		p.addLongs("gids", ids);
		List list = (List) query.findAllByQuery("select m " +
				"from GroupExperimenterMap as m "
				+ "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                		+" where m.parent.id in (:gids)", p);
		assertNotNull(list);
		Iterator i = list.iterator();
		GroupExperimenterMap geMap;
		int count = 0;
		while (i.hasNext()) {
			geMap = (GroupExperimenterMap) i.next();
			if (geMap.getChild().getId().getValue() == id)
				count++;
		}
		assertTrue(count == 2);
    }
	
	/**
	 * Tests the creation of a user with a password and adds the user
	 * to the newly created group.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateUserWithPassword() 
    	throws Exception
    {
		//First create a group.
		String uuid = UUID.randomUUID().toString();
		Experimenter e = new ExperimenterI();
		e.setOmeName(omero.rtypes.rstring(uuid));
		e.setFirstName(omero.rtypes.rstring("user"));
		e.setLastName(omero.rtypes.rstring("user"));
		
		//Create a group and add the experimenter to that group
		ExperimenterGroup g = new ExperimenterGroupI();
		g.setName(omero.rtypes.rstring(uuid));
		g.getDetails().setPermissions(new PermissionsI("rw----"));
		IAdminPrx svc = root.getSession().getAdminService();
		long id = svc.createGroup(g);
		//Load the group
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNotNull(eg);
		long groupId = id;
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		//method tested elsewhere
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg);
		groups.add(userGroup);
		id = svc.createExperimenterWithPassword(e, 
				omero.rtypes.rstring(PASSWORD), eg, groups);
		//Check if we have a user
		p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
		//now check if the user is in correct groups.
		List<Long> ids = new ArrayList<Long>();
		ids.add(groupId);
		ids.add(userGroup.getId().getValue());
		p = new ParametersI();
		p.addLongs("gids", ids);
		List list = (List) query.findAllByQuery("select m " +
				"from GroupExperimenterMap as m "
				+ "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                		+" where m.parent.id in (:gids)", p);
		assertNotNull(list);
		Iterator i = list.iterator();
		GroupExperimenterMap geMap;
		int count = 0;
		while (i.hasNext()) {
			geMap = (GroupExperimenterMap) i.next();
			if (geMap.getChild().getId().getValue() == id)
				count++;
		}
		assertTrue(count == 2);
    }

	/**
	 * Tests the creation of a user using the <code>createUser</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateUser() 
    	throws Exception
    {
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long groupId = svc.createGroup(g);
        
		long id = svc.createUser(e, uuid);
		IQueryPrx query = root.getSession().getQueryService();
		
		//Check if we have a user
		ParametersI p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
        //check if we are in the correct group i.e. user and uuid
		//now check if the user is in correct groups.
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		List<Long> ids = new ArrayList<Long>();
		ids.add(groupId);
		ids.add(userGroup.getId().getValue());
		p = new ParametersI();
		p.addLongs("gids", ids);
		List list = (List) query.findAllByQuery("select m " +
				"from GroupExperimenterMap as m "
				+ "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                		+" where m.parent.id in (:gids)", p);
		assertNotNull(list);
		Iterator i = list.iterator();
		GroupExperimenterMap geMap;
		int count = 0;
		while (i.hasNext()) {
			geMap = (GroupExperimenterMap) i.next();
			if (geMap.getChild().getId().getValue() == id)
				count++;
		}
		assertTrue(count == 2);
    }
	
	/**
	 * Tests the creation of a user using the <code>createUser</code> method.
	 * A group not created is specified as the default group, in that case, 
	 * an exception should be thrown.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testCreateUserNoGroup() 
    	throws Exception
    {
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        try {
        	svc.createUser(e, uuid);
        	fail("The user should not have been created. No group specified.");
		} catch (Exception ex) {
		}
    }
	
	/**
	 * Tests the update an existing experimenter.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testUpdateExperimenterByAdmin() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        
		long id = svc.createUser(e, uuid);
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		
		String name = "userModified";
		uuid = UUID.randomUUID().toString();
		e.setOmeName(omero.rtypes.rstring(uuid));
		e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
		svc.updateExperimenter(e);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
		assertTrue(e.getFirstName().getValue().equals(name));
		assertTrue(e.getLastName().getValue().equals(name));
    }
	
	/**
	 * Tests the update of the details of the user currently logged in 
	 * using the <code>updateSelf</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testUpdateExperimenterByUserUsingUpdateSelf() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        
		long id = svc.createUser(e, uuid);
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		
		String name = "userModified";
		//uuid = UUID.randomUUID().toString();
		e.setOmeName(omero.rtypes.rstring(uuid));
		e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
        //
        //owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
		iAdmin.updateSelf(e);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
		assertTrue(e.getFirstName().getValue().equals(name));
		assertTrue(e.getLastName().getValue().equals(name));
    }
	
	/**
	 * Tests the update of the details of the user currently logged in 
	 * using the <code>updateExperimenter</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(enabled = true)
    public void testUpdateExperimenterByUserUsingUpdateExperimenter() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        
		long id = svc.createUser(e, uuid);
		IQueryPrx query = root.getSession().getQueryService();
		ParametersI p = new ParametersI();
		p.addId(id);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		
		String name = "userModified";
		//uuid = UUID.randomUUID().toString();
		e.setOmeName(omero.rtypes.rstring(uuid));
		e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
        //
        //owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
		iAdmin.updateExperimenter(e);
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNotNull(e);
		assertTrue(e.getOmeName().getValue().equals(uuid));
		assertTrue(e.getFirstName().getValue().equals(name));
		assertTrue(e.getLastName().getValue().equals(name));
    }
	
	/**
	 * Tests the update of the user password by the administrator
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testChangePasswordByAdmin() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        svc.createUser(e, uuid);
		e = svc.lookupExperimenter(uuid);
		try {
			svc.changeUserPassword(uuid, rstring(PASSWORD_MODIFIED));
		} catch (Exception ex) {
			fail("Not possible to modify the experimenter's password.");
		}
    }
	
	/**
	 * Tests the default group of an experimenter.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testChangeDefaultGroup()
		throws Exception
	{
		//Create 2 groups and add a user 
    	String uuid1 = UUID.randomUUID().toString();
		ExperimenterGroup g1 = new ExperimenterGroupI();
		g1.setName(omero.rtypes.rstring(uuid1));
		g1.getDetails().setPermissions(new PermissionsI("rw----"));
		
		String uuid2 = UUID.randomUUID().toString();
		ExperimenterGroup g2 = new ExperimenterGroupI();
		g2.setName(omero.rtypes.rstring(uuid2));
		g2.getDetails().setPermissions(new PermissionsI("rw----"));
			
		IAdminPrx svc = root.getSession().getAdminService();
		IQueryPrx query = root.getSession().getQueryService();
		long id1 = svc.createGroup(g1);
		long id2 = svc.createGroup(g2);
		
		ParametersI p = new ParametersI();
		p.addId(id1);
		
		ExperimenterGroup eg1 = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		p = new ParametersI();
		p.addId(id2);
		
		ExperimenterGroup eg2 = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		Experimenter e = new ExperimenterI();
		e.setOmeName(omero.rtypes.rstring(uuid1));
		e.setFirstName(omero.rtypes.rstring("user"));
		e.setLastName(omero.rtypes.rstring("user"));
		
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		//method tested elsewhere
		ExperimenterGroup userGroup = svc.lookupGroup(USER_GROUP);
		groups.add(eg1);
		groups.add(eg2);
		groups.add(userGroup);
		
		long id = svc.createExperimenter(e, eg1, groups);
		e = svc.lookupExperimenter(uuid1);
		List<GroupExperimenterMap> links = e.copyGroupExperimenterMap();
		assertTrue(groups.get(0).getId().getValue() == eg1.getId().getValue());
		svc.setDefaultGroup(e, eg2);
		
		e = svc.lookupExperimenter(uuid1);
		links = e.copyGroupExperimenterMap();
		groups = new ArrayList<ExperimenterGroup>();
		for (GroupExperimenterMap link : links) {
            groups.add(link.getParent());
        }
		assertTrue(groups.get(0).getId().getValue() == eg2.getId().getValue());
	}
	
	/**
	 * Tests the deletion of an experimenter.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDeleteExperimenter() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        long id = svc.createUser(e, uuid);
		e = svc.lookupExperimenter(uuid);
		svc.deleteExperimenter(e);
		ParametersI p = new ParametersI();
		p.addId(id);
		IQueryPrx query = root.getSession().getQueryService();
		e = (Experimenter) query.findByQuery(
				"select distinct e from Experimenter e where e.id = :id", p);
		assertNull(e);
    }
	
	/**
	 * Tests the deletion of a group.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testDeleteGroup() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        long id = svc.createGroup(g);
		g = svc.lookupGroup(uuid);
		svc.deleteGroup(g);
		ParametersI p = new ParametersI();
		p.addId(id);
		IQueryPrx query = root.getSession().getQueryService();
		g = (ExperimenterGroup) query.findByQuery(
				"select distinct g from ExperimenterGroup g where g.id = :id", 
				p);
		assertNull(g);
    }
	
	/**
	 * Tests to make a user the owner of a group.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testSetOwner() 
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        long groupId = svc.createGroup(g);
        g = svc.lookupGroup(uuid);
        //create the user.
        long expId = svc.createUser(e, uuid);
        e = svc.lookupExperimenter(uuid);
        //set the user as the group owner.
        svc.setGroupOwner(g, e);
        IQueryPrx query = root.getSession().getQueryService();
        String sql = "select m from GroupExperimenterMap as m ";
    	sql += "left outer join fetch m.child as c ";
    	sql += "left outer join fetch m.parent as p ";
    	sql += "where ";
    	sql += "c.id = :expId ";
    	sql += " and p.id = :groupId";
    	ParametersI p = new ParametersI();
    	p.addLong("expId", expId);
		p.addLong("groupId", groupId);
    	List l = (List) query.findAllByQuery(sql, p);
    	Iterator i = l.iterator();
        GroupExperimenterMap map;
        while (i.hasNext()) {
        	map = (GroupExperimenterMap) i.next();
			assertTrue(map.getOwner().getValue());
		}
    }
	
	/**
	 * Tests the upload of the user picture.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testUploadMyUserPhoto() 
    	throws Exception
    {
        IAdminPrx svc = root.getSession().getAdminService();
        //First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        svc.createGroup(g);
        //create the user.
        svc.createUser(e, uuid);
        omero.client client = new omero.client(root.getPropertyMap());
        try {
            client.createSession(uuid, uuid);
            IAdminPrx prx = client.getSession().getAdminService();
            long id = prx.uploadMyUserPhoto("/tmp/foto.jpg", "image/jpeg", 
            		new byte[]{1});
            assertTrue(id >= 0);
        } finally {
            client.closeSession();
        }
    }
	
	 /**
     * Tests the adding and modifying of a user photo, specify all
     * the exception thrown by ticket:1791
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUserPhoto() 
    	throws Exception
    {
        IAdminPrx prx = root.getSession().getAdminService();
		String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup rwr = new ExperimenterGroupI();
        rwr.setName(rstring(uuid));
        rwr.getDetails().setPermissions(new PermissionsI("rwr---"));
        long rwrID = prx.createGroup(rwr);
        rwr = prx.getGroup(rwrID);

        ExperimenterGroup rwrw = new ExperimenterGroupI();
        rwrw.setName(rstring(UUID.randomUUID().toString()));
        rwr.getDetails().setPermissions(new PermissionsI("rwrw--"));
        long rwrwID = prx.createGroup(rwrw);
        rwrw = prx.getGroup(rwrwID);

        Experimenter e = new ExperimenterI();
        e.setOmeName(rstring(uuid));
        e.setFirstName(rstring(uuid));
        e.setLastName(rstring(uuid));
        long userID = prx.createUser(e, rwrw.getName().getValue());
        e = prx.getExperimenter(userID);

        prx.addGroups(e, Arrays.asList(rwr));

        omero.client client = new omero.client(root.getPropertyMap());
        try {
            client.createSession(e.getOmeName().getValue(), "foo");
            prx = client.getSession().getAdminService();
            prx.uploadMyUserPhoto("/tmp/foto.jpg", "image/jpeg", new byte[]{1});
            client.getSession().setSecurityContext(
            		new ExperimenterGroupI(rwrID, false));
            prx.uploadMyUserPhoto("/tmp/foto2.jpg", "image/jpeg", new byte[]{2});
        } finally {
            client.closeSession();
        }
    }
    
    /**
	 * Tests the modification of the password by the user currently logged in.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testChangePasswordByUser() 
    	throws Exception
    {
		//current user change the password.
		iAdmin.changePassword(rstring(PASSWORD_MODIFIED));
    }
	
    /**
	 * Tests the attempt to modify the password by another user (non admin)
	 * than the one currently logged in.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testChangePasswordByOtherUser() 
    	throws Exception
    {
		IAdminPrx prx = root.getSession().getAdminService();
		//add a new user
		String groupName = iAdmin.getEventContext().groupName;
		String userName = iAdmin.getEventContext().userName;
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        //create the user.
        long userID = prx.createUser(e, groupName);
        //now the new user is going to try to modify the password
        omero.client client = new omero.client(root.getPropertyMap());
        try {
        	 client.createSession(uuid, groupName);
        	 client.getSession().getAdminService().changeUserPassword(userName, 
        			 rstring(PASSWORD_MODIFIED));
        	 fail("The user should not have been able to modify the password.");
		} catch (Exception ex) {
			
		}
        client.closeSession();
    }
	
    /**
	 * Tests to modify the permissions of a group. Creates a <code>rwr---</code>
	 * group and increases the permissions to <code>rwrw--</code>
	 * then back again to <code>rwr--</code>. This tests the 
	 * <code>ChangePermissions</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(groups = "changePermissions")
    public void testChangePermissions()
    	throws Exception
    {
        root = newRootOmeroClient();
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();

        // First group rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rwr---"));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        assertTrue(permissions.isGroupRead());
        assertFalse(permissions.isGroupWrite());
        
        //change permissions
        permissions.setGroupWrite(true);
        prx.changePermissions(g, permissions);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        assertTrue(permissions.isGroupRead());
        assertTrue(permissions.isGroupWrite());
        
        //now reduce the permissions.
        permissions.setGroupWrite(false);
        prx.changePermissions(g, permissions);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        assertTrue(permissions.isGroupRead());
        assertFalse(permissions.isGroupWrite());
    }
	
    /**
	 * Tests to promote a group. The permissions of the group are initially
	 * <code>rw---</code> then upgrade to <code>rwr--</code>. This tests the 
	 * <code>ChangePermissions</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testPromoteGroup()
    	throws Exception
    {
		IAdminPrx prx = root.getSession().getAdminService();
		String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        
        //change permissions and promote the group
        permissions.setGroupRead(true);
        prx.changePermissions(g, permissions);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        assertTrue(permissions.isGroupRead());
        assertFalse(permissions.isGroupWrite());
    }
	
    /**
	 * Tests to promote a group and try to reduce the permission. 
	 * The permissions of the group are initially
	 * <code>rw---</code> then upgrade to <code>rwr--</code>
	 * then back to a <code>rw---</code>. The latest change should return
	 * an exception. This tests the <code>ChangePermissions</code> method.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testPromoteAndDowngradeGroup()
    	throws Exception
    {
		IAdminPrx prx = root.getSession().getAdminService();
		String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        
        //change permissions and promote the group
        permissions.setGroupRead(true);
        prx.changePermissions(g, permissions);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        assertTrue(permissions.isGroupRead());
        assertFalse(permissions.isGroupWrite());
        g = prx.getGroup(id);
        //now try to turn it back to rw----
        try {
        	permissions = g.getDetails().getPermissions();
        	permissions.setGroupRead(false);
        	prx.changePermissions(g, permissions);
        	fail("Not possible to turn group back to private");
		} catch (Exception e) {
		}
    }
	
    /**
	 * Tests the addition of existing user by the owner of the group.
	 * The owner is NOT an administator.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(enabled = true)
    public void testOwnerAddExistingExperimenterToGroup()
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        long groupId = svc.createGroup(g);
        g = svc.lookupGroup(uuid);
        //create the user.
        long expId = svc.createUser(e, uuid);
        Experimenter owner = svc.lookupExperimenter(uuid);
        //set the user as the group owner.
        svc.setGroupOwner(g, owner);
        
        
        //create another group and user
        String uuidGroup = UUID.randomUUID().toString();
        ExperimenterGroup g2 = new ExperimenterGroupI();
        g2.setName(omero.rtypes.rstring(uuidGroup));
        g2.getDetails().setPermissions(new PermissionsI("rw----"));
        svc.createGroup(g2);
        
        String uuid2 = UUID.randomUUID().toString();
        e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid2));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        expId = svc.createUser(e, uuidGroup);
        e = svc.lookupExperimenter(uuid2);
        //owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
        //iAdmin.
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        iAdmin.addGroups(e, groups);
    }
	
    /**
	 * Tests the removal of a member of the group by the owner of the group.
	 * The owner is NOT an administator.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(enabled = true)
    public void testOwnerRemoveExperimenterToGroup()
    	throws Exception
    {
		//First create a new user.
		String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        IAdminPrx svc = root.getSession().getAdminService();
        
        //already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        //create group.
        long groupId = svc.createGroup(g);
        g = svc.lookupGroup(uuid);
        //create the user.
        long expId = svc.createUser(e, uuid);
        Experimenter owner = svc.lookupExperimenter(uuid);
        //set the user as the group owner.
        svc.setGroupOwner(g, owner);
        
        
        //create another group and user
        String uuid2 = UUID.randomUUID().toString();
        e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid2));
        e.setFirstName(omero.rtypes.rstring("user"));
        e.setLastName(omero.rtypes.rstring("user"));
        expId = svc.createUser(e, uuid);
        e = svc.lookupExperimenter(uuid2);
        //owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
        //iAdmin.
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        iAdmin.removeGroups(e, groups);
    }
	
}
