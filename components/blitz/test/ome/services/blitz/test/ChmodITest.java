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
import omero.cmd._HandleTie;
import omero.cmd.graphs.ChmodI;
import omero.model.PermissionsI;


/**
 * Tests around the changing of group-based permissions.
 *
 * @see ticket:2874
 * @see 4.4.0
 * @see https://www.openmicroscopy.org/site/community/minutes/minigroup/2012.03.12-groupperms
 */
@Test(groups = { "integration", "chmod" })
public class ChmodITest extends AbstractGraphTest {

    Long newGrpId = null;

    @BeforeMethod
    protected void nullGroup() {
        newGrpId = null;
    }

    protected ExperimenterGroup setupNewGroup(String perms) throws Exception {
        newGrpId = root.newGroup(Permissions.parseString(perms));
        root.addUserToGroup(user.getCurrentEventContext().getCurrentUserId(),
                newGrpId, true);

        IAdmin admin = user.managedSf.getAdminService();
        admin.getEventContext(); // Refresh
        return admin.getGroup(newGrpId);
    }

    ChmodI newChmod(String perms) {
        return newChmod("/ExperimenterGroup", newGrpId, perms, null);
    }

    ChmodI newChmod(String type, long id, String perms,
            Map<String, String> options) {
        ChmodI chmod = (ChmodI) ic.findObjectFactory(ChmodI.ice_staticId()).create("");
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
        ChmodI chmod = newChmod("rwr---");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertSuccess(handle);
    }

    @Test
    public void testReducingPermissionsOk() throws Exception {
        setupNewGroup("rwr---");
        ChmodI chmod = newChmod("rw----");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertSuccess(handle);
    }

    @Test
    public void testReducingPermissionsErr() throws Exception {
        ExperimenterGroup grp = setupNewGroup("rwrw--");

        // Add data as user
        Image i = new Image();
        i.setName("testReducingPermissionsErr");
        i.setAcquisitionDate(new Timestamp(0L));
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        // Cross-link as another user
        String name = root.newUser(grp.getName());
        ManagedContextFixture other = new ManagedContextFixture(ctx);
        other.setCurrentUser(name);

        Dataset d = new Dataset();
        d.setName("testReducingPermissionsErr");
        i = new Image(i.getId(), false);
        d.linkImage(i);
        d = other.managedSf.getUpdateService().saveAndReturnObject(d);

        ChmodI chmod = newChmod("rw----");
        _HandleTie handle = submit(chmod);
        block(handle, 5, 1000);
        assertFailure(handle);
    }


}
