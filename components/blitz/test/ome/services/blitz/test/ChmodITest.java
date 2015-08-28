/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.test;

import java.sql.Timestamp;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.api.IAdmin;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;

import omero.ClientError;
import omero.cmd.Chmod;
import omero.cmd.IRequest;
import omero.cmd._HandleTie;
import omero.cmd.graphs.ChmodFacadeI;
import omero.model.PermissionsI;


/**
 * Tests around the changing of group-based permissions.
 *
 * @see ticket:2874
 * @see 4.4.0
 * @see http://www.openmicroscopy.org/site/community/minutes/minigroup/2012.03.12-groupperms
 */
@Test(groups = { "integration", "chmod" })
@SuppressWarnings("deprecation")
public class ChmodITest extends AbstractGraphTest {

    ManagedContextFixture user; // Overrides super class
    Long newGrpId;

    protected ExperimenterGroup setupNewGroup(String perms) throws Exception {
        user = new ManagedContextFixture(ctx, true, perms);
        newGrpId = user.getCurrentEventContext().getCurrentGroupId();
        return user.managedSf.getAdminService().getGroup(newGrpId);
    }

    IRequest newChmod(String perms) {
        return newChmod("/ExperimenterGroup", newGrpId, perms, null);
    }

    IRequest newChmod(String type, long id, String perms,
            Map<String, String> options) {
        ChmodFacadeI chmod = (ChmodFacadeI) ic.findObjectFactory(Chmod.ice_staticId()).create("");
        chmod.type = type;
        chmod.id = id;
        chmod.options = options;
        chmod.permissions = perms;
        return chmod;
    }

    @Test
    public void testImmutablePermissions() throws Exception {
        PermissionsI p = new PermissionsI();
        p.ice_postUnmarshal();
        try {
            p.setPerm1(-1L);
            fail("should throw a client error");
        } catch (ClientError err) {
            // good;
        }
        try {
            p.setPerm1(new Long(-1L));
            fail("should throw a client error");
        } catch (ClientError err) {
            // good;
        }
        try {
            p.setPerm1(-1L, null);
            fail("should throw a client error");
        } catch (ClientError err) {
            // good;
        }
    }

    @Test
    public void testSimple() throws Exception {
        setupNewGroup("rw----");
        IRequest chmod = newChmod("rwr---");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertSuccess(handle);
    }

    @Test
    public void testReducingPermissionsOkNoData() throws Exception {
        setupNewGroup("rwr---");
        IRequest chmod = newChmod("rw----");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertSuccess(handle);
    }

    @Test
    public void testReducingPermissionsOkUserOnlyData() throws Exception {
        setupNewGroup("rwr---");

        // Add data as user
        Image i = new Image();
        i.setName("testReducingPermissionsOkUserOnlyData");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Dataset d = new Dataset();
        d.setName("testReducingPermissionsErrOkUserOnlyData");
        i = new Image(i.getId(), false);
        d.linkImage(i);
        d = user.managedSf.getUpdateService().saveAndReturnObject(d);

        IRequest chmod = newChmod("rw----");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertSuccess(handle);
    }

    @Test
    public void testReducingPermissionsErrGroupDrop() throws Exception {
        ExperimenterGroup grp = setupNewGroup("rwrw--");

        // Add data as user
        Image i = new Image();
        i.setName("testReducingPermissionsErrGroupDrop");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        // Cross-link as another user
        String name = root.newUser(grp.getName());
        ManagedContextFixture other = new ManagedContextFixture(ctx);
        other.setCurrentUser(name);

        Dataset d = new Dataset();
        d.setName("testReducingPermissionsErrGroupDrop");
        i = new Image(i.getId(), false);
        d.linkImage(i);
        d = other.managedSf.getUpdateService().saveAndReturnObject(d);

        IRequest chmod = newChmod("rw----");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertFailure(handle, "check failed");
    }

    @Test
    public void testReducingPermissionsErrWorldDrop() throws Exception {
        ExperimenterGroup grp1 = setupNewGroup("rwrwrw");
        Long grp2 = root.newGroup(Permissions.parseString("rw----"));
        String grp2Name = root.managedSf.getAdminService().getGroup(grp2).getName();

        // Add data as user
        Image i = new Image();
        i.setName("testReducingPermissionsErrWorldDrop");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        // Cross-link as another user
        String name = root.newUser(grp2Name);
        ManagedContextFixture other = new ManagedContextFixture(ctx);
        other.setCurrentUser(name);

        Dataset d = new Dataset();
        d.setName("testReducingPermissionsErrWorldDrop");
        i = new Image(i.getId(), false);
        d.linkImage(i);
        d = other.managedSf.getUpdateService().saveAndReturnObject(d);

        IRequest chmod = newChmod("rwrw--");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertFailure(handle, "check failed");
    }
}
