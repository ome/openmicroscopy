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

import pojos.ExperimenterData;
import pojos.GroupData;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class AdminFacilityTest extends GatewayTest {

    @Test
    public void testCreateGroup() throws DSOutOfServiceException, DSAccessException {
        GroupData group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        super.testGroup = adminFacility.createGroup(ctx, group, null, GroupData.PERMISSIONS_GROUP_READ_WRITE);
        Assert.assertTrue(super.testGroup.getId()>-1);
    }
    
    @Test(dependsOnMethods = {"testCreateGroup"})
    public void testCreateExperimenter() throws DSOutOfServiceException, DSAccessException {
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        List<GroupData> groups = new ArrayList<GroupData>();
        groups.add(super.testGroup);
        super.testUser = adminFacility.createExperimenter(ctx, exp, UUID.randomUUID().toString(), "test", groups, false, true);
        Assert.assertTrue(super.testUser.getId()>-1);
    }
}
