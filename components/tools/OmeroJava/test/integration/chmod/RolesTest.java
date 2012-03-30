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
    
    /**
     * Test the interaction with an object in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testInteractionRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rw----");
    	Dataset img = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	Permissions perms = img.getDetails().getPermissions();
    	long id = img.getId().getValue();
    	assertTrue(perms.canEdit());
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
    }
    
    /**
     * Test the interaction with an image in a RW group by the owner
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testCreationImageRW()
	throws Exception
    {
    	EventContext ec = newUserAndGroup("rw----");
    	Image img = (Image) iUpdate.saveAndReturnObject(
    			mmFactory.createImage());
    	Permissions perms = img.getDetails().getPermissions();
    	long id = img.getId().getValue();
    	assertTrue(perms.canEdit());
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
    
}
