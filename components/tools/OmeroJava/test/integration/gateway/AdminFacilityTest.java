/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;


/**
 * Tests for the {@link AdminFacility} methods.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */
public class AdminFacilityTest extends GatewayTest {

    GroupData group;
    ExperimenterData exp;
    
    /**
     * Test creation of groups
     * @throws DSOutOfServiceException If an error occurred
     * @throws DSAccessException If an error occurred
     */
    @Test
    public void testCreateGroup() throws DSOutOfServiceException, DSAccessException {
        group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        group = adminFacility.createGroup(rootCtx, group, null, GroupData.PERMISSIONS_GROUP_READ_WRITE);
        Assert.assertTrue(group.getId()>-1);
    }
    
    /**
     * Test creation of users
     * @throws DSOutOfServiceException If an error occurred
     * @throws DSAccessException If an error occurred
     */
    @Test(dependsOnMethods = {"testCreateGroup"})
    public void testCreateExperimenter() throws DSOutOfServiceException, DSAccessException {
        exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        List<GroupData> groups = new ArrayList<GroupData>();
        groups.add(group);
        // create a 'light admin' user without any admin privileges
        exp = adminFacility
                .createExperimenter(rootCtx, exp, UUID.randomUUID().toString(),
                        "test", groups, true, true, Collections.<String>emptyList());
        Assert.assertTrue(exp.getId()>-1);
        Assert.assertTrue(adminFacility.getAdminPrivileges(rootCtx, exp).isEmpty());
    }
    
    /**
     * Test lookup of users
     * @throws DSOutOfServiceException If an error occurred
     * @throws DSAccessException If an error occurred
     */
    @Test(dependsOnMethods = {"testCreateExperimenter"})
    public void testLookupExperimenter() throws DSOutOfServiceException, DSAccessException {
        ExperimenterData e = adminFacility.lookupExperimenter(rootCtx, exp.getUserName());
        Assert.assertEquals(exp.getId(), e.getId());
    }
    
    /**
     * Test lookup of groups
     * @throws DSOutOfServiceException If an error occurred
     * @throws DSAccessException If an error occurred
     */
    @Test(dependsOnMethods = {"testCreateGroup"})
    public void testLookupGroup() throws DSOutOfServiceException, DSAccessException {
        GroupData g = adminFacility.lookupGroup(rootCtx, group.getName());
        Assert.assertEquals(group.getId(), g.getId());
    }
    
    /**
     * Test getting and setting admin privileges
     * @throws DSOutOfServiceException If an error occurred
     * @throws DSAccessException If an error occurred
     */
    @Test(dependsOnMethods = { "testCreateExperimenter" })
    public void testAdminPrivileges() throws DSOutOfServiceException,
            DSAccessException {
        SecurityContext userCtx = new SecurityContext(exp.getGroupId());
        Collection<String> privs = adminFacility.getAdminPrivileges(userCtx, exp);
        Assert.assertTrue(privs.isEmpty());
        
        adminFacility.setAdminPrivileges(userCtx, exp,
                Collections.singletonList(AdminPrivilegeChgrp.value));
        privs = adminFacility.getAdminPrivileges(userCtx, exp);
        Assert.assertEquals(privs.size(), 1);
        Assert.assertEquals(privs.iterator().next(), AdminPrivilegeChgrp.value);
        
        adminFacility.addAdminPrivileges(userCtx, exp,
                Collections.singletonList(AdminPrivilegeChown.value));
        privs = adminFacility.getAdminPrivileges(userCtx, exp);
        Assert.assertEquals(privs.size(), 2);
        Assert.assertTrue(privs.contains(AdminPrivilegeChgrp.value));
        Assert.assertTrue(privs.contains(AdminPrivilegeChown.value));
        
        adminFacility.removeAdminPrivileges(userCtx, exp,
                Collections.singletonList(AdminPrivilegeChown.value));
        privs = adminFacility.getAdminPrivileges(userCtx, exp);
        Assert.assertEquals(privs.size(), 1);
        Assert.assertTrue(privs.contains(AdminPrivilegeChgrp.value));
        
        Collection<String> allPrivs = adminFacility.getAvailableAdminPrivileges(userCtx);
        adminFacility.addAdminPrivileges(userCtx, exp, allPrivs);
        Assert.assertTrue(adminFacility.isFullAdmin(userCtx, exp));
        
        adminFacility.removeAdminPrivileges(userCtx, exp,
                Collections.singletonList(AdminPrivilegeChown.value));
        Assert.assertFalse(adminFacility.isFullAdmin(userCtx, exp));
    }
}
