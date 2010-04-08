/*
 * coverage.AdminTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package coverage;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import static omero.rtypes.*;
import omero.ServerError;

import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMap;
import omero.sys.ParametersI;



/** 
 * Verifies the methods used to manipulate groups and experimenters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME4.2
 */
public class AdminTest 
	extends IceTest
{

	/** Identifies the default group. */
	private static final String  DEFAULT_GROUP = "default";
	
	/** Identifies the default group. */
	private static final String  SYSTEM_GROUP = "system";
	
	/** Identifies the default group. */
	private static final String  USER_GROUP = "user";
	
	/**
	 * Creates a new experimenter and returns the identifier of 
	 * the experimenter.
	 * 
	 * @param prx Reference to the service.
	 * @return See above.
	 * @throws ServerError Thrown if an error occurred.
	 */
    private long newUser(IAdminPrx prx) 
    	throws ServerError
    {
        return prx.createUser(createExperimenter(), DEFAULT_GROUP);
    }
    
    /**
     * Creates a default experimenter.
     * 
     * @return See above.
     */
    private Experimenter createExperimenter()
    {
    	 Experimenter e = new ExperimenterI();
         e.setFirstName(rstring("admin"));
         e.setLastName(rstring("test"));
         e.setOmeName(rstring(Ice.Util.generateUUID()));
         return e;
    }
    
    /**
     * Returns the experimenter-group links.
     * 
     * @param prx   Reference to the service.
     * @param expId The experimenter's identifier.
     * @param groupId The group's identifier.
     * @return See above.
     * @throws ServerError Thrown if an error occurred.
     */
    private List loadExperimenterGroupLinks(IQueryPrx prx, long expId, 
    				long groupId)
    	throws ServerError
    {
    	if (expId < 0 && groupId < 0) return new ArrayList();
    	
    	String sql = "select m from ExperimenterGroupMap as m ";
    	sql += "left outer join fetch m.child as c ";
    	sql += "left outer join fetch m.parent as p ";
    	sql += "where ";
    	ParametersI p = new ParametersI();
    	if (expId > 0) {
    		p.addLong("expId", expId);
    		sql += "c.id = :expId ";
    		if (groupId > 0) {
    			p.addLong("groupId", groupId);
        		sql += " and p.id = :groupId";
        	}
    	} else {
    		p.addLong("groupId", groupId);
    		sql += "p.id = :groupId";
    	}
    	return (List) prx.findAllByQuery(sql, p);
    }
    
    /**
     * Creates a group.
     * 
     * @return See above.
     */
    private ExperimenterGroup createGroup()
    {
    	 ExperimenterGroup e = new ExperimenterGroupI();
         e.setName(rstring("testGroup"));
         e.setDescription(rstring("test"));
         return e;
    }
    
    /**
     * Creates an experimenter group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNewGroup() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        long id = prx.createGroup(createGroup());
        assertTrue(id > 0);
        //TODO: delete group.
    }

    /**
     * Deletes a group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteGroup()
    	throws Exception 
    {
    	IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        //first create a group
        long id = prx.createGroup(createGroup());
        ExperimenterGroup g = prx.getGroup(id);
        assertNotNull(g);
        //delete it
        prx.deleteGroup(g);
        //Now check that the group has been deleted.
        g = prx.getGroup(id);
        assertNull(g);
    }

    /**
     * Modifies the permissions of an experimenter group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testChangeGroupPermissions() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
    }
    
    /**
     * Creates a new user.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNewUser() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        long id = newUser(prx);
        assertTrue(id > 0);
        //TODO: delete user.
    }

    /**
     * Creates a new experimenter with a password.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNewExperimenterWithPassword() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        IQueryPrx svc = root.getServiceFactory().getQueryService();
        assertNotNull(prx);
        assertNotNull(svc);
        ExperimenterGroup g = prx.lookupGroup(DEFAULT_GROUP);
        assertNotNull(g);
        
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        Experimenter e = createExperimenter();
        long id = prx.createExperimenterWithPassword(e, rstring("omero"), g, 
        		groups);
        
        assertTrue(id > 0);
        //make sure the experimenter is in the correct group
        List l = loadExperimenterGroupLinks(svc, id, g.getId().getValue());
        assertTrue(l.size() == 1);
        //TODO: delete user.
    }
    
    /**
     * Creates a new experimenter without a password.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNewExperimenter() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        IQueryPrx svc = root.getServiceFactory().getQueryService();
        assertNotNull(prx);
        assertNotNull(svc);
        ExperimenterGroup g = prx.lookupGroup(DEFAULT_GROUP);
       
        assertNotNull(g);
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        
        Experimenter e = createExperimenter();
        long id = prx.createExperimenter(e, g, groups);
        
        assertTrue(id > 0);
        List l = loadExperimenterGroupLinks(svc, id, g.getId().getValue());
        assertTrue(l.size() == 1);
    }
    
    /**
     * Creates a new experimenter as administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testNewExperimenterAsAdministrator() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        IQueryPrx svc = root.getServiceFactory().getQueryService();
        assertNotNull(prx);
        assertNotNull(svc);
        ExperimenterGroup g = prx.lookupGroup(SYSTEM_GROUP);
        assertNotNull(g);
        
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        Experimenter e = createExperimenter();
        long id = prx.createExperimenter(e, g, groups);
        
        assertTrue(id > 0);
        List l = loadExperimenterGroupLinks(svc, id, g.getId().getValue());
        assertTrue(l.size() == 1);
        //TODO: delete user.
    }
    
    /**
     * Creates an experimenter as owner of a group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSetGroupOwner() 
    	throws Exception 
    {
    	IAdminPrx prx = root.getServiceFactory().getAdminService();
    	IQueryPrx svc = root.getServiceFactory().getQueryService();
        assertNotNull(prx);
        assertNotNull(svc);
        //create the group
        ExperimenterGroup ref = createGroup();
        long groupId = prx.createGroup(ref);
        assertTrue(groupId > 0); //group created
        ExperimenterGroup g = prx.getGroup(groupId);
        assertNotNull(g);
        //load the group
        
        ExperimenterGroup ug = prx.lookupGroup(USER_GROUP);
        assertNotNull(ug);
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        groups.add(ug);
        
        Experimenter e = createExperimenter();
        long expId = prx.createExperimenter(e, g, groups);
        assertTrue(expId > 0); //experimenter created
        Experimenter exp = prx.getExperimenter(expId);
        assertNotNull(g);
        prx.setGroupOwner(g, exp);
        //Now check that the exp is the owner of the group
        List l = loadExperimenterGroupLinks(svc, expId, groupId);
        Iterator i = l.iterator();
        GroupExperimenterMap map;
        while (i.hasNext()) {
        	map = (GroupExperimenterMap) i.next();
			assertTrue(map.getOwner().getValue() == true);
		}
    }
    
    /**
     * Modifies the password of an experimenter.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testChangeUserPassword() 
    	throws Exception 
    {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        long id = newUser(prx);
        Experimenter e = prx.getExperimenter(id);
        prx.changeUserPassword(e.getOmeName().getValue(), rstring("foo"));
    }

    /**
     * Deletes an experimenter.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeleteExperimenter()
    	throws Exception 
    {
    	IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        //first create a group
        ExperimenterGroup g = prx.lookupGroup(DEFAULT_GROUP);
        assertNotNull(g);
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        
        Experimenter e = createExperimenter();
        long id = prx.createExperimenter(e, g, groups);
        e = prx.getExperimenter(id);
        assertNotNull(e);
        //delete it
        prx.deleteExperimenter(e);
        //Now check that the group has been deleted.
        e = prx.getExperimenter(id);
        assertNull(g);
    }
    
}
