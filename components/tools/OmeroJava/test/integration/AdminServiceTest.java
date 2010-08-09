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
import omero.model.OriginalFile;
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

	/** Identifies the <code>system</code> group. */
	private String SYSTEM_GROUP = "system";
	
	/** Identifies the <code>user</code> group. */
	private String USER_GROUP = "user";
	
	/** Identifies the <code>guest</code> group. */
	private String GUEST_GROUP = "guest";
	
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
    public void testUpdateExperimenter() 
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
	 * Tests the update an existing user.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
    public void testChangePassword() 
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
			svc.changeUserPassword(uuid, rstring("foo"));
		} catch (Exception ex) {
			fail("Not possible to modify the experimenter's password.");
		}
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
	 * Tests the deletion of a group.
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
            client.createSession(uuid, "foo");
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
	
}
