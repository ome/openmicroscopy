/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.Principal;
import ome.util.Utils;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Test of the re-enabled group permissions in Beta4.2
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class PermissionsTest extends AbstractManagedContextTest {

    protected class Fixture {
        private ExperimenterGroup _group = new ExperimenterGroup();
        Experimenter user;
        String groupName;

        void init() {
            user = loginNewUser();
            groupName = uuid();
            _group.setName(groupName);
            iAdmin.createGroup(_group);
            _group = iAdmin.lookupGroup(groupName);
            iAdmin.addGroups(user, _group);
            // Prevents weirdness via loginNewUserInOtherUsersGroup
            iAdmin.setDefaultGroup(user, _group);
        }

        Fixture() {
            init();
        }

        Fixture(Permissions groupPermissions) {
            _group.getDetails().setPermissions(groupPermissions);
            init();
        }

        /**
         * Always returns a fresh copy.
         */
        ExperimenterGroup group() {
            return iQuery.findByQuery("select eg from ExperimenterGroup eg " +
            		"join fetch eg.groupExperimenterMap where eg.id = "+ _group.getId(),
            		null);
        }

        Image saveImage() {
            return saveImage(null);
        }

        Image saveImage(Permissions p) {
            Image image = new_Image("ticket:1434");
            image.getDetails().setPermissions(p);
            return iUpdate.saveAndReturnObject(image);
        }


        void log_in() {
            login(user.getOmeName(), groupName, "Test");
        }

        void use_fixture_group() {
            String uuid = iAdmin.getEventContext().getCurrentSessionUuid();
            Principal principal = new Principal(uuid);
            sessionManager.setSecurityContext(principal, group());
        }

        void make_leader() {
            loginRoot();
            iAdmin.addGroupOwners(group(), user);
            log_in();
        }

        void make_admin() {
            loginRoot();
            iAdmin.addGroupOwners(
                    new ExperimenterGroup(roles.getSystemGroupId(), false),
                    user);
            log_in();
        }
        
        ExperimenterGroup new_group() {
            loginRoot();
            ExperimenterGroup g = new ExperimenterGroup();
            g.setName(uuid());
            long gid = iAdmin.createGroup(g);
            iAdmin.addGroups(user, new ExperimenterGroup(gid, false));
            log_in();
            return g;
        }

    }

    protected Fixture fixture;

    // Not done automatically for speed
    /**
     * Create fixture with a group of the given permissions and login to it.
     */
    protected void setup(Permissions perms) {
        fixture = new Fixture(perms);
        fixture.log_in();
    }

    protected void setupOnce(Permissions perms) {
        if (fixture == null) {
            setup(perms);
        }
    }

    @AfterMethod
    protected void teardownFixture() {
        fixture = null;
    }

    //
    // Helpers
    //

    @SuppressWarnings("unchecked")
    private IObject lookup(IObject obj) {
        Class k = Utils.trueClass(obj.getClass());
        return iQuery.get(k, obj.getId());
    }

    protected void assertPrivate(IObject obj) {
        obj = lookup(obj);
        Permissions p = obj.getDetails().getPermissions();
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.USER, Right.READ));
        assertFalse(obj + " is " + p + " !!", p.isGranted(Role.GROUP, Right.READ));
        assertFalse(obj + " is " + p + " !!", p.isGranted(Role.WORLD, Right.READ));
    }

    protected void assertShared(IObject obj) {
        obj = lookup(obj);
        Permissions p = obj.getDetails().getPermissions();
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.USER, Right.READ));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.GROUP, Right.READ));
        assertFalse(obj + " is " + p + " !!", p.isGranted(Role.WORLD, Right.READ));
    }

    protected void assertSharedAndWritable(IObject obj) {
        obj = lookup(obj);
        Permissions p = obj.getDetails().getPermissions();
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.USER, Right.READ));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.GROUP, Right.READ));
        assertFalse(obj + " is " + p + " !!", p.isGranted(Role.WORLD, Right.READ));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.USER, Right.WRITE));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.GROUP, Right.WRITE));
        assertFalse(obj + " is " + p + " !!", p.isGranted(Role.WORLD, Right.WRITE));

    }

    protected void assertPublic(IObject obj) {
        obj = lookup(obj);
        Permissions p = obj.getDetails().getPermissions();
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.USER, Right.READ));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.GROUP, Right.READ));
        assertTrue(obj + " is " + p + " !!", p.isGranted(Role.WORLD, Right.READ));
    }

}
