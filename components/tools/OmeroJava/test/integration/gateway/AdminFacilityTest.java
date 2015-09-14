/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package integration.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;


/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class AdminFacilityTest extends GatewayTest {

    GroupData group;
    ExperimenterData exp;
    
    @Test
    public void testCreateGroup() throws DSOutOfServiceException, DSAccessException {
        group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        group = adminFacility.createGroup(rootCtx, group, null, GroupData.PERMISSIONS_GROUP_READ_WRITE);
        Assert.assertTrue(group.getId()>-1);
    }
    
    @Test(dependsOnMethods = {"testCreateGroup"})
    public void testCreateExperimenter() throws DSOutOfServiceException, DSAccessException {
        exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        List<GroupData> groups = new ArrayList<GroupData>();
        groups.add(group);
        exp = adminFacility.createExperimenter(rootCtx, exp, UUID.randomUUID().toString(), "test", groups, false, true);
        Assert.assertTrue(exp.getId()>-1);
    }
    
    @Test(dependsOnMethods = {"testCreateExperimenter"})
    public void testLookupExperimenter() throws DSOutOfServiceException, DSAccessException {
        ExperimenterData e = adminFacility.lookupExperimenter(rootCtx, exp.getUserName());
        Assert.assertEquals(exp.getId(), e.getId());
    }
    
    @Test(dependsOnMethods = {"testCreateGroup"})
    public void testLookupGroup() throws DSOutOfServiceException, DSAccessException {
        GroupData g = adminFacility.lookupGroup(rootCtx, group.getName());
        Assert.assertEquals(group.getId(), g.getId());
    }
}
