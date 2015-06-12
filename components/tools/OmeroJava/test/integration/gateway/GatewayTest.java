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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import omero.api.IAdminPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.Facility;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.facility.SearchFacility;
import omero.gateway.facility.TransferFacility;
import omero.log.NullLogger;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Permissions;

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
    ExperimenterData user = null;
    SecurityContext ctx = null;
    
    AdminFacility adminFacility = null;
    BrowseFacility browseFacility = null;
    RawDataFacility rawdataFacility = null;
    SearchFacility searchFacility = null;
    TransferFacility transferFacility = null;
    DataManagerFacility datamanagerFacility = null;
 
    ExperimenterData testUser = null;
    GroupData testGroup = null;
    
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
        user = gw.connect(c);
        
        ctx = new SecurityContext(user.getDefaultGroup().getGroupId());
        ctx.setExperimenter(user);
        
        adminFacility = Facility.getFacility(AdminFacility.class, gw);
        browseFacility = Facility.getFacility(BrowseFacility.class, gw);
        rawdataFacility = Facility.getFacility(RawDataFacility.class, gw);
        searchFacility = Facility.getFacility(SearchFacility.class, gw);
        transferFacility = Facility.getFacility(TransferFacility.class, gw);
        datamanagerFacility = Facility.getFacility(DataManagerFacility.class, gw);
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        gw.disconnect();
    }
    
    /**
     * Creates a new group and experimenter and returns the event context.
     *
     * @param perms
     *            The permissions level.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void newUserAndGroup(Permissions perms, boolean owner)
            throws Exception {
        IAdminPrx rootAdmin = gw.getAdminService(ctx);
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(perms);
        g = new ExperimenterGroupI(rootAdmin.createGroup(g), false);
        newUserInGroup(g, owner);
    }

    /**
     * Creates a new user in the specified group.
     *
     * @param group
     *            The group to add the user to.
     * @param owner
     *            Pass <code>true</code> to indicate that the new user is an
     *            owner of the group, <code>false</code> otherwise.
     * @return The context.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    protected void newUserInGroup(ExperimenterGroup group, boolean owner)
            throws Exception {
        IAdminPrx rootAdmin = gw.getAdminService(ctx);
        group = rootAdmin.getGroup(group.getId().getValue());

        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("gateway"));
        e.setLastName(omero.rtypes.rstring("tester"));
        e.setLdap(omero.rtypes.rbool(false));
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>(1);
        groups.add(group);
        long id = newUserInGroupWithPassword(e, groups, uuid);
        e = rootAdmin.getExperimenter(id);
        rootAdmin.addGroups(e, Arrays.asList(group));
        if (owner) {
            rootAdmin.addGroupOwners(group, Arrays.asList(e));
        }
    }
    
    /**
     * Creates the specified user in the specified groups. Also adds the user
     * to the default user group. Requires a password.
     *
     * @param experimenter The pre-populated Experimenter object.
     * @param groups The target groups.
     * @param password The user password.
     * @return long The created user ID.
     */
    protected long newUserInGroupWithPassword(Experimenter experimenter,
            List<ExperimenterGroup> groups, String password) throws Exception {
        IAdminPrx rootAdmin = gw.getAdminService(ctx);
        ExperimenterGroup userGroup = rootAdmin.lookupGroup(USER_GROUP);
        return rootAdmin.createExperimenterWithPassword(experimenter,
                omero.rtypes.rstring(password), userGroup, groups);
    }
    
}
