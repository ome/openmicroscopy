/*
 * integration.chmod.RolesTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration.chmod;


//Java imports

//Third-party libraries
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Permissions;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import integration.AbstractServerTest;

/** 
 * Tests the can edit, can annotate.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RolesTest 
	extends AbstractServerTest
{

    /**
     * Since we are creating a new client on each invocation, we should also
     * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but
     * not the very last invocation.
     */
    @AfterMethod
    public void close() 
    	throws Exception
    {
        clean();
    }
    
    //Group RW----
    /**
     * Test the interaction with an image in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionImageByOwnerRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rw----");
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.createImage());
    	Permissions perms = img.getDetails().getPermissions();
    	long id = img.getId().getValue();
    	assertTrue(perms.canEdit());

    	assertTrue(perms.canAnnotate());
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	makeGroupOwner();
    	String sql = "select i from Image as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    }

    /**
     * Test the interaction with an object in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByOwnerRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rw----");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	makeGroupOwner();
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
    		fail("Owner should not be allowed to add an image to the dataset");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Test the interaction with an object in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rw----");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	logRootIntoGroup(ec);
    	
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
    		fail("Admin should not be allowed to add an image to the dataset");
		} catch (Exception e) {
			
		}
    }
    //Group RWR---
    /**
     * Test the interaction with an object in a RWR group by a member
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRWR()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwr---");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	disconnect();

    	//Now a new member to the group.
    	newUserInGroup(ec);
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
        assertEquals(images.size(), 1);

    	// Just a member should be able to neither (for the moment)
    	// Reload the perms (from the object that the member loaded)
    	// and check status.
    	perms = images.get(0).getDetails().getPermissions();
    	assertFalse(perms.canAnnotate());
    	assertFalse(perms.canEdit());

    	
    	//Try to link an image
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
    		fail("Member should not be allowed to add an image to the dataset");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Test the interaction with an object in a RWR group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByOwnerRWR()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwr---");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	makeGroupOwner();
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	//Try to link an image
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
	    	fail("Owner should not be allowed to add an image to the dataset");
		} catch (Exception e) {
			
		}
    }

    /**
     * Test the interaction with an object in a RWR group by the admin
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRWR()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwr---");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	disconnect();
    	//Now a new member to the group.
    	logRootIntoGroup(ec);
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	//Try to link an image
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
	    	fail("Owner should not be allowed to add an image to the dataset");
		} catch (Exception e) {
			
		}
    }
    
    //Group RWRW--
    /**
     * Test the interaction with an object in a RWRW group by a member
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRWRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwrw--");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	assertTrue(perms.canAnnotate());
        assertTrue(perms.canEdit());
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canAnnotate());
        assertTrue(perms.canEdit());
    	
    	//Try to link an image
    	try {
			Image img = (Image) iUpdate.saveAndReturnObject(
	    			mmFactory.createImage());
			DatasetImageLink l = new DatasetImageLinkI();
	    	l.link(new DatasetI(d.getId().getValue(), false), img);
	    	iUpdate.saveAndReturnObject(l);
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Test the interaction with an object in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByOwnerRWRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwrw--");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	makeGroupOwner();
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> images = iQuery.findAllByQuery(sql, param);
    	assertEquals(images.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	//Try to link an image
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.createImage());
		DatasetImageLink l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img);
    	iUpdate.saveAndReturnObject(l);
    }
    
    /**
     * Test the interaction with an object in a RWRW group by the admin
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRWRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rwrw--");
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = d.getDetails().getPermissions();
    	long id = d.getId().getValue();
    	disconnect();
    	//Now a new member to the group.
    	newUserInGroup(ec);
    	makeGroupOwner();
    	String sql = "select i from Dataset as i ";
		sql += "where i.id = :id";
		ParametersI param = new ParametersI();
    	param.addId(id);
    	List<IObject> values = iQuery.findAllByQuery(sql, param);
    	assertEquals(values.size(), 1);
    	assertTrue(perms.canEdit());
    	assertTrue(perms.canAnnotate());
    	//Try to link an image
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.createImage());
		DatasetImageLink l = new DatasetImageLinkI();
    	l.link(new DatasetI(d.getId().getValue(), false), img);
    	iUpdate.saveAndReturnObject(l);
    }
}
