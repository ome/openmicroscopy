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

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.Facility;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.facility.SearchFacility;
import omero.gateway.facility.TransferFacility;
import omero.log.NullLogger;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.ExperimenterData;
import pojos.GroupData;
import spec.AbstractTest;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class GatewayTest extends AbstractTest {

    /** Identifies the <code>user</code> group. */
    public String USER_GROUP = "user";

    Gateway gw = null;
    ExperimenterData root = null;
    SecurityContext rootCtx = null;

    AdminFacility adminFacility = null;
    BrowseFacility browseFacility = null;
    RawDataFacility rawdataFacility = null;
    SearchFacility searchFacility = null;
    TransferFacility transferFacility = null;
    DataManagerFacility datamanagerFacility = null;

    @Test
    public void testConnected() throws DSOutOfServiceException {
        String version = gw.getServerVersion();
        Assert.assertTrue(version != null && version.trim().length() > 0);
    }

    /**
     * Initializes the Gateway.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {

        LoginCredentials c = new LoginCredentials();
        c.getServer().setHostname("localhost");
        c.getServer().setPort(4064);
        c.getUser().setUsername("root");
        c.getUser().setPassword("omero");

        gw = new Gateway(new NullLogger());
        root = gw.connect(c);

        rootCtx = new SecurityContext(root.getDefaultGroup().getGroupId());
        rootCtx.setExperimenter(root);

        adminFacility = Facility.getFacility(AdminFacility.class, gw);
        browseFacility = Facility.getFacility(BrowseFacility.class, gw);
        rawdataFacility = Facility.getFacility(RawDataFacility.class, gw);
        searchFacility = Facility.getFacility(SearchFacility.class, gw);
        transferFacility = Facility.getFacility(TransferFacility.class, gw);
        datamanagerFacility = Facility.getFacility(DataManagerFacility.class,
                gw);
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        gw.disconnect();
    }

    public GroupData createGroup() throws DSOutOfServiceException,
            DSAccessException {
        GroupData group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        return adminFacility.createGroup(rootCtx, group, null,
                GroupData.PERMISSIONS_GROUP_READ_WRITE);
    }

    public ExperimenterData createExperimenter(GroupData group)
            throws DSOutOfServiceException, DSAccessException {
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        List<GroupData> groups = new ArrayList<GroupData>();
        if (group != null)
            groups.add(group);
        return adminFacility.createExperimenter(rootCtx, exp, UUID.randomUUID()
                .toString(), "test", groups, false, true);
    }

}
